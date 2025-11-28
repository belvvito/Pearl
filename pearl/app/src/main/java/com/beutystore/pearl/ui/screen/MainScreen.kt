// ui/screen/MainScreen.kt
package com.beutystore.pearl.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.viewmodel.CartViewModel
import com.beutystore.pearl.ui.viewmodel.FavoritesViewModel
import com.beutystore.pearl.ui.viewmodel.SkinTestViewModel

@Composable
fun MainScreen(
    isUserLoggedIn: Boolean = false,
    accessToken: String? = null,
    initialSelectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onAuthRequired: () -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onBonusCardClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onSkinTestClick: () -> Unit = {},
    onAIConsultantClick: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {},
    cartViewModel: CartViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel? = null,
    skinTestViewModel: SkinTestViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(initialSelectedTab) }
    
    // Обновляем selectedTab при изменении initialSelectedTab (при возврате на MainScreen)
    LaunchedEffect(initialSelectedTab) {
        if (selectedTab != initialSelectedTab) {
            selectedTab = initialSelectedTab
        }
    }
    // История переходов между вкладками для корректной обработки кнопки "назад"
    // История содержит предыдущие вкладки в порядке посещения
    val tabHistory = remember { mutableListOf<Int>() }

    // ОПТИМИЗАЦИЯ: remember для статических данных
    val tabs = remember {
        listOf("Главная", "Каталог", "Корзина", "Профиль")
    }

    // ОПТИМИЗАЦИЯ: remember для иконок
    val tabIcons = remember {
        listOf(
            Icons.Default.Home,
            Icons.Default.ShoppingBag,
            Icons.Default.ShoppingCart,
            Icons.Default.Person
        )
    }
    
    // Обработка кнопки "назад" - возврат на предыдущую вкладку
    BackHandler(enabled = tabHistory.isNotEmpty()) {
        // Берем последнюю вкладку из истории (предыдущая посещенная вкладка)
        val previousTab = tabHistory.removeLastOrNull() ?: 0
        if (selectedTab != previousTab) {
            selectedTab = previousTab
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PearlRed,
                            selectedTextColor = PearlRed,
                            indicatorColor = PearlRed.copy(alpha = 0.2f)
                        ),
                        onClick = {
                            // ОПТИМИЗАЦИЯ: Быстрая проверка без тяжелых операций
                            if (!isUserLoggedIn && (index == 2 || index == 3)) {
                                onAuthRequired()
                            } else {
                                // Сохраняем текущую вкладку в историю перед переключением
                                if (selectedTab != index) {
                                    tabHistory.add(selectedTab)
                                    selectedTab = index
                                    onTabSelected(index) // Сохраняем выбранную вкладку в навигации
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // ОПТИМИЗАЦИЯ: Условный рендеринг с ключами для переиспользования
        when (selectedTab) {
            0 -> HomeScreen(
                onProductClick = onProductClick,
                onSkinTestClick = onSkinTestClick,
                onAIConsultantClick = onAIConsultantClick,
                skinTestViewModel = skinTestViewModel,
                cartViewModel = cartViewModel,
                favoritesViewModel = favoritesViewModel,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired
            )
            1 -> CatalogScreen(
                modifier = Modifier.padding(innerPadding),
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                onCategoryClick = onCategoryClick,
                onProductClick = onProductClick,
                cartViewModel = cartViewModel
            )
            2 -> CartScreen(
                modifier = Modifier.padding(innerPadding),
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                onCheckoutClick = onCheckoutClick,
                cartViewModel = cartViewModel
            )
            3 -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                isUserLoggedIn = isUserLoggedIn,
                accessToken = accessToken,
                onAuthRequired = onAuthRequired,
                onLogout = onLogout,
                onFavoritesClick = onFavoritesClick,
                onOrdersClick = onOrdersClick,
                onBonusCardClick = onBonusCardClick,
                onNotificationsClick = onNotificationsClick,
                onHelpClick = onHelpClick,
                onAboutClick = onAboutClick,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }
    }
}


@Composable
fun AuthRequiredState(
    message: String,
    onAuthRequired: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔐",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Требуется авторизация",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onAuthRequired,
            colors = ButtonDefaults.buttonColors(
                containerColor = PearlRed
            )
        ) {
            Text("Войти в аккаунт")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PearlTheme {
        MainScreen(
            isUserLoggedIn = true,
            onAuthRequired = {},
            onProductClick = {},
            onCategoryClick = {}
        )
    }
}