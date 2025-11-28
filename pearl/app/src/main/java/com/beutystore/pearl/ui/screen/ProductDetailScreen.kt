// ui/screen/ProductDetailScreen.kt
package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import coil.compose.AsyncImage
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.data.model.Review
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlCoralPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlPeach
import com.beutystore.pearl.ui.viewmodel.FavoritesViewModel
import com.beutystore.pearl.ui.viewmodel.ReviewsViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProductDetailScreen(
    product: Product,
    isUserLoggedIn: Boolean = false,
    onBackClick: () -> Unit = {},
    onAuthRequired: () -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onAddToFavorites: (Product) -> Unit = {},
    favoritesViewModel: FavoritesViewModel? = null,
    cartViewModel: com.beutystore.pearl.ui.viewmodel.CartViewModel? = null,
    reviewsViewModel: ReviewsViewModel = viewModel()
) {
    var selectedSize by remember { mutableStateOf(product.sizes.firstOrNull() ?: "") }
    var quantity by remember { mutableStateOf(1) }
    
    // Наблюдаем за состоянием избранного
    val isFavorite = if (favoritesViewModel != null) {
        favoritesViewModel.favorites.collectAsState().value.contains(product.id)
    } else {
        false
    }
    
    // Проверяем, находится ли товар в корзине
    val isInCart = if (cartViewModel != null) {
        cartViewModel.cartItems.collectAsState().value.containsKey(product.id)
    } else {
        false
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
            .verticalScroll(rememberScrollState())
    ) {
        // Header with image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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
                    android.util.Log.e("ProductDetailScreen", "Ошибка загрузки изображения для ${product.name}: ${error.result.throwable.message}")
                }
            )

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Actions
                val context = LocalContext.current
                Row {
                    // Share button
                    IconButton(
                        onClick = {
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Посмотрите этот товар: ${product.name}\n${product.description}\nЦена: ${product.price} ₽"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Поделиться товаром"))
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Favorite button
                    IconButton(
                        onClick = {
                            if (isUserLoggedIn) {
                                favoritesViewModel?.toggleFavorite(product)
                            } else {
                                onAuthRequired()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Product info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Brand and name
            if (product.brand != null) {
                Text(
                    text = product.brand,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Rating
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = PearlRed,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "%.1f".format(product.rating),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Text(
                    text = "(${product.reviewCount} отзывов)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Price
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.price} ₽",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PearlRed
                )

                product.originalPrice?.let { originalPrice ->
                    Text(
                        text = "$originalPrice ₽",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Description
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Features
            if (product.features.isNotEmpty()) {
                Text(
                    text = "Особенности:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    product.features.forEach { feature ->
                        Text(
                            text = "• $feature",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            // Size selection
            if (product.sizes.isNotEmpty()) {
                Text(
                    text = "Размер:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp)
                )
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(product.sizes) { size ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = { selectedSize = size },
                            label = { Text(size) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PearlRed,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Quantity selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Количество:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                QuantitySelector(
                    quantity = quantity,
                    onQuantityChange = { newQuantity -> quantity = newQuantity }
                )
            }

            // Add to cart button
            Button(
                onClick = {
                    if (isUserLoggedIn) {
                        if (isInCart) {
                            cartViewModel?.removeAllFromCart(product.id)
                        } else {
                            cartViewModel?.addToCart(product, quantity)
                        }
                    } else {
                        onAuthRequired()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInCart) {
                        PearlPeach.copy(alpha = 0.3f)
                    } else {
                        PearlRed
                    },
                    contentColor = if (isInCart) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = product.inStock
            ) {
                Text(
                    text = when {
                        !product.inStock -> "Нет в наличии"
                        isInCart -> "Товар добавлен"
                        else -> "Добавить в корзину - ${product.price * quantity} ₽"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Delivery info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Доставка и возврат",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Бесплатная доставка от 2000 ₽\n• Доставка за 1-2 дня\n• Легкий возврат в течение 14 дней",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Reviews section
            ReviewsSection(
                productId = product.id,
                isUserLoggedIn = isUserLoggedIn,
                onAuthRequired = onAuthRequired,
                reviewsViewModel = reviewsViewModel
            )
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ReviewsSection(
    productId: Int,
    isUserLoggedIn: Boolean,
    onAuthRequired: () -> Unit,
    reviewsViewModel: ReviewsViewModel,
    accessToken: String? = null
) {
    val reviews by reviewsViewModel.reviews.collectAsState()
    val productReviews = reviews[productId] ?: emptyList()
    val canReview by reviewsViewModel.canReview.collectAsState()
    val canReviewProduct = canReview[productId]?.can_review ?: false
    
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var expandedReviews by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    LaunchedEffect(productId) {
        reviewsViewModel.loadReviewsForProduct(productId)
        if (isUserLoggedIn && accessToken != null) {
            reviewsViewModel.checkCanReviewProduct(productId, accessToken)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Отзывы (${productReviews.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isUserLoggedIn && canReviewProduct) {
                TextButton(onClick = { showAddReviewDialog = true }) {
                    Text("Написать отзыв")
                }
            }
        }

        if (productReviews.isNotEmpty()) {
            val averageRating = productReviews.map { it.rating }.average()
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.1f", averageRating),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < averageRating.toInt()) {
                                PearlRed
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            val reviewsToShow = if (expandedReviews) productReviews else productReviews.take(3)
            
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                reviewsToShow.forEach { review ->
                    ReviewCard(
                        review = review,
                        onHelpfulClick = {
                            if (isUserLoggedIn && accessToken != null) {
                                reviewsViewModel.likeReview(review.id, productId, accessToken)
                            }
                        }
                    )
                }
            }

            if (productReviews.size > 3) {
                TextButton(
                    onClick = { expandedReviews = !expandedReviews },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (expandedReviews) "Свернуть отзывы" else "Показать все отзывы (${productReviews.size})")
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Пока нет отзывов",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (isUserLoggedIn && canReviewProduct) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddReviewDialog = true }) {
                            Text("Написать первый отзыв")
                        }
                    } else if (isUserLoggedIn && !canReviewProduct) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Вы можете оставить отзыв только на купленный товар",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showAddReviewDialog && accessToken != null) {
        AddReviewDialog(
            productId = productId,
            onDismiss = { showAddReviewDialog = false },
            onReviewAdded = { orderId, rating, comment ->
                reviewsViewModel.createReview(
                    productId = productId,
                    orderId = orderId,
                    rating = rating,
                    comment = comment,
                    title = null,
                    accessToken = accessToken,
                    onSuccess = {
                        showAddReviewDialog = false
                    },
                    onError = { error ->
                        // Показываем ошибку пользователю
                    }
                )
            },
            isUserLoggedIn = isUserLoggedIn,
            onAuthRequired = onAuthRequired,
            reviewsViewModel = reviewsViewModel,
            accessToken = accessToken
        )
    }
}

@Composable
fun ReviewCard(
    review: Review,
    onHelpfulClick: () -> Unit
) {
    var isHelpful by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.userName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (review.isVerifiedPurchase) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge {
                                Text("✓", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < review.rating) {
                                    PearlRed
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = review.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (!isHelpful) {
                            isHelpful = true
                            onHelpfulClick()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Полезно",
                        tint = if (isHelpful) PearlRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "${review.helpfulCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AddReviewDialog(
    productId: Int,
    onDismiss: () -> Unit,
    onReviewAdded: (Int, Int, String) -> Unit,
    isUserLoggedIn: Boolean,
    onAuthRequired: () -> Unit,
    reviewsViewModel: ReviewsViewModel,
    accessToken: String
) {
    if (!isUserLoggedIn) {
        onAuthRequired()
        onDismiss()
        return
    }

    // Загружаем доступные заказы
    LaunchedEffect(productId) {
        reviewsViewModel.checkCanReviewProduct(productId, accessToken)
    }
    
    val canReview by reviewsViewModel.canReview.collectAsState()
    val canReviewData = canReview[productId]
    val availableOrders = canReviewData?.available_orders ?: emptyList()
    
    var selectedOrderId by remember { mutableStateOf<Int?>(null) }
    var selectedRating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    
    // Автоматически выбираем первый заказ, если есть
    LaunchedEffect(availableOrders) {
        if (availableOrders.isNotEmpty() && selectedOrderId == null) {
            selectedOrderId = availableOrders.first().id
        }
    }

    if (availableOrders.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Невозможно оставить отзыв") },
            text = {
                Text(
                    text = "Вы можете оставить отзыв только на купленный и доставленный товар. Пожалуйста, сначала купите товар и дождитесь его доставки.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Понятно")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Написать отзыв") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Выбор заказа
                    Text(
                        text = "Выберите заказ",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    availableOrders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOrderId = order.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOrderId == order.id,
                                onClick = { selectedOrderId = order.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Заказ №${order.order_number}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Дата: ${order.date}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Сумма: ${order.total_amount} ₽",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Оценка
                    Text(
                        text = "Оцените товар",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { selectedRating = index + 1 }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "${index + 1} звезд",
                                    tint = if (index < selectedRating) {
                                        PearlRed
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    },
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    
                    // Комментарий
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Ваш отзыв") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6,
                        isError = comment.isBlank() && comment.isNotEmpty()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedOrderId != null && selectedRating > 0 && comment.isNotBlank()) {
                            onReviewAdded(selectedOrderId!!, selectedRating, comment)
                        }
                    },
                    enabled = selectedOrderId != null && selectedRating > 0 && comment.isNotBlank()
                ) {
                    Text("Отправить")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    val sampleProduct = Product(
        id = 1,
        name = "Увлажняющий крем для лица с гиалуроновой кислотой",
        price = 2500,
        originalPrice = 3000,
        imageUrl = "https://via.placeholder.com/400",
        description = "Интенсивно увлажняющий крем с гиалуроновой кислотой для сияния и упругости кожи. Подходит для всех типов кожи, обеспечивает 24-часовое увлажнение.",
        category = "Уход за лицом",
        brand = "L'Oreal",
        rating = 4.7f,
        reviewCount = 128,
        inStock = true,
        features = listOf(
            "Глубокое увлажнение на 24 часа",
            "Подходит для чувствительной кожи",
            "Нежирная текстура",
            "SPF 15"
        ),
        colors = listOf("Белый", "Прозрачный"),
        sizes = listOf("50 мл", "100 мл")
    )

    PearlTheme {
        ProductDetailScreen(
            product = sampleProduct,
            isUserLoggedIn = true,
            onBackClick = {},
            onAuthRequired = {},
            onAddToCart = {},
            onAddToFavorites = {}
        )
    }
}
