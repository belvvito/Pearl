package com.beutystore.pearl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beutystore.pearl.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Функция для получения рекомендаций на основе результатов теста
fun getRecommendedProducts(
    allProducts: List<Product>,
    testResult: SkinTestResult?
): List<Product> {
    if (testResult == null) return emptyList()

    val filtered = allProducts.filter { product ->
        val name = product.name.lowercase()
        val description = product.description.lowercase()
        val features = product.features.joinToString(" ").lowercase()
        val category = product.category.lowercase()
        
        val matchesNeed = when (testResult.primaryNeed) {
            SkinNeed.HYDRATION -> name.contains("увлажн") || description.contains("увлажн") || 
                                features.contains("увлажн") || name.contains("гиалурон")
            SkinNeed.NUTRITION -> name.contains("пита") || description.contains("пита") || 
                                features.contains("пита") || name.contains("витамин")
            SkinNeed.PROTECTION -> name.contains("защит") || description.contains("защит") || 
                                 features.contains("spf") || features.contains("защит")
            SkinNeed.REGENERATION -> name.contains("регенер") || description.contains("регенер") || 
                                    features.contains("регенер") || name.contains("восстанов")
            SkinNeed.MATTIFICATION -> name.contains("матирующ") || description.contains("матирующ") || 
                                     features.contains("матирующ") || name.contains("контроль")
            SkinNeed.SOOTHING -> name.contains("успокаивающ") || description.contains("успокаивающ") || 
                               features.contains("успокаивающ") || name.contains("успокоен")
        }
        
        val matchesSkinType = when (testResult.skinType) {
            SkinType.DRY -> !name.contains("матирующ") && !name.contains("для жирной")
            SkinType.OILY -> name.contains("для жирной") || name.contains("матирующ") || 
                           category.contains("жирн")
            SkinType.SENSITIVE -> name.contains("чувствител") || description.contains("чувствител") || 
                                features.contains("гипоаллерген")
            SkinType.COMBINATION -> true // Комбинированная кожа подходит для большинства средств
            SkinType.NORMAL -> true
        }
        
        matchesNeed && matchesSkinType
    }
    
    // Если не нашли точных совпадений, возвращаем товары, которые хотя бы частично подходят
    return if (filtered.isNotEmpty()) {
        filtered.take(6)
    } else {
        // Fallback: возвращаем товары, которые подходят по типу кожи или по потребности
        allProducts.filter { product ->
            val name = product.name.lowercase()
            val description = product.description.lowercase()
            val features = product.features.joinToString(" ").lowercase()
            
            when (testResult.primaryNeed) {
                SkinNeed.HYDRATION -> name.contains("увлажн") || description.contains("увлажн") || features.contains("увлажн")
                SkinNeed.NUTRITION -> name.contains("пита") || description.contains("пита") || features.contains("пита")
                SkinNeed.PROTECTION -> name.contains("защит") || description.contains("защит") || features.contains("spf")
                SkinNeed.REGENERATION -> name.contains("регенер") || description.contains("регенер") || name.contains("восстанов")
                SkinNeed.MATTIFICATION -> name.contains("матирующ") || description.contains("матирующ")
                SkinNeed.SOOTHING -> name.contains("успокаивающ") || description.contains("успокаивающ")
            }
        }.take(6)
    }
}

class SkinTestViewModel : ViewModel() {

    private val _testResult = MutableStateFlow<SkinTestResult?>(null)
    val testResult: StateFlow<SkinTestResult?> = _testResult.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, TestOption>>(emptyMap())
    val selectedAnswersFlow: StateFlow<Map<Int, TestOption>> = _selectedAnswers.asStateFlow()
    private val selectedAnswers = mutableMapOf<Int, TestOption>()

