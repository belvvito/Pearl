package com.beutystore.pearl.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.ui.theme.PearlCoralPeach
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlPeach

@Composable
fun ProductCard(
    product: Product,
    isUserLoggedIn: Boolean,
    onAuthRequired: () -> Unit,
    onAddToCart: () -> Unit,
    onProductClick: (Product) -> Unit,
    cartViewModel: com.beutystore.pearl.ui.viewmodel.CartViewModel? = null
) {
    // Логируем URL изображения для отладки
    LaunchedEffect(product.id) {
        android.util.Log.d("ProductCard", "Product: ${product.name}, ImageURL: ${product.imageUrl}")
    }
    val cartItems = if (cartViewModel != null) {
        cartViewModel.cartItems.collectAsState().value
    } else {
        emptyMap<Int, com.beutystore.pearl.ui.viewmodel.CartViewModel.CartItem>()
    }
    val isInCart = cartItems.containsKey(product.id)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Product Image and Info Row - кликабельная область для перехода к товару
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProductClick(product) },
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Image
                val imageUrl = remember(product.id, product.imageUrl) {
                    val url = if (product.imageUrl.isNotBlank()) {
                        product.imageUrl.trim()
                    } else {
                        "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80&fit=crop"
                    }
                    android.util.Log.d("ProductCard", "Загрузка изображения для ${product.name}: $url")
                    url
                }
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onError = { error ->
                        android.util.Log.e("ProductCard", "❌ Ошибка загрузки изображения для ${product.name}")
                        android.util.Log.e("ProductCard", "   URL: $imageUrl")
                        android.util.Log.e("ProductCard", "   Ошибка: ${error.result.throwable.message}")
                        error.result.throwable.printStackTrace()
                    },
                    onSuccess = {
                        android.util.Log.d("ProductCard", "✅ Изображение успешно загружено для ${product.name}")
                    }
                )

                // Product Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Price
                    if (product.originalPrice != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${product.price} ₽",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PearlRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${product.originalPrice} ₽",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textDecoration = TextDecoration.LineThrough
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

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(product.rating),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${product.reviewCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add to Cart Button
            Button(
                onClick = {
                    if (isUserLoggedIn) {
                        onAddToCart()
                    } else {
                        onAuthRequired()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInCart && product.inStock) {
                        PearlPeach.copy(alpha = 0.3f)
                    } else {
                        PearlRed
                    },
                    contentColor = if (isInCart && product.inStock) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            ) {
                Text(
                    text = when {
                        !product.inStock -> "Нет в наличии"
                        isInCart -> "Добавлено"
                        else -> "В корзину"
                    }
                )
            }
        }
    }
}