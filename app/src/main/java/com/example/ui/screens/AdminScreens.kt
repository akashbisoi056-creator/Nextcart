package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.*
import com.example.viewmodel.MarketplaceViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- ADMIN MAIN PORTAL & METRICS ---
@Composable
fun AdminDashboardScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToSellers: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCommissions: () -> Unit
) {
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val cfg by viewModel.config.collectAsStateWithLifecycle()

    // Calculations
    val totalGmv = orders.sumOf { it.totalPayable }
    val totalOrdersCount = orders.size
    val totalSellersCount = sellers.size
    val totalBuyersCount = users.count { it.role == "buyer" }

    // Platform commissions earnings
    var commissionsEarned = 0.0
    orders.forEach { order ->
        val type = Types.newParameterizedType(List::class.java, OrderItem::class.java)
        val items = try {
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<OrderItem>>(type)
                .fromJson(order.itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        items.forEach { item ->
            val commRate = when (item.category) {
                "Fashion" -> cfg?.fashionCommission ?: 12.0
                "Books" -> cfg?.booksCommission ?: 8.0
                "Electronics" -> cfg?.electronicsCommission ?: 10.0
                else -> 10.0
            }
            commissionsEarned += (item.price * item.quantity) * (commRate / 100.0)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color.White, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Marketplace Control Center", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text("Admin Dashboard & Platform Policies", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(title = "Total GMV Sales", value = "₹${totalGmv.toInt()}", icon = Icons.Default.TrendingUp, color = Color(0xFF388E3C), modifier = Modifier.weight(1f))
                    MetricCard(title = "Platform Earnings", value = "₹${commissionsEarned.toInt()}", icon = Icons.Default.Payments, color = Color(0xFFE65100), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(title = "Registered Sellers", value = totalSellersCount.toString(), icon = Icons.Default.Storefront, modifier = Modifier.weight(1f), onClick = onNavigateToSellers)
                    MetricCard(title = "Total Customers", value = totalBuyersCount.toString(), icon = Icons.Default.People, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(title = "Total Orders", value = totalOrdersCount.toString(), icon = Icons.Default.Receipt, modifier = Modifier.weight(1f), onClick = onNavigateToOrders)
                    MetricCard(title = "Active Products", value = products.size.toString(), icon = Icons.Default.GridOn, modifier = Modifier.weight(1f), onClick = onNavigateToProducts)
                }
            }
        }

        // Admin Management Quick Links
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("System Parameters Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionIcon(label = "Commissions", icon = Icons.Default.Percent, onClick = onNavigateToCommissions)
                        QuickActionIcon(label = "Manage Sellers", icon = Icons.Default.Group, onClick = onNavigateToSellers)
                        QuickActionIcon(label = "All Orders", icon = Icons.Default.ListAlt, onClick = onNavigateToOrders)
                    }
                }
            }
        }
    }
}

