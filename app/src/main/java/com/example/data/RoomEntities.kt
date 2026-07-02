package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val mobile: String,
    val passwordHash: String,
    val role: String, // "buyer", "seller", "admin"
    val addressesJson: String = "[]", // List of saved Address objects
    val wishlistJson: String = "[]", // List of Int (Product IDs)
    val cartJson: String = "[]" // List of CartItem objects (productId, quantity, isSelected)
)

@Entity(tableName = "sellers")
data class SellerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val businessName: String,
    val email: String,
    val mobile: String,
    val passwordHash: String,
    val pan: String,
    val gst: String = "", // Optional GST, can be added later
    val address: String,
    val bankHolderName: String,
    val bankAccount: String,
    val ifsc: String,
    val kycStatus: String = "PENDING", // "PENDING", "VERIFIED", "REJECTED"
    val defaultShippingCharge: Double = 49.0,
    val freeShippingThreshold: Double = 999.0,
    val handlingTimeDays: Int = 2
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sellerId: String,
    val sellerName: String,
    val title: String,
    val slug: String,
    val category: String, // "Fashion", "Books", "Electronics"
    val subcategory: String,
    val brand: String,
    val price: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val rating: Double = 4.0,
    val reviewCount: Int = 12,
    val stock: Int,
    val imagesJson: String = "[]", // List of Strings (URLs or local descriptions)
    val highlightsJson: String = "[]", // List of Strings
    val specsJson: String = "{}", // Map of String to String
    val description: String,
    val returnPolicy: String = "7 Days Replacement",
    val shippingTimeDays: Int = 3,
    val shippingCharge: Double = 49.0,
    val isFreeShippingEligible: Boolean = false,
    val deliveryEstimate: String = "Delivery in 3-4 Days"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val buyerId: String,
    val buyerName: String,
    val buyerMobile: String,
    val itemsJson: String, // List of OrderItem objects (productId, title, category, price, quantity, sellerId, sellerName, shippingCharge)
    val shippingAddressJson: String, // Address object
    val paymentMethod: String, // "COD", "UPI", "CARD", "NET_BANKING", "WALLET"
    val subtotal: Double,
    val shippingCharge: Double,
    val codCharge: Double = 0.0,
    val platformFee: Double = 9.0,
    val discount: Double = 0.0,
    val totalPayable: Double,
    val orderDate: Long = System.currentTimeMillis(),
    val status: String = "Pending", // "Pending", "Confirmed", "Packed", "Shipped", "Out for Delivery", "Delivered", "Cancelled", "Return Requested", "Returned", "Refunded"
    val trackingNumber: String = "",
    val returnReason: String = "",
    val refundStatus: String = "None" // "None", "Refund Initiated", "Refund Processed", "Refund Completed"
)

@Entity(tableName = "configs")
data class ConfigEntity(
    @PrimaryKey val id: String = "marketplace_config",
    val fashionCommission: Double = 12.0, // %
    val booksCommission: Double = 8.0, // %
    val electronicsCommission: Double = 10.0, // %
    val defaultPlatformFee: Double = 9.0, // ₹
    val defaultCodCharge: Double = 29.0, // ₹
    val prepaidPaymentGatewayFeePercent: Double = 2.0, // %
    val freeShippingThreshold: Double = 999.0, // ₹
    val fashionShippingFee: Double = 49.0, // ₹
    val booksShippingFee: Double = 39.0, // ₹
    val electronicsShippingFee: Double = 79.0 // ₹
)
