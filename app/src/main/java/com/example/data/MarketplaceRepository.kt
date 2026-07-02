package com.example.data

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SettlementBreakdown(
    val productId: Int,
    val title: String,
    val quantity: Int,
    val sellingPrice: Double,
    val grossValue: Double,
    val discount: Double,
    val shippingCollected: Double,
    val codCollected: Double,
    val platformFee: Double,
    val commissionRate: Double,
    val commissionDeducted: Double,
    val pgFee: Double,
    val returnAdjustment: Double,
    val netEarnings: Double,
    val status: String, // "Pending", "Settled", "Refunded"
    val payoutDate: String
)

class MarketplaceRepository(context: Context) {
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "nextcart_marketplace_db"
    )
    .fallbackToDestructiveMigration()
    .build()

    private val dao = database.marketplaceDao()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Run seed on startup if database is empty
        repositoryScope.launch {
            val config = dao.getConfig()
            if (config == null) {
                // First seed config and sellers
                Seeder.seedDatabase(dao)
                
                // Then seed products if we have none
                val products = Seeder.generateProductsToSeed()
                for (prod in products) {
                    dao.insertProduct(prod)
                }
            }
        }
    }

    // --- Configurations ---
    val config: Flow<ConfigEntity?> = dao.getConfigFlow().flowOn(Dispatchers.IO)
    suspend fun saveConfig(newConfig: ConfigEntity) = withContext(Dispatchers.IO) {
        dao.insertConfig(newConfig)
    }

    // --- User Management ---
    fun getUserById(id: String): Flow<UserEntity?> = dao.getUserById(id).flowOn(Dispatchers.IO)
    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers().flowOn(Dispatchers.IO)

    suspend fun registerUser(name: String, email: String, mobile: String, passwordHash: String, role: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val existing = dao.getUserByEmailDirect(email)
        if (existing != null) {
            return@withContext Result.failure(Exception("Email already registered!"))
        }
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            mobile = mobile,
            passwordHash = passwordHash,
            role = role
        )
        dao.insertUser(user)
        Result.success(user)
    }

    suspend fun loginUser(email: String, passwordHash: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = dao.getUserByEmailDirect(email)
        if (user == null) {
            return@withContext Result.failure(Exception("User not found!"))
        }
        if (user.passwordHash != passwordHash) {
            return@withContext Result.failure(Exception("Incorrect password!"))
        }
        Result.success(user)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        dao.insertUser(user)
    }

    // --- Seller Management ---
    fun getSellerById(id: String): Flow<SellerEntity?> = dao.getSellerById(id).flowOn(Dispatchers.IO)
    fun getAllSellers(): Flow<List<SellerEntity>> = dao.getAllSellers().flowOn(Dispatchers.IO)

    suspend fun registerSeller(
        name: String,
        businessName: String,
        email: String,
        mobile: String,
        passwordHash: String,
        pan: String,
        gst: String,
        address: String,
        bankHolderName: String,
        bankAccount: String,
        ifsc: String
    ): Result<SellerEntity> = withContext(Dispatchers.IO) {
        val existingUser = dao.getUserByEmailDirect(email)
        val existingSeller = dao.getSellerByEmailDirect(email)
        if (existingUser != null || existingSeller != null) {
            return@withContext Result.failure(Exception("Email already in use!"))
        }
        if (pan.length != 10) {
            return@withContext Result.failure(Exception("PAN must be exactly 10 alphanumeric characters!"))
        }

        val sellerId = "seller_" + UUID.randomUUID().toString().take(6)
        val seller = SellerEntity(
            id = sellerId,
            name = name,
            businessName = businessName,
            email = email,
            mobile = mobile,
            passwordHash = passwordHash,
            pan = pan.uppercase(),
            gst = gst.uppercase(),
            address = address,
            bankHolderName = bankHolderName,
            bankAccount = bankAccount,
            ifsc = ifsc.uppercase(),
            kycStatus = "VERIFIED", // Auto-verify KYC mock to make demo onboarding satisfying!
            defaultShippingCharge = 49.0,
            freeShippingThreshold = 999.0
        )
        dao.insertSeller(seller)

        // Also insert as user role="seller" so they can log in to the same platform
        val user = UserEntity(
            id = sellerId,
            name = name,
            email = email,
            mobile = mobile,
            passwordHash = passwordHash,
            role = "seller"
        )
        dao.insertUser(user)

        Result.success(seller)
    }

    suspend fun updateSeller(seller: SellerEntity) = withContext(Dispatchers.IO) {
        dao.insertSeller(seller)
    }

    // --- Product Management ---
    fun getAllProducts(): Flow<List<ProductEntity>> = dao.getAllProducts().flowOn(Dispatchers.IO)
    fun getProductById(id: Int): Flow<ProductEntity?> = dao.getProductById(id).flowOn(Dispatchers.IO)

    suspend fun addProduct(
        sellerId: String,
        sellerName: String,
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
        returnPolicy: String
    ): Result<ProductEntity> = withContext(Dispatchers.IO) {
        if (category !in listOf("Fashion", "Books", "Electronics")) {
            return@withContext Result.failure(Exception("Products can only be added under Fashion, Books, or Electronics!"))
        }
        val slug = title.lowercase().replace(" ", "-").replace("/", "-")
        val product = ProductEntity(
            sellerId = sellerId,
            sellerName = sellerName,
            title = title,
            slug = slug,
            category = category,
            subcategory = subcategory,
            brand = brand,
            price = price,
            originalPrice = originalPrice,
            discountPercent = discountPercent,
            stock = stock,
            description = description,
            highlightsJson = Converters().toStringList(highlights) ?: "[]",
            specsJson = Converters().toStringMap(specs) ?: "{}",
            shippingCharge = shippingCharge,
            returnPolicy = returnPolicy,
            isFreeShippingEligible = price >= 999.0,
            deliveryEstimate = "Delivery in 3-5 Days"
        )
        dao.insertProduct(product)
        Result.success(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        dao.insertProduct(product)
    }

    suspend fun deleteProduct(productId: Int) = withContext(Dispatchers.IO) {
        dao.deleteProductById(productId)
    }

    // --- Order Processing ---
    fun getAllOrders(): Flow<List<OrderEntity>> = dao.getAllOrders().flowOn(Dispatchers.IO)
    fun getOrdersByBuyer(buyerId: String): Flow<List<OrderEntity>> = dao.getOrdersByBuyer(buyerId).flowOn(Dispatchers.IO)

    suspend fun placeOrder(
        buyerId: String,
        buyerName: String,
        buyerMobile: String,
        items: List<OrderItem>,
        shippingAddress: Address,
        paymentMethod: String,
        subtotal: Double,
        shippingCharge: Double,
        codCharge: Double,
        platformFee: Double,
        discount: Double,
        totalPayable: Double
    ): Result<OrderEntity> = withContext(Dispatchers.IO) {
        val orderId = "NC" + System.currentTimeMillis().toString().takeLast(8) + (10..99).random()

        // Serialize fields
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val orderItemType = Types.newParameterizedType(List::class.java, OrderItem::class.java)
        val itemsJson = moshi.adapter<List<OrderItem>>(orderItemType).toJson(items)
        val addressJson = moshi.adapter<Address>(Address::class.java).toJson(shippingAddress)

        val order = OrderEntity(
            id = orderId,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerMobile = buyerMobile,
            itemsJson = itemsJson,
            shippingAddressJson = addressJson,
            paymentMethod = paymentMethod,
            subtotal = subtotal,
            shippingCharge = shippingCharge,
            codCharge = codCharge,
            platformFee = platformFee,
            discount = discount,
            totalPayable = totalPayable,
            status = "Pending"
        )

        dao.insertOrder(order)

        // Deduct product inventory
        for (item in items) {
            val prod = dao.getProductById(item.productId).first()
            if (prod != null) {
                val updatedProd = prod.copy(stock = (prod.stock - item.quantity).coerceAtLeast(0))
                dao.insertProduct(updatedProd)
            }
        }

        // Empty buyer's cart
        val buyer = dao.getUserById(buyerId).first()
        if (buyer != null) {
            val updatedBuyer = buyer.copy(cartJson = "[]")
            dao.insertUser(updatedBuyer)
        }

        Result.success(order)
    }

    suspend fun updateOrderStatus(orderId: String, status: String, trackingNumber: String = "") = withContext(Dispatchers.IO) {
        val orderFlow = dao.getAllOrders().first()
        val order = orderFlow.firstOrNull { it.id == orderId }
        if (order != null) {
            val updatedOrder = order.copy(
                status = status,
                trackingNumber = if (trackingNumber.isNotEmpty()) trackingNumber else order.trackingNumber
            )
            dao.insertOrder(updatedOrder)
        }
    }

    suspend fun requestReturn(orderId: String, reason: String) = withContext(Dispatchers.IO) {
        val orderFlow = dao.getAllOrders().first()
        val order = orderFlow.firstOrNull { it.id == orderId }
        if (order != null) {
            val updatedOrder = order.copy(
                status = "Return Requested",
                returnReason = reason,
                refundStatus = "Refund Initiated"
            )
            dao.insertOrder(updatedOrder)
        }
    }

    suspend fun processReturnApproval(orderId: String, approved: Boolean) = withContext(Dispatchers.IO) {
        val orderFlow = dao.getAllOrders().first()
        val order = orderFlow.firstOrNull { it.id == orderId }
        if (order != null) {
            val (newStatus, refundStatus) = if (approved) {
                Pair("Returned", "Refund Completed")
            } else {
                Pair("Delivered", "None")
            }
            val updatedOrder = order.copy(
                status = newStatus,
                refundStatus = refundStatus
            )
            dao.insertOrder(updatedOrder)

            // Put items back into stock if approved
            if (approved) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val orderItemType = Types.newParameterizedType(List::class.java, OrderItem::class.java)
                val items = moshi.adapter<List<OrderItem>>(orderItemType).fromJson(order.itemsJson) ?: emptyList()
                for (item in items) {
                    val prod = dao.getProductById(item.productId).first()
                    if (prod != null) {
                        dao.insertProduct(prod.copy(stock = prod.stock + item.quantity))
                    }
                }
            }
        }
    }

    // --- Settlement Economics (Indian Marketplace Splits) ---
    suspend fun calculateSettlement(order: OrderEntity, item: OrderItem): SettlementBreakdown = withContext(Dispatchers.IO) {
        val cfg = dao.getConfig() ?: ConfigEntity()

        val grossValue = item.price * item.quantity
        val discount = 0.0 // Discount is already deducted from selling price

        // Determine commission percentage based on product category
        val commissionRate = when (item.category) {
            "Fashion" -> cfg.fashionCommission
            "Books" -> cfg.booksCommission
            "Electronics" -> cfg.electronicsCommission
            else -> 10.0
        }
        val commissionDeducted = grossValue * (commissionRate / 100.0)

        // Gateway Fee: prepaid gets 2%, COD gets 0%
        val pgFeeRate = if (order.paymentMethod != "COD") cfg.prepaidPaymentGatewayFeePercent else 0.0
        val pgFee = grossValue * (pgFeeRate / 100.0)

        // Split platform fees & COD charges proportionally to the items
        val totalItemsCount = try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val orderItemType = Types.newParameterizedType(List::class.java, OrderItem::class.java)
            val allItems = moshi.adapter<List<OrderItem>>(orderItemType).fromJson(order.itemsJson) ?: emptyList()
            allItems.sumOf { it.quantity }
        } catch (e: Exception) {
            1
        }
        
        val platformFee = (cfg.defaultPlatformFee / totalItemsCount) * item.quantity
        val codCollected = if (order.paymentMethod == "COD") {
            (cfg.defaultCodCharge / totalItemsCount) * item.quantity
        } else {
            0.0
        }

        // Shipping Collected for this item
        val shippingCollected = item.shippingCharge

        // Reverse shipping deduction if returned
        val returnAdjustment = if (order.status == "Returned") {
            // Deduct original shipping & gateway cost as return logistics penalty
            -(shippingCollected + 30.0)
        } else {
            0.0
        }

        // Net Earnings Formula:
        // Earnings = Gross - Commission - PlatformFee - PGFee + ShippingCollected + ReturnPenalty
        val netEarnings = grossValue - commissionDeducted - platformFee - pgFee + shippingCollected + returnAdjustment

        val status = when (order.status) {
            "Returned" -> "Refunded"
            "Delivered" -> "Settled"
            "Cancelled" -> "Refunded"
            else -> "Pending"
        }

        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val payoutDate = if (status == "Settled") {
            df.format(Date(order.orderDate + 7 * 24 * 60 * 60 * 1000)) // 7-day payout cycle
        } else {
            "N/A"
        }

        SettlementBreakdown(
            productId = item.productId,
            title = item.title,
            quantity = item.quantity,
            sellingPrice = item.price,
            grossValue = grossValue,
            discount = discount,
            shippingCollected = shippingCollected,
            codCollected = codCollected,
            platformFee = platformFee,
            commissionRate = commissionRate,
            commissionDeducted = commissionDeducted,
            pgFee = pgFee,
            returnAdjustment = returnAdjustment,
            netEarnings = netEarnings,
            status = status,
            payoutDate = payoutDate
        )
    }

    suspend fun getSellerSettlements(sellerId: String): List<SettlementBreakdown> = withContext(Dispatchers.IO) {
        val allOrders = dao.getAllOrders().first()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val orderItemType = Types.newParameterizedType(List::class.java, OrderItem::class.java)
        
        val list = mutableListOf<SettlementBreakdown>()
        for (order in allOrders) {
            val items = try {
                moshi.adapter<List<OrderItem>>(orderItemType).fromJson(order.itemsJson) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            val sellerItems = items.filter { it.sellerId == sellerId }
            for (item in sellerItems) {
                list.add(calculateSettlement(order, item))
            }
        }
        list
    }
}