// --- SELLER KYC AUDITOR ---
@Composable
fun AdminSellersScreen(
    viewModel: MarketplaceViewModel
) {
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()

    if (sellers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No sellers registered yet.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Seller KYC Approvals & Audit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(sellers) { seller ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(seller.businessName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("ID: ${seller.id} | Email: ${seller.email}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (seller.kycStatus == "VERIFIED") Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = seller.kycStatus,
                                color = if (seller.kycStatus == "VERIFIED") Color(0xFF388E3C) else Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Tax Verification", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("PAN Card: ${seller.pan}", fontSize = 11.sp)
                    Text("GSTIN: ${if (seller.gst.isEmpty()) "Not Provided (Optional)" else seller.gst}", fontSize = 11.sp)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Settlement Bank Account", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Holder: ${seller.bankHolderName}", fontSize = 11.sp)
                    Text("Account No: ${seller.bankAccount}", fontSize = 11.sp)
                    Text("IFSC Code: ${seller.ifsc}", fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pickup Location", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(seller.address, fontSize = 11.sp, color = Color.DarkGray)

                    if (seller.kycStatus == "PENDING") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    val verified = seller.copy(kycStatus = "VERIFIED")
                                    viewModel.updateSellerProfile(verified) // Wait, updateSeller functions
                                },
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("APPROVE KYC", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    val rejected = seller.copy(kycStatus = "REJECTED")
                                    viewModel.updateSellerProfile(rejected)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("REJECT", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- MASTER CATALOG AUDITOR ---
@Composable
fun AdminProductsScreen(
    viewModel: MarketplaceViewModel
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()

    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No products exist in catalog.")
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
        item {
            Text("Master Product Audit Catalog", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(products) { prod ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductImage(category = prod.category, subcategory = prod.subcategory, modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Category: ${prod.category} > ${prod.subcategory}", fontSize = 11.sp, color = Color.Gray)
                        Text("Seller: ${prod.sellerName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = { viewModel.removeProduct(prod.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("TAKEDOWN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- MASTER ORDERS OVERSIGHT ---
@Composable
fun AdminOrdersScreen(
    viewModel: MarketplaceViewModel
) {
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transactions registered on platform.")
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
        item {
            Text("Global Transactions Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(orders) { order ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ID: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(order.status, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Customer: ${order.buyerName} (${order.buyerMobile})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("Payment: ${order.paymentMethod} | Total Paid: ₹${order.totalPayable.toInt()}", fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var expandedOverride by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { expandedOverride = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("OVERRIDE STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(expanded = expandedOverride, onDismissRequest = { expandedOverride = false }) {
                            listOf("Pending", "Confirmed", "Packed", "Shipped", "Out for Delivery", "Delivered", "Cancelled").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        viewModel.updateOrderStatusBySeller(order.id, opt, "")
                                        expandedOverride = false
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

// --- COMMISSION & COMMODITIES SETTINGS CONTROL ---
@Composable
fun AdminCommissionsScreen(
    viewModel: MarketplaceViewModel
) {
    val cfg by viewModel.config.collectAsStateWithLifecycle()

    var fashionCommStr by remember { mutableStateOf("") }
    var booksCommStr by remember { mutableStateOf("") }
    var elecCommStr by remember { mutableStateOf("") }
    var platformFeeStr by remember { mutableStateOf("") }
    var codFeeStr by remember { mutableStateOf("") }

    LaunchedEffect(cfg) {
        if (cfg != null) {
            fashionCommStr = cfg!!.fashionCommission.toString()
            booksCommStr = cfg!!.booksCommission.toString()
            elecCommStr = cfg!!.electronicsCommission.toString()
            platformFeeStr = cfg!!.defaultPlatformFee.toString()
            codFeeStr = cfg!!.defaultCodCharge.toString()
        }
    }

    if (cfg == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F6))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Category Commission Setup (%)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = fashionCommStr, onValueChange = { fashionCommStr = it }, label = { Text("Fashion Marketplace Commission (%)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = booksCommStr, onValueChange = { booksCommStr = it }, label = { Text("Books Marketplace Commission (%)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = elecCommStr, onValueChange = { elecCommStr = it }, label = { Text("Electronics Marketplace Commission (%)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Platform Transaction Charges (₹)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = platformFeeStr, onValueChange = { platformFeeStr = it }, label = { Text("Secure Convenience/Platform Fee (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = codFeeStr, onValueChange = { codFeeStr = it }, label = { Text("Cash on Delivery COD Fee (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        }

        Button(
            onClick = {
                val f = fashionCommStr.toDoubleOrNull() ?: 12.0
                val b = booksCommStr.toDoubleOrNull() ?: 8.0
                val e = elecCommStr.toDoubleOrNull() ?: 10.0
                val p = platformFeeStr.toDoubleOrNull() ?: 9.0
                val c = codFeeStr.toDoubleOrNull() ?: 29.0

                viewModel.updateAdminGlobalCommissions(f, b, e, p, c)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_commissions_button"),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("APPLY SETTINGS SYSTEM-WIDE", fontWeight = FontWeight.Bold)
        }
    }
}
