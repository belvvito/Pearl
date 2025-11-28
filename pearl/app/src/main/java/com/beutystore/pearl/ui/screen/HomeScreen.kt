package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlDarkRed
import com.beutystore.pearl.ui.theme.PearlPeach
import com.beutystore.pearl.ui.theme.PearlCrimson
import com.beutystore.pearl.ui.viewmodel.SkinTestViewModel
import com.beutystore.pearl.ui.viewmodel.getRecommendedProducts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import kotlin.math.abs

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.beutystore.pearl.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onProductClick: (Product) -> Unit,
    onSkinTestClick: () -> Unit = {},
    onAIConsultantClick: () -> Unit = {},
    skinTestViewModel: SkinTestViewModel,
    productsViewModel: com.beutystore.pearl.ui.viewmodel.ProductsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    cartViewModel: com.beutystore.pearl.ui.viewmodel.CartViewModel? = null,
    favoritesViewModel: com.beutystore.pearl.ui.viewmodel.FavoritesViewModel? = null,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {}
) {
    val testResult by skinTestViewModel.testResult.collectAsState()
    val allProducts by productsViewModel.allProducts.collectAsState()
    val popularProducts by productsViewModel.popularProducts.collectAsState()
    val specialOffers by productsViewModel.specialOffers.collectAsState()
    val isLoading by productsViewModel.isLoading.collectAsState()
    
    val recommendedProducts = remember(testResult, allProducts) {
        if (testResult != null && allProducts.isNotEmpty()) {
            getRecommendedProducts(allProducts, testResult)
        } else {
            emptyList()
        }
    }
    val scrollState = rememberScrollState()
    
    // Состояние поиска
    var searchQuery by remember { mutableStateOf("") }
    val searchedProducts = remember(searchQuery, allProducts) {
        if (searchQuery.isBlank() || searchQuery.length < 2) {
            emptyList()
        } else {
            com.beutystore.pearl.data.utils.SearchUtils.searchProducts(allProducts, searchQuery)
        }
    }

    // Определяем фон: светло-лавандовый для светлой темы, темный для темной
    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlLightPeach
    } else {
        MaterialTheme.colorScheme.background
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Modern Header with Search
        ModernHomeHeader(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it }
        )
        
        // Если есть поисковый запрос, показываем результаты поиска
        if (searchQuery.isNotBlank() && searchQuery.length >= 2) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (searchedProducts.isEmpty()) {
                    // Нет результатов
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ничего не найдено",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Попробуйте изменить запрос",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    // Результаты поиска
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Найдено: ${searchedProducts.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(searchedProducts) { product ->
                            // Отслеживаем состояние корзины и избранного
                            val isInCartSearch = if (cartViewModel != null) {
                                cartViewModel.cartItems.collectAsState().value.containsKey(product.id)
                            } else {
                                false
                            }
                            
                            com.beutystore.pearl.ui.components.ProductCard(
                                product = product,
                                isUserLoggedIn = isUserLoggedIn,
                                onAuthRequired = onAuthRequired,
                                onAddToCart = {
                                    if (isUserLoggedIn) {
                                        if (isInCartSearch) {
                                            cartViewModel?.removeAllFromCart(product.id)
                                        } else {
                                            cartViewModel?.addToCart(product, 1)
                                        }
                                    } else {
                                        onAuthRequired()
                                    }
                                },
                                onProductClick = onProductClick,
                                cartViewModel = cartViewModel
                            )
                        }
                    }
                }
            }
        } else {
            // Обычный контент главного экрана
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

        Spacer(modifier = Modifier.height(8.dp))

        // Skin Test Banner (if test not completed)
        if (testResult == null) {
            SkinTestBanner(onClick = onSkinTestClick)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Promo Banner
        PromoBanner()

        Spacer(modifier = Modifier.height(16.dp))

        // AI Consultant Banner
        AIConsultantBanner(onClick = onAIConsultantClick)

        Spacer(modifier = Modifier.height(24.dp))

        // Recommended Products Section (based on skin test)
        val currentTestResult = testResult
        if (currentTestResult != null && recommendedProducts.isNotEmpty()) {
            RecommendedProductsSection(
                products = recommendedProducts,
                onProductClick = onProductClick,
                skinType = currentTestResult.skinType,
                primaryNeed = currentTestResult.primaryNeed,
                cartViewModel = cartViewModel,
                favoritesViewModel = favoritesViewModel,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Popular Products Section
        if (isLoading && popularProducts.isEmpty()) {
            // Показываем индикатор загрузки
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PopularProductsSection(
                products = popularProducts,
                onProductClick = onProductClick,
                cartViewModel = cartViewModel,
                favoritesViewModel = favoritesViewModel,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Special Offers Section
        if (specialOffers.isNotEmpty()) {
            SpecialOffersSection(
                products = specialOffers,
                onProductClick = onProductClick,
                cartViewModel = cartViewModel,
                favoritesViewModel = favoritesViewModel,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ModernHomeHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PearlRed.copy(alpha = 0.15f),
                        PearlPeach.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Greeting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Привет! 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Что ищем сегодня?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar with TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Поиск товаров...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PearlRed
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PearlRed,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            singleLine = true
        )
    }
}

@Composable
private fun HomeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Добро пожаловать в Pearl",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = PearlRed
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Косметика и уход за собой",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PromoBanner() {
    // Получаем цвета темы
    val primaryColor = PearlRed
    val secondaryColor = PearlPeach
    val tertiaryColor = PearlCrimson
    
    val promoBanners = remember(primaryColor, secondaryColor, tertiaryColor) {
        listOf(
            PromoBannerData(
                title = "Новая коллекция",
                subtitle = "Премиальная косметика для идеального ухода",
                icon = Icons.Default.ShoppingBag,
                gradientColors = listOf(
                    PearlRed.copy(alpha = 0.4f),
                    PearlPeach.copy(alpha = 0.35f),
                    PearlRed.copy(alpha = 0.4f)
                )
            ),
            PromoBannerData(
                title = "Скидки до 50%",
                subtitle = "На всю косметику категории",
                icon = Icons.Default.LocalOffer,
                gradientColors = listOf(
                    PearlPeach.copy(alpha = 0.4f),
                    PearlRed.copy(alpha = 0.35f),
                    PearlPeach.copy(alpha = 0.38f)
                )
            ),
            PromoBannerData(
                title = "Бесплатная доставка",
                subtitle = "При заказе косметики от 2000 ₽",
                icon = Icons.Default.LocalShipping,
                gradientColors = listOf(
                    PearlPeach.copy(alpha = 0.4f),
                    PearlRed.copy(alpha = 0.38f),
                    PearlPeach.copy(alpha = 0.35f)
                )
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { promoBanners.size })

    // Auto-scroll
    LaunchedEffect(pagerState.currentPage) {
        while (true) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % promoBanners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            ) { page ->
                val banner = promoBanners[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = banner.gradientColors
                            )
                        )
                ) {
                    // Фоновое изображение, если есть
                    val hasImage = banner.imageUrl != null || banner.drawableResId != null
                    
                    if (banner.drawableResId != null) {
                        // Локальное изображение из ресурсов
                        Image(
                            painter = painterResource(id = banner.drawableResId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Затемнение для лучшей читаемости текста
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                    } else if (banner.imageUrl != null) {
                        // Изображение по URL
                        AsyncImage(
                            model = banner.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Затемнение для лучшей читаемости текста
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                    }
                    
                    // Контент с текстом поверх изображения
                    if (hasImage) {
                        // Для баннеров с изображением - текст по центру
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = banner.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = PearlWhite,
                                modifier = Modifier.padding(bottom = 8.dp),
                                lineHeight = 32.sp
                            )
                            Text(
                                text = banner.subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = PearlWhite.copy(alpha = 0.95f),
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Для баннеров без изображения - стандартный layout
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Иконка (если есть)
                            if (banner.icon != null) {
                                Icon(
                                    imageVector = banner.icon,
                                    contentDescription = null,
                                    tint = PearlDarkRed,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                                
                            // Текст
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = banner.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PearlDarkRed
                                )
                                Text(
                                    text = banner.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PearlDarkRed.copy(alpha = 0.85f),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // Page indicators overlay
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(promoBanners.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        primaryColor
                    } else {
                        primaryColor.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color)
                            .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp)
                    )
                }
            }
        }
    }
}

private data class PromoBannerData(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val gradientColors: List<Color>,
    val imageUrl: String? = null,
    val drawableResId: Int? = null
)

@Composable
private fun SkinTestBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PearlRed.copy(alpha = 0.9f),
                        PearlPeach.copy(alpha = 0.7f),
                        PearlCrimson.copy(alpha = 0.8f)
                    )
                )
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "✨ Пройдите тест кожи",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PearlWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Получите персональные рекомендации",
                    style = MaterialTheme.typography.bodySmall,
                    color = PearlWhite.copy(alpha = 0.9f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PearlRed
                )
            ) {
                Text(
                    "Начать",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AIConsultantBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PearlCrimson.copy(alpha = 0.9f),
                        PearlRed.copy(alpha = 0.8f),
                        PearlPeach.copy(alpha = 0.7f)
                    )
                )
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PearlWhite.copy(alpha = 0.3f),
                                    PearlWhite.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = PearlWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "🤖 AI-Консультант",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PearlWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Получите персональные советы по косметике",
                        style = MaterialTheme.typography.bodySmall,
                        color = PearlWhite.copy(alpha = 0.9f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PearlRed
                )
            ) {
                Text(
                    "Чат",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun PopularProductsSection(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    cartViewModel: com.beutystore.pearl.ui.viewmodel.CartViewModel? = null,
    favoritesViewModel: com.beutystore.pearl.ui.viewmodel.FavoritesViewModel? = null,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {}
) {
    if (products.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Популярные товары",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                // Отслеживаем состояние корзины для каждого товара
                val isInCart = if (cartViewModel != null) {
                    cartViewModel.cartItems.collectAsState().value.containsKey(product.id)
                } else {
                    false
                }
                
                // Отслеживаем состояние избранного для каждого товара
                val isFavorite = if (favoritesViewModel != null) {
                    favoritesViewModel.favorites.collectAsState().value.contains(product.id)
                } else {
                    false
                }
                
                RecommendedProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                    onAddToCart = {
                        if (isUserLoggedIn) {
                            if (isInCart) {
                                cartViewModel?.removeAllFromCart(product.id)
                            } else {
                                cartViewModel?.addToCart(product, 1)
                            }
                        } else {
                            onAuthRequired()
                        }
                    },
                    onToggleFavorite = {
                        if (isUserLoggedIn) {
                            favoritesViewModel?.toggleFavorite(product)
                        } else {
                            onAuthRequired()
                        }
                    },
                    isFavorite = isFavorite,
                    isInCart = isInCart
                )
            }
        }
    }
}

@Composable
private fun PopularProductItem(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(280.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Image with badge
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                val imageUrl = remember(product.id, product.imageUrl) {
                    if (product.imageUrl.isNotBlank()) {
                        product.imageUrl
                    } else {
                        "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80&fit=crop"
                    }
                }
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onError = { error ->
                        android.util.Log.e("HomeScreen", "Ошибка загрузки изображения для ${product.name}: ${error.result.throwable.message}")
                    }
                )
                
                // Discount badge
                if (product.originalPrice != null) {
                    val discount = ((product.originalPrice - product.price) * 100 / product.originalPrice).toInt()
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "-$discount%",
                            style = MaterialTheme.typography.labelSmall,
                            color = PearlWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brand
            if (product.brand != null) {
                Text(
                    text = product.brand,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFA000),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "%.1f".format(product.rating),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${product.reviewCount})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price
            if (product.originalPrice != null) {
                Column {
                    Text(
                        text = "${product.price} ₽",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PearlRed
                    )
                    Text(
                        text = "${product.originalPrice} ₽",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                }
            } else {
                Text(
                    text = "${product.price} ₽",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PearlRed
                )
            }
        }
    }
}

@Composable
private fun SpecialOffersSection(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    cartViewModel: com.beutystore.pearl.ui.viewmodel.CartViewModel? = null,
    favoritesViewModel: com.beutystore.pearl.ui.viewmodel.FavoritesViewModel? = null,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {}
) {
    if (products.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Специальные предложения",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            products.forEach { product ->
                // Отслеживаем состояние корзины и избранного для каждого товара
                val isInCart = if (cartViewModel != null) {
                    cartViewModel.cartItems.collectAsState().value.containsKey(product.id)
                } else {
                    false
                }
                
                val isFavorite = if (favoritesViewModel != null) {
                    favoritesViewModel.favorites.collectAsState().value.contains(product.id)
                } else {
                    false
                }
                
                SpecialOfferItem(
                    product = product,
                    onClick = { onProductClick(product) },
                    onAddToCart = {
                        if (isUserLoggedIn) {
                            if (isInCart) {
                                cartViewModel?.removeAllFromCart(product.id)
                            } else {
                                cartViewModel?.addToCart(product, 1)
                            }
                        } else {
                            onAuthRequired()
                        }
                    },
                    onToggleFavorite = {
                        if (isUserLoggedIn) {
                            favoritesViewModel?.toggleFavorite(product)
                        } else {
                            onAuthRequired()
                        }
                    },
                    isFavorite = isFavorite,
                    isInCart = isInCart
                )
            }
        }
    }
}

