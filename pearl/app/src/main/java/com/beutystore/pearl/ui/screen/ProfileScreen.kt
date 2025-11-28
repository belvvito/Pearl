package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness7
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beutystore.pearl.data.model.User
import com.beutystore.pearl.ui.theme.*
import com.beutystore.pearl.ui.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    accessToken: String? = null,
    onAuthRequired: () -> Unit = {},
    onLogout: () -> Unit = {},
    onFavoritesClick: (() -> Unit)? = null,
    onOrdersClick: (() -> Unit)? = null,
    onBonusCardClick: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null,
    onAboutClick: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {},
    userViewModel: UserViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()
    
    // Загружаем профиль при входе и обновляем при изменении accessToken
    // Объединяем оба LaunchedEffect в один, чтобы избежать дублирования вызовов
    LaunchedEffect(isUserLoggedIn, accessToken) {
        android.util.Log.d("ProfileScreen", "LaunchedEffect: isUserLoggedIn=$isUserLoggedIn, accessToken=${if (accessToken != null) "present (length=${accessToken.length})" else "null"}")
        if (isUserLoggedIn && accessToken != null) {
            android.util.Log.d("ProfileScreen", "Loading profile with token")
            userViewModel.loadProfile(accessToken)
        } else if (!isUserLoggedIn) {
            android.util.Log.d("ProfileScreen", "User not logged in, clearing user")
            userViewModel.clearUser()
        }
    }
    
    // Логируем состояние пользователя
    LaunchedEffect(user) {
        android.util.Log.d("ProfileScreen", "User state changed: user=${if (user != null) "present (username=${user?.username}, phone=${user?.phone})" else "null"}")
    }
    
    // Если пользователь не авторизован, показываем экран авторизации
    if (!isUserLoggedIn) {
        UnauthorizedProfileScreen(
            modifier = modifier,
            onAuthRequired = onAuthRequired
        )
    } else {
        AuthorizedProfileScreen(
            modifier = modifier,
            user = user,
            isLoading = isLoading,
            error = error,
            onLogout = {
                userViewModel.clearUser()
                onLogout()
            },
            onFavoritesClick = onFavoritesClick ?: {},
            onOrdersClick = onOrdersClick ?: {},
            onBonusCardClick = onBonusCardClick ?: {},
            onNotificationsClick = onNotificationsClick ?: {},
            onHelpClick = onHelpClick ?: {},
            onAboutClick = onAboutClick ?: {},
            isDarkTheme = isDarkTheme,
            onThemeToggle = onThemeToggle
        )
    }
}

