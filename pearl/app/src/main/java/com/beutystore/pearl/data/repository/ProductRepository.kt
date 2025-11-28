// data/repository/ProductRepository.kt
package com.beutystore.pearl.data.repository

import com.beutystore.pearl.data.api.RetrofitInstance
import com.beutystore.pearl.data.model.Product

class ProductRepository {
    private val api = RetrofitInstance.api

    suspend fun getProducts(
        category: String? = null,
        search: String? = null,
        brand: String? = null,
        page: Int? = null
    ): Result<List<Product>> {
        return try {
            android.util.Log.d("ProductRepository", "Запрос продуктов: category=$category, search=$search, brand=$brand")
            val response = api.getProducts(category, search, brand, page)
            android.util.Log.d("ProductRepository", "Ответ API: code=${response.code()}, isSuccessful=${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.results
                android.util.Log.d("ProductRepository", "Получено продуктов: ${products.size}")
                // Логируем первые несколько продуктов для проверки изображений
                if (products.isNotEmpty()) {
                    products.take(3).forEach { product ->
                        android.util.Log.d("ProductRepository", "Product: ${product.name}, ImageURL: ${product.imageUrl}")
                    }
                }
                Result.success(products)
            } else {
                val errorMsg = "Failed to load products: ${response.code()} - ${response.message()}"
                android.util.Log.e("ProductRepository", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Ошибка при загрузке продуктов", e)
            Result.failure(e)
        }
    }

    suspend fun getProduct(id: Int): Result<Product> {
        return try {
            val response = api.getProduct(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load product: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularProducts(limit: Int = 10): Result<List<Product>> {
        return try {
            val response = api.getPopularProducts(limit)
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!.results
                android.util.Log.d("ProductRepository", "Получено популярных продуктов: ${products.size}")
                if (products.isNotEmpty()) {
                    products.take(2).forEach { product ->
                        android.util.Log.d("ProductRepository", "Popular Product: ${product.name}, ImageURL: ${product.imageUrl}")
                    }
                }
                Result.success(products)
            } else {
                Result.failure(Exception("Failed to load popular products: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecommendedProducts(token: String? = null, limit: Int = 10): Result<List<Product>> {
        return try {
            val response = api.getRecommendedProducts(token, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.results)
            } else {
                Result.failure(Exception("Failed to load recommended products: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchProducts(query: String): Result<List<Product>> {
        return getProducts(search = query)
    }

    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        return getProducts(category = category)
    }

    suspend fun getProductsByBrand(brand: String): Result<List<Product>> {
        return getProducts(brand = brand)
    }

    suspend fun getCategories(): Result<List<String>> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

