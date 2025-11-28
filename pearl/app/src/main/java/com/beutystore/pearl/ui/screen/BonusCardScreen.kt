package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beutystore.pearl.data.model.CardLevel
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlPeach
import com.beutystore.pearl.ui.theme.PearlCrimson
import androidx.compose.material3.MaterialTheme
import com.beutystore.pearl.ui.viewmodel.BonusCardViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BonusCardScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    accessToken: String? = null,
    onAuthRequired: () -> Unit = {},
    onBackClick: () -> Unit = {},
    bonusCardViewModel: BonusCardViewModel = viewModel(),
    userViewModel: com.beutystore.pearl.ui.viewmodel.UserViewModel = viewModel()
) {
    val bonusCard by bonusCardViewModel.bonusCard.collectAsState()
    val user by userViewModel.user.collectAsState()
    
    // Загружаем профиль и бонусную карту при входе
    LaunchedEffect(isUserLoggedIn, accessToken) {
        if (isUserLoggedIn && accessToken != null && user == null) {
            userViewModel.loadProfile(accessToken)
        }
    }
    
    // Обновляем бонусную карту из данных пользователя
    LaunchedEffect(user?.bonus_card) {
        val currentUser = user
        if (currentUser != null) {
            if (currentUser.bonus_card != null) {
                bonusCardViewModel.loadBonusCardFromUser(currentUser.bonus_card)
            } else {
                // Если пользователь есть, но карты нет - создаем пустую карту
                bonusCardViewModel.loadBonusCardFromUser(null)
            }
        }
    }

    if (!isUserLoggedIn) {
        AuthRequiredState(
            message = "Войдите в аккаунт, чтобы просмотреть бонусную карту",
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
            title = { Text("Бонусная карта") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (bonusCard != null) {
                // Bonus Card Display
                BonusCardDisplay(
                    bonusCard = bonusCard!!,
                    bonusCardViewModel = bonusCardViewModel
                )

                // Statistics
                StatisticsSection(bonusCard = bonusCard!!)

                // Level Info
                LevelInfoSection(
                    currentLevel = bonusCardViewModel.getCurrentLevel(),
                    nextLevel = bonusCardViewModel.getNextLevel(),
                    pointsToNext = bonusCardViewModel.getPointsToNextLevel(),
                    currentPoints = bonusCard!!.bonusPoints
                )

                // How to earn points
                HowToEarnSection()
            } else {
                EmptyBonusCardState()
            }
        }
    }
}

@Composable
private fun BonusCardDisplay(
    bonusCard: com.beutystore.pearl.data.model.BonusCard,
    bonusCardViewModel: BonusCardViewModel
) {
    val levelColor = getLevelColor(bonusCard.cardLevel)
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            com.beutystore.pearl.ui.theme.PearlRed.copy(alpha = 0.9f),
            com.beutystore.pearl.ui.theme.PearlPeach.copy(alpha = 0.7f),
            com.beutystore.pearl.ui.theme.PearlCrimson.copy(alpha = 0.8f),
            Color(android.graphics.Color.parseColor(levelColor)).copy(alpha = 0.6f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pearl Card",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = bonusCard.cardLevel.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Text(
                        text = bonusCard.cardLevel.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column {
                    Text(
                        text = formatCardNumber(bonusCard.cardNumber),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Бонусы",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${bonusCard.bonusPoints}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsSection(
    bonusCard: com.beutystore.pearl.data.model.BonusCard
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Статистика",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = "Всего потрачено",
                    value = "${bonusCard.totalSpent} ₽"
                )
                StatisticItem(
                    label = "Всего начислено",
                    value = "${bonusCard.totalEarned} бонусов"
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LevelInfoSection(
    currentLevel: CardLevel,
    nextLevel: CardLevel?,
    pointsToNext: Int,
    currentPoints: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Уровень карты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Текущий уровень",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentLevel.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(getLevelColor(currentLevel)))
                    )
                }
                if (nextLevel != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "До ${nextLevel.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$pointsToNext бонусов",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (nextLevel != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val progress = (currentPoints.toFloat() / nextLevel.minPoints).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(android.graphics.Color.parseColor(getLevelColor(nextLevel))),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HowToEarnSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Как получить бонусы",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val rules = listOf(
                "10% с каждой покупки начисляется на карту",
                "При регистрации - 100 бонусов на старте",
                "1 бонус = 1 ₽ при оплате",
                "Бонусы не сгорают"
            )

            rules.forEach { rule ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = PearlRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = rule,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Visible
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyBonusCardState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Бонусная карта не найдена",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Обратитесь в службу поддержки",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatCardNumber(cardNumber: String): String {
    return cardNumber.chunked(4).joinToString(" ")
}

private fun getLevelColor(level: CardLevel): String {
    return level.color
}

@Preview(showBackground = true)
@Composable
fun BonusCardScreenPreview() {
    PearlTheme {
        BonusCardScreen(
            isUserLoggedIn = true,
            onAuthRequired = {},
            onBackClick = {}
        )
    }
}

