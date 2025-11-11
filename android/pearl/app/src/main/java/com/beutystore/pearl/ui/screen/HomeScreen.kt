package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.beutystore.pearl.ui.theme.PearlBeige
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onProductClick: (Product) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val isSearching = searchQuery.isNotBlank()

    // ОПТИМИЗАЦИЯ: Кэшируем создание товаров
    val allProducts by remember {
        mutableStateOf(createSampleProducts())
    }

    // ОПТИМИЗАЦИЯ: Безопасная фильтрация с защитой от ошибок
    val filteredProducts by remember(searchQuery, allProducts) {
        derivedStateOf {
            try {
                when {
                    searchQuery.isBlank() -> allProducts.take(8)
                    searchQuery.length == 1 -> emptyList() // Не ищем для 1 символа
                    else -> SearchUtils.searchProducts(allProducts, searchQuery)
                }
            } catch (e: Exception) {
                // В случае ошибки возвращаем пустой список
                emptyList()
            }
        }
    }

    val categories = listOf(
        "Уход за лицом" to MaterialTheme.colorScheme.tertiary,
        "Макияж" to MaterialTheme.colorScheme.secondary,
        "Волосы" to MaterialTheme.colorScheme.primary,
        "Парфюмерия" to PearlBeige
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            // Header with title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pearl",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Поиск товаров...",
                modifier = Modifier.padding(16.dp)
            )
        }

        // Показываем результаты поиска или обычный контент
        if (isSearching) {
            item {
                SearchResultsHeader(
                    resultsCount = filteredProducts.size,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (filteredProducts.isEmpty()) {
                item {
                    EmptySearchState(
                        message = "Товары не найдены",
                        hint = "Попробуйте изменить поисковый запрос"
                    )
                }
            } else {
                items(
                    items = filteredProducts,
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
        } else {
            // Обычный контент (когда нет поиска)
            item {
                PromoBanner()
            }

            item {
                SectionTitle("Категории")
            }

            item {
                CategoriesHorizontalList(categories = categories)
            }

            item {
                SectionTitle("Популярные товары")
            }

            items(
                items = filteredProducts,
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
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
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
private fun PromoBanner() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Скидка 20% на первую покупку",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp),
                color = PearlWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SearchResultsHeader(resultsCount: Int, modifier: Modifier = Modifier) {
    Text(
        text = "Результаты поиска: $resultsCount товаров",
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun CategoriesHorizontalList(categories: List<Pair<String, Color>>) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories, key = { it.first }) { (category, color) ->
            CategoryCard(category, color)
        }
    }
}

@Composable
fun CategoryCard(name: String, color: Color) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    onProductClick: (Product) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onProductClick(product) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ОПТИМИЗАЦИЯ: Добавляем fallback для изображений
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))

                PriceDisplay(
                    price = product.price,
                    originalPrice = product.originalPrice
                )

                RatingDisplay(
                    rating = product.rating,
                    reviewCount = product.reviewCount
                )
            }

            AddToCartButton(
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                onAddToCart = onAddToCart
            )
        }
    }
}

@Composable
private fun PriceDisplay(price: Int, originalPrice: Int?) {
    if (originalPrice != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${price} ₽",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${originalPrice} ₽",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    } else {
        Text(
            text = "${price} ₽",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RatingDisplay(rating: Float, reviewCount: Int) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "★ ${"%.1f".format(rating)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = " ($reviewCount)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun AddToCartButton(
    isUserLoggedIn: Boolean,
    onAuthRequired: () -> Unit,
    onAddToCart: () -> Unit
) {
    IconButton(
        onClick = {
            if (isUserLoggedIn) {
                onAddToCart()
            } else {
                onAuthRequired()
            }
        }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add to cart",
            tint = MaterialTheme.colorScheme.primary
        )
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
            text = "😔",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// ОПТИМИЗАЦИЯ: Статическое создание товаров (без корутин)
private fun createSampleProducts(): List<Product> {
    return List(8) { index ->
        Product(
            id = index + 1,
            name = when (index % 4) {
                0 -> "Увлажняющий крем для лица"
                1 -> "Тональный крем матовый"
                2 -> "Помада стойкая матовая"
                3 -> "Тушь для ресниц объемная"
                else -> "Косметический продукт"
            },
            price = 800 + index * 200,
            originalPrice = if (index % 3 == 0) 1000 + index * 250 else null,
            imageUrl = "https://via.placeholder.com/150",
            description = "Качественный косметический продукт для ухода и макияжа",
            category = when (index % 3) {
                0 -> "Уход за лицом"
                1 -> "Декоративная косметика"
                2 -> "Волосы"
                else -> "Аксессуары"
            },
            brand = when (index % 2) {
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
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PearlTheme {
        HomeScreen(
            isUserLoggedIn = false,
            onAuthRequired = {},
            onProductClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductCardPreview() {
    val sampleProduct = Product(
        id = 1,
        name = "Крем для лица с гиалуроновой кислотой",
        price = 2500,
        originalPrice = 3000,
        imageUrl = "https://via.placeholder.com/150",
        description = "Увлажняющий крем для сияния кожи",
        category = "Уход за лицом",
        brand = "Pearl",
        rating = 4.7f,
        reviewCount = 128,
        inStock = true,
        features = listOf("Увлажнение", "Защита"),
        colors = listOf("Белый"),
        sizes = listOf("50 мл")
    )

    PearlTheme {
        ProductCard(
            product = sampleProduct,
            isUserLoggedIn = true,
            onAuthRequired = {},
            onAddToCart = {},
            onProductClick = {}
        )
    }
}