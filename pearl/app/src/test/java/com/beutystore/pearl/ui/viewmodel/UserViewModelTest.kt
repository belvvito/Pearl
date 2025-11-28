package com.beutystore.pearl.ui.viewmodel

import com.beutystore.pearl.data.model.User
import com.beutystore.pearl.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.flow
import kotlin.Result

/**
 * Unit тесты для UserViewModel.
 * 
 * Тестирует загрузку профиля пользователя, обработку ошибок и очистку данных.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {
    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var userViewModel: UserViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userViewModel = UserViewModel(userRepository)
    }

    @Test
    fun testLoadProfile_Success() = runTest(testDispatcher) {
        // Arrange
        val testToken = "test_access_token"
        val testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            phone = "+79991234567",
            date_of_birth = null,
            is_verified = true,
            created_at = "2024-01-01T00:00:00Z",
            profile = null,
            bonus_card = null
        )

        whenever(userRepository.getProfile(testToken))
            .thenReturn(flowOf(Result.success(testUser)))

        // Act
        userViewModel.loadProfile(testToken)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val user = userViewModel.user.first()
        assertNotNull("Пользователь должен быть загружен", user)
        assertEquals("Имя пользователя должно совпадать", "testuser", user?.username)
        assertEquals("Email должен совпадать", "test@example.com", user?.email)
        assertEquals("Телефон должен совпадать", "+79991234567", user?.phone)
        
        val isLoading = userViewModel.isLoading.first()
        assertFalse("Загрузка должна быть завершена", isLoading)
        
        val error = userViewModel.error.first()
        assertNull("Ошибки не должно быть", error)
    }

    @Test
    fun testLoadProfile_Error() = runTest(testDispatcher) {
        // Arrange
        val testToken = "invalid_token"
        val errorMessage = "Токен недействителен"

        whenever(userRepository.getProfile(testToken))
            .thenReturn(flowOf(Result.failure(Exception(errorMessage))))

        // Act
        userViewModel.loadProfile(testToken)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val error = userViewModel.error.first()
        assertNotNull("Должна быть ошибка", error)
        assertEquals("Сообщение об ошибке должно совпадать", errorMessage, error)
        
        val isLoading = userViewModel.isLoading.first()
        assertFalse("Загрузка должна быть завершена", isLoading)
    }

    @Test
    fun testClearUser() = runTest(testDispatcher) {
        // Arrange
        val testToken = "test_token"
        val testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            phone = "+79991234567",
            date_of_birth = null,
            is_verified = true,
            created_at = "2024-01-01T00:00:00Z",
            profile = null,
            bonus_card = null
        )

        whenever(userRepository.getProfile(testToken))
            .thenReturn(flowOf(Result.success(testUser)))

        userViewModel.loadProfile(testToken)
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        userViewModel.clearUser()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val user = userViewModel.user.first()
        assertNull("Пользователь должен быть очищен", user)
        
        val isLoading = userViewModel.isLoading.first()
        assertFalse("Загрузка должна быть завершена", isLoading)
    }
}

