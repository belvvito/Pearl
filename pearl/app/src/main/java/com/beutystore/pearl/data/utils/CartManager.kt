package com.beutystore.pearl.data.utils

import com.beutystore.pearl.data.model.Product

object CartManager {
    private val _cartItems = mutableListOf<Product>()
    val cartItems: List<Product> get() = _cartItems

    fun addToCart(product: Product) {
        val existingItem = _cartItems.find { it.id == product.id }
        if (existingItem != null) {
            // Создаем новый объект с увеличенным количеством
            val updatedItem = existingItem.copy(cartQuantity = existingItem.cartQuantity + 1)
            _cartItems.remove(existingItem)
            _cartItems.add(updatedItem)
        } else {
            val newProduct = product.copy(cartQuantity = 1)
            _cartItems.add(newProduct)
        }
    }

    fun removeFromCart(product: Product) {
        val existingItem = _cartItems.find { it.id == product.id }
        if (existingItem != null) {
            if (existingItem.cartQuantity > 1) {
                val updatedItem = existingItem.copy(cartQuantity = existingItem.cartQuantity - 1)
                _cartItems.remove(existingItem)
                _cartItems.add(updatedItem)
            } else {
                _cartItems.remove(existingItem)
            }
        }
    }

    fun removeAllFromCart(product: Product) {
        _cartItems.removeAll { it.id == product.id }
    }

    fun clearCart() {
        _cartItems.clear()
    }

    fun getCartItemCount(): Int {
        return _cartItems.sumOf { it.cartQuantity }
    }

    fun getTotalPrice(): Int {
        return _cartItems.sumOf { it.price * it.cartQuantity }
    }

    fun isProductInCart(productId: Int): Boolean {
        return _cartItems.any { it.id == productId }
    }

    fun getProductQuantity(productId: Int): Int {
        return _cartItems.find { it.id == productId }?.cartQuantity ?: 0
    }
}