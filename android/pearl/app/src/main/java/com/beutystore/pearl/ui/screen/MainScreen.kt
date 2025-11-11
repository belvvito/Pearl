// ui/screen/MainScreen.kt
package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.ui.theme.PearlTheme

@Composable
fun MainScreen(
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onCategoryClick: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

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

    Scaffold(
        bottomBar = {
            NavigationBar {
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
                        onClick = {
                            // ОПТИМИЗАЦИЯ: Быстрая проверка без тяжелых операций
                            if (!isUserLoggedIn && (index == 2 || index == 3)) {
                                onAuthRequired()
                            } else {
                                selectedTab = index
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
                modifier = Modifier.padding(innerPadding),
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                onProductClick = onProductClick
            )
            1 -> CatalogScreen(
                modifier = Modifier.padding(innerPadding),
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                onCategoryClick = onCategoryClick,
                onProductClick = onProductClick
            )
            2 -> CartScreen(
                modifier = Modifier.padding(innerPadding),
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired
            )
            3 -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                onLogout = {
                    // Легкая операция - сброс состояния
                    selectedTab = 0
                }
            )
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