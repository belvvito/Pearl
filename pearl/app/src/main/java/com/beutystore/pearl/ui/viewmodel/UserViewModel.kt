package com.beutystore.pearl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beutystore.pearl.data.api.RetrofitInstance
import com.beutystore.pearl.data.model.User
import com.beutystore.pearl.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository(RetrofitInstance.api)
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Отслеживаем текущий запрос, чтобы избежать дублирования
    private var loadProfileJob: Job? = null
    
    fun loadProfile(accessToken: String) {
        // Проверяем валидность токена
        if (accessToken.isBlank()) {
            _error.value = "Токен авторизации не может быть пустым"
            return
        }
        
        // Отменяем предыдущий запрос, если он еще выполняется
        loadProfileJob?.cancel()
        
        loadProfileJob = viewModelScope.launch {
            // Не загружаем повторно, если уже загружаем
            if (_isLoading.value) return@launch
            
            _isLoading.value = true
            _error.value = null
            
            try {
                // Используем first() вместо collect, так как Flow эмитит только один раз
                val result = userRepository.getProfile(accessToken).first()
                
                if (result.isSuccess) {
                    val loadedUser = result.getOrNull()
                    android.util.Log.d("UserViewModel", "Profile loaded successfully: username=${loadedUser?.username}, phone=${loadedUser?.phone}")
                    _user.value = loadedUser
                    _error.value = null // Очищаем ошибку при успехе
                } else {
                    val exception = result.exceptionOrNull()
                    android.util.Log.e("UserViewModel", "Profile load failed: ${exception?.message}")
                    _error.value = exception?.message ?: "Ошибка загрузки профиля"
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Игнорируем отмену корутины - это нормальное поведение
                throw e
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки профиля"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearUser() {
        loadProfileJob?.cancel()
        _user.value = null
        _error.value = null
        _isLoading.value = false
    }
}

