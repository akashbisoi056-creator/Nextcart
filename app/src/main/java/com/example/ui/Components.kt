package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.data.OrderItem
import com.example.data.ProductEntity
import com.example.data.SettlementBreakdown

// --- Announcement Bar ---
@Composable
fun AnnouncementBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(vertical = 4.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "Free Shipping",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Next Cart Mega Sale: FREE SHIPPING above ₹999 across India!",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- Product Fallback Image with Beautiful Gradients ---
@Composable
fun ProductImage(category: String, subcategory: String, modifier: Modifier = Modifier) {
    val (gradient, icon) = when (category) {
        "Fashion" -> Pair(
            Brush.verticalGradient(listOf(Color(0xFFFF8A80), Color(0xFFFF5252))),
            Icons.Default.Checkroom
        )
        "Books" -> Pair(
            Brush.verticalGradient(listOf(Color(0xFF80DEEA), Color(0xFF00ACC1))),
            Icons.Default.Book
        )
        else -> Pair(
            Brush.verticalGradient(listOf(Color(0xFFCE93D8), Color(0xFF8E24AA))),
            Icons.Default.PhoneAndroid
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = subcategory,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subcategory,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Dynamic Rating Star Badge ---
@Composable
fun RatingBadge(rating: Double, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF388E3C))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = String.format(Locale.getDefault(), "%.1f", rating),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating",
            tint = Color.White,
            modifier = Modifier.size(10.dp)
        )
    }
    Spacer(modifier = Modifier.width(6.dp))
    Text(
        text = "($count)",
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        fontSize = 11.sp
    )
}

// --- Product Card ---
@Composable
fun ProductCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onWishlistToggle: () -> Unit,
    isWishlisted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                ProductImage(
                    category = product.category,
                    subcategory = product.subcategory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )

                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = product.brand,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingBadge(rating = product.rating, count = product.reviewCount)
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "₹${product.price.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "₹${product.originalPrice.toInt()}",
                            textDecoration = TextDecoration.LineThrough,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "${product.discountPercent}% OFF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF388E3C)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Seller: ${product.sellerName}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Wishlist Heart Button
            IconButton(
                onClick = onWishlistToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) Color.Red else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --- Order Status Tracking Timeline Component ---
@Composable
fun TrackingTimeline(currentStatus: String) {
    val statuses = listOf("Pending", "Confirmed", "Packed", "Shipped", "Out for Delivery", "Delivered")
    val currentIndex = statuses.indexOf(currentStatus).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Tracking Timeline",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            statuses.forEachIndexed { index, status ->
                val isActive = index <= currentIndex
                val isLast = index == statuses.size - 1

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    CircleShape
                                )
                        )
                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(30.dp)
                                    .background(
                                        if (index < currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = status,
                            fontWeight = if (status == currentStatus) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                        if (status == currentStatus) {
                            val description = when (status) {
                                "Pending" -> "Waiting for vendor confirmation."
                                "Confirmed" -> "Vendor accepted. Preparing package."
                                "Packed" -> "Order packed. Ready for courier pick-up."
                                "Shipped" -> "In transit across logistics partners."
                                "Out for Delivery" -> "Courier executive is near your location."
                                "Delivered" -> "Delivered successfully. Thank you!"
                                else -> ""
                            }
                            Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// --- Settlement Breakdown Grid Component ---
@Composable
fun SettlementTable(settlement: SettlementBreakdown) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Seller Payout Split",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = settlement.status.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (settlement.status == "Settled") Color(0xFF388E3C) else Color(0xFFE65100),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (settlement.status == "Settled") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            val rows = listOf(
                Pair("Selling Price x Qty (${settlement.quantity})", "₹${settlement.grossValue.toInt()}"),
                Pair("Shipping Collected from Buyer", "+ ₹${settlement.shippingCollected.toInt()}"),
                Pair("COD Charges (if any)", "+ ₹${settlement.codCollected.toInt()}"),
                Pair("Marketplace Commission (${settlement.commissionRate}%)", "- ₹${settlement.commissionDeducted.toInt()}"),
                Pair("Platform Handling Fee", "- ₹${settlement.platformFee.toInt()}"),
                Pair("Payment Gateway Fee", "- ₹${settlement.pgFee.toInt()}"),
                Pair("Returns / Reverse Ship Penalty", if (settlement.returnAdjustment < 0) "- ₹${(-settlement.returnAdjustment).toInt()}" else "₹0")
            )

            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 12.sp, color = Color.Gray)
                    Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Net Seller Earnings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "₹${settlement.netEarnings.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (settlement.netEarnings >= 0) Color(0xFF388E3C) else Color.Red
                )
            }

            if (settlement.status == "Settled") {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Settled on: ${settlement.payoutDate} via Bank Transfer",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// --- Footer Component ---
@Composable
fun MarketplaceFooter(onNavigate: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "NEXT CART",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = "Smart Shopping, Better Prices",
            color = Color.LightGray,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "SHOPPING", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Fashion", color = Color.LightGray, fontSize = 11.sp)
                Text(text = "Books", color = Color.LightGray, fontSize = 11.sp)
                Text(text = "Electronics", color = Color.LightGray, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "SELLER PORTAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Sell Products", color = Color.LightGray, fontSize = 11.sp)
                Text(text = "Payout Cycles", color = Color.LightGray, fontSize = 11.sp)
                Text(text = "PAN Onboarding", color = Color.LightGray, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color.Gray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SupportAgent,
                contentDescription = "Support",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Next Cart Hotline: +91 6371991098",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "© 2026 Next Cart Private Limited India. All Rights Reserved.",
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}
