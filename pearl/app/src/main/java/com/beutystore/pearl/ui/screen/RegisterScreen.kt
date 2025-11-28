// ui/screen/RegisterScreen.kt
package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.theme.PearlPeach
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordConfirmError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }
    
    val userRepository = remember { com.beutystore.pearl.data.repository.UserRepository(com.beutystore.pearl.data.api.RetrofitInstance.api) }
    val coroutineScope = rememberCoroutineScope()

    // Определяем фон
    val backgroundColor = if (MaterialTheme.colorScheme.background == PearlWhite) {
        PearlLightPeach
    } else {
        MaterialTheme.colorScheme.background
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
                    text = "Регистрация",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Форма в карточке с прокруткой
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Поле имени
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = null
                        },
                        label = { 
                            Text(
                                "Имя и фамилия",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = nameError != null,
                        supportingText = nameError?.let { 
                            { Text(text = it, color = MaterialTheme.colorScheme.error) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (nameError != null) MaterialTheme.colorScheme.error else PearlRed,
                            focusedLabelColor = if (nameError != null) MaterialTheme.colorScheme.error else PearlRed,
                            unfocusedBorderColor = if (nameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

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

                    // Поле email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = null
                        },
                        label = { 
                            Text(
                                "Email",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        isError = emailError != null,
                        supportingText = emailError?.let { 
                            { Text(text = it, color = MaterialTheme.colorScheme.error) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else PearlRed,
                            focusedLabelColor = if (emailError != null) MaterialTheme.colorScheme.error else PearlRed,
                            unfocusedBorderColor = if (emailError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    // Поле пароля
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null
                            if (passwordConfirm.isNotEmpty() && password != passwordConfirm) {
                                passwordConfirmError = "Пароли не совпадают"
                            } else {
                                passwordConfirmError = null
                            }
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

                    // Поле подтверждения пароля
                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { 
                            passwordConfirm = it
                            passwordConfirmError = null
                            if (it.isNotEmpty() && password != it) {
                                passwordConfirmError = "Пароли не совпадают"
                            }
                        },
                        label = { 
                            Text(
                                "Подтвердите пароль",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        isError = passwordConfirmError != null,
                        supportingText = passwordConfirmError?.let { 
                            { Text(text = it, color = MaterialTheme.colorScheme.error) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (passwordConfirmError != null) MaterialTheme.colorScheme.error else PearlRed,
                            focusedLabelColor = if (passwordConfirmError != null) MaterialTheme.colorScheme.error else PearlRed,
                            unfocusedBorderColor = if (passwordConfirmError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
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

                    // Поле даты рождения
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { 
                            Text(
                                "Дата рождения",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text(
                                "ДД.ММ.ГГГГ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            ) 
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PearlRed,
                            focusedLabelColor = PearlRed,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ссылка на вход
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Уже есть аккаунт? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    modifier = Modifier.wrapContentHeight()
                ) {
                    Text(
                        text = "Войти",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PearlRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка регистрации с градиентом
            Button(
                onClick = { 
                    // Валидация полей
                    var hasError = false
                    if (name.isBlank() || name.trim().isEmpty()) {
                        nameError = "Имя не может быть пустым"
                        hasError = true
                    }
                    if (phone.isBlank() || phone.trim().isEmpty()) {
                        phoneError = "Номер телефона не может быть пустым"
                        hasError = true
                    }
                    if (email.isBlank() || email.trim().isEmpty()) {
                        emailError = "Email не может быть пустым"
                        hasError = true
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Введите корректный email"
                        hasError = true
                    }
                    if (password.isBlank() || password.trim().isEmpty()) {
                        passwordError = "Пароль не может быть пустым"
                        hasError = true
                    } else if (password.length < 6) {
                        passwordError = "Пароль должен содержать минимум 6 символов"
                        hasError = true
                    }
                    if (passwordConfirm.isBlank() || passwordConfirm.trim().isEmpty()) {
                        passwordConfirmError = "Подтвердите пароль"
                        hasError = true
                    } else if (password != passwordConfirm) {
                        passwordConfirmError = "Пароли не совпадают"
                        hasError = true
                    }
                    if (!hasError && !isLoading) {
                        isLoading = true
                        registerError = null
                        
                        // Вызов API для регистрации
                        coroutineScope.launch {
                            try {
                                val registerRequest = com.beutystore.pearl.data.model.RegisterRequest(
                                    username = name.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    date_of_birth = if (birthDate.isNotBlank()) birthDate else null,
                                    password = password,
                                    password_confirm = passwordConfirm
                                )
                                
                                userRepository.register(registerRequest).collect { result ->
                                    if (result.isSuccess) {
                                        val authResponse = result.getOrNull()
                                        isLoading = false
                                        if (authResponse != null) {
                                            val token = authResponse.tokens?.access ?: ""
                                            if (token.isNotEmpty()) {
                                                onRegisterSuccess(token)
                                            } else {
                                                registerError = "Регистрация успешна, но не удалось получить токен"
                                            }
                                        } else {
                                            registerError = "Ошибка получения данных"
                                        }
                                    } else {
                                        isLoading = false
                                        val exception = result.exceptionOrNull()
                                        registerError = exception?.message ?: "Ошибка регистрации. Попробуйте снова"
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                registerError = "Ошибка подключения. Проверьте интернет и попробуйте снова"
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
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PearlRed,
                                    PearlPeach
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Зарегистрироваться",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PearlWhite
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    PearlTheme {
        RegisterScreen(
            onNavigateToLogin = {},
            onRegisterSuccess = {},
            onNavigateBack = {}
        )
    }
}
