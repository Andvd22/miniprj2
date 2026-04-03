package com.example.miniprj2.util


import java.text.NumberFormat
import java.util.Locale

object AppFormatters {
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

    fun formatCurrency(value: Double): String = currencyFormatter.format(value)

    fun toBadge(text: String): String = text.trim().firstOrNull()?.uppercase() ?: "F"

    fun formatUnitPrice(price: Double, unit: String): String = "${formatCurrency(price)} / $unit"

    fun formatOrderCode(orderId: Long): String = "Đơn hàng #${orderId.toString().padStart(4, '0')}"

    fun formatInvoiceCode(orderId: Long): String = "Hóa đơn #${orderId.toString().padStart(4, '0')}"
}