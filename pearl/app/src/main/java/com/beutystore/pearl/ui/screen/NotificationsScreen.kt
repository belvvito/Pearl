package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val date: Date,
    val isRead: Boolean = false,
    val type: NotificationType
)

enum class NotificationType {
    ORDER, PROMO, SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    
    // Демо-уведомления
    val notifications = remember {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("ru"))
        
        listOf(
            NotificationItem(
                id = "1",
                title = "Заказ доставлен",
                message = "Ваш заказ №ORD001 успешно доставлен",
                date = calendar.time,
                type = NotificationType.ORDER
            ),
            NotificationItem(
                id = "2",
                title = "Специальное предложение",
                message = "Скидка 20% на все товары категории 'Уход за лицом'",
                date = {
                    calendar.add(Calendar.HOUR, -2)
                    calendar.time
                }(),
                type = NotificationType.PROMO
            ),
            NotificationItem(
                id = "3",
                title = "Новое поступление",
                message = "В каталоге появились новые товары от ваших любимых брендов",
                date = {
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    calendar.time
                }(),
                type = NotificationType.SYSTEM
            )
        )
    }

    if (!isUserLoggedIn) {
        AuthRequiredState(
            message = "Войдите в аккаунт, чтобы просмотреть уведомления",
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
                    text = "Уведомления",
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
            actions = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (!notificationsEnabled) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Уведомления отключены",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Включите уведомления, чтобы не пропустить важные обновления",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Нет уведомлений",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Здесь будут отображаться ваши уведомления",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationCard(notification = notification)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationItem) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("ru"))
    
    val borderColor = if (notification.isRead) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    } else {
        PearlRed.copy(alpha = 0.3f)
    }
    
    // Используем surface, который автоматически адаптируется к теме
    // В темной теме это будет темный цвет (0xFF1E1E1E), в светлой - белый
    val cardColor = MaterialTheme.colorScheme.surface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
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
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dateFormat.format(notification.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    PearlTheme {
        NotificationsScreen(
            isUserLoggedIn = true,
            onAuthRequired = {},
            onBackClick = {}
        )
    }
}