    val questions = listOf(
        TestQuestion(
            id = 1,
            question = "Как вы опишете свою кожу?",
            options = listOf(
                TestOption(
                    text = "Сухая, часто шелушится",
                    skinTypePoints = mapOf(SkinType.DRY to 3, SkinType.SENSITIVE to 1),
                    needPoints = mapOf(SkinNeed.HYDRATION to 3, SkinNeed.NUTRITION to 2)
                ),
                TestOption(
                    text = "Жирная, блестит в Т-зоне",
                    skinTypePoints = mapOf(SkinType.OILY to 3, SkinType.COMBINATION to 2),
                    needPoints = mapOf(SkinNeed.MATTIFICATION to 3, SkinNeed.HYDRATION to 1)
                ),
                TestOption(
                    text = "Нормальная, без проблем",
                    skinTypePoints = mapOf(SkinType.NORMAL to 3),
                    needPoints = mapOf(SkinNeed.PROTECTION to 2, SkinNeed.REGENERATION to 1)
                ),
                TestOption(
                    text = "Комбинированная (сухая на щеках, жирная в Т-зоне)",
                    skinTypePoints = mapOf(SkinType.COMBINATION to 3, SkinType.NORMAL to 1),
                    needPoints = mapOf(SkinNeed.HYDRATION to 2, SkinNeed.MATTIFICATION to 1)
                )
            )
        ),
        TestQuestion(
            id = 2,
            question = "Как часто вы чувствуете стянутость кожи?",
            options = listOf(
                TestOption(
                    text = "Постоянно",
                    skinTypePoints = mapOf(SkinType.DRY to 3, SkinType.SENSITIVE to 2),
                    needPoints = mapOf(SkinNeed.HYDRATION to 3, SkinNeed.NUTRITION to 2),
                    concernPoints = mapOf(SkinConcern.DRYNESS to 3)
                ),
                TestOption(
                    text = "После умывания",
                    skinTypePoints = mapOf(SkinType.DRY to 2, SkinType.NORMAL to 1),
                    needPoints = mapOf(SkinNeed.HYDRATION to 2)
                ),
                TestOption(
                    text = "Редко",
                    skinTypePoints = mapOf(SkinType.NORMAL to 2, SkinType.OILY to 1),
                    needPoints = mapOf(SkinNeed.PROTECTION to 1)
                ),
                TestOption(
                    text = "Никогда",
                    skinTypePoints = mapOf(SkinType.OILY to 2, SkinType.COMBINATION to 1),
                    needPoints = mapOf(SkinNeed.MATTIFICATION to 1)
                )
            )
        ),
        TestQuestion(
            id = 3,
            question = "Какой у вас возраст?",
            options = listOf(
                TestOption(text = "18-25 лет"),
                TestOption(text = "26-35 лет"),
                TestOption(text = "36-45 лет"),
                TestOption(text = "45+ лет")
            )
        ),
        TestQuestion(
            id = 4,
            question = "Какие проблемы кожи вас беспокоят?",
            options = listOf(
                TestOption(
                    text = "Акне и воспаления",
                    concernPoints = mapOf(SkinConcern.ACNE to 3, SkinConcern.OILINESS to 2),
                    needPoints = mapOf(SkinNeed.SOOTHING to 2, SkinNeed.MATTIFICATION to 1)
                ),
                TestOption(
                    text = "Морщины и потеря упругости",
                    concernPoints = mapOf(SkinConcern.WRINKLES to 3),
                    needPoints = mapOf(SkinNeed.REGENERATION to 3, SkinNeed.NUTRITION to 2)
                ),
                TestOption(
                    text = "Пигментация и темные пятна",
                    concernPoints = mapOf(SkinConcern.DARK_SPOTS to 3),
                    needPoints = mapOf(SkinNeed.REGENERATION to 2, SkinNeed.PROTECTION to 2)
                ),
                TestOption(
                    text = "Покраснения и чувствительность",
                    concernPoints = mapOf(SkinConcern.SENSITIVITY to 3, SkinConcern.REDNESS to 2),
                    needPoints = mapOf(SkinNeed.SOOTHING to 3, SkinNeed.PROTECTION to 1)
                ),
                TestOption(
                    text = "Сухость и шелушение",
                    concernPoints = mapOf(SkinConcern.DRYNESS to 3),
                    needPoints = mapOf(SkinNeed.HYDRATION to 3, SkinNeed.NUTRITION to 2)
                )
            )
        ),
        TestQuestion(
            id = 5,
            question = "Что для вас важнее в уходе?",
            options = listOf(
                TestOption(
                    text = "Интенсивное увлажнение",
                    needPoints = mapOf(SkinNeed.HYDRATION to 3)
                ),
                TestOption(
                    text = "Питание и восстановление",
                    needPoints = mapOf(SkinNeed.NUTRITION to 3, SkinNeed.REGENERATION to 2)
                ),
                TestOption(
                    text = "Защита от солнца и окружающей среды",
                    needPoints = mapOf(SkinNeed.PROTECTION to 3)
                ),
                TestOption(
                    text = "Матирование и контроль жирности",
                    needPoints = mapOf(SkinNeed.MATTIFICATION to 3)
                )
            )
        )
    )

