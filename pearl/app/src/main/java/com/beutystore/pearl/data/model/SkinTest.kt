package com.beutystore.pearl.data.model

data class SkinTestResult(
    val skinType: SkinType,
    val primaryNeed: SkinNeed,
    val secondaryNeeds: List<SkinNeed>,
    val concerns: List<SkinConcern>,
    val ageGroup: AgeGroup
)

enum class SkinType(val displayName: String) {
    DRY("Сухая"),
    OILY("Жирная"),
    COMBINATION("Комбинированная"),
    NORMAL("Нормальная"),
    SENSITIVE("Чувствительная")
}

enum class SkinNeed(val displayName: String) {
    HYDRATION("Увлажнение"),
    NUTRITION("Питание"),
    PROTECTION("Защита"),
    REGENERATION("Регенерация"),
    MATTIFICATION("Матирование"),
    SOOTHING("Успокоение")
}

enum class SkinConcern(val displayName: String) {
    ACNE("Акне"),
    WRINKLES("Морщины"),
    DARK_SPOTS("Пигментация"),
    REDNESS("Покраснения"),
    DRYNESS("Сухость"),
    OILINESS("Жирность"),
    SENSITIVITY("Чувствительность")
}

enum class AgeGroup(val displayName: String) {
    YOUNG("18-25"),
    ADULT("26-35"),
    MATURE("36-45"),
    SENIOR("45+")
}

data class TestQuestion(
    val id: Int,
    val question: String,
    val options: List<TestOption>
)

data class TestOption(
    val text: String,
    val skinTypePoints: Map<SkinType, Int> = emptyMap(),
    val needPoints: Map<SkinNeed, Int> = emptyMap(),
    val concernPoints: Map<SkinConcern, Int> = emptyMap()
)

