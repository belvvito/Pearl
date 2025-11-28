// ui/viewmodel/ProductsViewModel.kt
package com.beutystore.pearl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    private val _popularProducts = MutableStateFlow<List<Product>>(emptyList())
    val popularProducts: StateFlow<List<Product>> = _popularProducts.asStateFlow()

    private val _specialOffers = MutableStateFlow<List<Product>>(emptyList())
    val specialOffers: StateFlow<List<Product>> = _specialOffers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAllProducts()
        loadPopularProducts()
        loadSpecialOffers()
    }

    fun loadAllProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            productRepository.getProducts().fold(
                onSuccess = { products ->
                    _allProducts.value = products
                    _isLoading.value = false
                    android.util.Log.d("ProductsViewModel", "Загружено продуктов: ${products.size}")
                    // Логируем изображения первых продуктов для проверки
                    if (products.isNotEmpty()) {
                        products.take(3).forEach { product ->
                            android.util.Log.d("ProductsViewModel", "Product: ${product.name}, ImageURL: ${product.imageUrl}")
                        }
                    }
                },
                onFailure = { exception ->
                    val errorMsg = exception.message ?: "Ошибка загрузки продуктов"
                    _error.value = errorMsg
                    _isLoading.value = false
                    android.util.Log.e("ProductsViewModel", "Ошибка загрузки продуктов: $errorMsg", exception)
                }
            )
        }
    }

    fun loadPopularProducts(limit: Int = 10) {
        viewModelScope.launch {
            productRepository.getPopularProducts(limit).fold(
                onSuccess = { products ->
                    _popularProducts.value = products
                    android.util.Log.d("ProductsViewModel", "Загружено популярных продуктов: ${products.size}")
                    // Логируем изображения популярных продуктов
                    if (products.isNotEmpty()) {
                        products.take(2).forEach { product ->
                            android.util.Log.d("ProductsViewModel", "Popular Product: ${product.name}, ImageURL: ${product.imageUrl}")
                        }
                    }
                },
                onFailure = { exception ->
                    android.util.Log.e("ProductsViewModel", "Ошибка загрузки популярных продуктов: ${exception.message}", exception)
                    // Если популярные продукты не загрузились, используем все продукты
                    _popularProducts.value = _allProducts.value.take(limit)
                }
            )
        }
    }

    fun loadSpecialOffers() {
        viewModelScope.launch {
            // Загружаем продукты со скидкой (originalPrice != null)
            productRepository.getProducts().fold(
                onSuccess = { products ->
                    _specialOffers.value = products.filter { it.originalPrice != null }
                },
                onFailure = {
                    _specialOffers.value = emptyList()
                }
            )
        }
    }

    fun refresh() {
        loadAllProducts()
        loadPopularProducts()
        loadSpecialOffers()
    }
}

