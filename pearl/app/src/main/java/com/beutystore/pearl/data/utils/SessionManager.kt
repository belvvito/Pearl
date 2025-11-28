package com.beutystore.pearl.data.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Менеджер сессии пользователя.
 * 
 * Управляет сохранением и восстановлением сессии пользователя через SharedPreferences.
 * Позволяет сохранять токен доступа и состояние авторизации между запусками приложения.
 */
class SessionManager(context: Context) {
    /**
     * SharedPreferences для хранения данных сессии.
     */
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        /** Имя файла SharedPreferences для хранения сессии */
        private const val PREFS_NAME = "PearlSession"
        
        /** Ключ для хранения токена доступа */
        private const val KEY_ACCESS_TOKEN = "access_token"
        
        /** Ключ для хранения статуса авторизации */
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * Сохраняет токен доступа и устанавливает статус авторизации.
     * 
     * @param token Токен доступа для сохранения
     */
    fun saveAccessToken(token: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    /**
     * Получает сохраненный токен доступа.
     * 
     * @return Токен доступа или null, если токен не сохранен
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Проверяет, авторизован ли пользователь.
     * 
     * @return true, если пользователь авторизован и токен существует, иначе false
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getAccessToken() != null
    }

    /**
     * Очищает сохраненную сессию пользователя.
     * Вызывается при выходе из системы.
     */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)      // Удаляем токен доступа
            .putBoolean(KEY_IS_LOGGED_IN, false)  // Сбрасываем статус авторизации
            .apply()
    }
}

