package com.beutystore.pearl.data.model

data class User (
    val id: Int,
    val username: String,
    val email: String,
    val phone: String,
    val date_of_birth: String?,
    val is_verified: Boolean,
    val created_at: String,
    val profile: UserProfile?
)

data class UserProfile(
    val id: Int,
    val avatar: String?,
    val bio: String?,
    val city: String?,
    val country: String?
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val phone: String,
    val date_of_birth: String? = null,
    val password: String,
    val password_confirm: String
)

data class LoginRequest(
    val phone: String,
    val password: String
)

data class AuthResponse(
    val user: User,
    val tokens: Tokens?,
    val message: String?,
    val needs_verification: Boolean?
)

data class Tokens(
    val refresh: String,
    val access: String
)