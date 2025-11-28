package com.beutystore.pearl.data.model

import com.google.gson.annotations.SerializedName

/**
 * Модель отзыва о товаре.
 * 
 * Соответствует структуре данных, возвращаемой Django API.
 */
data class Review(
    val id: String,
    @SerializedName("productId")
    val productId: Int,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("userAvatar")
    val userAvatar: String? = null,
    val rating: Int, // 1-5
    val comment: String,
    val date: String,
    @SerializedName("helpfulCount")
    val helpfulCount: Int = 0,
    @SerializedName("isVerifiedPurchase")
    val isVerifiedPurchase: Boolean = false,
    @SerializedName("is_approved")
    val isApproved: Boolean? = null,  // Только для админов
    @SerializedName("ratingAttributes")
    val ratingAttributes: Map<String, Any>? = null,  // Атрибуты оценки (критерии)
    @SerializedName("ratingExplanation")
    val ratingExplanation: String? = null  // Объяснение оценки
)

