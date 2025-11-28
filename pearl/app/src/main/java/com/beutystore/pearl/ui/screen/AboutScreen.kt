package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
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
                    text = "О приложении",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // App Icon/Logo placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PearlRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PEARL",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // App Name
            Text(
                text = "Pearl Store",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Version
            Text(
                text = "Версия 1.0.0",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "О приложении",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "Pearl Store - это современное приложение для покупки косметики и средств по уходу. " +
                                "Мы предлагаем широкий ассортимент качественных товаров от ведущих брендов, " +
                                "удобную систему заказов и доставки, а также бонусную программу для наших клиентов.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Features
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Возможности",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    val features = listOf(
                        "Каталог с широким ассортиментом товаров",
                        "Удобная корзина и оформление заказов",
                        "Система избранного",
                        "Бонусная программа Pearl Card",
                        "История заказов",
                        "Уведомления о статусе заказов"
                    )
                    features.forEach { feature ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = PearlRed
                            )
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Copyright
            Text(
                text = "© 2024 Pearl Store. Все права защищены.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    PearlTheme {
        AboutScreen(onBackClick = {})
    }
}

