// ui/screen/AIConsultantScreen.kt
package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlPeach
import com.beutystore.pearl.ui.viewmodel.AIConsultantViewModel
import com.beutystore.pearl.ui.viewmodel.ChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIConsultantScreen(
    onBackClick: () -> Unit,
    viewModel: AIConsultantViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Автопрокрутка к последнему сообщению
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Определяем фон
    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlLightPeach
    } else {
        MaterialTheme.colorScheme.background
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            PearlRed,
                                            PearlPeach
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = PearlWhite
                            )
                        }
                        Column {
                            Text(
                                text = "AI-Консультант",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Онлайн",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearChat() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Очистить чат"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            // Список сообщений
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(
                        message = message,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Индикатор загрузки
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            ChatBubble(
                                message = ChatMessage(
                                    text = "Печатает...",
                                    isUser = false
                                ),
                                modifier = Modifier.fillMaxWidth(0.7f),
                                isLoading = true
                            )
                        }
                    }
                }
            }

            // Поле ввода
            ChatInputField(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser && !isLoading) {
            // Аватар AI
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PearlRed,
                                PearlPeach
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PearlWhite,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) {
                        PearlRed
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = if (isLoading) "Печатает..." else message.text,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    lineHeight = 20.sp
                )
            }
        }

        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Аватар пользователя
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PearlPeach.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Вы",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ChatInputField(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                placeholder = {
                    Text(
                        text = "Напишите сообщение...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = PearlRed.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                singleLine = true,
                maxLines = 1
            )

            val canSend = enabled && messageText.isNotBlank()
            FloatingActionButton(
                onClick = {
                    if (canSend) {
                        onSendClick()
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = if (canSend) {
                    PearlRed
                } else {
                    PearlRed.copy(alpha = 0.5f)
                },
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Отправить"
                )
            }
        }
    }
}

