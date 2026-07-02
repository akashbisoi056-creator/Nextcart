package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.*
import com.example.viewmodel.AuthState
import com.example.viewmodel.MarketplaceViewModel
import java.util.Locale

// --- BUYER HOME SCREEN ---
@Composable
fun BuyerHomeScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToProduct: (Int) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToSellerRegistration: () -> Unit
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    var homeSearchText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
    ) {
        // Hero Slider / Branding Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1E3A8A),
                                Color(0xFF3B82F6)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "BIG SAVE DAYS ARE ON",
                        color = Color(0xFFFFA000),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Next Cart Marketplace",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Smart Shopping, Better Prices from Indian Vendors",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onNavigateToCategory("Electronics") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("Shop Electronics", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Category Strip (Fashion, Books, Electronics)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cats = listOf(
                    Triple("Fashion", Icons.Default.Checkroom, Color(0xFFFF8A80)),
                    Triple("Books", Icons.Default.Book, Color(0xFF80DEEA)),
                    Triple("Electronics", Icons.Default.PhoneAndroid, Color(0xFFCE93D8))
                )

                cats.forEach { (cat, icon, color) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                viewModel.selectedCategory.value = cat
                                onNavigateToCategory(cat)
                            }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = cat, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = cat, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search Bar Row
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = homeSearchText,
                    onValueChange = {
                        homeSearchText = it
                        viewModel.searchQuery.value = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    placeholder = { Text("Search for products, brands and categories...", fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (homeSearchText.isNotEmpty()) {
                            IconButton(onClick = {
                                homeSearchText = ""
                                viewModel.searchQuery.value = ""
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F3F6),
                        unfocusedContainerColor = Color(0xFFF1F3F6),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }

        // Featured Electronics
        item {
            SectionHeader(title = "Trending Electronics", onSeeAll = {
                viewModel.selectedCategory.value = "Electronics"
                onNavigateToCategory("Electronics")
            })
            val elecProducts = products.filter { it.category == "Electronics" }.take(6)
            HorizontalProductRow(
                products = elecProducts,
                viewModel = viewModel,
                onNavigateToProduct = onNavigateToProduct
            )
        }

        // Fashion Picks
        item {
            SectionHeader(title = "Fashion Highlights", onSeeAll = {
                viewModel.selectedCategory.value = "Fashion"
                onNavigateToCategory("Fashion")
            })
            val fashionProds = products.filter { it.category == "Fashion" }.take(6)
            HorizontalProductRow(
                products = fashionProds,
                viewModel = viewModel,
                onNavigateToProduct = onNavigateToProduct
            )
        }

        // Popular Books
        item {
            SectionHeader(title = "Popular Books & Academics", onSeeAll = {
                viewModel.selectedCategory.value = "Books"
                onNavigateToCategory("Books")
            })
            val booksProds = products.filter { it.category == "Books" }.take(6)
            HorizontalProductRow(
                products = booksProds,
                viewModel = viewModel,
                onNavigateToProduct = onNavigateToProduct
            )
        }

        // Onboarding Seller Call-to-Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Grow Your Business with Next Cart",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Register as a seller with PAN card only. GST is optional for registration. Set your shipping rates & process orders instantly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToSellerRegistration,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Start Selling Now", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Footer Section
        item {
            MarketplaceFooter()
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(
            text = "See All",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.clickable(onClick = onSeeAll)
        )
    }
}

@Composable
fun HorizontalProductRow(
    products: List<ProductEntity>,
    viewModel: MarketplaceViewModel,
    onNavigateToProduct: (Int) -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val wishlistIds = try {
        val type = Types.newParameterizedType(List::class.java, java.lang.Integer::class.java)
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<Int>>(type)
            .fromJson(user?.wishlistJson ?: "[]") ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp), contentAlignment = Alignment.Center
        ) {
            Text("No Products Seeded", color = Color.Gray, fontSize = 12.sp)
        }
    } else {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products) { prod ->
                ProductCard(
                    product = prod,
                    onClick = { onNavigateToProduct(prod.id) },
                    onWishlistToggle = { viewModel.toggleWishlist(prod.id) },
                    isWishlisted = prod.id in wishlistIds,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
    }
}

// --- CATEGORY & SEARCH FILTER PAGE ---
@Composable
fun CategoryBrowseScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToProduct: (Int) -> Unit
) {
    val filteredProds by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    val wishlistIds = try {
        val type = Types.newParameterizedType(List::class.java, java.lang.Integer::class.java)
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<Int>>(type)
            .fromJson(user?.wishlistJson ?: "[]") ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val subcategories = when (selectedCategory) {
        "Fashion" -> listOf("Men’s Clothing", "Women’s Clothing", "Kids Wear", "Footwear", "Bags & Accessories")
        "Books" -> listOf("Fiction", "Non-Fiction", "Academic", "Competitive Exam Books", "Children’s Books")
        "Electronics" -> listOf("Smartphones", "Earbuds / Headphones", "Smartwatches", "Laptops", "Accessories", "Power Banks", "Speakers")
        else -> emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal subcategory strip
        if (subcategories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedSubcategory == null,
                            onClick = { viewModel.selectedSubcategory.value = null },
                            label = { Text("All Products", fontSize = 12.sp) }
                        )
                    }
                    items(subcategories) { sub ->
                        FilterChip(
                            selected = selectedSubcategory == sub,
                            onClick = { viewModel.selectedSubcategory.value = sub },
                            label = { Text(sub, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Filter / Sort Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val sortByText by viewModel.sortBy.collectAsStateWithLifecycle()
            var showSortMenu by remember { mutableStateOf(false) }

            Text(
                text = "${filteredProds.size} items in $selectedCategory",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Box {
                Button(
                    onClick = { showSortMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(sortByText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    listOf("Popularity", "Price Low-High", "Price High-Low", "Rating", "Newest").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.sortBy.value = option
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Products Grid
        if (filteredProds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.Inventory, contentDescription = "Empty", modifier = Modifier.size(60.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No products match your filters.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F3F6)),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProds) { prod ->
                    ProductCard(
                        product = prod,
                        onClick = { onNavigateToProduct(prod.id) },
                        onWishlistToggle = { viewModel.toggleWishlist(prod.id) },
                        isWishlisted = prod.id in wishlistIds
                    )
                }
            }
        }
    }
}

// --- PRODUCT DETAILS SCREEN ---
@Composable
fun ProductDetailScreen(
    viewModel: MarketplaceViewModel,
    productId: Int,
    onNavigateToCart: () -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val product = allProducts.find { it.id == productId }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found.")
        }
        return
    }

    val wishlistIds = try {
        val type = Types.newParameterizedType(List::class.java, java.lang.Integer::class.java)
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<Int>>(type)
            .fromJson(user?.wishlistJson ?: "[]") ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    val isWishlisted = product.id in wishlistIds

    val highlightsList = try {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<String>>(type)
            .fromJson(product.highlightsJson) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val specsMap = try {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<Map<String, String>>(type)
            .fromJson(product.specsJson) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF1F3F6))
        ) {
            item {
                ProductImage(
                    category = product.category,
                    subcategory = product.subcategory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.brand,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { viewModel.toggleWishlist(product.id) }) {
                                Icon(
                                    imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Wishlist",
                                    tint = if (isWishlisted) Color.Red else Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RatingBadge(rating = product.rating, count = product.reviewCount)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "₹${product.price.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${product.originalPrice.toInt()}",
                                textDecoration = TextDecoration.LineThrough,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${product.discountPercent}% OFF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF388E3C)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (product.stock > 0) "In Stock (${product.stock} units left)" else "Out of Stock",
                            color = if (product.stock > 0) Color(0xFF388E3C) else Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Indian Marketplace Delivery / Shipping Info Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Delivery & Vendor Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sold By:", fontSize = 12.sp, color = Color.Gray)
                            Text(product.sellerName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Fee:", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = if (product.price >= 999.0 || product.shippingCharge == 0.0) "FREE Delivery" else "₹${product.shippingCharge.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (product.price >= 999.0 || product.shippingCharge == 0.0) Color(0xFF388E3C) else Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Timeline:", fontSize = 12.sp, color = Color.Gray)
                            Text(product.deliveryEstimate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Return/Refund Window:", fontSize = 12.sp, color = Color.Gray)
                            Text(product.returnPolicy, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFE65100))
                        }
                    }
                }
            }

            // Highlights
            if (highlightsList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Product Highlights",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            highlightsList.forEach { high ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Circle,
                                        contentDescription = "Bullet",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(6.dp).padding(top = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = high, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Specifications Table
            if (specsMap.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Technical Specifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            specsMap.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = key,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = value,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Full Description
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.description,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Floating Buy / Add to Cart Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (user == null) return@Button
                    viewModel.addToCart(product.id, 1)
                    onNavigateToCart()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("add_to_cart_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                shape = RoundedCornerShape(4.dp),
                enabled = product.stock > 0
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Add to Cart")
                Spacer(modifier = Modifier.width(6.dp))
                Text("ADD TO CART", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (user == null) return@Button
                    viewModel.addToCart(product.id, 1)
                    onNavigateToCart()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("buy_now_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(4.dp),
                enabled = product.stock > 0
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Buy Now")
                Spacer(modifier = Modifier.width(6.dp))
                Text("BUY NOW", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- BUYER CART SCREEN ---
@Composable
fun CartScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToCheckout: () -> Unit,
    onNavigateToProduct: (Int) -> Unit
) {
    val cartItems by viewModel.getCartItemsFlow().collectAsStateWithLifecycle(emptyList())
    val cfg by viewModel.config.collectAsStateWithLifecycle()

    if (cartItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = "Empty Cart",
                    modifier = Modifier.size(80.dp),
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your Next Cart is empty!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Add items to start shopping.", color = Color.Gray, fontSize = 12.sp)
            }
        }
        return
    }

    val subtotal = cartItems.sumOf { it.first.price * it.second.quantity }
    val freeThreshold = cfg?.freeShippingThreshold ?: 999.0
    val isFreeShipping = subtotal > freeThreshold

    val shippingFee = if (isFreeShipping) {
        0.0
    } else {
        cartItems.sumOf { item ->
            if (item.first.shippingCharge > 0) item.first.shippingCharge else {
                when (item.first.category) {
                    "Fashion" -> cfg?.fashionShippingFee ?: 49.0
                    "Books" -> cfg?.booksShippingFee ?: 39.0
                    "Electronics" -> cfg?.electronicsShippingFee ?: 79.0
                    else -> 49.0
                }
            }
        }
    }

    val platformFee = cfg?.defaultPlatformFee ?: 9.0
    val totalPayable = subtotal + shippingFee + platformFee

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFF1F3F6))
        ) {
            // Address Pin Strip
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delivering to standard address in India",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Items List
            items(cartItems) { (product, cartItem) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        ProductImage(
                            category = product.category,
                            subcategory = product.subcategory,
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { onNavigateToProduct(product.id) }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Seller: ${product.sellerName}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "₹${product.price.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${product.originalPrice.toInt()}",
                                    textDecoration = TextDecoration.LineThrough,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${product.discountPercent}% Off",
                                    color = Color(0xFF388E3C),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quantity Adjuster
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.updateCartQuantity(product.id, cartItem.quantity - 1) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = cartItem.quantity.toString(),
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                IconButton(
                                    onClick = { viewModel.updateCartQuantity(product.id, cartItem.quantity + 1) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = "Remove",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { viewModel.removeFromCart(product.id) }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Price Details Bill
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Price Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Price (${cartItems.sumOf { it.second.quantity }} items)", fontSize = 13.sp)
                            Text("₹${subtotal.toInt()}", fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Charges", fontSize = 13.sp)
                            Text(
                                text = if (isFreeShipping) "FREE" else "₹${shippingFee.toInt()}",
                                color = if (isFreeShipping) Color(0xFF388E3C) else Color.Black,
                                fontSize = 13.sp,
                                fontWeight = if (isFreeShipping) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Secure Platform Fee", fontSize = 13.sp)
                            Text("₹${platformFee.toInt()}", fontSize = 13.sp)
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("₹${totalPayable.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Checkout Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total Payable", fontSize = 11.sp, color = Color.Gray)
                Text("₹${totalPayable.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = onNavigateToCheckout,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("checkout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("PLACE ORDER", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- CHECKOUT SCREEN ---
@Composable
fun CheckoutScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToSuccess: (String) -> Unit
) {
    val addresses = viewModel.getAddresses()
    val cartItems by viewModel.getCartItemsFlow().collectAsStateWithLifecycle(emptyList())
    val cfg by viewModel.config.collectAsStateWithLifecycle()

    var selectedAddressIndex by remember { mutableStateOf(0) }
    var paymentMethod by remember { mutableStateOf("COD") } // "COD", "UPI", "CARD"

    // Custom address addition form
    var showAddressForm by remember { mutableStateOf(false) }
    var addName by remember { mutableStateOf("") }
    var addPhone by remember { mutableStateOf("") }
    var addLine by remember { mutableStateOf("") }
    var addCity by remember { mutableStateOf("") }
    var addState by remember { mutableStateOf("") }
    var addPin by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.first.price * it.second.quantity }
    val freeThreshold = cfg?.freeShippingThreshold ?: 999.0
    val isFreeShipping = subtotal > freeThreshold

    val shippingFee = if (isFreeShipping) {
        0.0
    } else {
        cartItems.sumOf { item ->
            if (item.first.shippingCharge > 0) item.first.shippingCharge else {
                when (item.first.category) {
                    "Fashion" -> cfg?.fashionShippingFee ?: 49.0
                    "Books" -> cfg?.booksShippingFee ?: 39.0
                    "Electronics" -> cfg?.electronicsShippingFee ?: 79.0
                    else -> 49.0
                }
            }
        }
    }

    // COD charges apply only if Cash on Delivery is chosen
    val codCharge = if (paymentMethod == "COD") (cfg?.defaultCodCharge ?: 29.0) else 0.0
    val platformFee = cfg?.defaultPlatformFee ?: 9.0
    val totalPayable = subtotal + shippingFee + codCharge + platformFee

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp)
    ) {
        // Step 1: Delivery Address
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. Delivery Address", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (!showAddressForm) {
                            Button(
                                onClick = { showAddressForm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("+ Add New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (showAddressForm) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = addName, onValueChange = { addName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = addPhone, onValueChange = { addPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = addLine, onValueChange = { addLine = it }, label = { Text("Address (House No, Street)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(value = addCity, onValueChange = { addCity = it }, label = { Text("City") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = addState, onValueChange = { addState = it }, label = { Text("State") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = addPin, onValueChange = { addPin = it }, label = { Text("Pincode (6 digits)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (addName.isNotEmpty() && addPhone.isNotEmpty() && addLine.isNotEmpty()) {
                                        val newAdd = Address(
                                            name = addName,
                                            phone = addPhone,
                                            addressLine = addLine,
                                            city = addCity,
                                            state = addState,
                                            pincode = addPin
                                        )
                                        viewModel.saveAddress(newAdd)
                                        showAddressForm = false
                                        // Reset fields
                                        addName = ""
                                        addPhone = ""
                                        addLine = ""
                                        addCity = ""
                                        addState = ""
                                        addPin = ""
                                    }
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Save Address", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showAddressForm = false },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Cancel", fontSize = 12.sp)
                            }
                        }
                    } else if (addresses.isEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No delivery address saved yet. Please add one above.", color = Color.Red, fontSize = 12.sp)
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        addresses.forEachIndexed { idx, addr ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAddressIndex = idx }
                                    .background(
                                        if (selectedAddressIndex == idx) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedAddressIndex == idx, onClick = { selectedAddressIndex = idx })
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(addr.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(addr.toDisplayString(), fontSize = 11.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Payment Options
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Payment Options", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val payOptions = listOf(
                        Pair("COD", "Cash on Delivery (₹${cfg?.defaultCodCharge?.toInt()} handling fee applies)"),
                        Pair("UPI", "UPI (Instant Transfer via GPay/PhonePe)"),
                        Pair("CARD", "Credit / Debit Card (Secure checkout)")
                    )

                    payOptions.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = code }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = paymentMethod == code, onClick = { paymentMethod = code })
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Step 3: Billing breakdown
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Price Breakup", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 12.sp)
                        Text("₹${subtotal.toInt()}", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Fee", fontSize = 12.sp)
                        Text(if (isFreeShipping) "FREE" else "₹${shippingFee.toInt()}", fontSize = 12.sp, color = if (isFreeShipping) Color(0xFF388E3C) else Color.Black)
                    }
                    if (paymentMethod == "COD") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("COD Fee", fontSize = 12.sp)
                            Text("₹${codCharge.toInt()}", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Secure Platform Fee", fontSize = 12.sp)
                        Text("₹${platformFee.toInt()}", fontSize = 12.sp)
                    }
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("₹${totalPayable.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Place Order Trigger
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (addresses.isNotEmpty()) {
                        val finalAddr = addresses[selectedAddressIndex]
                        viewModel.checkout(finalAddr, paymentMethod) { orderId ->
                            onNavigateToSuccess(orderId)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("place_order_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                shape = RoundedCornerShape(4.dp),
                enabled = addresses.isNotEmpty()
            ) {
                Text("PLACE ORDER (₹${totalPayable.toInt()})", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- ORDER SUCCESS SCREEN ---
@Composable
fun OrderSuccessScreen(
    orderId: String,
    onNavigateHome: () -> Unit,
    onNavigateOrders: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF388E3C),
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Order Placed Successfully!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Order ID: $orderId",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = "Thank you for shopping on Next Cart. Your order has been registered and sent to our trusted sellers for packaging.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onNavigateOrders,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("TRACK MY ORDERS", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CONTINUE SHOPPING", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- MY ORDERS LISTING ---
@Composable
fun MyOrdersScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToTracking: (String) -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val orders by viewModel.getOrdersByBuyer(user?.id ?: "").collectAsStateWithLifecycle(emptyList())

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Outlined.ShoppingBag, contentDescription = "No orders", modifier = Modifier.size(80.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No orders placed yet!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Sellers are waiting for you.", color = Color.Gray, fontSize = 12.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6)),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(orders) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order ID: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = order.status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when (order.status) {
                                "Delivered" -> Color(0xFF388E3C)
                                "Cancelled" -> Color.Red
                                "Pending" -> Color(0xFFE65100)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    val items = try {
                        val type = Types.newParameterizedType(List::class.java, OrderItem::class.java)
                        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<OrderItem>>(type)
                            .fromJson(order.itemsJson) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    items.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            ProductImage(category = item.category, subcategory = item.category, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Qty: ${item.quantity} | Seller: ${item.sellerName}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Paid", fontSize = 11.sp, color = Color.Gray)
                            Text("₹${order.totalPayable.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Track Order
                            Button(
                                onClick = { onNavigateToTracking(order.id) },
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Track Status", fontSize = 11.sp)
                            }

                            // Cancel Order or Request Return
                            if (order.status == "Pending" || order.status == "Confirmed" || order.status == "Packed") {
                                OutlinedButton(
                                    onClick = { viewModel.updateOrderStatusBySeller(order.id, "Cancelled", "") },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Cancel", fontSize = 11.sp)
                                }
                            } else if (order.status == "Delivered") {
                                var showReturnDialog by remember { mutableStateOf(false) }
                                var returnReasonText by remember { mutableStateOf("") }

                                OutlinedButton(
                                    onClick = { showReturnDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100)),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Return Item", fontSize = 11.sp)
                                }

                                if (showReturnDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showReturnDialog = false },
                                        title = { Text("Request Return", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                                        text = {
                                            Column {
                                                Text("Please specify the reason for return/refund:", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = returnReasonText,
                                                    onValueChange = { returnReasonText = it },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    placeholder = { Text("e.g., Wrong size, Damaged product") }
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            Button(onClick = {
                                                if (returnReasonText.isNotEmpty()) {
                                                    viewModel.initiateReturnByBuyer(order.id, returnReasonText)
                                                    showReturnDialog = false
                                                }
                                            }) {
                                                Text("Submit")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showReturnDialog = false }) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ORDER TRACKING VISUAL TIMELINE ---
@Composable
fun OrderTrackingScreen(
    viewModel: MarketplaceViewModel,
    orderId: String
) {
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val order = orders.find { it.id == orderId }

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Order tracking data unavailable.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tracking Order ID: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Payment Method: ${order.paymentMethod}", fontSize = 12.sp, color = Color.Gray)
                    if (order.trackingNumber.isNotEmpty()) {
                        Text("AWB Tracking Number: ${order.trackingNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (order.status == "Cancelled") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancelled", tint = Color.Red, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Order Cancelled", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 14.sp)
                            Text("This order has been successfully cancelled and any refunds have been initiated.", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            } else if (order.status == "Return Requested" || order.status == "Returned") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AssignmentReturn, contentDescription = "Return", tint = Color(0xFFE65100), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Return Status: ${order.status}", fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontSize = 14.sp)
                            Text("Reason: ${order.returnReason}", fontSize = 11.sp, color = Color.DarkGray)
                            Text("Refund Status: ${order.refundStatus}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                TrackingTimeline(currentStatus = order.status)
            }
        }
    }
}

// --- USER PROFILE SCREEN ---
@Composable
fun UserProfileScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToAddresses: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please log in to view your profile.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user!!.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Text(user!!.email, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Mobile Number") },
                        supportingContent = { Text(user!!.mobile) },
                        leadingContent = { Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone") }
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Saved Addresses") },
                        supportingContent = { Text("Manage delivery locations") },
                        leadingContent = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Address") },
                        modifier = Modifier.clickable(onClick = onNavigateToAddresses)
                    )
                    Divider()
                    ListItem(
                        headlineContent = { Text("Log Out") },
                        supportingContent = { Text("Sign out of Next Cart") },
                        leadingContent = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout") },
                        modifier = Modifier.clickable {
                            viewModel.logout()
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

// --- AUTH PAGES (LOGIN / SIGNUP) ---
@Composable
fun LoginScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val user = (authState as AuthState.Success).user
            onLoginSuccess(user.role)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text("NEXT CART", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = MaterialTheme.colorScheme.primary)
            Text("Smart Shopping, Better Prices", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password"),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.login(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_button_submit"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("LOG IN", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                Text((authState as AuthState.Error).message, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("New to Next Cart? ", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = "Sign Up",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToSignup)
                )
            }
        }
    }
}

@Composable
fun SignupScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSignupSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), singleLine = true)

            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.registerUser(name, email, mobile, password, "buyer")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("signup_button_submit"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("SIGN UP", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                Text((authState as AuthState.Error).message, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("Already have an account? ", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = "Log In",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }
        }
    }
}
