package com.example.miniprj2.model

data class User(
    val id: Long,
    val username: String,
    val password: String,
    val fullName: String
)

data class Category(
    val id: Long,
    val name: String,
    val description: String,
    val accentColor: String,
    val productCount: Int
)

data class Product(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val name: String,
    val price: Double,
    val unit: String,
    val stock: Int,
    val description: String,
    val accentColor: String,
    val isToday: Boolean
)

data class OrderSummary(
    val orderId: Long,
    val status: String,
    val createdAt: String,
    val totalAmount: Double,
    val itemCount: Int
)

data class CartItem(
    val productId: Long,
    val name: String,
    val categoryName: String,
    val accentColor: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    val unit: String
)

data class InvoiceData(
    val orderId: Long,
    val customerName: String,
    val createdAt: String,
    val status: String,
    val totalAmount: Double,
    val items: List<CartItem>
)