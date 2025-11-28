package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.beutystore.pearl.data.model.Order
import com.beutystore.pearl.data.model.OrderStatus
import androidx.compose.foundation.background
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlPeach
import com.beutystore.pearl.ui.theme.PearlCrimson
import com.beutystore.pearl.ui.viewmodel.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    accessToken: String? = null,
    onAuthRequired: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onProductClick: (com.beutystore.pearl.data.model.Product) -> Unit = {},
    ordersViewModel: OrdersViewModel = viewModel()
) {
    val orders by ordersViewModel.sortedOrders.collectAsState()
    val ordersCount by ordersViewModel.ordersCount.collectAsState()

    // Загружаем заказы при входе пользователя
    LaunchedEffect(isUserLoggedIn, accessToken) {
        if (isUserLoggedIn && accessToken != null) {
            ordersViewModel.loadOrders(accessToken)
        }
    }

    if (!isUserLoggedIn) {
        AuthRequiredState(
            message = "Войдите в аккаунт, чтобы просмотреть заказы",
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
            title = { Text("Мои заказы") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад"
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
            if (orders.isEmpty()) {
                EmptyOrdersState()
            } else {
                Text(
                    text = "$ordersCount ${if (ordersCount == 1) "заказ" else if (ordersCount < 5) "заказа" else "заказов"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onProductClick = onProductClick,
                            onCancelOrder = {
                                ordersViewModel.cancelOrder(order.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onProductClick: (com.beutystore.pearl.data.model.Product) -> Unit,
    onCancelOrder: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Заказ ${order.displayOrderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(order.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Status badge
                StatusBadge(status = order.statusEnum)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${order.itemCount} ${if (order.itemCount == 1) "товар" else if (order.itemCount < 5) "товара" else "товаров"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Используем totalPrice из заказа, если он не равен 0, иначе вычисляем из товаров
                val displayTotalPrice = if (order.totalPrice > 0) {
                    order.totalPrice
                } else {
                    order.items.sumOf { item ->
                        val itemPrice = if (item.price > 0) item.price else item.product.price
                        itemPrice * item.quantity
                    }
                }
                Text(
                    text = "$displayTotalPrice ₽",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PearlRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expand/Collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Скрыть детали" else "Показать детали",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PearlRed
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Скрыть" else "Показать",
                    tint = PearlRed
                )
            }

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Order items
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    order.items.forEach { orderItem ->
                        OrderItemRow(
                            orderItem = orderItem,
                            onProductClick = { onProductClick(orderItem.product) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Delivery info
                if (order.deliveryAddress != null) {
                    InfoRow(
                        label = "Адрес доставки",
                        value = order.deliveryAddress
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (order.paymentMethod != null) {
                    InfoRow(
                        label = "Способ оплаты",
                        value = order.paymentMethod
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Bonus points info (only if not zero)
                if (order.bonusPointsUsed > 0) {
                    InfoRow(
                        label = "Использовано баллов",
                        value = "${order.bonusPointsUsed} баллов"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (order.bonusPointsEarned > 0) {
                    InfoRow(
                        label = "Начислено баллов",
                        value = "${order.bonusPointsEarned} баллов"
                    )
                }

                // Cancel button (only for pending/confirmed orders)
                if (order.statusEnum == OrderStatus.PENDING || order.statusEnum == OrderStatus.CONFIRMED) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onCancelOrder,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Отменить заказ")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(
    orderItem: com.beutystore.pearl.data.model.OrderItem,
    onProductClick: () -> Unit
) {
    // Используем цену из заказа, если она не равна 0, иначе используем цену продукта
    val itemPrice = if (orderItem.price > 0) orderItem.price else orderItem.product.price
    val totalItemPrice = itemPrice * orderItem.quantity
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = orderItem.product.imageUrl,
            contentDescription = orderItem.product.name,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = orderItem.product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Количество: ${orderItem.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$itemPrice ₽ за шт.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$totalItemPrice ₽",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusBadge(status: OrderStatus) {
    val (color, text) = when (status) {
        OrderStatus.PENDING -> PearlCrimson to status.displayName
        OrderStatus.CONFIRMED -> PearlRed to status.displayName
        OrderStatus.PROCESSING -> PearlRed to status.displayName
        OrderStatus.SHIPPED -> PearlPeach to status.displayName
        OrderStatus.DELIVERED -> PearlRed to status.displayName
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error to status.displayName
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun EmptyOrdersState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "Нет заказов",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "У вас пока нет заказов",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Оформите первый заказ,\nчтобы он появился здесь",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun OrdersScreenPreview() {
    PearlTheme {
        OrdersScreen(
            isUserLoggedIn = true,
            onAuthRequired = {},
            onBackClick = {},
            onProductClick = {}
        )
    }
}

