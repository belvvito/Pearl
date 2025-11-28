package com.beutystore.pearl.ui.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Конфигурация ImageLoader для библиотеки Coil.
 * 
 * Настраивает кастомный ImageLoader с увеличенными таймаутами и оптимизированным кэшированием
 * для надежной загрузки изображений даже при медленном соединении.
 */
object ImageLoaderConfig {
    /**
     * Создает кастомный ImageLoader с увеличенными таймаутами для загрузки изображений.
     * 
     * Это помогает избежать таймаутов при загрузке изображений с медленных серверов
     * (например, Unsplash или других внешних источников).
     * 
     * Особенности конфигурации:
     * - Увеличенные таймауты для медленных соединений
     * - Кэширование в памяти (25% доступной памяти)
     * - Кэширование на диске (50 MB)
     * - Автоматический повтор при ошибке подключения
     * - Плавное появление изображений (crossfade)
     * 
     * @param context Контекст приложения
     * @return Настроенный ImageLoader для использования в AsyncImage
     */
    fun createImageLoader(context: Context): ImageLoader {
        // Настраиваем HTTP клиент с увеличенными таймаутами
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Таймаут подключения: 30 секунд (увеличено с 10)
            .readTimeout(60, TimeUnit.SECONDS)    // Таймаут чтения: 60 секунд (увеличено с 15)
            .writeTimeout(30, TimeUnit.SECONDS)   // Таймаут записи: 30 секунд (увеличено с 15)
            .retryOnConnectionFailure(true)       // Повтор при ошибке подключения
            .build()

        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)           // Используем настроенный HTTP клиент
            .memoryCache {
                // Кэш в памяти для быстрого доступа к часто используемым изображениям
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)         // 25% от доступной памяти устройства
                    .build()
            }
            .diskCache {
                // Кэш на диске для долгосрочного хранения изображений
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // Максимальный размер: 50 MB
                    .build()
            }
            .respectCacheHeaders(false)           // Игнорируем заголовки кэша для более агрессивного кэширования
            .crossfade(true)                      // Плавное появление изображений при загрузке
            .build()
    }
}

