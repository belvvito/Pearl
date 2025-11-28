package com.beutystore.pearl.data.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Менеджер для управления конфигурацией сервера.
 * 
 * Позволяет динамически менять BASE_URL без пересборки приложения.
 * Поддерживает несколько предустановленных серверов и кастомный URL.
 */
class ServerConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "server_config",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_SERVER_NAME = "server_name"
        
        // Предустановленные серверы
        val PRESET_SERVERS = listOf(
            ServerConfig(
                name = "Локальный (эмулятор)",
                url = "http://10.0.2.2:8000/api/",
                description = "Для эмулятора Android"
            ),
            ServerConfig(
                name = "Локальный (устройство)",
                url = "http://192.168.0.18:8000/api/",
                description = "Для реального устройства в локальной сети"
            ),
            ServerConfig(
                name = "Production",
                url = "https://your-server.com/api/",
                description = "Production сервер"
            ),
            ServerConfig(
                name = "Staging",
                url = "https://staging.your-server.com/api/",
                description = "Staging сервер для тестирования"
            )
        )
    }
    
    /**
     * Получает текущий URL сервера.
     * Если кастомный URL не задан, возвращает значение из BuildConfig.
     */
    fun getServerUrl(): String {
        val customUrl = prefs.getString(KEY_SERVER_URL, null)
        return customUrl ?: com.beutystore.pearl.BuildConfig.BASE_URL
    }
    
    /**
     * Устанавливает кастомный URL сервера.
     * @param url URL сервера (должен заканчиваться на /api/)
     */
    fun setServerUrl(url: String) {
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"
        prefs.edit()
            .putString(KEY_SERVER_URL, normalizedUrl)
            .apply()
    }
    
    /**
     * Устанавливает сервер из предустановленного списка.
     */
    fun setPresetServer(serverConfig: ServerConfig) {
        prefs.edit()
            .putString(KEY_SERVER_URL, serverConfig.url)
            .putString(KEY_SERVER_NAME, serverConfig.name)
            .apply()
    }
    
    /**
     * Получает название текущего сервера.
     */
    fun getServerName(): String {
        val savedName = prefs.getString(KEY_SERVER_NAME, null)
        return savedName ?: try {
            // Пытаемся получить название из BuildConfig (если доступно)
            com.beutystore.pearl.BuildConfig.SERVER_NAME
        } catch (e: Exception) {
            "По умолчанию"
        }
    }
    
    /**
     * Сбрасывает настройки на значения по умолчанию (из BuildConfig).
     */
    fun resetToDefault() {
        prefs.edit()
            .remove(KEY_SERVER_URL)
            .remove(KEY_SERVER_NAME)
            .apply()
    }
    
    /**
     * Проверяет, используется ли кастомный URL.
     */
    fun isCustomUrl(): Boolean {
        return prefs.contains(KEY_SERVER_URL)
    }
}

/**
 * Конфигурация сервера.
 */
data class ServerConfig(
    val name: String,
    val url: String,
    val description: String = ""
)

