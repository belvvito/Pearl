package com.beutystore.pearl.data.utils

object SearchSuggestions {

    private val commonRussianSearches = listOf(
        "крем", "помада", "тушь", "тональный", "шампунь",
        "духи", "парфюм", "маска", "сыворотка", "лосьон",
        "гель", "скраб", "бальзам", "кондиционер", "пудра",
        "тени", "румяна", "хайлайтер", "консилер", "праймер",
        "лак", "дезодорант", "мыло", "масло", "спрей"
    )

    private val brandSuggestions = listOf(
        "l'oreal", "maybelline", "nyx", "cerave", "vichy",
        "la roche-posay", "schwarzkopf", "pantene", "nivea",
        "gillette", "chanel", "huda beauty"
    )

    fun getSuggestions(query: String): List<String> {
        if (query.length < 2) return emptyList()

        val normalizedQuery = SearchUtils.normalizeRussianText(query)

        val allSuggestions = commonRussianSearches + brandSuggestions

        return allSuggestions
            .filter { it.contains(normalizedQuery) }
            .take(5) // Ограничиваем количество подсказок
    }
}