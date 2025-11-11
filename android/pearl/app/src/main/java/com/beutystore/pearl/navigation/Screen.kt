// navigation/Screen.kt
package com.beutystore.pearl.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object ProductDetail : Screen("product_detail") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    object CategoryProducts : Screen("category_products") {
        fun createRoute(categoryName: String) = "category_products/${categoryName.replace(" ", "_")}"
    }

    // Отдельные экраны для табов MainScreen
    object Home : Screen("home")
    object Catalog : Screen("catalog")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
}