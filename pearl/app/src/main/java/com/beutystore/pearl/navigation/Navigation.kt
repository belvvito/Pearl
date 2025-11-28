// navigation/Navigation.kt
package com.beutystore.pearl.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement // Добавьте этот импорт
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beutystore.pearl.data.utils.SessionManager
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.ui.screen.*
import com.beutystore.pearl.ui.viewmodel.BonusCardViewModel
import com.beutystore.pearl.ui.viewmodel.CartViewModel
import com.beutystore.pearl.ui.viewmodel.FavoritesViewModel
import com.beutystore.pearl.ui.viewmodel.OrdersViewModel
import com.beutystore.pearl.ui.viewmodel.SkinTestViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PearlNavigation(
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    var isUserLoggedIn by remember { mutableStateOf(false) }
    var accessToken by remember { mutableStateOf<String?>(null) }
    // Сохраняем выбранную вкладку в MainScreen для восстановления при возврате
    var mainScreenSelectedTab by remember { mutableStateOf(0) }
    // Shared ViewModels для всего приложения
    val cartViewModel: CartViewModel = viewModel()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val ordersViewModel: OrdersViewModel = viewModel()
    val bonusCardViewModel: BonusCardViewModel = viewModel()
    val skinTestViewModel: SkinTestViewModel = viewModel()

    // Восстанавливаем сессию при запуске приложения
    LaunchedEffect(Unit) {
        val savedToken = sessionManager.getAccessToken()
        if (savedToken != null && sessionManager.isLoggedIn()) {
            isUserLoggedIn = true
            accessToken = savedToken
            // Автоматически переходим на главный экран, если пользователь был авторизован
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        }
    }

    // Отслеживаем изменения состояния аутентификации
    LaunchedEffect(isUserLoggedIn) {
        snapshotFlow { isUserLoggedIn }
            .distinctUntilChanged()
            .collect { loggedIn ->
                // Можно добавить логику при изменении состояния аутентификации
            }
    }

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
                onLoginSuccess = { token ->
                    isUserLoggedIn = true
                    accessToken = token
                    // Сохраняем токен для следующего запуска
                    sessionManager.saveAccessToken(token)
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
                onRegisterSuccess = { token ->
                    isUserLoggedIn = true
                    accessToken = token
                    // Сохраняем токен для следующего запуска
                    sessionManager.saveAccessToken(token)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SkinTest.route) {
            SkinTestScreen(
                onComplete = { testResult ->
                    // Результат теста уже сохранен в ViewModel
                    // Возвращаемся назад, если есть экран в стеке, иначе переходим на главный
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                onSkip = {
                    // Возвращаемся назад, если есть экран в стеке, иначе переходим на главный
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                skinTestViewModel = skinTestViewModel
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                isUserLoggedIn = isUserLoggedIn,
                accessToken = accessToken,
                initialSelectedTab = mainScreenSelectedTab,
                onTabSelected = { tabIndex ->
                    mainScreenSelectedTab = tabIndex
                },
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                },
                onCategoryClick = { category ->
                    navController.navigate(Screen.CategoryProducts.createRoute(category))
                },
                onLogout = {
                    isUserLoggedIn = false
                    accessToken = null
                    // Очищаем сохраненную сессию
                    sessionManager.clearSession()
                    mainScreenSelectedTab = 0 // Сбрасываем вкладку при выходе
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) // Очищаем весь стек
                    }
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onOrdersClick = {
                    navController.navigate(Screen.Orders.route)
                },
                onBonusCardClick = {
                    navController.navigate(Screen.BonusCard.route)
                },
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onHelpClick = {
                    navController.navigate(Screen.Help.route)
                },
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                },
                onSkinTestClick = {
                    navController.navigate(Screen.SkinTest.route)
                },
                onAIConsultantClick = {
                    navController.navigate(Screen.AIConsultant.route)
                },
                onCheckoutClick = {
                    navController.navigate(Screen.Checkout.route)
                },
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                cartViewModel = cartViewModel,
                favoritesViewModel = favoritesViewModel,
                skinTestViewModel = skinTestViewModel
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
                },
                cartViewModel = cartViewModel
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
                },
                cartViewModel = cartViewModel
            )
        }

        // Экран деталей товара
        composable(
            route = "${Screen.ProductDetail.route}/{productId}",
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 1
            val productRepository = com.beutystore.pearl.data.repository.ProductRepository()
            
            // Загружаем продукт из API
            var product: com.beutystore.pearl.data.model.Product? by remember { mutableStateOf(null) }
            var isLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(productId) {
                isLoading = true
                productRepository.getProduct(productId).fold(
                    onSuccess = { loadedProduct ->
                        product = loadedProduct
                        isLoading = false
                    },
                    onFailure = {
                        isLoading = false
                    }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (product != null) {
                ProductDetailScreen(
                    product = product!!,
                    isUserLoggedIn = isUserLoggedIn,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAuthRequired = {
                        navController.navigate(Screen.Login.route)
                    },
                    onAddToCart = { product ->
                        if (isUserLoggedIn) {
                            cartViewModel.addToCart(product, 1)
                            // Убрали popBackStack(), чтобы остаться на экране деталей товара
                        } else {
                            navController.navigate(Screen.Login.route)
                        }
                    },
                    onAddToFavorites = { product ->
                        if (isUserLoggedIn) {
                            favoritesViewModel.toggleFavorite(product)
                        } else {
                            navController.navigate(Screen.Login.route)
                        }
                    },
                    favoritesViewModel = favoritesViewModel,
                    cartViewModel = cartViewModel
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Товар не найден")
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Назад")
                        }
                    }
                }
            }
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                },
                onSkinTestClick = {
                    navController.navigate(Screen.SkinTest.route)
                },
                onAIConsultantClick = {
                    navController.navigate(Screen.AIConsultant.route)
                },
                skinTestViewModel = skinTestViewModel,
                cartViewModel = cartViewModel,
                favoritesViewModel = favoritesViewModel,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Cart.route) {
            // Временно показываем заглушку для корзины
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛒",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Корзина",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Раздел в разработке",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Назад")
                    }
                }
            }
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                isUserLoggedIn = isUserLoggedIn,
                accessToken = accessToken,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onLogout = {
                    isUserLoggedIn = false
                    accessToken = null
                    // Очищаем сохраненную сессию
                    sessionManager.clearSession()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) // Очищаем весь стек
                    }
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onOrdersClick = {
                    navController.navigate(Screen.Orders.route)
                },
                onBonusCardClick = {
                    navController.navigate(Screen.BonusCard.route)
                },
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onHelpClick = {
                    navController.navigate(Screen.Help.route)
                },
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                },
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }
        
composable(Screen.Favorites.route) {
            FavoritesScreen(
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                favoritesViewModel = favoritesViewModel,
                cartViewModel = cartViewModel
            )
        }

        composable(Screen.Orders.route) {
            OrdersScreen(
                isUserLoggedIn = isUserLoggedIn,
                accessToken = accessToken,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onProductClick = { product ->
                    navController.navigate(Screen.ProductDetail.createRoute(product.id))
                },
                ordersViewModel = ordersViewModel
            )
        }

        composable(Screen.Checkout.route) {
            val userViewModel: com.beutystore.pearl.ui.viewmodel.UserViewModel = viewModel()
            CheckoutScreen(
                cartViewModel = cartViewModel,
                ordersViewModel = ordersViewModel,
                userViewModel = userViewModel,
                accessToken = accessToken,
                onBackClick = {
                    navController.popBackStack()
                },
                onOrderSuccess = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.BonusCard.route) {
            BonusCardScreen(
                isUserLoggedIn = isUserLoggedIn,
                accessToken = accessToken,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                bonusCardViewModel = bonusCardViewModel
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = {
                    navController.navigate(Screen.Login.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Help.route) {
            HelpScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AIConsultant.route) {
            AIConsultantScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}