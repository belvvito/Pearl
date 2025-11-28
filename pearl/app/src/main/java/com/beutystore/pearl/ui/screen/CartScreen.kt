package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.beutystore.pearl.ui.theme.PearlSoftPeach
import com.beutystore.pearl.ui.viewmodel.CartViewModel

@Composable
fun CartScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onCheckoutClick: () -> Unit = {},
    cartViewModel: CartViewModel = viewModel()
) {
    val cartItems by cartViewModel.cartItemsList.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    val itemCount by cartViewModel.cartItemCount.collectAsState()

    if (!isUserLoggedIn) {
        AuthRequiredState(
            message = "Войдите в аккаунт, чтобы просмотреть корзину",
            onAuthRequired = onAuthRequired
        )
        return
    }

    // Определяем фон: оранжево-персиковый для светлой темы, темный для темной
    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlSoftPeach
    } else {
        MaterialTheme.colorScheme.background
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Text(
            text = "Корзина",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (cartItems.isEmpty()) {
            EmptyCartState()
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems, key = { it.product.id }) { cartItem ->
                    CartItemComponent(
                        cartItem = cartItem,
                        onAdd = {
                            cartViewModel.addToCart(cartItem.product, 1)
                        },
                        onRemove = {
                            cartViewModel.removeFromCart(cartItem.product.id)
                        },
                        onRemoveAll = {
                            cartViewModel.removeAllFromCart(cartItem.product.id)
                        }
                    )
                }
            }

            // Итого
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Товары:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$itemCount шт.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Итого:",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalPrice ₽",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PearlRed
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onCheckoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PearlRed
                        )
                    ) {
                        Text(
                            text = "Перейти к оформлению",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemComponent(
    cartItem: CartViewModel.CartItem,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onRemoveAll: () -> Unit
) {
    val product = cartItem.product

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Цена
                if (product.originalPrice != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${product.price} ₽",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PearlRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${product.originalPrice} ₽",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                } else {
                    Text(
                        text = "${product.price} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PearlRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Управление количеством
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp),
                        enabled = cartItem.quantity > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Уменьшить"
                        )
                    }

                    Text(
                        text = "${cartItem.quantity}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Увеличить"
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Общая стоимость для этого товара
                    Text(
                        text = "${product.price * cartItem.quantity} ₽",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onRemoveAll
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCartState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "Пустая корзина",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Корзина пуста",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Добавьте товары из каталога",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

//@Composable
//private fun AuthRequiredState(
//    message: String,
//    onAuthRequired: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(
//            text = "🔐",
//            fontSize = 48.sp,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//        Text(
//            text = "Требуется авторизация",
//            style = MaterialTheme.typography.headlineSmall,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(bottom = 8.dp),
//            textAlign = TextAlign.Center
//        )
//        Text(
//            text = message,
//            style = MaterialTheme.typography.bodyLarge,
//            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//            textAlign = TextAlign.Center,
//            modifier = Modifier.padding(bottom = 24.dp)
//        )
//        Button(
//            onClick = onAuthRequired
//        ) {
//            Text("Войти в аккаунт")
//        }
//    }
//}


@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    PearlTheme {
        CartScreen(
            isUserLoggedIn = true,
            onAuthRequired = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenEmptyPreview() {
    PearlTheme {
        CartScreen(
            isUserLoggedIn = true,
            onAuthRequired = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenAuthRequiredPreview() {
    PearlTheme {
        CartScreen(
            isUserLoggedIn = false,
            onAuthRequired = {}
        )
    }
}