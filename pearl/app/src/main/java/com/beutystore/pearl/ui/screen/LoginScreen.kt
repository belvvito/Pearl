// ui/screen/LoginScreen.kt
package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlPeach
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    
    val userRepository = remember { com.beutystore.pearl.data.repository.UserRepository(com.beutystore.pearl.data.api.RetrofitInstance.api) }
    val coroutineScope = rememberCoroutineScope()

    // Определяем фон
    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlLightPeach
    } else {
        MaterialTheme.colorScheme.background
    }
    
    // Мемоизируем градиент кнопки для оптимизации
    val buttonGradient = remember {
        Brush.horizontalGradient(
            colors = listOf(
                PearlRed,
                PearlPeach
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PearlRed
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Вход в аккаунт",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Форма в карточке
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Поле телефона
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { 
                            phone = it
                            phoneError = null
                        },
                        label = { 
                            Text(
                                "Номер телефона",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        isError = phoneError != null,
                        supportingText = phoneError?.let { 
                            { Text(text = it, color = MaterialTheme.colorScheme.error) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (phoneError != null) MaterialTheme.colorScheme.error else PearlRed,
                            focusedLabelColor = if (phoneError != null) MaterialTheme.colorScheme.error else PearlRed,
                            unfocusedBorderColor = if (phoneError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    // Поле пароля
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null
                        },
                        label = { 
                            Text(
                                "Пароль",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { 
                            { Text(text = it, color = MaterialTheme.colorScheme.error) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else PearlRed,
                            focusedLabelColor = if (passwordError != null) MaterialTheme.colorScheme.error else PearlRed,
                            unfocusedBorderColor = if (passwordError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        },
                        singleLine = true
                    )

                    // Запомнить меня
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PearlRed
                            )
                        )
                        Text(
                            text = "Запомнить меня",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Забыли пароль по центру
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Забыли пароль?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PearlRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Сообщение об ошибке
            if (loginError != null) {
                Text(
                    text = loginError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            
            // Кнопка входа с градиентом
            Button(
                onClick = { 
                    // Валидация полей
                    var hasError = false
                    loginError = null
                    
                    if (phone.isBlank() || phone.trim().isEmpty()) {
                        phoneError = "Номер телефона не может быть пустым"
                        hasError = true
                    }
                    if (password.isBlank() || password.trim().isEmpty()) {
                        passwordError = "Пароль не может быть пустым"
                        hasError = true
                    }
                    if (!hasError && !isLoading) {
                        isLoading = true
                        loginError = null
                        
                        // Вызов API для авторизации
                        coroutineScope.launch {
                            try {
                                userRepository.login(phone.trim(), password).collect { result ->
                                
                                    if (result.isSuccess) {
                                        val authResponse = result.getOrNull()
                                        isLoading = false
                                        if (authResponse != null) {
                                            val token = authResponse.tokens?.access ?: ""
                                            if (token.isNotEmpty()) {
                                                onLoginSuccess(token)
                                            } else {
                                                loginError = "Не удалось получить токен авторизации"
                                            }
                                        } else {
                                            loginError = "Ошибка получения данных"
                                        }
                                    } else {
                                        isLoading = false
                                        val exception = result.exceptionOrNull()
                                        loginError = exception?.message ?: "Ошибка входа. Проверьте данные и попробуйте снова"
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                loginError = "Ошибка подключения. Проверьте интернет и попробуйте снова"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = buttonGradient,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Войти",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PearlWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ссылка на регистрацию
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Еще нет аккаунта? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier.wrapContentHeight()
                ) {
                    Text(
                        text = "Зарегистрироваться",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PearlRed
                    )
                }
            }
        }
        
        // Диалог восстановления пароля вынесен на уровень компонента
        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPasswordDialog = false },
                onSendCode = { phoneNumber ->
                    // TODO: Отправить код восстановления
                    showForgotPasswordDialog = false
                }
            )
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendCode: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (codeSent) "Код отправлен" else "Восстановление пароля",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (codeSent) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Код восстановления отправлен на номер:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PearlRed
                    )
                    Text(
                        text = "Пожалуйста, проверьте SMS сообщения.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Введите номер телефона, привязанный к вашему аккаунту. Мы отправим вам код для восстановления пароля.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Номер телефона") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            if (codeSent) {
                Button(onClick = onDismiss) {
                    Text("Понятно")
                }
            } else {
                Button(
                    onClick = {
                        if (phoneNumber.isNotBlank()) {
                            onSendCode(phoneNumber)
                            codeSent = true
                        }
                    },
                    enabled = phoneNumber.isNotBlank()
                ) {
                    Text("Отправить код")
                }
            }
        },
        dismissButton = {
            if (!codeSent) {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    PearlTheme {
        LoginScreen(
            onNavigateToRegister = {},
            onLoginSuccess = {},
            onNavigateBack = {}
        )
    }
}
