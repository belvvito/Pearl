package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.data.utils.SearchUtils
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed

import com.beutystore.pearl.ui.components.ProductCard
import com.beutystore.pearl.ui.viewmodel.CartViewModel
import com.beutystore.pearl.data.repository.ProductRepository
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beutystore.pearl.ui.viewmodel.ProductsViewModel

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    cartViewModel: CartViewModel? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val isSearching = searchQuery.isNotBlank()

    // Загружаем товары для поиска
    var allProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoadingProducts by remember { mutableStateOf(true) }
    
    // Загружаем категории
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingCategories by remember { mutableStateOf(true) }
    
    val productRepository = remember { ProductRepository() }
    val apiService = com.beutystore.pearl.data.api.RetrofitInstance.api

    // Загрузка категорий при первом запуске
    LaunchedEffect(Unit) {
        isLoadingCategories = true
        try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body() != null) {
                categories = response.body()!!
                android.util.Log.d("CatalogScreen", "Загружено категорий: ${categories.size}")
            } else {
                android.util.Log.e("CatalogScreen", "Ошибка загрузки категорий: ${response.message()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("CatalogScreen", "Ошибка загрузки категорий: ${e.message}", e)
        } finally {
            isLoadingCategories = false
        }
    }

    // Загрузка товаров при первом запуске
    LaunchedEffect(Unit) {
        isLoadingProducts = true
        productRepository.getProducts().fold(
            onSuccess = { products ->
                allProducts = products
                isLoadingProducts = false
                android.util.Log.d("CatalogScreen", "Загружено товаров: ${products.size}")
            },
            onFailure = { exception ->
                android.util.Log.e("CatalogScreen", "Ошибка загрузки товаров: ${exception.message}", exception)
                isLoadingProducts = false
            }
        )
    }
    
    // ОПТИМИЗАЦИЯ: derivedStateOf для фильтрации товаров
    val filteredProducts by remember(searchQuery, allProducts) {
        derivedStateOf {
            if (searchQuery.isBlank()) allProducts
            else SearchUtils.searchProducts(allProducts, searchQuery)
        }
    }

    // Определяем фон: светло-лавандовый для светлой темы, темный для темной
    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlLightPeach
    } else {
        MaterialTheme.colorScheme.background
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Поиск товаров...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Header
        Text(
            text = if (isSearching) "Результаты поиска" else "Каталог",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isSearching) {
            Text(
                text = "Найдено товаров: ${filteredProducts.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Показываем категории, если не идет поиск
        if (!isSearching && categories.isNotEmpty()) {
            Text(
                text = "Категории",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Отображаем категории в виде горизонтальной прокрутки
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories) { category ->
                    CategoryGridItem(
                        name = category,
                        modifier = Modifier.width(160.dp),
                        onClick = { onCategoryClick(category) }
                    )
                }
            }
            Text(
                text = "Все товары",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Список товаров
        if (isLoadingProducts) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (isSearching && filteredProducts.isEmpty()) {
            EmptySearchState(
                message = "Товары не найдены",
                hint = "Попробуйте изменить поисковый запрос"
            )
        } else if (!isSearching && allProducts.isEmpty()) {
            EmptyProductsState(isSearching = false)
        } else {
            ProductsList(
                products = if (isSearching) filteredProducts else allProducts,
                isSearching = isSearching,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                onProductClick = onProductClick,
                cartViewModel = cartViewModel
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(25.dp)
            ),
        shape = RoundedCornerShape(25.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = PearlRed.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PearlRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            // Используем BasicTextField для полностью прозрачного поля ввода
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                        
                        if (query.isNotBlank()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun CategoriesGrid(
    categories: List<String>,
    isSearching: Boolean,
    onCategoryClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (categories.isEmpty() && isSearching) {
            item {
                EmptySearchState(
                    message = "Категории не найдены",
                    hint = "Попробуйте изменить поисковый запрос"
                )
            }
        } else {
            items(
                items = categories.chunked(2),
                key = { it.joinToString() }
            ) { rowItems ->
                CategoriesRow(
                    categories = rowItems,
                    onCategoryClick = onCategoryClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CategoriesRow(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categories.forEach { category ->
            CategoryGridItem(
                name = category,
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryGridItem(
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val imageUrl = remember(name) {
        getCategoryImageUrl(name)
    }

    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80&fit=crop" },
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = androidx.compose.ui.graphics.painter.ColorPainter(
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Text(
                text = name,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                color = PearlWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProductListScreen(
    modifier: Modifier = Modifier,
    categoryName: String,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    cartViewModel: CartViewModel? = null,
    productsViewModel: ProductsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val isSearching = searchQuery.isNotBlank()

    // Загружаем продукты из API
    var allProducts by remember { mutableStateOf(emptyList<Product>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val productRepository = remember { ProductRepository() }

    LaunchedEffect(categoryName) {
        isLoading = true
        error = null
        
        val categoryFilter = if (categoryName == "Все товары" || categoryName.isEmpty()) {
            null
        } else {
            categoryName
        }
        
        productRepository.getProducts(category = categoryFilter).fold(
            onSuccess = { products ->
                allProducts = products
                isLoading = false
                android.util.Log.d("ProductListScreen", "Загружено продуктов: ${products.size} для категории: $categoryName")
            },
            onFailure = { exception ->
                val errorMsg = exception.message ?: "Ошибка загрузки товаров"
                error = errorMsg
                isLoading = false
                android.util.Log.e("ProductListScreen", "Ошибка загрузки товаров для категории $categoryName: $errorMsg", exception)
            }
        )
    }

    // ОПТИМИЗАЦИЯ: derivedStateOf для фильтрации
    val filteredProducts by remember(searchQuery, allProducts) {
        derivedStateOf {
            if (searchQuery.isBlank()) allProducts
            else SearchUtils.searchProducts(allProducts, searchQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button and search
        ProductListHeader(
            categoryName = categoryName,
            onBackClick = onBackClick,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            showSearch = allProducts.isNotEmpty()
        )

        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Ошибка загрузки",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = error ?: "Неизвестная ошибка",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // Products count
            Text(
                text = if (isSearching) {
                    "Найдено товаров: ${filteredProducts.size}"
                } else {
                    "${allProducts.size} товаров"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Products list с оптимизацией
            ProductsList(
                products = filteredProducts,
                isSearching = isSearching,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                onProductClick = onProductClick,
                cartViewModel = cartViewModel
            )
        }
    }
}

@Composable
private fun ProductListHeader(
    categoryName: String,
    onBackClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearch: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        // Search bar in ProductListScreen
        if (showSearch) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Поиск товаров...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ProductsList(
    products: List<Product>,
    isSearching: Boolean,
    isUserLoggedIn: Boolean,
    onAuthRequired: () -> Unit,
    onProductClick: (Product) -> Unit,
    cartViewModel: CartViewModel? = null
) {
    if (products.isEmpty()) {
        EmptyProductsState(isSearching = isSearching)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(
                items = products,
                key = { it.id }
            ) { product ->
                ProductCard(
                    product = product,
                    isUserLoggedIn = isUserLoggedIn,
                    onAuthRequired = onAuthRequired,
                    onAddToCart = {
                        if (isUserLoggedIn) {
                            cartViewModel?.addToCart(product, 1)
                        } else {
                            onAuthRequired()
                        }
                    },
                    onProductClick = onProductClick,
                    cartViewModel = cartViewModel
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EmptySearchState(
    message: String,
    hint: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔍",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyProductsState(isSearching: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSearching) "😔" else "📦",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = if (isSearching) "Товары не найдены" else "Товары отсутствуют",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (isSearching) {
                "Попробуйте изменить поисковый запрос"
            } else {
                "В этой категории пока нет товаров"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// Вспомогательные функции для получения реальных изображений категорий из Unsplash
private fun getCategoryImageUrl(category: String): String {
    return when (category) {
        "Все товары" -> "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80&fit=crop"
        "Уход за лицом" -> "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80&fit=crop"
        "Декоративная косметика" -> "https://images.unsplash.com/photo-1571781926291-c477ebfd024b?w=500&q=80&fit=crop"
        "Волосы" -> "https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=500&q=80&fit=crop"
        "Парфюмерия" -> "https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=500&q=80&fit=crop"
        "Тело" -> "https://images.unsplash.com/photo-1631729670470-1df5e9888c15?w=500&q=80&fit=crop"
        "Мужская косметика" -> "https://images.unsplash.com/photo-1556228577-8ed324c4f5ab?w=500&q=80&fit=crop"
        "Аксессуары" -> "https://images.unsplash.com/photo-1589666564452-e94edaddd8f5?w=500&q=80&fit=crop"
        else -> "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80&fit=crop"
    }
}


@Preview(showBackground = true)
@Composable
fun CatalogScreenPreview() {
    PearlTheme {
        CatalogScreen(
            isUserLoggedIn = false,
            onAuthRequired = {},
            onCategoryClick = {},
            onProductClick = {}
        )
    }
}