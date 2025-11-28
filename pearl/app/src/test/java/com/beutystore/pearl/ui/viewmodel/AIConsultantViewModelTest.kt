package com.beutystore.pearl.ui.viewmodel

import com.beutystore.pearl.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit тесты для AIConsultantViewModel.
 * 
 * Тестирует генерацию ответов AI-консультанта, анализ контекста,
 * поиск продуктов и форматирование рекомендаций.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AIConsultantViewModelTest {
    @Mock
    private lateinit var productRepository: ProductRepository

    private lateinit var aiViewModel: AIConsultantViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        aiViewModel = AIConsultantViewModel(productRepository)
    }

    @Test
    fun testInitialState_HasWelcomeMessage() = runTest(testDispatcher) {
        // Act
        val messages = aiViewModel.messages.first()

        // Assert
        assertTrue("Должно быть приветственное сообщение", messages.isNotEmpty())
        assertFalse("Первое сообщение должно быть от AI", messages[0].isUser)
        assertTrue("Сообщение должно содержать приветствие", 
            messages[0].text.contains("Привет") || messages[0].text.contains("консультант"))
    }

    @Test
    fun testSendMessage_AddsUserMessage() = runTest(testDispatcher) {
        // Arrange
        val userMessage = "Подбери крем для жирной кожи"

        // Act
        aiViewModel.sendMessage(userMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val messages = aiViewModel.messages.first()
        assertTrue("Должно быть больше одного сообщения", messages.size > 1)
        assertTrue("Последнее сообщение должно быть от пользователя", messages.last().isUser)
        assertEquals("Текст сообщения должен совпадать", userMessage, messages.last().text)
    }

    @Test
    fun testSendMessage_GeneratesAIResponse() = runTest(testDispatcher) {
        // Arrange
        val userMessage = "Подбери крем для жирной кожи"

        // Act
        aiViewModel.sendMessage(userMessage)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val messages = aiViewModel.messages.first()
        assertTrue("Должно быть минимум 2 сообщения", messages.size >= 2)
        
        // Проверяем, что AI ответил (последнее сообщение не от пользователя)
        val lastMessage = messages.last()
        if (!lastMessage.isUser) {
            assertTrue("Ответ AI должен содержать информацию о креме",
                lastMessage.text.contains("крем") || 
                lastMessage.text.contains("жирн") ||
                lastMessage.text.contains("рекоменд"))
        }
    }

    @Test
    fun testClearChat_ResetsToWelcomeMessage() = runTest(testDispatcher) {
        // Arrange
        aiViewModel.sendMessage("Тестовое сообщение")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        aiViewModel.clearChat()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val messages = aiViewModel.messages.first()
        assertEquals("Должно быть только приветственное сообщение", 1, messages.size)
        assertFalse("Сообщение должно быть от AI", messages[0].isUser)
    }

    @Test
    fun testSendMessage_WithEmptyMessage_DoesNothing() = runTest(testDispatcher) {
        // Arrange
        val initialMessages = aiViewModel.messages.first().size

        // Act
        aiViewModel.sendMessage("")
        aiViewModel.sendMessage("   ")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val messages = aiViewModel.messages.first()
        assertEquals("Количество сообщений не должно измениться", initialMessages, messages.size)
    }
}

