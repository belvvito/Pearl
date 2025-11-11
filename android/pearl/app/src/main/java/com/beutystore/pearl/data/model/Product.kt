// data/model/Product.kt
package com.beutystore.pearl.data.model

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val originalPrice: Int? = null,
    val imageUrl: String,
    val description: String,
    val category: String,
    val brand: String,
    val rating: Float = 0.0f,
    val reviewCount: Int = 0,
    val inStock: Boolean = true,
    val features: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val sizes: List<String> = emptyList()
)