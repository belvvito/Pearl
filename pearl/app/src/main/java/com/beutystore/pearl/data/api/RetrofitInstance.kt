package com.beutystore.pearl.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.beutystore.pearl.data.utils.ServerConfigManager

/**
 * Singleton объект для настройки и создания экземпляра Retrofit.
 * 
 * Retrofit используется для выполнения HTTP запросов к API сервера.
 * Настроен с увеличенными таймаутами для надежной работы при медленном соединении.
 * 
 * Поддерживает динамическое изменение BASE_URL через ServerConfigManager.
 */
object RetrofitInstance {
    private var context: Context? = null
    private var serverConfigManager: ServerConfigManager? = null
    
    /**
     * Инициализация RetrofitInstance с контекстом приложения.
     * Должна быть вызвана при запуске приложения.
     */
    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        serverConfigManager = ServerConfigManager(appContext.applicationContext)
        
        // Проверяем и исправляем URL, если он содержит HTTPS для локальных адресов
        val currentUrl = serverConfigManager?.getServerUrl() ?: ""
        if (currentUrl.startsWith("https://") && 
            (currentUrl.contains("10.0.2.2") || currentUrl.contains("192.168.") || 
             currentUrl.contains("127.0.0.1") || currentUrl.contains("localhost"))) {
            // Сбрасываем настройки, чтобы использовать значение из BuildConfig
            serverConfigManager?.resetToDefault()
            android.util.Log.w("RetrofitInstance", "Reset invalid HTTPS URL for local server")
        }
        
        // Сбрасываем retrofit, чтобы он пересоздался с новым URL
        _retrofit = null
        _api = null
    }
    
    /**
     * Базовый URL API сервера.
     * Использует ServerConfigManager для получения URL (может быть кастомным),
     * иначе берется из BuildConfig.
     */
    private fun getBaseUrl(): String {
        val url = serverConfigManager?.getServerUrl() ?: com.beutystore.pearl.BuildConfig.BASE_URL
        // Логируем используемый URL для отладки
        android.util.Log.d("RetrofitInstance", "Using BASE_URL: $url")
        return url
    }

    /**
     * Интерцептор для логирования HTTP запросов и ответов.
     * Уровень BODY позволяет видеть полное содержимое запросов и ответов в логах.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Настроенный OkHttpClient для HTTP запросов.
     * 
     * Особенности:
     * - Логирование всех запросов/ответов
     * - Увеличенные таймауты для избежания таймаутов при медленном соединении
     * - Автоматический повтор при ошибке подключения
     */
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)  // Добавляем логирование
        // Увеличиваем таймауты для избежания таймаутов при медленном соединении
        .connectTimeout(30, TimeUnit.SECONDS)  // Таймаут подключения: 30 секунд
        .readTimeout(60, TimeUnit.SECONDS)      // Таймаут чтения: 60 секунд (для больших ответов)
        .writeTimeout(30, TimeUnit.SECONDS)    // Таймаут записи: 30 секунд
        .retryOnConnectionFailure(true)         // Повтор при ошибке подключения
        .build()

    /**
     * Экземпляр Retrofit для создания API сервисов.
     * Инициализируется лениво (lazy) при первом обращении.
     */
    private var _retrofit: Retrofit? = null
    private val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                _retrofit = Retrofit.Builder()
                    .baseUrl(getBaseUrl())                        // Базовый URL API (динамический)
                    .client(client)                                // Настроенный HTTP клиент
                    .addConverterFactory(GsonConverterFactory.create())   // Конвертер JSON в объекты Kotlin
                    .build()
            }
            return _retrofit!!
        }

    /**
     * API сервис для работы с бэкендом.
     * Создается лениво при первом обращении.
     * Используется во всех репозиториях для выполнения HTTP запросов.
     */
    private var _api: PearlApiService? = null
    val api: PearlApiService
        get() {
            if (_api == null) {
                _api = retrofit.create(PearlApiService::class.java)
            }
            return _api!!
        }
    
    /**
     * Сбрасывает экземпляр Retrofit и API для пересоздания с новым URL.
     */
    fun reset() {
        _retrofit = null
        _api = null
    }
}