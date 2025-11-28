package com.beutystore.pearl.data.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit тесты для SessionManager.
 * 
 * Тестирует сохранение и восстановление сессии пользователя.
 */
class SessionManagerTest {
    private lateinit var sessionManager: SessionManager
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sessionManager = SessionManager(context)
        // Очищаем сессию перед каждым тестом
        sessionManager.clearSession()
    }

    @Test
    fun testSaveAndGetAccessToken() {
        // Arrange
        val testToken = "test_access_token_12345"

        // Act
        sessionManager.saveAccessToken(testToken)
        val retrievedToken = sessionManager.getAccessToken()

        // Assert
        assertEquals("Токен должен быть сохранен и восстановлен", testToken, retrievedToken)
    }

    @Test
    fun testIsLoggedIn_WhenTokenSaved_ReturnsTrue() {
        // Arrange
        val testToken = "test_access_token_12345"

        // Act
        sessionManager.saveAccessToken(testToken)
        val isLoggedIn = sessionManager.isLoggedIn()

        // Assert
        assertTrue("Пользователь должен быть авторизован после сохранения токена", isLoggedIn)
    }

    @Test
    fun testIsLoggedIn_WhenNoToken_ReturnsFalse() {
        // Act
        val isLoggedIn = sessionManager.isLoggedIn()

        // Assert
        assertFalse("Пользователь не должен быть авторизован без токена", isLoggedIn)
    }

    @Test
    fun testClearSession_RemovesToken() {
        // Arrange
        val testToken = "test_access_token_12345"
        sessionManager.saveAccessToken(testToken)

        // Act
        sessionManager.clearSession()
        val retrievedToken = sessionManager.getAccessToken()
        val isLoggedIn = sessionManager.isLoggedIn()

        // Assert
        assertNull("Токен должен быть удален", retrievedToken)
        assertFalse("Пользователь не должен быть авторизован после очистки", isLoggedIn)
    }

    @Test
    fun testSaveAccessToken_SetsLoggedInStatus() {
        // Arrange
        val testToken = "test_access_token_12345"

        // Act
        sessionManager.saveAccessToken(testToken)

        // Assert
        assertTrue("Статус авторизации должен быть установлен", sessionManager.isLoggedIn())
    }
}

