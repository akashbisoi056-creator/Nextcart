package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.AuthState
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout()
            }
        }
    }
}

object Routes {
    // Buyer / Public
    const val HOME = "home"
    const val CATEGORY_BROWSE = "category_browse"
    const val PRODUCT_DETAILS = "product_details/{productId}"
    const val CART = "cart"
    const val WISHLIST = "wishlist"
    const val CHECKOUT = "checkout"
    const val ORDER_SUCCESS = "order_success/{orderId}"
    const val MY_ORDERS = "my_orders"
    const val ORDER_TRACKING = "order_tracking/{orderId}"
    const val PROFILE = "profile"
    const val SAVED_ADDRESSES = "saved_addresses"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val BECOME_SELLER = "become_seller"

    // Seller Dashboard
    const val SELLER_PORTAL = "seller_portal"
    const val SELLER_ADD_PRODUCT = "seller_add_product"
    const val SELLER_MANAGE_PRODUCTS = "seller_manage_products"
    const val SELLER_ORDERS = "seller_orders"
    const val SELLER_PAYOUTS = "seller_payouts"
    const val SELLER_SETTINGS = "seller_settings"
    const val SELLER_RETURNS = "seller_returns"
    const val SELLER_PROFILE = "seller_profile"

    // Admin Dashboard
    const val ADMIN_PORTAL = "admin_portal"
    const val ADMIN_COMMISSIONS = "admin_commissions"
    const val ADMIN_SELLERS = "admin_sellers"
    const val ADMIN_PRODUCTS = "admin_products"
    const val ADMIN_ORDERS = "admin_orders"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout() {
    val viewModel: MarketplaceViewModel = viewModel()
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val cartItems by viewModel.getCartItemsFlow().collectAsStateWithLifecycle(emptyList())

    var activeRole by remember { mutableStateOf("buyer") } // "buyer", "seller", "admin"
    var expandedRoleMenu by remember { mutableStateOf(false) }

    val cartCount = cartItems.sumOf { it.second.quantity }

    Scaffold(
        topBar = {
            Column {
                AnnouncementBar()
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "NEXT CART",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Smart Shopping, Better Prices",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    },
                    actions = {
                        // Demo Portal Switcher (Extremely helpful for testing and demo reviews!)
                        Box {
                            Button(
                                onClick = { expandedRoleMenu = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("portal_switcher_btn")
                            ) {
                                Icon(
                                    imageVector = when (activeRole) {
                                        "seller" -> Icons.Default.Storefront
                                        "admin" -> Icons.Default.AdminPanelSettings
                                        else -> Icons.Default.ShoppingBag
                                    },
                                    contentDescription = "Role",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (activeRole) {
                                        "seller" -> "Seller Portal"
                                        "admin" -> "Admin Control"
                                        else -> "Buyer Store"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Down", modifier = Modifier.size(14.dp))
                            }

                            DropdownMenu(expanded = expandedRoleMenu, onDismissRequest = { expandedRoleMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Buyer Store (Customer)") },
                                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Buyer") },
                                    onClick = {
                                        activeRole = "buyer"
                                        expandedRoleMenu = false
                                        navController.navigate(Routes.HOME) {
                                            popUpTo(0)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Seller Portal (Merchant)") },
                                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = "Seller") },
                                    onClick = {
                                        expandedRoleMenu = false
                                        if (currentUser?.role == "seller") {
                                            activeRole = "seller"
                                            navController.navigate(Routes.SELLER_PORTAL) {
                                                popUpTo(0)
                                            }
                                        } else {
                                            // Prompt Seller Login or Onboarding
                                            activeRole = "buyer"
                                            navController.navigate(Routes.LOGIN)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Admin Console (Platform Owner)") },
                                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                    onClick = {
                                        expandedRoleMenu = false
                                        if (currentUser?.role == "admin") {
                                            activeRole = "admin"
                                            navController.navigate(Routes.ADMIN_PORTAL) {
                                                popUpTo(0)
                                            }
                                        } else {
                                            activeRole = "buyer"
                                            navController.navigate(Routes.LOGIN)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (activeRole == "buyer") {
                            // Shopping Cart Badge Icon
                            IconButton(onClick = { navController.navigate(Routes.CART) }) {
                                BadgedBox(
                                    badge = {
                                        if (cartCount > 0) {
                                            Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                                Text(cartCount.toString(), color = Color.White)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                                }
                            }
                        }

                        // Profile / Auth Status Indicator
                        IconButton(onClick = {
                            if (currentUser != null) {
                                if (currentUser!!.role == "seller") {
                                    navController.navigate(Routes.SELLER_PROFILE)
                                } else {
                                    navController.navigate(Routes.PROFILE)
                                }
                            } else {
                                navController.navigate(Routes.LOGIN)
                            }
                        }) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle,
                                contentDescription = "Profile",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            }
        },
        bottomBar = {
            // Context Aware Bottom Navigation Bars to give native experience!
            when (activeRole) {
                "buyer" -> {
                    NavigationBar {
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.HOME,
                            onClick = { navController.navigate(Routes.HOME) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.CATEGORY_BROWSE,
                            onClick = {
                                viewModel.selectedCategory.value = "Electronics"
                                navController.navigate(Routes.CATEGORY_BROWSE)
                            },
                            icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                            label = { Text("Categories", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.WISHLIST,
                            onClick = { navController.navigate(Routes.WISHLIST) },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Wishlist") },
                            label = { Text("Wishlist", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.MY_ORDERS,
                            onClick = { navController.navigate(Routes.MY_ORDERS) },
                            icon = { Icon(Icons.Default.ListAlt, contentDescription = "Orders") },
                            label = { Text("Orders", fontSize = 11.sp) }
                        )
                    }
                }
                "seller" -> {
                    NavigationBar {
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.SELLER_PORTAL,
                            onClick = { navController.navigate(Routes.SELLER_PORTAL) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Overview", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.SELLER_ADD_PRODUCT,
                            onClick = { navController.navigate(Routes.SELLER_ADD_PRODUCT) },
                            icon = { Icon(Icons.Default.AddBox, contentDescription = "Add Product") },
                            label = { Text("Upload", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.SELLER_MANAGE_PRODUCTS,
                            onClick = { navController.navigate(Routes.SELLER_MANAGE_PRODUCTS) },
                            icon = { Icon(Icons.Default.Inventory, contentDescription = "Catalog") },
                            label = { Text("Catalog", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.SELLER_ORDERS,
                            onClick = { navController.navigate(Routes.SELLER_ORDERS) },
                            icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Orders") },
                            label = { Text("Fulfill", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.SELLER_PAYOUTS,
                            onClick = { navController.navigate(Routes.SELLER_PAYOUTS) },
                            icon = { Icon(Icons.Default.Payments, contentDescription = "Payouts") },
                            label = { Text("Payouts", fontSize = 11.sp) }
                        )
                    }
                }
                "admin" -> {
                    NavigationBar {
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.ADMIN_PORTAL,
                            onClick = { navController.navigate(Routes.ADMIN_PORTAL) },
                            icon = { Icon(Icons.Default.Analytics, contentDescription = "Overview") },
                            label = { Text("Analytics", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.ADMIN_COMMISSIONS,
                            onClick = { navController.navigate(Routes.ADMIN_COMMISSIONS) },
                            icon = { Icon(Icons.Default.Percent, contentDescription = "Commissions") },
                            label = { Text("Rates", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.ADMIN_SELLERS,
                            onClick = { navController.navigate(Routes.ADMIN_SELLERS) },
                            icon = { Icon(Icons.Default.Group, contentDescription = "KYC Audit") },
                            label = { Text("KYC Audit", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.ADMIN_PRODUCTS,
                            onClick = { navController.navigate(Routes.ADMIN_PRODUCTS) },
                            icon = { Icon(Icons.Default.GridOn, contentDescription = "Products") },
                            label = { Text("Audit Prods", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = navController.currentBackStackEntry?.destination?.route == Routes.ADMIN_ORDERS,
                            onClick = { navController.navigate(Routes.ADMIN_ORDERS) },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                            label = { Text("Orders", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- BUYER / PUBLIC STOREFRONT ---
            composable(Routes.HOME) {
                BuyerHomeScreen(
                    viewModel = viewModel,
                    onNavigateToProduct = { id -> navController.navigate("product_details/$id") },
                    onNavigateToCategory = { cat -> navController.navigate(Routes.CATEGORY_BROWSE) },
                    onNavigateToSellerRegistration = { navController.navigate(Routes.BECOME_SELLER) }
                )
            }

            composable(Routes.CATEGORY_BROWSE) {
                CategoryBrowseScreen(
                    viewModel = viewModel,
                    onNavigateToProduct = { id -> navController.navigate("product_details/$id") }
                )
            }

            composable(
                route = Routes.PRODUCT_DETAILS,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStackEntry ->
                val prodId = backStackEntry.arguments?.getInt("productId") ?: 1
                ProductDetailScreen(
                    viewModel = viewModel,
                    productId = prodId,
                    onNavigateToCart = { navController.navigate(Routes.CART) }
                )
            }

            composable(Routes.CART) {
                CartScreen(
                    viewModel = viewModel,
                    onNavigateToCheckout = { navController.navigate(Routes.CHECKOUT) },
                    onNavigateToProduct = { id -> navController.navigate("product_details/$id") }
                )
            }

            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    viewModel = viewModel,
                    onNavigateToSuccess = { orderId -> navController.navigate("order_success/$orderId") }
                )
            }

            composable(
                route = Routes.ORDER_SUCCESS,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderSuccessScreen(
                    orderId = orderId,
                    onNavigateHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onNavigateOrders = {
                        navController.navigate(Routes.MY_ORDERS) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }

            composable(Routes.MY_ORDERS) {
                MyOrdersScreen(
                    viewModel = viewModel,
                    onNavigateToTracking = { id -> navController.navigate("order_tracking/$id") }
                )
            }

            composable(
                route = Routes.ORDER_TRACKING,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTrackingScreen(viewModel = viewModel, orderId = orderId)
            }

            composable(Routes.PROFILE) {
                UserProfileScreen(
                    viewModel = viewModel,
                    onNavigateToAddresses = { navController.navigate(Routes.SAVED_ADDRESSES) },
                    onLogout = {
                        activeRole = "buyer"
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.SAVED_ADDRESSES) {
                SavedAddressesScreen(viewModel = viewModel)
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignup = { navController.navigate(Routes.SIGNUP) },
                    onLoginSuccess = { role ->
                        if (role == "seller") {
                            activeRole = "seller"
                            navController.navigate(Routes.SELLER_PORTAL) {
                                popUpTo(Routes.HOME)
                            }
                        } else if (role == "admin") {
                            activeRole = "admin"
                            navController.navigate(Routes.ADMIN_PORTAL) {
                                popUpTo(Routes.HOME)
                            }
                        } else {
                            activeRole = "buyer"
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.SIGNUP) {
                SignupScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                    onSignupSuccess = {
                        activeRole = "buyer"
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.BECOME_SELLER) {
                BecomeSellerScreen(
                    viewModel = viewModel,
                    onSuccess = {
                        activeRole = "seller"
                        navController.navigate(Routes.SELLER_PORTAL) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }

            // --- SELLER CENTRAL PORTAL ---
            composable(Routes.SELLER_PORTAL) {
                SellerDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddProduct = { navController.navigate(Routes.SELLER_ADD_PRODUCT) },
                    onNavigateToProducts = { navController.navigate(Routes.SELLER_MANAGE_PRODUCTS) },
                    onNavigateToOrders = { navController.navigate(Routes.SELLER_ORDERS) },
                    onNavigateToPayouts = { navController.navigate(Routes.SELLER_PAYOUTS) },
                    onNavigateToSettings = { navController.navigate(Routes.SELLER_SETTINGS) },
                    onNavigateToReturns = { navController.navigate(Routes.SELLER_RETURNS) },
                    onNavigateToProfile = { navController.navigate(Routes.SELLER_PROFILE) }
                )
            }

            composable(Routes.SELLER_ADD_PRODUCT) {
                SellerAddProductScreen(
                    viewModel = viewModel,
                    onSuccess = {
                        navController.navigate(Routes.SELLER_MANAGE_PRODUCTS) {
                            popUpTo(Routes.SELLER_PORTAL)
                        }
                    }
                )
            }

            composable(Routes.SELLER_MANAGE_PRODUCTS) {
                SellerManageProductsScreen(viewModel = viewModel)
            }

            composable(Routes.SELLER_ORDERS) {
                SellerOrdersScreen(viewModel = viewModel)
            }

            composable(Routes.SELLER_PAYOUTS) {
                SellerPayoutsScreen(viewModel = viewModel)
            }

            composable(Routes.SELLER_RETURNS) {
                SellerReturnsScreen(viewModel = viewModel)
            }

            composable(Routes.SELLER_SETTINGS) {
                SellerShippingSettingsScreen(viewModel = viewModel)
            }

            composable(Routes.SELLER_PROFILE) {
                SellerProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        activeRole = "buyer"
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            // --- ADMIN CONTROL CONSOLE ---
            composable(Routes.ADMIN_PORTAL) {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSellers = { navController.navigate(Routes.ADMIN_SELLERS) },
                    onNavigateToProducts = { navController.navigate(Routes.ADMIN_PRODUCTS) },
                    onNavigateToOrders = { navController.navigate(Routes.ADMIN_ORDERS) },
                    onNavigateToCommissions = { navController.navigate(Routes.ADMIN_COMMISSIONS) }
                )
            }

            composable(Routes.ADMIN_COMMISSIONS) {
                AdminCommissionsScreen(viewModel = viewModel)
            }

            composable(Routes.ADMIN_SELLERS) {
                AdminSellersScreen(viewModel = viewModel)
            }

            composable(Routes.ADMIN_PRODUCTS) {
                AdminProductsScreen(viewModel = viewModel)
            }

            composable(Routes.ADMIN_ORDERS) {
                AdminOrdersScreen(viewModel = viewModel)
            }
        }
    }
}

// --- SAVED ADDRESSES MANAGER ---
@Composable
fun SavedAddressesScreen(
    viewModel: MarketplaceViewModel
) {
    val addresses = viewModel.getAddresses()

    var showAddressForm by remember { mutableStateOf(false) }
    var addName by remember { mutableStateOf("") }
    var addPhone by remember { mutableStateOf("") }
    var addLine by remember { mutableStateOf("") }
    var addCity by remember { mutableStateOf("") }
    var addState by remember { mutableStateOf("") }
    var addPin by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manage Saved Addresses", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (!showAddressForm) {
                    Button(
                        onClick = { showAddressForm = true },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("+ Add Address", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showAddressForm) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("New Delivery Address", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(value = addName, onValueChange = { addName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = addPhone, onValueChange = { addPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = addLine, onValueChange = { addLine = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(value = addCity, onValueChange = { addCity = it }, label = { Text("City") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = addState, onValueChange = { addState = it }, label = { Text("State") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = addPin, onValueChange = { addPin = it }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (addName.isNotEmpty() && addPhone.isNotEmpty() && addLine.isNotEmpty()) {
                                        viewModel.saveAddress(
                                            Address(
                                                name = addName,
                                                phone = addPhone,
                                                addressLine = addLine,
                                                city = addCity,
                                                state = addState,
                                                pincode = addPin
                                            )
                                        )
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
                                Text("Save")
                            }
                            OutlinedButton(onClick = { showAddressForm = false }, shape = RoundedCornerShape(4.dp)) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }

        if (addresses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp), contentAlignment = Alignment.Center
                ) {
                    Text("No addresses saved yet.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            items(addresses) { addr ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(addr.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(addr.toDisplayString(), fontSize = 11.sp, color = Color.DarkGray)
                        }

                        IconButton(onClick = { viewModel.deleteAddress(addr.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
