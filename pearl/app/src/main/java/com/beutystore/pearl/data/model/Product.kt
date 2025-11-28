package com.beutystore.pearl.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    @SerializedName("original_price")
    val originalPrice: Int? = null,
    @SerializedName("image_url")
    val imageUrl: String = "",
    val description: String,
    val category: String,
    val brand: String? = null,
    val rating: Float,
    @SerializedName("review_count")
    val reviewCount: Int,
    @SerializedName("in_stock")
    val inStock: Boolean,
    val features: List<String>,
    val colors: List<String>,
    val sizes: List<String>,
    // Поле для корзины (не приходит с API, используется только локально)
    // Gson будет игнорировать это поле при десериализации, так как его нет в JSON
    var cartQuantity: Int = 0
)