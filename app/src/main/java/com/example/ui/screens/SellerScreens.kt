package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.*
import com.example.viewmodel.AuthState
import com.example.viewmodel.MarketplaceViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Locale

// --- BECOME A SELLER / ONBOARDING FLOW ---
@Composable
fun BecomeSellerScreen(
    viewModel: MarketplaceViewModel,
    onSuccess: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // Step 1: Profile & KYC, Step 2: Store & Address, Step 3: Bank details

    // Step 1: Profile Info & PAN
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") } // Optional!

    // Step 2: Store Name & Address
    var businessName by remember { mutableStateOf("") }
    var pickupAddress by remember { mutableStateOf("") }

    // Step 3: Bank Account Details
    var bankAccountHolderName by remember { mutableStateOf("") }
    var bankAccountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSuccess()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Next Cart Seller Onboarding", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Text("Join India's premium multi-vendor network", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Step Indicator Line
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (step >= i) Color.White else Color.White.copy(alpha = 0.4f),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(i.toString(), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (i < 3) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(if (step > i) Color.White else Color.White.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Render Step Contents
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (step) {
                        1 -> {
                            Text("Step 1: Contact & Tax Information", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name (as per PAN)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Indian Taxation KYC Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = panNumber,
                                onValueChange = { panNumber = it.uppercase() },
                                label = { Text("PAN Card Number (Required - 10 chars)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = gstNumber,
                                onValueChange = { gstNumber = it.uppercase() },
                                label = { Text("GSTIN Number (Optional - can add later)") },
                                placeholder = { Text("22AAAAA0000A1Z5") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        2 -> {
                            Text("Step 2: Business & Warehouse Location", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Business / Shop Name") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pickupAddress,
                                onValueChange = { pickupAddress = it },
                                label = { Text("Pickup Warehouse Address") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                maxLines = 4
                            )
                        }
                        3 -> {
                            Text("Step 3: Payout Bank Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = bankAccountHolderName, onValueChange = { bankAccountHolderName = it }, label = { Text("Bank Account Holder Name") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = bankAccountNumber, onValueChange = { bankAccountNumber = it }, label = { Text("Bank Account Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = ifscCode, onValueChange = { ifscCode = it.uppercase() }, label = { Text("IFSC Code (11 digits)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = agreedToTerms, onCheckedChange = { agreedToTerms = it })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("I agree to the Next Cart Seller Agreement and Commission Payout Schedules.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Next / Back Triggers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(onClick = { step-- }, shape = RoundedCornerShape(4.dp)) {
                        Text("BACK")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (step < 3) {
                    Button(
                        onClick = {
                            if (step == 1) {
                                if (fullName.isNotEmpty() && email.isNotEmpty() && mobile.isNotEmpty() && password.isNotEmpty() && panNumber.length == 10) {
                                    step++
                                }
                            } else if (step == 2) {
                                if (businessName.isNotEmpty() && pickupAddress.isNotEmpty()) {
                                    step++
                                }
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        enabled = if (step == 1) (fullName.isNotEmpty() && email.isNotEmpty() && mobile.isNotEmpty() && password.isNotEmpty() && panNumber.length == 10) else (businessName.isNotEmpty() && pickupAddress.isNotEmpty())
                    ) {
                        Text("CONTINUE")
                    }
                } else {
                    Button(
                        onClick = {
                            if (bankAccountHolderName.isNotEmpty() && bankAccountNumber.isNotEmpty() && ifscCode.length == 11 && agreedToTerms) {
                                viewModel.registerSeller(
                                    name = fullName,
                                    businessName = businessName,
                                    email = email,
                                    mobile = mobile,
                                    passwordHash = password,
                                    pan = panNumber,
                                    gst = gstNumber,
                                    address = pickupAddress,
                                    bankHolder = bankAccountHolderName,
                                    bankAccount = bankAccountNumber,
                                    ifsc = ifscCode
                                )
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        enabled = bankAccountHolderName.isNotEmpty() && bankAccountNumber.isNotEmpty() && ifscCode.length == 11 && agreedToTerms,
                        modifier = Modifier.testTag("seller_onboarding_submit")
                    ) {
                        Text("FINISH & ACTIVATE")
                    }
                }
            }
        }

        if (authState is AuthState.Error) {
            item {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

// --- MAIN SELLER CENTRAL PORTAL ---
@Composable
fun SellerDashboardScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayouts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReturns: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val seller by viewModel.currentSeller.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val payoutHistory by viewModel.getSellerPayoutHistoryFlow().collectAsStateWithLifecycle(emptyList())

    if (seller == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active Seller Portal session.")
        }
        return
    }

    // Filter data for this specific seller
    val sellerProducts = allProducts.filter { it.sellerId == seller!!.id }
    
    val sellerOrders = allOrders.filter { order ->
        val type = Types.newParameterizedType(List::class.java, OrderItem::class.java)
        val items = try {
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<OrderItem>>(type)
                .fromJson(order.itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        items.any { it.sellerId == seller!!.id }
    }

    val activeOrdersCount = sellerOrders.count { it.status in listOf("Pending", "Confirmed", "Packed", "Shipped", "Out for Delivery") }
    val returnsCount = sellerOrders.count { it.status in listOf("Return Requested", "Returned") }
    val lowStockCount = sellerProducts.count { it.stock in 1..5 }

    // Settlement analytics
    val totalDeliveredEarnings = payoutHistory.filter { it.status == "Settled" }.sumOf { it.netEarnings }
    val pendingSettlementEarnings = payoutHistory.filter { it.status == "Pending" }.sumOf { it.netEarnings }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(seller!!.businessName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Merchant ID: ${seller!!.id}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("KYC VERIFIED", color = Color(0xFF388E3C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Metric Card Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(title = "Total Products", value = sellerProducts.size.toString(), icon = Icons.Default.Inventory, modifier = Modifier.weight(1f), onClick = onNavigateToProducts)
                    MetricCard(title = "Active Orders", value = activeOrdersCount.toString(), icon = Icons.Default.LocalShipping, modifier = Modifier.weight(1f), onClick = onNavigateToOrders)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(title = "Completed Sales", value = "₹${totalDeliveredEarnings.toInt()}", icon = Icons.Default.CheckCircle, color = Color(0xFF388E3C), modifier = Modifier.weight(1f), onClick = onNavigateToPayouts)
                    MetricCard(title = "Pending Payout", value = "₹${pendingSettlementEarnings.toInt()}", icon = Icons.Default.AccountBalanceWallet, color = Color(0xFFE65100), modifier = Modifier.weight(1f), onClick = onNavigateToPayouts)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(title = "Return Requests", value = returnsCount.toString(), icon = Icons.Default.AssignmentReturn, modifier = Modifier.weight(1f), onClick = onNavigateToReturns)
                    MetricCard(title = "Low Stock Alerts", value = lowStockCount.toString(), icon = Icons.Default.Warning, color = if (lowStockCount > 0) Color.Red else Color.Gray, modifier = Modifier.weight(1f), onClick = onNavigateToProducts)
                }
            }
        }

        // Quick Operations Panel
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionIcon(label = "Add Product", icon = Icons.Default.AddBox, onClick = onNavigateToAddProduct)
                        QuickActionIcon(label = "Shipping", icon = Icons.Default.Settings, onClick = onNavigateToSettings)
                        QuickActionIcon(label = "KYC Profile", icon = Icons.Default.VerifiedUser, onClick = onNavigateToProfile)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = if (color == Color.Unspecified) Color.Unspecified else color)
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun QuickActionIcon(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// --- ADD / MANAGE PRODUCTS ---
@Composable
fun SellerAddProductScreen(
    viewModel: MarketplaceViewModel,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Fashion") }
    var subcategory by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var originalPriceStr by remember { mutableStateOf("") }
    var discountPercentStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Lists & Maps for dynamic values
    var highlightsStr by remember { mutableStateOf("") } // comma-separated
    var specKey1 by remember { mutableStateOf("") }
    var specValue1 by remember { mutableStateOf("") }
    var specKey2 by remember { mutableStateOf("") }
    var specValue2 by remember { mutableStateOf("") }

    var customShippingChargeStr by remember { mutableStateOf("49") }
    var returnPolicy by remember { mutableStateOf("7 Days Replacement") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val subcategories = when (selectedCategory) {
        "Fashion" -> listOf("Men’s Clothing", "Women’s Clothing", "Kids Wear", "Footwear", "Bags & Accessories")
        "Books" -> listOf("Fiction", "Non-Fiction", "Academic", "Competitive Exam Books", "Children’s Books")
        "Electronics" -> listOf("Smartphones", "Earbuds / Headphones", "Smartwatches", "Laptops", "Accessories", "Power Banks", "Speakers")
        else -> emptyList()
    }

    LaunchedEffect(selectedCategory) {
        subcategory = subcategories.firstOrNull() ?: ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Add New Product Catalog", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Category selection
                    Text("Category Placement", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Fashion", "Books", "Electronics").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (selectedCategory == cat) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat, color = if (selectedCategory == cat) Color.White else Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Subcategory", fontSize = 12.sp, color = Color.Gray)
                    var expandedSub by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = subcategory,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { expandedSub = true },
                            trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Open") }
                        )
                        DropdownMenu(expanded = expandedSub, onDismissRequest = { expandedSub = false }) {
                            subcategories.forEach { sub ->
                                DropdownMenuItem(text = { Text(sub) }, onClick = {
                                    subcategory = sub
                                    expandedSub = false
                                })
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Product Description", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Product Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand / Manufacturer") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Long Description") }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 5)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pricing & Inventory (INR ₹)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = originalPriceStr, onValueChange = { originalPriceStr = it }, label = { Text("Original Price (M.R.P)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = discountPercentStr, onValueChange = { discountPercentStr = it }, label = { Text("Discount Percentage (%)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                    // Live auto price deduction calculation
                    val oPrice = originalPriceStr.toDoubleOrNull() ?: 0.0
                    val disc = discountPercentStr.toDoubleOrNull() ?: 0.0
                    val finalSellingPrice = (oPrice * (1 - (disc / 100.0))).toInt()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Auto Calculated Selling Price: ₹$finalSellingPrice", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C), fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = stockStr, onValueChange = { stockStr = it }, label = { Text("Available Stock Count") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Shipping & Returns", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = customShippingChargeStr, onValueChange = { customShippingChargeStr = it }, label = { Text("Custom Courier Shipping Fee (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = returnPolicy, onValueChange = { returnPolicy = it }, label = { Text("Return Policy Notes") }, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Highlights & Technical Specs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = highlightsStr,
                        onValueChange = { highlightsStr = it },
                        label = { Text("Highlights (comma separated)") },
                        placeholder = { Text("Premium Cotton, Regular Fit, Durable") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = specKey1, onValueChange = { specKey1 = it }, label = { Text("Spec Key (e.g., Color)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = specValue1, onValueChange = { specValue1 = it }, label = { Text("Value (e.g., Blue)") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = specKey2, onValueChange = { specKey2 = it }, label = { Text("Spec Key") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = specValue2, onValueChange = { specValue2 = it }, label = { Text("Value") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val oPrice = originalPriceStr.toDoubleOrNull() ?: 0.0
                    val disc = discountPercentStr.toIntOrNull() ?: 0
                    val stockVal = stockStr.toIntOrNull() ?: 0
                    val finalPrice = oPrice * (1 - (disc / 100.0))
                    val customShipping = customShippingChargeStr.toDoubleOrNull() ?: 49.0

                    if (title.isNotEmpty() && brand.isNotEmpty() && oPrice > 0) {
                        isSubmitting = true
                        val highList = highlightsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val specsMap = mutableMapOf<String, String>()
                        if (specKey1.isNotEmpty() && specValue1.isNotEmpty()) specsMap[specKey1] = specValue1
                        if (specKey2.isNotEmpty() && specValue2.isNotEmpty()) specsMap[specKey2] = specValue2

                        viewModel.addNewProduct(
                            title = title,
                            category = selectedCategory,
                            subcategory = subcategory,
                            brand = brand,
                            price = finalPrice,
                            originalPrice = oPrice,
                            discountPercent = disc,
                            stock = stockVal,
                            description = description,
                            highlights = highList,
                            specs = specsMap,
                            shippingCharge = customShipping,
                            returnPolicy = returnPolicy
                        ) { isOk ->
                            isSubmitting = false
                            if (isOk) {
                                onSuccess()
                            } else {
                                errorMessage = "Failed to add product Catalog. Check inputs."
                            }
                        }
                    } else {
                        errorMessage = "Product Title, Brand, and M.R.P are mandatory."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_new_product_button"),
                shape = RoundedCornerShape(4.dp),
                enabled = !isSubmitting
            ) {
                Text("UPLOAD TO CATALOG", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SellerManageProductsScreen(
    viewModel: MarketplaceViewModel
) {
    val seller by viewModel.currentSeller.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val sellerProducts = allProducts.filter { it.sellerId == seller?.id }

    if (sellerProducts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No products uploaded in your catalog.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sellerProducts) { prod ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductImage(category = prod.category, subcategory = prod.subcategory, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("M.R.P: ₹${prod.originalPrice.toInt()} | Selling: ₹${prod.price.toInt()}", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (prod.stock <= 5) "Stock Low: ${prod.stock} left!" else "Stock: ${prod.stock} left",
                            color = if (prod.stock <= 5) Color.Red else Color(0xFF388E3C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = { viewModel.removeProduct(prod.id) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

// --- ORDERS RECEIVED MANAGER ---
@Composable
fun SellerOrdersScreen(
    viewModel: MarketplaceViewModel
) {
    val seller by viewModel.currentSeller.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

    val sellerOrders = allOrders.filter { order ->
        val type = Types.newParameterizedType(List::class.java, OrderItem::class.java)
        val items = try {
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<OrderItem>>(type)
                .fromJson(order.itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        items.any { it.sellerId == seller?.id }
    }

    if (sellerOrders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer orders received yet.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sellerOrders) { order ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = order.status,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    val type = Types.newParameterizedType(List::class.java, OrderItem::class.java)
                    val items = try {
                        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<OrderItem>>(type)
                            .fromJson(order.itemsJson) ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // Render only seller-specific items
                    val sellerItems = items.filter { it.sellerId == seller?.id }
                    sellerItems.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            ProductImage(category = item.category, subcategory = item.category, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Qty: ${item.quantity} | Total: ₹${(item.price * item.quantity).toInt()}", fontSize = 11.sp)
                            }
                        }

                        // Settlement preview calculation
                        var settlementBreakdown by remember { mutableStateOf<SettlementBreakdown?>(null) }
                        LaunchedEffect(order, item) {
                            val rep = MarketplaceRepository(viewModel.getApplication())
                            settlementBreakdown = rep.calculateSettlement(order, item)
                        }

                        if (settlementBreakdown != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            SettlementTable(settlement = settlementBreakdown!!)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Deliver to: ${order.buyerName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val addr = try {
                        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(Address::class.java)
                            .fromJson(order.shippingAddressJson)
                    } catch (e: Exception) {
                        null
                    }
                    if (addr != null) {
                        Text(addr.toDisplayString(), fontSize = 11.sp, color = Color.DarkGray)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Order progression flow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        when (order.status) {
                            "Pending" -> {
                                Button(
                                    onClick = { viewModel.updateOrderStatusBySeller(order.id, "Confirmed", "") },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("CONFIRM ORDER", fontSize = 11.sp)
                                }
                            }
                            "Confirmed" -> {
                                Button(
                                    onClick = { viewModel.updateOrderStatusBySeller(order.id, "Packed", "") },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("PACK ORDER", fontSize = 11.sp)
                                }
                            }
                            "Packed" -> {
                                var trackingNo by remember { mutableStateOf("") }
                                var showTrackDialog by remember { mutableStateOf(false) }

                                Button(
                                    onClick = { showTrackDialog = true },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("SHIP COURIER", fontSize = 11.sp)
                                }

                                if (showTrackDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showTrackDialog = false },
                                        title = { Text("Courier Shipment details") },
                                        text = {
                                            Column {
                                                Text("Add mock tracking/AWB number for Indian Post/Delhivery:")
                                                Spacer(modifier = Modifier.height(6.dp))
                                                OutlinedTextField(value = trackingNo, onValueChange = { trackingNo = it }, label = { Text("AWB Tracking No") })
                                            }
                                        },
                                        confirmButton = {
                                            Button(onClick = {
                                                if (trackingNo.isNotEmpty()) {
                                                    viewModel.updateOrderStatusBySeller(order.id, "Shipped", trackingNo)
                                                    showTrackDialog = false
                                                }
                                            }) {
                                                Text("Ship Now")
                                            }
                                        }
                                    )
                                }
                            }
                            "Shipped" -> {
                                Button(
                                    onClick = { viewModel.updateOrderStatusBySeller(order.id, "Out for Delivery", "") },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("OUT FOR DELIVERY", fontSize = 11.sp)
                                }
                            }
                            "Out for Delivery" -> {
                                Button(
                                    onClick = { viewModel.updateOrderStatusBySeller(order.id, "Delivered", "") },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("MARK DELIVERED", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PAYOUTS & EARNINGS REPORTS ---
@Composable
fun SellerPayoutsScreen(
    viewModel: MarketplaceViewModel
) {
    val payoutHistory by viewModel.getSellerPayoutHistoryFlow().collectAsStateWithLifecycle(emptyList())

    if (payoutHistory.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No payout records found yet.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Ledger Settlement Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(payoutHistory) { item ->
            SettlementTable(settlement = item)
        }
    }
}

// --- RETURNS / REFUND REQUESTS ---
@Composable
fun SellerReturnsScreen(
    viewModel: MarketplaceViewModel
) {
    val seller by viewModel.currentSeller.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()

    val returnOrders = allOrders.filter { order ->
        val type = Types.newParameterizedType(List::class.java, OrderItem::class.java)
        val items = try {
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<OrderItem>>(type)
                .fromJson(order.itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        items.any { it.sellerId == seller?.id } && order.status in listOf("Return Requested", "Returned")
    }

    if (returnOrders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer returns requested.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(returnOrders) { order ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(order.status, color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Reason for return: \"${order.returnReason}\"", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Warning: Approving return will deduct product payment from ledger and initiate complete refund.", fontSize = 11.sp, color = Color.Red)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (order.status == "Return Requested") {
                            Button(
                                onClick = { viewModel.processReturnApprovalByAdmin(order.id, approved = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("APPROVE RETURN", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.processReturnApprovalByAdmin(order.id, approved = false) },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("REJECT", fontSize = 11.sp)
                            }
                        } else {
                            Text("Refund Processed: ${order.refundStatus}", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- SHIPPING SETTINGS ---
@Composable
fun SellerShippingSettingsScreen(
    viewModel: MarketplaceViewModel
) {
    val seller by viewModel.currentSeller.collectAsStateWithLifecycle()
    var defaultChargeStr by remember { mutableStateOf("") }
    var freeThresholdStr by remember { mutableStateOf("") }
    var handlingDaysStr by remember { mutableStateOf("") }

    LaunchedEffect(seller) {
        if (seller != null) {
            defaultChargeStr = seller!!.defaultShippingCharge.toInt().toString()
            freeThresholdStr = seller!!.freeShippingThreshold.toInt().toString()
            handlingDaysStr = seller!!.handlingTimeDays.toString()
        }
    }

    if (seller == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Courier Shipping Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = defaultChargeStr, onValueChange = { defaultChargeStr = it }, label = { Text("Default Shipping Charge Collected (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = freeThresholdStr, onValueChange = { freeThresholdStr = it }, label = { Text("Free Shipping Threshold (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = handlingDaysStr, onValueChange = { handlingDaysStr = it }, label = { Text("Order Handling Dispatch Time (Days)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val charge = defaultChargeStr.toDoubleOrNull() ?: 49.0
                        val thresh = freeThresholdStr.toDoubleOrNull() ?: 999.0
                        val days = handlingDaysStr.toIntOrNull() ?: 2
                        viewModel.updateSellerShippingSettings(charge, thresh, days)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("SAVE SHIPPING PARAMS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SELLER PROFILE KYC DETAILS & LATE GST ENTRY ---
@Composable
fun SellerProfileScreen(
    viewModel: MarketplaceViewModel,
    onLogout: () -> Unit
) {
    val seller by viewModel.currentSeller.collectAsStateWithLifecycle()
    var showGstForm by remember { mutableStateOf(false) }
    var gstInput by remember { mutableStateOf("") }

    if (seller == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Store: ${seller!!.businessName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Owner: ${seller!!.name}", fontSize = 12.sp, color = Color.Gray)
                    Text("Email: ${seller!!.email} | Mobile: ${seller!!.mobile}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("KYC Verification Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    KycBadgeItem(label = "PAN CARD Status", verified = true, details = seller!!.pan)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    val hasGst = seller!!.gst.isNotEmpty()
                    KycBadgeItem(label = "GSTIN Status", verified = hasGst, details = if (hasGst) seller!!.gst else "Not Provided (Optional for Registration)")

                    if (!hasGst && !showGstForm) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showGstForm = true },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Add GST details now", fontSize = 11.sp)
                        }
                    }

                    if (showGstForm) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = gstInput,
                                onValueChange = { gstInput = it },
                                label = { Text("GSTIN (15 chars)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (gstInput.length == 15) {
                                        viewModel.updateSellerGst(gstInput)
                                        showGstForm = false
                                        gstInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Save")
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    KycBadgeItem(label = "Bank Details Verified", verified = true, details = "Acc: ******${seller!!.bankAccount.takeLast(4)}")
                }
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("LOG OUT FROM SELLER PORTAL", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun KycBadgeItem(label: String, verified: Boolean, details: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = details, fontSize = 11.sp, color = Color.Gray)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (verified) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (verified) "VERIFIED" else "PENDING",
                color = if (verified) Color(0xFF388E3C) else Color.Red,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
