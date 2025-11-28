package com.beutystore.pearl.data.utils

import com.beutystore.pearl.data.model.Product

/**
 * Безопасный менеджер поиска с ограничениями и обработкой ошибок
 * Предоставляет защищенные методы для поиска товаров и категорий
 */
object SearchManager {

    // Максимальное количество товаров для поиска (для производительности)
    private const val MAX_PRODUCTS_FOR_SEARCH = 200
    private const val MAX_PRODUCTS_FOR_FUZZY_SEARCH = 100

    /**
     * Безопасный поиск товаров с ограничениями
     */
    fun safeSearchProducts(products: List<Product>, query: String): List<Product> {
        return try {
            when {
                query.isBlank() -> emptyList()
                query.length == 1 -> emptyList() // Не ищем для 1 символа
                else -> {
                    // Ограничиваем количество товаров для поиска
                    val limitedProducts = if (products.size > MAX_PRODUCTS_FOR_SEARCH) {
                        products.take(MAX_PRODUCTS_FOR_SEARCH)
                    } else {
                        products
                    }

                    // Используем обычный поиск
                    val results = SearchUtils.searchProducts(limitedProducts, query)

                    // Если не нашли результатов и запрос достаточно длинный, пробуем нечеткий поиск
                    if (results.isEmpty() && query.length >= 3) {
                        safeFuzzySearchProducts(limitedProducts, query)
                    } else {
                        results
                    }
                }
            }
        } catch (e: Exception) {
            // Логируем ошибку (в реальном приложении)
            // Log.e("SearchManager", "Error during search: ${e.message}")
            emptyList()
        }
    }

    /**
     * Безопасный нечеткий поиск с дополнительными ограничениями
     */
    private fun safeFuzzySearchProducts(products: List<Product>, query: String): List<Product> {
        return try {
            // Дополнительно ограничиваем для нечеткого поиска
            val limitedProducts = if (products.size > MAX_PRODUCTS_FOR_FUZZY_SEARCH) {
                products.take(MAX_PRODUCTS_FOR_FUZZY_SEARCH)
            } else {
                products
            }

            SearchUtils.safeFuzzySearchProducts(limitedProducts, query)
        } catch (e: Exception) {
            // Log.e("SearchManager", "Error during fuzzy search: ${e.message}")
            emptyList()
        }
    }

    /**
     * Безопасный поиск категорий
     */
    fun safeSearchCategories(categories: List<String>, query: String): List<String> {
        return try {
            SearchUtils.searchCategories(categories, query)
        } catch (e: Exception) {
            // Log.e("SearchManager", "Error during category search: ${e.message}")
            emptyList()
        }
    }

    /**
     * Поиск с пагинацией (для больших списков)
     */
    fun searchProductsWithPagination(
        allProducts: List<Product>,
        query: String,
        page: Int,
        pageSize: Int = 20
    ): List<Product> {
        return try {
            val results = safeSearchProducts(allProducts, query)
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, results.size)

            if (startIndex >= results.size) {
                emptyList()
            } else {
                results.subList(startIndex, endIndex)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Поиск с сортировкой
     */
    fun searchProductsWithSorting(
        products: List<Product>,
        query: String,
        sortBy: SortOption = SortOption.RELEVANCE
    ): List<Product> {
        return try {
            val results = safeSearchProducts(products, query)

            when (sortBy) {
                SortOption.PRICE_ASC -> results.sortedBy { it.price }
                SortOption.PRICE_DESC -> results.sortedByDescending { it.price }
                SortOption.RATING -> results.sortedByDescending { it.rating }
                SortOption.NAME -> results.sortedBy { it.name }
                else -> results // RELEVANCE - оставляем порядок поиска
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Быстрый поиск для автодополнения
     */
    fun quickSearchSuggestions(products: List<Product>, query: String): List<String> {
        return try {
            if (query.length < 2) return emptyList()

            val limitedProducts = if (products.size > 50) products.take(50) else products

            val brandSuggestions = limitedProducts
                .mapNotNull { it.brand }
                .distinct()
                .filter { it.contains(query, ignoreCase = true) }
                .take(3)

            val productSuggestions = limitedProducts
                .map { it.name }
                .distinct()
                .filter { it.contains(query, ignoreCase = true) }
                .take(3)

            (brandSuggestions + productSuggestions).distinct().take(5)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Опции сортировки для поиска
     */
    enum class SortOption {
        RELEVANCE,      // По релевантности (порядок поиска)
        PRICE_ASC,      // По цене (возрастание)
        PRICE_DESC,     // По цене (убывание)
        RATING,         // По рейтингу
        NAME            // По названию
    }
}