@Composable
fun UnauthorizedProfileScreen(
    modifier: Modifier = Modifier,
    onAuthRequired: () -> Unit = {}
) {
    // Определяем фон: светло-лавандовый для светлой темы, темный для темной
    // MaterialTheme.colorScheme уже оптимизирован внутри Compose, мемоизация не нужна
    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlLightPeach
    } else {
        MaterialTheme.colorScheme.background
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Иконка профиля
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Войдите в аккаунт",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Чтобы просматривать профиль и историю заказов",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAuthRequired,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PearlRed
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Войти в аккаунт",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AuthorizedProfileScreen(
    modifier: Modifier = Modifier,
    user: User? = null,
    isLoading: Boolean = false,
    error: String? = null,
    onLogout: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onBonusCardClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {}
) {
    // Мемоизируем данные пользователя для оптимизации
    val userName = remember(user) { 
        val username = user?.username ?: ""
        android.util.Log.d("ProfileScreen", "User username: $username, user: ${user?.username}")
        username
    }
    val userPhone = remember(user) { 
        val phone = user?.phone ?: ""
        android.util.Log.d("ProfileScreen", "User phone: $phone, user: ${user?.phone}")
        phone
    }
    val userEmail = remember(user) { user?.email ?: "" }
    val userAvatar = remember(user) { user?.profile?.avatar }
    val userCity = remember(user) { user?.profile?.city }
    val userCountry = remember(user) { user?.profile?.country }
    val userBonusPoints = remember(user) { user?.bonus_card?.bonusPoints ?: 0 }
    
    // Инициалы для аватара - оптимизировано с remember
    val initials = remember(userName) {
        if (userName.isNotEmpty()) {
        userName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
                .take(2).ifEmpty { userName.take(1).uppercase() }
        } else {
            "П"
        }
    }
    
    // Цвет карточек - мемоизирован для избежания пересоздания Color объектов
    val cardColor = remember(isDarkTheme) {
        if (isDarkTheme) {
            Color(0xFF2C2C2C) // Темно-серый цвет карточек для темной темы
    } else {
        Color(0xFFFFFFFF) // Белый цвет карточек для светлой темы
    }
    }
    
    // Цвет текста на карточках - MaterialTheme.colorScheme уже оптимизирован
    val cardTextColor = MaterialTheme.colorScheme.onSurface
    
    // Фон экрана - MaterialTheme.colorScheme уже оптимизирован
    val backgroundColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.background
    } else {
        PearlLightPeach
    }
    
    // Мемоизируем уровень карты для избежания повторных вычислений
    val cardLevelDisplayName = remember(user) {
        user?.bonus_card?.cardLevel?.displayName ?: "Бронза"
    }
    
    // Мемоизируем градиент для бонусной карты
    val bonusCardGradient = remember {
        Brush.horizontalGradient(
            colors = listOf(
                PearlRed.copy(alpha = 0.9f),
                PearlPeach.copy(alpha = 0.7f),
                PearlCrimson.copy(alpha = 0.8f)
            )
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = PearlRed
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(PearlRed),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userAvatar != null && userAvatar.isNotBlank()) {
                            // TODO: Загрузить изображение аватара
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Text(
                        text = "Загрузка...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = cardTextColor
                    )
                } else if (error != null) {
                    Text(
                        text = "Ошибка загрузки",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                } else {
                    // Логин пользователя (из первого поля регистрации - "Имя и фамилия")
                    // Всегда показываем логин, даже если он пустой (показываем placeholder)
                    Text(
                        text = if (userName.isNotEmpty()) userName else "Пользователь",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = cardTextColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Номер телефона (из второго поля регистрации)
                    // Всегда показываем номер телефона, даже если он пустой
                            Text(
                        text = if (userPhone.isNotEmpty()) userPhone else "Номер не указан",
                                style = MaterialTheme.typography.bodyMedium,
                        color = if (userPhone.isNotEmpty()) cardTextColor.copy(alpha = 0.8f) else cardTextColor.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium
                            )
                    
                    // Email (по центру)
                    if (userEmail.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall,
                            color = cardTextColor.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Loyalty card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brush = bonusCardGradient)
                            .clickable(onClick = onBonusCardClick)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pearl Card",
                                    color = PearlWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$userBonusPoints баллов",
                                    color = PearlWhite,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = cardLevelDisplayName,
                                color = PearlWhite,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
            }
        }

        // Menu items - мемоизирован для избежания пересоздания при каждой рекомпозиции
        val menuItems = remember {
            listOf(
                "Мои заказы" to Icons.Default.ShoppingCart,
                "Избранное" to Icons.Default.Favorite,
                "Бонусная карта" to Icons.Default.CardGiftcard,
                "Уведомления" to Icons.Default.Notifications,
                "Помощь" to Icons.AutoMirrored.Filled.Help,
                "О приложении" to Icons.Default.Info
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = menuItems,
                key = { it.first } // Используем title как ключ для оптимизации рекомпозиции
            ) { (title, icon) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    ),
                    onClick = {
                        when (title) {
                            "Избранное" -> onFavoritesClick()
                            "Мои заказы" -> onOrdersClick()
                            "Бонусная карта" -> onBonusCardClick()
                            "Уведомления" -> onNotificationsClick()
                            "Помощь" -> onHelpClick()
                            "О приложении" -> onAboutClick()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = PearlRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = cardTextColor
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Arrow",
                            tint = cardTextColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Theme toggle item
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Outlined.Brightness4 else Icons.Outlined.Brightness7,
                            contentDescription = "Theme",
                            tint = PearlRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (isDarkTheme) "Темная тема" else "Светлая тема",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = cardTextColor
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onThemeToggle
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Logout button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Выйти из аккаунта")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Authorized Profile")
@Composable
fun AuthorizedProfileScreenPreview() {
    PearlTheme {
        AuthorizedProfileScreen(
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, name = "Unauthorized Profile")
@Composable
fun UnauthorizedProfileScreenPreview() {
    PearlTheme {
        UnauthorizedProfileScreen(
            onAuthRequired = {}
        )
    }
}

@Preview(showBackground = true, name = "Profile Screen - Guest")
@Composable
fun ProfileScreenGuestPreview() {
    PearlTheme {
        ProfileScreen(
            isUserLoggedIn = false,
            onAuthRequired = {}
        )
    }
}

@Preview(showBackground = true, name = "Profile Screen - Logged In")
@Composable
fun ProfileScreenLoggedInPreview() {
    PearlTheme {
        ProfileScreen(
            isUserLoggedIn = true,
            onAuthRequired = {},
            onLogout = {}
        )
    }
}