    fun selectAnswer(questionId: Int, option: TestOption) {
        selectedAnswers[questionId] = option
        _selectedAnswers.value = selectedAnswers.toMap() // Обновляем StateFlow для перекомпозиции
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < questions.size - 1) {
            _currentQuestionIndex.value = _currentQuestionIndex.value + 1
        } else {
            calculateResult()
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value = _currentQuestionIndex.value - 1
        }
    }

    fun canGoNext(): Boolean {
        val currentQuestion = questions[_currentQuestionIndex.value]
        return selectedAnswers.containsKey(currentQuestion.id)
    }

    private fun calculateResult() {
        // Выполняем синхронно, чтобы результат был установлен сразу
        val skinTypeScores = mutableMapOf<SkinType, Int>()
        val needScores = mutableMapOf<SkinNeed, Int>()
        val concernScores = mutableMapOf<SkinConcern, Int>()

        // Подсчет очков
        selectedAnswers.values.forEach { option ->
            option.skinTypePoints.forEach { (type, points) ->
                skinTypeScores[type] = (skinTypeScores[type] ?: 0) + points
            }
            option.needPoints.forEach { (need, points) ->
                needScores[need] = (needScores[need] ?: 0) + points
            }
            option.concernPoints.forEach { (concern, points) ->
                concernScores[concern] = (concernScores[concern] ?: 0) + points
            }
        }

        // Определение типа кожи
        val skinType = skinTypeScores.maxByOrNull { it.value }?.key ?: SkinType.NORMAL

        // Определение основных потребностей
        val sortedNeeds = needScores.toList().sortedByDescending { it.second }
        val primaryNeed = sortedNeeds.firstOrNull()?.first ?: SkinNeed.HYDRATION
        val secondaryNeeds = sortedNeeds.drop(1).take(2).map { it.first }

        // Определение проблем
        val concerns = concernScores.toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }

        // Определение возрастной группы (из вопроса 3)
        val ageAnswer = selectedAnswers[3]?.text ?: "26-35 лет"
        val ageGroup = when {
            ageAnswer.contains("18-25") -> AgeGroup.YOUNG
            ageAnswer.contains("26-35") -> AgeGroup.ADULT
            ageAnswer.contains("36-45") -> AgeGroup.MATURE
            else -> AgeGroup.SENIOR
        }

        _testResult.value = SkinTestResult(
            skinType = skinType,
            primaryNeed = primaryNeed,
            secondaryNeeds = secondaryNeeds,
            concerns = concerns,
            ageGroup = ageGroup
        )
    }

    fun resetTest() {
        _currentQuestionIndex.value = 0
        selectedAnswers.clear()
        _selectedAnswers.value = emptyMap()
        _testResult.value = null
    }

    fun getCurrentQuestion(): TestQuestion {
        return questions[_currentQuestionIndex.value]
    }

    fun getSelectedAnswerForCurrentQuestion(): TestOption? {
        val currentQuestion = getCurrentQuestion()
        return selectedAnswers[currentQuestion.id]
    }
}

