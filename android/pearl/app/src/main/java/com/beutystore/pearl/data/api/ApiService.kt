package com.beutystore.pearl.data.api

import com.beutystore.pearl.data.model.AuthResponse
import com.beutystore.pearl.data.model.LoginRequest
import com.beutystore.pearl.data.model.RegisterRequest
import com.beutystore.pearl.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface PearlApiService {
    @POST("user/register/")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("user/login/")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("user/phone_login/")
    suspend fun phoneLogin(@Body request: PhoneLoginRequest): Response<AuthResponse>

    @POST("user/verify_code/")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<AuthResponse>

    @GET("user/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>
}

// Эти классы должны быть в отдельном файле или в том же пакете api
data class PhoneLoginRequest(val phone: String)
data class VerifyCodeRequest(val phone: String, val code: String)