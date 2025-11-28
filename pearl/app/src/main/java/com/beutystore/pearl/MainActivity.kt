package com.beutystore.pearl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil.compose.LocalImageLoader
import com.beutystore.pearl.data.api.RetrofitInstance
import com.beutystore.pearl.navigation.PearlNavigation
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.utils.ImageLoaderConfig

/**
 * Главная Activity приложения Pearl.
 * 
 * Инициализирует приложение, настраивает тему и предоставляет кастомный ImageLoader
 * для загрузки изображений с увеличенными таймаутами.
 */
class MainActivity : ComponentActivity() {
    /**
     * Вызывается при создании Activity.
     * Настраивает Compose UI, тему и ImageLoader для всего приложения.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализируем RetrofitInstance с контекстом приложения
        // Это позволяет использовать ServerConfigManager для динамического изменения URL
        RetrofitInstance.initialize(this)
        
        // Создаем кастомный ImageLoader с увеличенными таймаутами
        // для надежной загрузки изображений даже при медленном соединении
        val imageLoader = ImageLoaderConfig.createImageLoader(this)
        
        setContent {
            // Состояние темы приложения (светлая/темная)
            var isDarkTheme by remember { mutableStateOf(false) }
            
            // Предоставляем кастомный ImageLoader для всего приложения через CompositionLocal
            // Это позволяет всем AsyncImage использовать настройки с увеличенными таймаутами
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                // Применяем тему приложения
                PearlTheme(darkTheme = isDarkTheme) {
                    // Инициализируем навигацию приложения
                    PearlNavigation(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = it }
                    )
                }
            }
        }
    }
}