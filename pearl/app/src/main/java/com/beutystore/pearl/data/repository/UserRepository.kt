package com.beutystore.pearl.data.repository

import com.beutystore.pearl.data.api.PearlApiService
import com.beutystore.pearl.data.model.AuthResponse
import com.beutystore.pearl.data.model.LoginRequest
import com.beutystore.pearl.data.model.RegisterRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Репозиторий для работы с пользователями.
 * 
 * Обеспечивает взаимодействие с API для:
 * - Регистрации новых пользователей
 * - Входа в систему
 * - Получения профиля пользователя
 * 
 * Все методы возвращают Flow<Result<T>> для обработки успешных результатов и ошибок.
 */
class UserRepository(private val apiService: PearlApiService) {

    /**
     * Регистрация нового пользователя.
     * 
     * @param user Данные для регистрации (username, email, phone, password и т.д.)
     * @return Flow с результатом регистрации (успех или ошибка)
     */
    suspend fun register(user: RegisterRequest): Flow<Result<AuthResponse>> = flow {
        try {
            val response = apiService.register(user)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                val errorMessage = when (response.code()) {
                    400 -> {
                        when {
                            errorBody.contains("phone") -> "Пользователь с таким номером уже существует"
                            errorBody.contains("email") -> "Пользователь с таким email уже существует"
                            errorBody.contains("username") -> "Пользователь с таким именем уже существует"
                            errorBody.contains("password") -> "Пароли не совпадают"
                            else -> "Проверьте введенные данные: $errorBody"
                        }
                    }
                    else -> "Ошибка регистрации: $errorBody"
                }
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Вход в систему по номеру телефона и паролю.
     * 
     * @param phone Номер телефона пользователя
     * @param password Пароль пользователя
     * @return Flow с результатом входа (успех с токенами или ошибка)
     */
    suspend fun login(phone: String, password: String): Flow<Result<AuthResponse>> = flow {
        try {
            val response = apiService.login(LoginRequest(phone.trim(), password))
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                val errorMessage = when (response.code()) {
                    401 -> "Неверный номер телефона или пароль"
                    404 -> "Пользователь не найден"
                    else -> "Ошибка входа: $errorBody"
                }
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Получение профиля пользователя по токену доступа.
     * 
     * @param accessToken Токен доступа пользователя
     * @return Flow с данными профиля пользователя или ошибкой
     * 
     * Особенности:
     * - Валидирует токен перед запросом
     * - Обрабатывает CancellationException отдельно (для корректной работы с .first())
     * - Предоставляет понятные сообщения об ошибках для разных HTTP кодов
     */
    suspend fun getProfile(accessToken: String): Flow<Result<com.beutystore.pearl.data.model.User>> = flow {
        try {
            // Валидация токена перед запросом
            if (accessToken.isBlank()) {
                android.util.Log.e("UserRepository", "getProfile: accessToken is blank")
                emit(Result.failure(Exception("Токен авторизации не может быть пустым")))
                return@flow
            }
            
            android.util.Log.d("UserRepository", "getProfile: requesting profile with token length=${accessToken.length}")
            val response = apiService.getProfile("Bearer $accessToken")
            android.util.Log.d("UserRepository", "getProfile: response code=${response.code()}, isSuccessful=${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                android.util.Log.d("UserRepository", "getProfile: success - username=${user.username}, phone=${user.phone}, email=${user.email}")
                emit(Result.success(user))
                return@flow // Завершаем Flow после успешного результата
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                android.util.Log.e("UserRepository", "getProfile: failed - code=${response.code()}, errorBody=$errorBody")
                val errorMessage = when (response.code()) {
                    401 -> "Сессия истекла. Войдите снова"
                    404 -> "Профиль не найден"
                    500 -> "Ошибка сервера. Попробуйте позже"
                    else -> "Не удалось загрузить профиль: ${response.message()}"
                }
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: CancellationException) {
            // Игнорируем CancellationException (включая AbortFlowException) - это нормальное поведение
            // при использовании .first() или отмене корутины.
            // Просто пробрасываем исключение дальше, не эмитим ошибку в Flow.
            throw e
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "getProfile: exception", e)
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> 
                    "Проверьте подключение к интернету"
                e.message?.contains("timeout") == true -> 
                    "Превышено время ожидания. Попробуйте позже"
                else -> e.message ?: "Ошибка загрузки профиля"
            }
            emit(Result.failure(Exception(errorMessage)))
        }
    }
}