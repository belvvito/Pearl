package com.beutystore.pearl.data.model

import com.google.gson.annotations.SerializedName

data class User (
    val id: Int,
    val username: String,
    val email: String,
    val phone: String,
    @SerializedName("date_of_birth")
    val date_of_birth: String?,
    @SerializedName("is_verified")
    val is_verified: Boolean,
    @SerializedName("is_staff")
    val is_staff: Boolean = false,
    @SerializedName("is_superuser")
    val is_superuser: Boolean = false,
    @SerializedName("created_at")
    val created_at: String,
    val profile: UserProfile?,
    @SerializedName("bonus_card")
    val bonus_card: BonusCard?
) {
    /**
     * Проверяет, является ли пользователь администратором
     */
    val isAdmin: Boolean
        get() = is_staff || is_superuser
}

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