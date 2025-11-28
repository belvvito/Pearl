package com.beutystore.pearl.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Order(
    val id: String,
    @SerializedName("order_number")
    val orderNumber: String? = null,
    val date: Long, // timestamp
    val status: String, // статус как строка из API
    val items: List<OrderItem>,
    @SerializedName("total_price")
    val totalPrice: Int,
    @SerializedName("delivery_address")
    val deliveryAddress: String? = null,
    @SerializedName("payment_method")
    val paymentMethod: String? = null,
    @SerializedName("bonus_points_used")
    val bonusPointsUsed: Int = 0,
    @SerializedName("bonus_points_earned")
    val bonusPointsEarned: Int = 0
) {
    val itemCount: Int
        get() = items.sumOf { it.quantity }
    
    // Получает номер заказа или генерирует его из ID
    val displayOrderNumber: String
        get() = orderNumber ?: "ORD-${id.takeLast(8).uppercase()}"
    
    // Преобразует строковый статус в enum OrderStatus
    val statusEnum: OrderStatus
        get() = when (status.lowercase()) {
            "pending" -> OrderStatus.PENDING
            "confirmed" -> OrderStatus.CONFIRMED
            "processing" -> OrderStatus.PROCESSING
            "shipped" -> OrderStatus.SHIPPED
            "delivered" -> OrderStatus.DELIVERED
            "cancelled" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING // значение по умолчанию
        }
}

data class OrderItem(
    val product: Product,
    val quantity: Int,
    @SerializedName("unit_price")
    val price: Int // цена на момент заказа
)

enum class OrderStatus(val displayName: String) {
    PENDING("Ожидает подтверждения"),
    CONFIRMED("Подтвержден"),
    PROCESSING("В обработке"),
    SHIPPED("Отправлен"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен")
}

