package com.beutystore.pearl.ui.viewmodel

import com.beutystore.pearl.data.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit тесты для CartViewModel.
 * 
 * Тестирует добавление/удаление товаров из корзины, вычисление общей суммы,
 * очистку корзины и обновление количества товаров.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {
    private lateinit var cartViewModel: CartViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        cartViewModel = CartViewModel()
    }

    @Test
    fun testAddToCart_AddsProduct() = runTest(testDispatcher) {
        // Arrange
        val product = createTestProduct(id = 1, name = "Test Product", price = 1000)

        // Act
        cartViewModel.addToCart(product, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val cartItems = cartViewModel.cartItems.first()
        assertEquals("В корзине должен быть 1 товар", 1, cartItems.size)
        assertEquals("ID товара должен совпадать", 1, cartItems[0].product.id)
        assertEquals("Количество должно быть 1", 1, cartItems[0].quantity)
    }

    @Test
    fun testAddToCart_ExistingProduct_IncreasesQuantity() = runTest(testDispatcher) {
        // Arrange
        val product = createTestProduct(id = 1, name = "Test Product", price = 1000)
        cartViewModel.addToCart(product, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        cartViewModel.addToCart(product, 2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val cartItems = cartViewModel.cartItems.first()
        assertEquals("В корзине должен быть 1 товар", 1, cartItems.size)
        assertEquals("Количество должно быть увеличено до 3", 3, cartItems[0].quantity)
    }

    @Test
    fun testRemoveFromCart_RemovesProduct() = runTest(testDispatcher) {
        // Arrange
        val product = createTestProduct(id = 1, name = "Test Product", price = 1000)
        cartViewModel.addToCart(product, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        cartViewModel.removeFromCart(product)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val cartItems = cartViewModel.cartItems.first()
        assertTrue("Корзина должна быть пустой", cartItems.isEmpty())
    }

    @Test
    fun testUpdateQuantity_UpdatesProductQuantity() = runTest(testDispatcher) {
        // Arrange
        val product = createTestProduct(id = 1, name = "Test Product", price = 1000)
        cartViewModel.addToCart(product, 1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        cartViewModel.updateQuantity(product, 5)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val cartItems = cartViewModel.cartItems.first()
        assertEquals("Количество должно быть обновлено до 5", 5, cartItems[0].quantity)
    }

    @Test
    fun testClearCart_RemovesAllItems() = runTest(testDispatcher) {
        // Arrange
        val product1 = createTestProduct(id = 1, name = "Product 1", price = 1000)
        val product2 = createTestProduct(id = 2, name = "Product 2", price = 2000)
        cartViewModel.addToCart(product1, 1)
        cartViewModel.addToCart(product2, 2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        cartViewModel.clearCart()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val cartItems = cartViewModel.cartItems.first()
        assertTrue("Корзина должна быть пустой", cartItems.isEmpty())
    }

    @Test
    fun testTotalPrice_CalculatesCorrectly() = runTest(testDispatcher) {
        // Arrange
        val product1 = createTestProduct(id = 1, name = "Product 1", price = 1000)
        val product2 = createTestProduct(id = 2, name = "Product 2", price = 2000)
        cartViewModel.addToCart(product1, 2)  // 2 * 1000 = 2000
        cartViewModel.addToCart(product2, 1)  // 1 * 2000 = 2000
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        val totalPrice = cartViewModel.totalPrice.first()

        // Assert
        assertEquals("Общая сумма должна быть 4000", 4000, totalPrice)
    }

    @Test
    fun testItemCount_ReturnsCorrectCount() = runTest(testDispatcher) {
        // Arrange
        val product1 = createTestProduct(id = 1, name = "Product 1", price = 1000)
        val product2 = createTestProduct(id = 2, name = "Product 2", price = 2000)
        cartViewModel.addToCart(product1, 2)
        cartViewModel.addToCart(product2, 3)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        val itemCount = cartViewModel.itemCount.first()

        // Assert
        assertEquals("Общее количество товаров должно быть 5", 5, itemCount)
    }

    // Вспомогательная функция для создания тестового продукта
    private fun createTestProduct(
        id: Int,
        name: String,
        price: Int,
        category: String = "Test Category"
    ): Product {
        return Product(
            id = id,
            name = name,
            price = price,
            original_price = null,
            image_url = "",
            description = "Test description",
            category = category,
            brand = "Test Brand",
            rating = 4.5f,
            review_count = 10,
            in_stock = true,
            features = emptyList(),
            colors = emptyList(),
            sizes = emptyList()
        )
    }
}

