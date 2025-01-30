package com.example.swipeassignment.data.model
data class ProductResponse(
    val message: String,
    val product_details: Product,
    val product_id: Int,
    val success: Boolean
)
