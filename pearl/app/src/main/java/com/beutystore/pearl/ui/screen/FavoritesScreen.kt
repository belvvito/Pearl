package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.beutystore.pearl.data.model.Product
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlCoralPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.viewmodel.CartViewModel
import com.beutystore.pearl.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onProductClick: (Product) -> Unit = {},
    onBackClick: () -> Unit = {},
    favoritesViewModel: FavoritesViewModel = viewModel(),
    cartViewModel: CartViewModel? = null
) {
    val favoriteProducts by favoritesViewModel.favoriteProducts.collectAsState()
    val favoritesCount by favoritesViewModel.favoritesCount.collectAsState()

    if (!isUserLoggedIn) {
        AuthRequiredState(
            message = "Войдите в аккаунт, чтобы просмотреть избранное",
            onAuthRequired = onAuthRequired
        )
        return
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
        // TopBar
        TopAppBar(
            title = { 
                Text(
                    text = "Избранное",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            if (favoriteProducts.isEmpty()) {
                EmptyFavoritesState()
            } else {
                Text(
                    text = "$favoritesCount ${if (favoritesCount == 1) "товар" else if (favoritesCount < 5) "товара" else "товаров"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(favoriteProducts, key = { it.id }) { product ->
                        FavoriteProductCard(
                            product = product,
                            isFavorite = favoritesViewModel.isFavorite(product.id),
                            onFavoriteClick = {
                                favoritesViewModel.toggleFavorite(product)
                            },
                            onProductClick = {
                                onProductClick(product)
                            },
                            onAddToCart = {
                                if (isUserLoggedIn) {
                                    cartViewModel?.addToCart(product, 1)
                                } else {
                                    onAuthRequired()
                                }
                            },
                            isUserLoggedIn = isUserLoggedIn
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteProductCard(
    product: Product,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit = {},
    isUserLoggedIn: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Image with favorite button - кликабельная область для перехода к товару
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        Modifier.clickable { onProductClick() }
                    )
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Favorite button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Discount badge
                if (product.originalPrice != null) {
                    val discount = ((product.originalPrice - product.price) * 100 / product.originalPrice).toInt()
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "-$discount%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product name - кликабельная область для перехода к товару
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
                    .clickable { onProductClick() }
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

            Spacer(modifier = Modifier.height(8.dp))

            // Add to cart button
            Button(
                onClick = {
                    onAddToCart()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = product.inStock && isUserLoggedIn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PearlRed
                )
            ) {
                Text(
                    text = if (product.inStock) "В корзину" else "Нет в наличии",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = "Пустое избранное",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Избранное пусто",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Добавьте товары в избранное,\nчтобы вернуться к ним позже",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    PearlTheme {
        FavoritesScreen(
            isUserLoggedIn = true,
            onAuthRequired = {},
            onProductClick = {}
        )
    }
}

