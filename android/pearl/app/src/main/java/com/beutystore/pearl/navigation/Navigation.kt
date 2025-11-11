// navigation/Navigation.kt
package com.beutystore.pearl.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.ui.screen.*

@Composable
fun PearlNavigation() {
    val navController = rememberNavController()
    var isUserLoggedIn by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToMain = {
                    isUserLoggedIn = false // Гостевой режим
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    isUserLoggedIn = true
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterSuccess = {
                    isUserLoggedIn = true
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                },
                onCategoryClick = { category -> // ВАЖНО: ДОБАВЛЯЕМ ЭТОТ КОЛБЭК
                    navController.navigate(Screen.CategoryProducts.createRoute(category))
                }
            )
        }

        // Экран каталога
        composable(Screen.Catalog.route) {
            CatalogScreen(
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onCategoryClick = { category ->
                    navController.navigate(Screen.CategoryProducts.createRoute(category))
                },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                }
            )
        }

        // Экран товаров категории
        composable(
            route = "${Screen.CategoryProducts.route}/{categoryName}",
            arguments = listOf(
                navArgument("categoryName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName")
                ?.replace("_", " ") ?: "Все товары"

            ProductListScreen(
                categoryName = categoryName,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                }
            )
        }

        // Остальные экраны остаются без изменений...
        composable(
            route = "${Screen.ProductDetail.route}/{productId}",
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 1

            val sampleProduct = createSampleProduct(productId)

            ProductDetailScreen(
                product = sampleProduct,
                isUserLoggedIn = isUserLoggedIn,
                onBackClick = {
                    navController.popBackStack()
                },
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onAddToCart = { product ->
                    if (isUserLoggedIn) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onAddToFavorites = { product ->
                    if (isUserLoggedIn) {
                        // TODO: Реализовать добавление в избранное
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onLogout = {
                    isUserLoggedIn = false
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}

private fun createSampleProduct(id: Int): Product {
    return Product(
        id = id,
        name = "Товар $id",
        price = 1000 + id * 200,
        originalPrice = if (id % 3 == 0) 1500 + id * 200 else null,
        imageUrl = "https://via.placeholder.com/400",
        description = "Описание товара $id",
        category = "Категория",
        brand = "Бренд",
        rating = 4.0f + (id % 5) * 0.2f,
        reviewCount = 10 + id * 5,
        inStock = true,
        features = listOf("Качество", "Надежность"),
        colors = listOf("Стандартный"),
        sizes = listOf("50 мл")
    )
}