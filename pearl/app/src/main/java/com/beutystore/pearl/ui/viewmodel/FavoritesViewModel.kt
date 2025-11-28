// ui/viewmodel/FavoritesViewModel.kt
package com.beutystore.pearl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.beutystore.pearl.data.model.Product

class FavoritesViewModel : ViewModel() {

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    // Список товаров для отображения (нужно будет передавать список всех товаров)
    private val _favoriteProducts = MutableStateFlow<List<Product>>(emptyList())
    val favoriteProducts: StateFlow<List<Product>> = _favoriteProducts.asStateFlow()

    // Количество избранных товаров
    val favoritesCount: StateFlow<Int> = _favorites.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun isFavorite(productId: Int): Boolean {
        return _favorites.value.contains(productId)
    }

    fun addToFavorites(product: Product) {
        viewModelScope.launch {
            val currentFavorites = _favorites.value.toMutableSet()
            if (!currentFavorites.contains(product.id)) {
                currentFavorites.add(product.id)
                _favorites.value = currentFavorites
                
                // Обновляем список товаров
                val currentProducts = _favoriteProducts.value.toMutableList()
                if (!currentProducts.any { it.id == product.id }) {
                    currentProducts.add(product)
                    _favoriteProducts.value = currentProducts
                }
            }
        }
    }

    fun removeFromFavorites(productId: Int) {
        viewModelScope.launch {
            val currentFavorites = _favorites.value.toMutableSet()
            if (currentFavorites.contains(productId)) {
                currentFavorites.remove(productId)
                _favorites.value = currentFavorites
                
                // Обновляем список товаров
                val currentProducts = _favoriteProducts.value.toMutableList()
                currentProducts.removeAll { it.id == productId }
                _favoriteProducts.value = currentProducts
            }
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            if (isFavorite(product.id)) {
                removeFromFavorites(product.id)
            } else {
                addToFavorites(product)
            }
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            _favorites.value = emptySet()
            _favoriteProducts.value = emptyList()
        }
    }
}

