package com.beutystore.pearl.data.model

import com.google.gson.annotations.SerializedName

data class BonusCard(
    @SerializedName("card_number")
    val cardNumber: String,
    @SerializedName("bonus_points")
    val bonusPoints: Int,
    val level: String? = null,
    @SerializedName("total_spent")
    val totalSpent: Int = 0,
    @SerializedName("total_earned")
    val totalEarned: Int = 0
) {
    val cardLevel: CardLevel
        get() = when (level) {
            "VIP" -> CardLevel.VIP
            "PLATINUM" -> CardLevel.PLATINUM
            "GOLD" -> CardLevel.GOLD
            "SILVER" -> CardLevel.SILVER
            else -> CardLevel.BRONZE
        }
    
    val progressToNextLevel: Float
        get() {
            val nextLevel = getNextLevel()
            return if (nextLevel != null && nextLevel.minPoints > 0) {
                (bonusPoints.toFloat() / nextLevel.minPoints).coerceIn(0f, 1f)
            } else {
                1f
            }
        }
    
    private fun getNextLevel(): CardLevel? {
        return when (cardLevel) {
            CardLevel.BRONZE -> CardLevel.SILVER
            CardLevel.SILVER -> CardLevel.GOLD
            CardLevel.GOLD -> CardLevel.PLATINUM
            CardLevel.PLATINUM -> CardLevel.VIP
            CardLevel.VIP -> null
        }
    }
}

enum class CardLevel(val displayName: String, val minPoints: Int, val color: String) {
    BRONZE("Бронза", 0, "#CD7F32"),
    SILVER("Серебро", 500, "#C0C0C0"),
    GOLD("Золото", 2000, "#FFD700"),
    PLATINUM("Платина", 5000, "#E5E4E2"),
    VIP("VIP", 10000, "#8B00FF")
}

