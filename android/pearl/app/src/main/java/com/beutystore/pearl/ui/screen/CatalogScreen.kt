package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onProductClick: (Product) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val isSearching = searchQuery.isNotBlank()

    // ОПТИМИЗАЦИЯ: Простой список категорий
    val categories = remember {
        listOf(
            "Все товары",
            "Уход за лицом",
            "Декоративная косметика",
            "Волосы",
            "Парфюмерия",
            "Тело",
            "Мужская косметика",
            "Аксессуары"
        )
    }

    // ОПТИМИЗАЦИЯ: derivedStateOf для фильтрации
    val filteredCategories by remember(searchQuery, categories) {
        derivedStateOf {
            if (searchQuery.isBlank()) categories
            else SearchUtils.searchCategories(categories, searchQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Поиск категорий...",
            modifier = Modifier.padding(16.dp)
        )

        // Categories header
        Text(
            text = if (isSearching) "Результаты поиска" else "Категории",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (isSearching) {
            Text(
                text = "Найдено категорий: ${filteredCategories.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Categories grid с оптимизацией
        CategoriesGrid(
            categories = filteredCategories,
            isSearching = isSearching,
            onCategoryClick = onCategoryClick
        )
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(placeholder) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(
                            onClick = { onQueryChange("") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
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
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
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
    onProductClick: (Product) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val isSearching = searchQuery.isNotBlank()

    // ОПТИМИЗАЦИЯ: Ленивая загрузка товаров
    var allProducts by remember { mutableStateOf(emptyList<Product>()) }

    LaunchedEffect(categoryName) {
        if (allProducts.isEmpty()) {
            allProducts = createProductsByCategory(categoryName)
        }
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
            onProductClick = onProductClick
        )
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
                    contentDescription = "Назад"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
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
    onProductClick: (Product) -> Unit
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
                            // TODO: Реализовать логику добавления
                        } else {
                            onAuthRequired()
                        }
                    },
                    onProductClick = onProductClick
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

// Вспомогательные функции
private fun getCategoryImageUrl(category: String): String {
    return when (category) {
        "Все товары" -> "https://via.placeholder.com/300x150/87CEEB/FFFFFF?text=Все+товары"
        "Уход за лицом" -> "https://via.placeholder.com/300x150/FFB6C1/FFFFFF?text=Уход+за+лицом"
        "Декоративная косметика" -> "https://via.placeholder.com/300x150/E6E6FA/FFFFFF?text=Макияж"
        "Волосы" -> "https://via.placeholder.com/300x150/F5F5DC/FFFFFF?text=Волосы"
        "Парфюмерия" -> "https://via.placeholder.com/300x150/FFF0F5/FFFFFF?text=Парфюмерия"
        "Тело" -> "https://via.placeholder.com/300x150/F8F8FF/FFFFFF?text=Тело"
        "Мужская косметика" -> "https://via.placeholder.com/300x150/FAF0E6/FFFFFF?text=Мужская"
        "Аксессуары" -> "https://via.placeholder.com/300x150/87CEEB/FFFFFF?text=Аксессуары"
        else -> "https://via.placeholder.com/300x150/87CEEB/FFFFFF?text=$category"
    }
}

// ОПТИМИЗАЦИЯ: Упрощенные функции создания товаров
private fun createProductsByCategory(categoryName: String): List<Product> {
    return when (categoryName) {
        "Уход за лицом" -> List(2) { createSimpleProduct(it, "Уход за лицом") }
        "Декоративная косметика" -> List(2) { createSimpleProduct(it, "Декоративная косметика") }
        "Волосы" -> List(1) { createSimpleProduct(it, "Волосы") }
        "Парфюмерия" -> List(1) { createSimpleProduct(it, "Парфюмерия") }
        "Тело" -> List(1) { createSimpleProduct(it, "Тело") }
        "Мужская косметика" -> List(1) { createSimpleProduct(it, "Мужская косметика") }
        "Аксессуары" -> List(1) { createSimpleProduct(it, "Аксессуары") }
        else -> List(3) { createSimpleProduct(it, "Все товары") }
    }
}

private fun createSimpleProduct(index: Int, category: String): Product {
    return Product(
        id = index + 1,
        name = "$category товар ${index + 1}",
        price = 1000 + index * 200,
        originalPrice = if (index % 2 == 0) 1500 + index * 300 else null,
        imageUrl = "https://via.placeholder.com/150",
        description = "Качественный косметический продукт категории $category",
        category = category,
        brand = when (index % 3) {
            0 -> "L'Oreal"
            1 -> "Maybelline"
            else -> "Pearl"
        },
        rating = 4.0f + (index % 5) * 0.1f,
        reviewCount = 20 + index * 3,
        inStock = true,
        features = listOf("Качество", "Надежность"),
        colors = listOf("Стандартный"),
        sizes = listOf("50 мл")
    )
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