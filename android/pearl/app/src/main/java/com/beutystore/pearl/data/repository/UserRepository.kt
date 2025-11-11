package com.beutystore.pearl.data.repository

import com.beutystore.pearl.data.api.PearlApiService
import com.beutystore.pearl.data.model.AuthResponse
import com.beutystore.pearl.data.model.LoginRequest
import com.beutystore.pearl.data.model.RegisterRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository(private val apiService: PearlApiService) {

    suspend fun register(user: RegisterRequest): Flow<Result<AuthResponse>> = flow {
        try {
            val response = apiService.register(user)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Registration failed: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun login(phone: String, password: String): Flow<Result<AuthResponse>> = flow {
        try {
            val response = apiService.login(LoginRequest(phone, password))
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Login failed: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}