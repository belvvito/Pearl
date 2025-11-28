// ui/viewmodel/CartViewModel.kt
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

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<Map<Int, CartItem>>(emptyMap())
    val cartItems: StateFlow<Map<Int, CartItem>> = _cartItems.asStateFlow()

    // Список товаров для отображения
    val cartItemsList: StateFlow<List<CartItem>> = _cartItems.map { it.values.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Вычисляемые свойства
    val totalPrice: StateFlow<Int> = _cartItems.map { items ->
        items.values.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartItemCount: StateFlow<Int> = _cartItems.map { items ->
        items.values.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun getProductQuantity(productId: Int): Int {
        return _cartItems.value[productId]?.quantity ?: 0
    }

    fun isProductInCart(productId: Int): Boolean {
        return _cartItems.value.containsKey(productId)
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val currentItems = _cartItems.value.toMutableMap()
            val existingItem = currentItems[product.id]

            if (existingItem != null) {
                currentItems[product.id] = existingItem.copy(quantity = existingItem.quantity + quantity)
            } else {
                currentItems[product.id] = CartItem(product, quantity)
            }

            _cartItems.value = currentItems
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            val currentItems = _cartItems.value.toMutableMap()
            val existingItem = currentItems[productId]

            if (existingItem != null) {
                if (existingItem.quantity > 1) {
                    currentItems[productId] = existingItem.copy(quantity = existingItem.quantity - 1)
                } else {
                    currentItems.remove(productId)
                }

                _cartItems.value = currentItems
            }
        }
    }

    fun removeAllFromCart(productId: Int) {
        viewModelScope.launch {
            val currentItems = _cartItems.value.toMutableMap()
            currentItems.remove(productId)
            _cartItems.value = currentItems
        }
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            if (quantity <= 0) {
                removeAllFromCart(productId)
            } else {
                val currentItems = _cartItems.value.toMutableMap()
                val existingItem = currentItems[productId]
                if (existingItem != null) {
                    currentItems[productId] = existingItem.copy(quantity = quantity)
                    _cartItems.value = currentItems
                }
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _cartItems.value = emptyMap()
        }
    }

    data class CartItem(
        val product: Product,
        val quantity: Int
    )
}