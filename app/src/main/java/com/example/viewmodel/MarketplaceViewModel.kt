package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MarketplaceRepository(application)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // --- Session States ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentSeller = MutableStateFlow<SellerEntity?>(null)
    val currentSeller: StateFlow<SellerEntity?> = _currentSeller.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // --- Database Source Flows ---
    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSellers: StateFlow<List<SellerEntity>> = repository.getAllSellers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val config: StateFlow<ConfigEntity?> = repository.config
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Filter & Navigation states ---
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null) // "Fashion", "Books", "Electronics"
    val selectedSubcategory = MutableStateFlow<String?>(null)
    val priceFilterMax = MutableStateFlow(100000.0)
    val sortBy = MutableStateFlow("Popularity") // "Price Low-High", "Price High-Low", "Rating", "Newest"

    // --- Live UI Flows ---
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        searchQuery,
        selectedCategory,
        selectedSubcategory,
        priceFilterMax,
        sortBy
    ) { flows ->
        val products = flows[0] as List<ProductEntity>
        val query = flows[1] as String
        val cat = flows[2] as String?
        val subcat = flows[3] as String?
        val maxPrice = flows[4] as Double
        val sort = flows[5] as String

        var list = products

        // Search text filter
        if (query.isNotEmpty()) {
            list = list.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }

        // Category filter
        if (cat != null) {
            list = list.filter { it.category == cat }
        }

        // Subcategory filter
        if (subcat != null) {
            list = list.filter { it.subcategory == subcat }
        }

        // Price filter
        list = list.filter { it.price <= maxPrice }

        // Sorting
        when (sort) {
            "Price Low-High" -> list.sortedBy { it.price }
            "Price High-Low" -> list.sortedByDescending { it.price }
            "Rating" -> list.sortedByDescending { it.rating }
            "Newest" -> list.sortedByDescending { it.id }
            else -> list.sortedByDescending { it.reviewCount } // Popularity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Authentication ---
    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.loginUser(email, passwordHash)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                    if (user.role == "seller") {
                        // Load seller profile
                        repository.getSellerById(user.id).collect { seller ->
                            _currentSeller.value = seller
                        }
                    }
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Authentication Failed")
                }
        }
    }

    fun registerUser(name: String, email: String, mobile: String, passwordHash: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.registerUser(name, email, mobile, passwordHash, role)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration Failed")
                }
        }
    }

    fun registerSeller(
        name: String,
        businessName: String,
        email: String,
        mobile: String,
        passwordHash: String,
        pan: String,
        gst: String,
        address: String,
        bankHolder: String,
        bankAccount: String,
        ifsc: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.registerSeller(
                name = name,
                businessName = businessName,
                email = email,
                mobile = mobile,
                passwordHash = passwordHash,
                pan = pan,
                gst = gst,
                address = address,
                bankHolderName = bankHolder,
                bankAccount = bankAccount,
                ifsc = ifsc
            ).onSuccess { seller ->
                // Registered seller is auto logged in as seller
                val user = UserEntity(seller.id, name, email, mobile, passwordHash, "seller")
                _currentUser.value = user
                _currentSeller.value = seller
                _authState.value = AuthState.Success(user)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Seller registration failed")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentSeller.value = null
        _authState.value = AuthState.Idle
    }

    fun clearAuthError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    // --- Buyer Wishlist ---
    fun toggleWishlist(productId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val type = Types.newParameterizedType(List::class.java, java.lang.Integer::class.java)
            val adapter = moshi.adapter<List<Int>>(type)
            val wishlist: MutableList<Int> = try {
                adapter.fromJson(user.wishlistJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            if (wishlist.contains(productId)) {
                wishlist.remove(productId)
            } else {
                wishlist.add(productId)
            }

            val updatedUser = user.copy(wishlistJson = adapter.toJson(wishlist))
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    // --- Buyer Cart & Checkout Calculations ---
    fun addToCart(productId: Int, quantity: Int = 1) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val type = Types.newParameterizedType(List::class.java, CartItem::class.java)
            val adapter = moshi.adapter<List<CartItem>>(type)
            val cart: MutableList<CartItem> = try {
                adapter.fromJson(user.cartJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val existing = cart.indexOfFirst { it.productId == productId }
            if (existing != -1) {
                cart[existing] = cart[existing].copy(quantity = cart[existing].quantity + quantity)
            } else {
                cart.add(CartItem(productId, quantity, isSelected = true))
            }

            val updatedUser = user.copy(cartJson = adapter.toJson(cart))
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun removeFromCart(productId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val type = Types.newParameterizedType(List::class.java, CartItem::class.java)
            val adapter = moshi.adapter<List<CartItem>>(type)
            val cart: MutableList<CartItem> = try {
                adapter.fromJson(user.cartJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            cart.removeAll { it.productId == productId }

            val updatedUser = user.copy(cartJson = adapter.toJson(cart))
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun updateCartQuantity(productId: Int, quantity: Int) {
        val user = _currentUser.value ?: return
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        viewModelScope.launch {
            val type = Types.newParameterizedType(List::class.java, CartItem::class.java)
            val adapter = moshi.adapter<List<CartItem>>(type)
            val cart: MutableList<CartItem> = try {
                adapter.fromJson(user.cartJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            val existing = cart.indexOfFirst { it.productId == productId }
            if (existing != -1) {
                cart[existing] = cart[existing].copy(quantity = quantity)
            }

            val updatedUser = user.copy(cartJson = adapter.toJson(cart))
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    // Get live parsed cart structures
    fun getCartItemsFlow(): Flow<List<Pair<ProductEntity, CartItem>>> {
        return combine(currentUser, allProducts) { user, products ->
            if (user == null) return@combine emptyList()
            val type = Types.newParameterizedType(List::class.java, CartItem::class.java)
            val cartList: List<CartItem> = try {
                moshi.adapter<List<CartItem>>(type).fromJson(user.cartJson) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            cartList.mapNotNull { item ->
                val prod = products.find { it.id == item.productId }
                if (prod != null) Pair(prod, item) else null
            }
        }
    }

    // Get live parsed wishlist structures
    fun getWishlistFlow(): Flow<List<ProductEntity>> {
        return combine(currentUser, allProducts) { user, products ->
            if (user == null) return@combine emptyList()
            val type = Types.newParameterizedType(List::class.java, java.lang.Integer::class.java)
            val wishlistIds = try {
                moshi.adapter<List<Int>>(type).fromJson(user.wishlistJson) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            products.filter { it.id in wishlistIds }
        }
    }

    // --- Order Placement ---
    fun checkout(shippingAddress: Address, paymentMethod: String, onComplete: (String) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val cartItems = getCartItemsFlow().first()
            if (cartItems.isEmpty()) return@launch

            val subtotal = cartItems.sumOf { it.first.price * it.second.quantity }
            val cfg = config.value ?: ConfigEntity()

            // Calculate shipping charges
            val isFreeShipping = subtotal > cfg.freeShippingThreshold
            val shippingCharge = if (isFreeShipping) {
                0.0
            } else {
                cartItems.sumOf { item ->
                    // Apply product level customized charge, fallback to category level default shipping fee
                    if (item.first.shippingCharge > 0) {
                        item.first.shippingCharge
                    } else {
                        when (item.first.category) {
                            "Fashion" -> cfg.fashionShippingFee
                            "Books" -> cfg.booksShippingFee
                            "Electronics" -> cfg.electronicsShippingFee
                            else -> 49.0
                        }
                    }
                }
            }

            val codCharge = if (paymentMethod == "COD") cfg.defaultCodCharge else 0.0
            val platformFee = cfg.defaultPlatformFee
            val totalPayable = subtotal + shippingCharge + codCharge + platformFee

            val orderItems = cartItems.map { item ->
                OrderItem(
                    productId = item.first.id,
                    title = item.first.title,
                    category = item.first.category,
                    price = item.first.price,
                    quantity = item.second.quantity,
                    sellerId = item.first.sellerId,
                    sellerName = item.first.sellerName,
                    shippingCharge = if (isFreeShipping) 0.0 else item.first.shippingCharge
                )
            }

            repository.placeOrder(
                buyerId = user.id,
                buyerName = user.name,
                buyerMobile = user.mobile,
                items = orderItems,
                shippingAddress = shippingAddress,
                paymentMethod = paymentMethod,
                subtotal = subtotal,
                shippingCharge = shippingCharge,
                codCharge = codCharge,
                platformFee = platformFee,
                discount = 0.0,
                totalPayable = totalPayable
            ).onSuccess { order ->
                // Refresh local user cart memory
                _currentUser.value = repository.getUserById(user.id).first()
                onComplete(order.id)
            }
        }
    }

    // --- Saved Addresses ---
    fun saveAddress(address: Address) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val type = Types.newParameterizedType(List::class.java, Address::class.java)
            val adapter = moshi.adapter<List<Address>>(type)
            val addresses: MutableList<Address> = try {
                adapter.fromJson(user.addressesJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            // If editing, find by ID
            val index = addresses.indexOfFirst { it.id == address.id }
            if (index != -1) {
                addresses[index] = address
            } else {
                addresses.add(address)
            }

            val updatedUser = user.copy(addressesJson = adapter.toJson(addresses))
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun deleteAddress(addressId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val type = Types.newParameterizedType(List::class.java, Address::class.java)
            val adapter = moshi.adapter<List<Address>>(type)
            val addresses: MutableList<Address> = try {
                adapter.fromJson(user.addressesJson)?.toMutableList() ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            addresses.removeAll { it.id == addressId }

            val updatedUser = user.copy(addressesJson = adapter.toJson(addresses))
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun getAddresses(): List<Address> {
        val user = _currentUser.value ?: return emptyList()
        val type = Types.newParameterizedType(List::class.java, Address::class.java)
        return try {
            moshi.adapter<List<Address>>(type).fromJson(user.addressesJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Seller Product Inventory Actions ---
    fun addNewProduct(
        title: String,
        category: String,
        subcategory: String,
        brand: String,
        price: Double,
        originalPrice: Double,
        discountPercent: Int,
        stock: Int,
        description: String,
        highlights: List<String>,
        specs: Map<String, String>,
        shippingCharge: Double,
        returnPolicy: String,
        onComplete: (Boolean) -> Unit
    ) {
        val seller = _currentSeller.value ?: return
        viewModelScope.launch {
            repository.addProduct(
                sellerId = seller.id,
                sellerName = seller.businessName,
                title = title,
                category = category,
                subcategory = subcategory,
                brand = brand,
                price = price,
                originalPrice = originalPrice,
                discountPercent = discountPercent,
                stock = stock,
                description = description,
                highlights = highlights,
                specs = specs,
                shippingCharge = shippingCharge,
                returnPolicy = returnPolicy
            ).onSuccess {
                onComplete(true)
            }.onFailure {
                onComplete(false)
            }
        }
    }

    fun updateProductDetails(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun removeProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    // --- Seller Shipping Profiles ---
    fun updateSellerShippingSettings(defaultCharge: Double, freeThreshold: Double, handlingTime: Int) {
        val seller = _currentSeller.value ?: return
        viewModelScope.launch {
            val updated = seller.copy(
                defaultShippingCharge = defaultCharge,
                freeShippingThreshold = freeThreshold,
                handlingTimeDays = handlingTime
            )
            repository.updateSeller(updated)
            _currentSeller.value = updated
        }
    }

    // --- Seller GST Details On-the-fly Update ---
    fun updateSellerGst(gstNumber: String) {
        val seller = _currentSeller.value ?: return
        viewModelScope.launch {
            val updated = seller.copy(gst = gstNumber.uppercase())
            repository.updateSeller(updated)
            _currentSeller.value = updated
        }
    }

    // --- Seller Settlement breakdown fetch ---
    fun getSellerPayoutHistoryFlow(): Flow<List<SettlementBreakdown>> {
        return currentSeller.flatMapLatest { seller ->
            if (seller == null) return@flatMapLatest flowOf(emptyList())
            flow {
                emit(repository.getSellerSettlements(seller.id))
            }
        }
    }

    // --- Fulfillment pipeline updates ---
    fun updateOrderStatusBySeller(orderId: String, status: String, trackingNumber: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, trackingNumber)
        }
    }

    fun initiateReturnByBuyer(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.requestReturn(orderId, reason)
        }
    }

    fun processReturnApprovalByAdmin(orderId: String, approved: Boolean) {
        viewModelScope.launch {
            repository.processReturnApproval(orderId, approved)
        }
    }

    // --- Admin Marketplace Settings Controls ---
    fun updateAdminGlobalCommissions(fashion: Double, books: Double, electronics: Double, platformFee: Double, codFee: Double) {
        val currentCfg = config.value ?: ConfigEntity()
        viewModelScope.launch {
            val updated = currentCfg.copy(
                fashionCommission = fashion,
                booksCommission = books,
                electronicsCommission = electronics,
                defaultPlatformFee = platformFee,
                defaultCodCharge = codFee
            )
            repository.saveConfig(updated)
        }
    }

    fun updateSellerProfile(seller: SellerEntity) {
        viewModelScope.launch {
            repository.updateSeller(seller)
        }
    }

    fun getOrdersByBuyer(buyerId: String): Flow<List<OrderEntity>> {
        return allOrders.map { orders ->
            orders.filter { it.buyerId == buyerId }
        }
    }
}
