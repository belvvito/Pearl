// ui/viewmodel/BonusCardViewModel.kt
package com.beutystore.pearl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.beutystore.pearl.data.model.BonusCard
import com.beutystore.pearl.data.model.CardLevel

class BonusCardViewModel : ViewModel() {

    private val _bonusCard = MutableStateFlow<BonusCard?>(null)
    val bonusCard: StateFlow<BonusCard?> = _bonusCard.asStateFlow()

    init {
        // Инициализация с демо-данными (будет заменено данными пользователя при загрузке)
        loadDemoBonusCard()
    }

    private fun loadDemoBonusCard() {
        viewModelScope.launch {
            val demoCard = BonusCard(
                cardNumber = "1234567890123456",
                bonusPoints = 100,
                level = "BRONZE",
                totalSpent = 0,
                totalEarned = 100
            )
            _bonusCard.value = demoCard
        }
    }

    // Загружает бонусную карту из данных пользователя
    fun loadBonusCardFromUser(userBonusCard: BonusCard?) {
        viewModelScope.launch {
            if (userBonusCard != null) {
                _bonusCard.value = userBonusCard
            } else {
                // Если карты нет, создаем карту с начальными значениями (100 баллов, BRONZE)
                _bonusCard.value = BonusCard(
                    cardNumber = "0000000000000000",
                    bonusPoints = 100,
                    level = "BRONZE",
                    totalSpent = 0,
                    totalEarned = 100
                )
            }
        }
    }

    fun getCurrentLevel(): CardLevel {
        val points = _bonusCard.value?.bonusPoints ?: 0
        return when {
            points >= CardLevel.VIP.minPoints -> CardLevel.VIP
            points >= CardLevel.PLATINUM.minPoints -> CardLevel.PLATINUM
            points >= CardLevel.GOLD.minPoints -> CardLevel.GOLD
            points >= CardLevel.SILVER.minPoints -> CardLevel.SILVER
            else -> CardLevel.BRONZE
        }
    }

    fun getNextLevel(): CardLevel? {
        val currentLevel = getCurrentLevel()
        return when (currentLevel) {
            CardLevel.BRONZE -> CardLevel.SILVER
            CardLevel.SILVER -> CardLevel.GOLD
            CardLevel.GOLD -> CardLevel.PLATINUM
            CardLevel.PLATINUM -> CardLevel.VIP
            CardLevel.VIP -> null
        }
    }

    fun getPointsToNextLevel(): Int {
        val nextLevel = getNextLevel() ?: return 0
        val currentPoints = _bonusCard.value?.bonusPoints ?: 0
        return (nextLevel.minPoints - currentPoints).coerceAtLeast(0)
    }

    fun addBonusPoints(points: Int) {
        viewModelScope.launch {
            val currentCard = _bonusCard.value
            if (currentCard != null) {
                val newPoints = currentCard.bonusPoints + points
                val newLevel = calculateLevel(newPoints)
                
                _bonusCard.value = currentCard.copy(
                    bonusPoints = newPoints,
                    level = newLevel.name,
                    totalEarned = currentCard.totalEarned + points
                )
            }
        }
    }

    fun spendBonusPoints(points: Int): Boolean {
        viewModelScope.launch {
            val currentCard = _bonusCard.value
            if (currentCard != null && currentCard.bonusPoints >= points) {
                val newPoints = currentCard.bonusPoints - points
                val newLevel = calculateLevel(newPoints)
                
                _bonusCard.value = currentCard.copy(
                    bonusPoints = newPoints,
                    level = newLevel.name
                )
            }
        }
        return _bonusCard.value?.bonusPoints ?: 0 >= points
    }

    private fun calculateLevel(points: Int): CardLevel {
        return when {
            points >= CardLevel.VIP.minPoints -> CardLevel.VIP
            points >= CardLevel.PLATINUM.minPoints -> CardLevel.PLATINUM
            points >= CardLevel.GOLD.minPoints -> CardLevel.GOLD
            points >= CardLevel.SILVER.minPoints -> CardLevel.SILVER
            else -> CardLevel.BRONZE
        }
    }

    private fun getNextLevelForPoints(points: Int): CardLevel? {
        return when {
            points < CardLevel.SILVER.minPoints -> CardLevel.SILVER
            points < CardLevel.GOLD.minPoints -> CardLevel.GOLD
            points < CardLevel.PLATINUM.minPoints -> CardLevel.PLATINUM
            points < CardLevel.VIP.minPoints -> CardLevel.VIP
            else -> null
        }
    }
}

