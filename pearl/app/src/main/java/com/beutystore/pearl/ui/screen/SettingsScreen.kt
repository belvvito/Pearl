package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness7
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,
    onAuthRequired: () -> Unit = {},
    onBackClick: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf("Русский") }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // TopBar
        TopAppBar(
            title = { Text("Настройки") },
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Appearance section
            item {
                SettingsSectionTitle("Внешний вид")
            }
            
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Outlined.Brightness4 else Icons.Outlined.Brightness7,
                                contentDescription = "Тема",
                                tint = PearlRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Темная тема",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isDarkTheme) "Включена" else "Выключена",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onThemeToggle
                        )
                    }
                }
            }

            // Notifications section
            item {
                SettingsSectionTitle("Уведомления")
            }

            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Уведомления",
                                tint = PearlRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Push-уведомления",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Получать уведомления о заказах и акциях",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
            }

            // Language section
            item {
                SettingsSectionTitle("Язык и регион")
            }

            item {
                SettingsCard(
                    onClick = {
                        // TODO: Открыть диалог выбора языка
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Язык",
                                tint = PearlRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Язык приложения",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = language,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Открыть",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Account section
            if (isUserLoggedIn) {
                item {
                    SettingsSectionTitle("Аккаунт")
                }

                item {
                    SettingsCard(
                        onClick = {
                            // TODO: Открыть диалог удаления аккаунта
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить аккаунт",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Удалить аккаунт",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Безвозвратное удаление всех данных",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Открыть",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // About section
            item {
                SettingsSectionTitle("О приложении")
            }

            item {
                SettingsCard(
                    onClick = {
                        // TODO: Открыть экран информации о приложении
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "О приложении",
                                tint = PearlRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Версия приложения",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "1.0.0",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Открыть",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = PearlRed,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsCard(
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PearlTheme {
        SettingsScreen(
            isUserLoggedIn = true,
            onAuthRequired = {},
            onBackClick = {},
            isDarkTheme = false,
            onThemeToggle = {}
        )
    }
}

