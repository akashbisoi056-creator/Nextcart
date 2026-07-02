package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Address(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String = "",
    val addressLine: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val type: String = "Home" // "Home", "Work"
) {
    fun toDisplayString(): String {
        return "$addressLine, $city, $state - $pincode. Phone: $phone"
    }
}

@JsonClass(generateAdapter = true)
data class CartItem(
    val productId: Int,
    val quantity: Int,
    val isSelected: Boolean = true
)

@JsonClass(generateAdapter = true)
data class OrderItem(
    val productId: Int,
    val title: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val sellerId: String,
    val sellerName: String,
    val shippingCharge: Double
)
