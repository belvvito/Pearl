package com.beutystore.pearl.data.utils

import com.beutystore.pearl.data.model.Product
import java.util.Locale

object SearchUtils {

    /**
     * Оптимизированный поиск товаров с поддержкой русского языка
     */
    fun searchProducts(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products
        if (query.length < 2) return emptyList()

        val normalizedQuery = normalizeRussianText(query)

        return products.filter { product ->
            // Быстрая проверка по основным полям в порядке приоритета
            normalizeRussianText(product.name).contains(normalizedQuery) ||
                    normalizeRussianText(product.brand).contains(normalizedQuery) ||
                    (query.length > 2 && normalizeRussianText(product.category).contains(normalizedQuery)) ||
                    (query.length > 3 && (
                            normalizeRussianText(product.description).contains(normalizedQuery) ||
                                    product.features.any { feature ->
                                        normalizeRussianText(feature).contains(normalizedQuery)
                                    }
                            ))
        }
    }

    /**
     * Оптимизированный поиск категорий
     */
    fun searchCategories(categories: List<String>, query: String): List<String> {
        if (query.isBlank()) return categories
        if (query.length < 2) return emptyList()

        val normalizedQuery = normalizeRussianText(query)

        return categories.filter { category ->
            normalizeRussianText(category).contains(normalizedQuery)
        }
    }

    /**
     * Упрощенная нормализация русского текста для поиска
     * ИЗМЕНЕНИЕ: Сделали метод публичным
     */
    fun normalizeRussianText(text: String): String {
        return text
            .lowercase(Locale.forLanguageTag("ru"))
            .replace("ё", "е")
            .trim()
    }

    // ... остальные методы остаются без изменений ...

    /**
     * Публичный метод для безопасного нечеткого поиска
     */
    fun safeFuzzySearchProducts(products: List<Product>, query: String): List<Product> {
        return try {
            fuzzySearchProducts(products, query, maxDistance = 1)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Оптимизированный нечеткий поиск (только для длинных запросов)
     */
    private fun fuzzySearchProducts(products: List<Product>, query: String, maxDistance: Int = 2): List<Product> {
        if (query.isBlank()) return emptyList()
        if (query.length < 3) return emptyList()

        val normalizedQuery = normalizeRussianText(query)

        return products.filter { product ->
            // Сначала проверяем точные совпадения
            val exactMatch = normalizeRussianText(product.name).contains(normalizedQuery) ||
                    normalizeRussianText(product.brand).contains(normalizedQuery)

            if (exactMatch) {
                true
            } else {
                // Только если точного совпадения нет, используем нечеткий поиск
                val searchFields = listOf(product.name, product.brand)
                searchFields.any { field ->
                    val normalizedField = normalizeRussianText(field)
                    // Быстрая проверка перед расчетом расстояния
                    if (abs(normalizedField.length - normalizedQuery.length) > maxDistance) {
                        false
                    } else {
                        calculateOptimizedLevenshteinDistance(normalizedField, normalizedQuery) <= maxDistance
                    }
                }
            }
        }
    }

    /**
     * Оптимизированный расчет расстояния Левенштейна
     */
    private fun calculateOptimizedLevenshteinDistance(s1: String, s2: String): Int {
        // Быстрая проверка на одинаковые строки
        if (s1 == s2) return 0

        val len1 = s1.length
        val len2 = s2.length

        // Если одна из строк очень короткая, используем упрощенный расчет
        if (len1 == 0) return len2
        if (len2 == 0) return len1

        // Ограничиваем максимальную длину для сравнения
        val maxLen = 15
        val str1 = if (len1 > maxLen) s1.substring(0, maxLen) else s1
        val str2 = if (len2 > maxLen) s2.substring(0, maxLen) else s2

        val n = str1.length
        val m = str2.length

        // Используем только два массива для экономии памяти
        var prev = IntArray(m + 1) { it }
        var curr = IntArray(m + 1)

        for (i in 1..n) {
            curr[0] = i
            for (j in 1..m) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                curr[j] = minOfThree(
                    prev[j] + 1,      // удаление
                    curr[j - 1] + 1,  // вставка
                    prev[j - 1] + cost // замена
                )
            }
            // Меняем массивы местами
            val temp = prev
            prev = curr
            curr = temp
        }

        return prev[m]
    }

    /**
     * Вспомогательные функции
     */
    private fun abs(x: Int): Int = if (x < 0) -x else x

    private fun minOfThree(a: Int, b: Int, c: Int): Int = minOf(minOf(a, b), c)

    // ... остальные методы без изменений ...
}