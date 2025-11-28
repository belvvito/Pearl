// navigation/Screen.kt
package com.beutystore.pearl.navigation

/**
 * Sealed класс для определения всех экранов приложения.
 * 
 * Каждый экран имеет уникальный route (маршрут) для навигации.
 * Некоторые экраны имеют параметры и используют функцию createRoute для генерации маршрута.
 */
sealed class Screen(val route: String) {
    // Экран приветствия (первый экран при запуске)
    object Welcome : Screen("welcome")
    
    // Экран входа в систему
    object Login : Screen("login")
    
    // Экран регистрации
    object Register : Screen("register")
    
    // Главный экран с табами (Home, Catalog, Cart, Profile)
    object Main : Screen("main")
    
    // Экран деталей товара с параметром ID
    object ProductDetail : Screen("product_detail") {
        /**
         * Создает маршрут для экрана деталей товара.
         * @param productId ID товара
         * @return Маршрут вида "product_detail/{productId}"
         */
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    
    // Экран товаров категории с параметром названия категории
    object CategoryProducts : Screen("category_products") {
        /**
         * Создает маршрут для экрана товаров категории.
         * @param categoryName Название категории
         * @return Маршрут вида "category_products/{categoryName}" (пробелы заменяются на "_")
         */
        fun createRoute(categoryName: String) = "category_products/${categoryName.replace(" ", "_")}"
    }

    // Отдельные экраны для табов MainScreen
    object Home : Screen("home")              // Главная страница
    object Catalog : Screen("catalog")       // Каталог товаров
    object Cart : Screen("cart")             // Корзина
    object Profile : Screen("profile")       // Профиль пользователя
    object Favorites : Screen("favorites")   // Избранное
    object Orders : Screen("orders")         // Мои заказы
    object Checkout : Screen("checkout")     // Оформление заказа
    object BonusCard : Screen("bonus_card")  // Бонусная карта
    object Notifications : Screen("notifications") // Уведомления
    object Help : Screen("help")             // Помощь
    object About : Screen("about")           // О приложении
    object SkinTest : Screen("skin_test")    // Тест типа кожи
    object AIConsultant : Screen("ai_consultant") // AI-консультант
}