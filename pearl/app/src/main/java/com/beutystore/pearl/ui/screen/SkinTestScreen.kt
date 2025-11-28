package com.beutystore.pearl.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beutystore.pearl.data.model.TestQuestion
import com.beutystore.pearl.data.model.TestOption
import com.beutystore.pearl.ui.theme.PearlLightPeach
import com.beutystore.pearl.ui.theme.PearlTheme
import com.beutystore.pearl.ui.theme.PearlWhite
import com.beutystore.pearl.ui.theme.PearlRed
import com.beutystore.pearl.ui.viewmodel.SkinTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinTestScreen(
    modifier: Modifier = Modifier,
    onComplete: (com.beutystore.pearl.data.model.SkinTestResult) -> Unit = {},
    onSkip: () -> Unit = {},
    skinTestViewModel: SkinTestViewModel = viewModel()
) {
    val currentQuestionIndex by skinTestViewModel.currentQuestionIndex.collectAsState()
    val testResult by skinTestViewModel.testResult.collectAsState()
    val selectedAnswers by skinTestViewModel.selectedAnswersFlow.collectAsState()
    
    // Мемоизируем текущий вопрос
    val currentQuestion = remember(currentQuestionIndex) {
        skinTestViewModel.getCurrentQuestion()
    }
    
    // Получаем выбранный ответ для текущего вопроса из StateFlow
    val selectedAnswer = remember(currentQuestionIndex, selectedAnswers) {
        selectedAnswers[currentQuestion.id]
    }
    
    // canGoNext обновляется при изменении currentQuestionIndex или selectedAnswers
    val canGoNext = remember(currentQuestionIndex, selectedAnswer) {
        skinTestViewModel.canGoNext()
    }

    val progress = remember(currentQuestionIndex) {
        (currentQuestionIndex + 1).toFloat() / skinTestViewModel.questions.size.toFloat()
    }

    // Если тест завершен, показываем результаты
    LaunchedEffect(testResult) {
        testResult?.let {
            onComplete(it)
        }
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
            title = { Text("Тест кожи") },
            navigationIcon = {
                IconButton(onClick = onSkip) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Пропустить"
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicator
            Text(
                text = "Вопрос ${currentQuestionIndex + 1} из ${skinTestViewModel.questions.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(bottom = 32.dp),
                color = PearlRed
            )

            // Question
            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Options - используем ключи для оптимизации перекомпозиции
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = selectedAnswer == option
                    val questionId = currentQuestion.id
                    
                    // Используем прямой вызов без remember для простоты
                    key("${questionId}_${index}") {
                        TestOptionItem(
                            option = option,
                            isSelected = isSelected,
                            onClick = { skinTestViewModel.selectAnswer(questionId, option) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Navigation buttons - мемоизируем обработчики
            val onPreviousClick = remember { { skinTestViewModel.previousQuestion() } }
            val onNextClick = remember { { skinTestViewModel.nextQuestion() } }
            val buttonText = remember(currentQuestionIndex) {
                if (currentQuestionIndex < skinTestViewModel.questions.size - 1) {
                    "Далее"
                } else {
                    "Завершить"
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentQuestionIndex > 0) {
                    OutlinedButton(
                        onClick = onPreviousClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Назад")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                
                Button(
                    onClick = onNextClick,
                    enabled = canGoNext,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(buttonText)
                }
            }

            // Skip button
            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Пропустить тест")
            }
        }
    }
}

@Composable
private fun TestOptionItem(
    option: TestOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Мемоизируем только elevation, цвета получаем напрямую из MaterialTheme
    val elevation = remember(isSelected) { if (isSelected) 4.dp else 2.dp }
    val containerColor = if (isSelected) {
        PearlRed.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null // Убираем дублирование onClick, обрабатывается через Card
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SkinTestScreenPreview() {
    PearlTheme {
        SkinTestScreen(
            onComplete = {},
            onSkip = {}
        )
    }
}