@Composable
private fun SpecialOfferItem(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    isInCart: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Product Image
            Box {
                val imageUrl = remember(product.id, product.imageUrl) {
                    if (product.imageUrl.isNotBlank()) {
                        product.imageUrl
                    } else {
                        "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80&fit=crop"
                    }
                }
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onError = { error ->
                        android.util.Log.e("HomeScreen", "Ошибка загрузки изображения для ${product.name}: ${error.result.throwable.message}")
                    }
                )
                
                // Discount badge
                if (product.originalPrice != null) {
                    val discount = ((product.originalPrice - product.price) * 100 / product.originalPrice).toInt()
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "-$discount%",
                            style = MaterialTheme.typography.labelSmall,
                            color = PearlWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                // Brand
                if (product.brand != null) {
                    Text(
                        text = product.brand,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Product Name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price with discount
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${product.price} ₽",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PearlRed
                    )

                    if (product.originalPrice != null) {
                        Text(
                            text = "${product.originalPrice} ₽",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
            }

            // Кнопки избранного и корзины
            Column(
                modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка избранного
                IconButton(
                    onClick = { onToggleFavorite() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Кнопка корзины
                IconButton(
                    onClick = { onAddToCart() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = if (isInCart) "Удалить из корзины" else "Добавить в корзину",
                        tint = if (isInCart) PearlRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedProductsSection(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    skinType: com.beutystore.pearl.data.model.SkinType,
    primaryNeed: com.beutystore.pearl.data.model.SkinNeed,
    cartViewModel: com.beutystore.pearl.ui.viewmodel.CartViewModel? = null,
    favoritesViewModel: com.beutystore.pearl.ui.viewmodel.FavoritesViewModel? = null,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Рекомендации для вас",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Тип кожи: ${skinType.displayName} • ${primaryNeed.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(products) { product ->
                // Отслеживаем состояние корзины и избранного для каждого товара
                val cartItems = if (cartViewModel != null) {
                    cartViewModel.cartItems.collectAsState().value
                } else {
                    emptyMap<Int, com.beutystore.pearl.ui.viewmodel.CartViewModel.CartItem>()
                }
                val favoriteProducts = if (favoritesViewModel != null) {
                    favoritesViewModel.favoriteProducts.collectAsState().value
                } else {
                    emptyList<Product>()
                }
                
                val isInCart = cartItems.containsKey(product.id)
                val isFavorite = favoriteProducts.any { it.id == product.id }
                
                RecommendedProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                    onAddToCart = {
                        if (isUserLoggedIn) {
                            cartViewModel?.addToCart(product, 1)
                        } else {
                            onAuthRequired()
                        }
                    },
                    onToggleFavorite = {
                        if (isUserLoggedIn) {
                            favoritesViewModel?.toggleFavorite(product)
                        } else {
                            onAuthRequired()
                        }
                    },
                    isFavorite = isFavorite,
                    isInCart = isInCart
                )
            }
        }
    }
}

@Composable
private fun RecommendedProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    isInCart: Boolean = false
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(260.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable(onClick = onClick)
            ) {
                val imageUrl = remember(product.id, product.imageUrl) {
                    if (product.imageUrl.isNotBlank()) {
                        product.imageUrl
                    } else {
                        "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80&fit=crop"
                    }
                }
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onError = { error ->
                        android.util.Log.e("HomeScreen", "Ошибка загрузки изображения для ${product.name}: ${error.result.throwable.message}")
                    }
                )
                
                // Кнопка избранного (вверху справа)
                FloatingActionButton(
                    onClick = { onToggleFavorite() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp),
                    containerColor = Color.White,
                    contentColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Кнопка добавления в корзину (внизу справа)
                FloatingActionButton(
                    onClick = { onAddToCart() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp),
                    containerColor = if (isInCart) PearlPeach.copy(alpha = 0.3f)
                                    else PearlRed,
                    contentColor = if (isInCart) MaterialTheme.colorScheme.onSecondaryContainer 
                                  else MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = if (isInCart) "В корзине" else "Добавить в корзину",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Бейдж скидки
                if (product.originalPrice != null) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "-${((1 - product.price.toFloat() / product.originalPrice) * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.height(40.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f".format(product.rating),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${product.reviewCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    if (product.originalPrice != null) {
                        Text(
                            text = "${product.originalPrice} ₽",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                    Text(
                        text = "${product.price} ₽",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PearlRed
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PearlTheme {
        val skinTestViewModel: SkinTestViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        HomeScreen(
            onProductClick = {},
            onSkinTestClick = {},
            onAIConsultantClick = {},
            skinTestViewModel = skinTestViewModel
        )
    }
}