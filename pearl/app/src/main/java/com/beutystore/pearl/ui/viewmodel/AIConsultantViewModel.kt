// ui/viewmodel/AIConsultantViewModel.kt
package com.beutystore.pearl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.beutystore.pearl.data.repository.ProductRepository

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// База знаний о продуктах
data class ProductInfo(
    val id: Int,
    val name: String,
    val brand: String?,
    val category: String,
    val price: Int,
    val rating: Float,
    val description: String,
    val features: List<String>,
    val skinTypes: List<String>, // Для какого типа кожи подходит
    val keywords: List<String> // Ключевые слова для поиска
)

// База знаний о брендах
data class BrandInfo(
    val name: String,
    val description: String,
    val specialties: List<String>, // Специализация бренда
    val priceRange: String, // Бюджетный, Средний, Премиум
    val popularProducts: List<String>
)

class AIConsultantViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Кэш продуктов из API
    private var cachedProducts: List<ProductInfo> = emptyList()
    private var productsLoaded = false

    // База знаний о продуктах (fallback если API недоступен)
    private val fallbackProductsDatabase = listOf(
        ProductInfo(
            id = 1,
            name = "Увлажняющий крем с гиалуроновой кислотой",
            brand = "L'Oreal",
            category = "Уход за лицом",
            price = 1200,
            rating = 4.5f,
            description = "Интенсивное увлажнение для сухой кожи с гиалуроновой кислотой",
            features = listOf("Увлажнение", "Гиалуроновая кислота", "24 часа"),
            skinTypes = listOf("сухая", "нормальная", "обезвоженная"),
            keywords = listOf("увлажнение", "гиалуроновая кислота", "крем", "сухая кожа", "l'oreal")
        ),
        ProductInfo(
            id = 2,
            name = "Питательный крем для зрелой кожи",
            brand = "Maybelline",
            category = "Уход за лицом",
            price = 2500,
            rating = 4.7f,
            description = "Глубокое питание и регенерация для зрелой кожи",
            features = listOf("Питание", "Регенерация", "Витамины"),
            skinTypes = listOf("зрелая", "сухая", "нормальная"),
            keywords = listOf("питание", "регенерация", "зрелая кожа", "омоложение", "maybelline")
        ),
        ProductInfo(
            id = 3,
            name = "Матирующий крем для жирной кожи",
            brand = "Pearl",
            category = "Уход за лицом",
            price = 1800,
            rating = 4.3f,
            description = "Контроль жирности и матирование для жирной кожи",
            features = listOf("Матирование", "Контроль жирности", "SPF 15"),
            skinTypes = listOf("жирная", "комбинированная"),
            keywords = listOf("матирование", "жирная кожа", "контроль жирности", "spf", "pearl")
        ),
        ProductInfo(
            id = 4,
            name = "Успокаивающий крем для чувствительной кожи",
            brand = "L'Oreal",
            category = "Уход за лицом",
            price = 1500,
            rating = 4.6f,
            description = "Нежное успокоение и защита для чувствительной кожи",
            features = listOf("Успокоение", "Гипоаллергенно", "Для чувствительной кожи"),
            skinTypes = listOf("чувствительная", "раздраженная", "аллергичная"),
            keywords = listOf("успокоение", "чувствительная кожа", "гипоаллергенно", "l'oreal")
        ),
        ProductInfo(
            id = 5,
            name = "Защитный крем с SPF 50",
            brand = "Maybelline",
            category = "Уход за лицом",
            price = 2000,
            rating = 4.8f,
            description = "Защита от солнца и окружающей среды с SPF 50",
            features = listOf("SPF 50", "Защита", "УФ-фильтры"),
            skinTypes = listOf("все типы"),
            keywords = listOf("spf", "защита", "солнце", "uv", "maybelline", "spf50")
        ),
        ProductInfo(
            id = 6,
            name = "Восстанавливающая сыворотка",
            brand = "Pearl",
            category = "Уход за лицом",
            price = 3000,
            rating = 4.9f,
            description = "Интенсивное восстановление и регенерация",
            features = listOf("Регенерация", "Восстановление", "Антиоксиданты"),
            skinTypes = listOf("все типы", "зрелая", "проблемная"),
            keywords = listOf("сыворотка", "восстановление", "регенерация", "антиоксиданты", "pearl")
        )
    )

    // База знаний о брендах
    private val brandsDatabase = listOf(
        BrandInfo(
            name = "L'Oreal",
            description = "Французский косметический бренд с более чем 100-летней историей",
            specialties = listOf("Уход за кожей", "Увлажнение", "Антивозрастной уход", "Для чувствительной кожи"),
            priceRange = "Средний",
            popularProducts = listOf("Увлажняющие кремы", "Сыворотки", "Средства для чувствительной кожи")
        ),
        BrandInfo(
            name = "Maybelline",
            description = "Американский бренд, известный качественной косметикой по доступным ценам",
            specialties = listOf("Макияж", "Защита от солнца", "Антивозрастной уход", "Питательные средства"),
            priceRange = "Бюджетный-Средний",
            popularProducts = listOf("Тональные кремы", "SPF защита", "Питательные кремы")
        ),
        BrandInfo(
            name = "Pearl",
            description = "Премиальный бренд с инновационными формулами для профессионального ухода",
            specialties = listOf("Премиум уход", "Матирование", "Восстановление", "Сыворотки"),
            priceRange = "Премиум",
            popularProducts = listOf("Сыворотки", "Матирующие средства", "Восстанавливающие кремы")
        )
    )

    init {
        // Загружаем продукты из API
        loadProductsFromApi()
        
        // Приветственное сообщение
        val welcomeMessage = ChatMessage(
            text = "Привет! Я ваш AI-консультант по косметике. Могу помочь с выбором средств, уходом за кожей, макияжем и другими вопросами. " +
                    "Я знаю все товары в нашем магазине и могу порекомендовать конкретные продукты. " +
                    "Что вас интересует?",
            isUser = false
        )
        _messages.value = listOf(welcomeMessage)
    }

    private fun loadProductsFromApi() {
        viewModelScope.launch {
            try {
                val result = productRepository.getProducts()
                result.onSuccess { products ->
                    cachedProducts = products.map { product ->
                        ProductInfo(
                            id = product.id,
                            name = product.name,
                            brand = product.brand,
                            category = product.category,
                            price = product.price,
                            rating = product.rating,
                            description = product.description,
                            features = product.features,
                            skinTypes = extractSkinTypes(product),
                            keywords = extractKeywords(product)
                        )
                    }
                    productsLoaded = true
                }.onFailure {
                    // Используем fallback данные если API недоступен
                    cachedProducts = fallbackProductsDatabase
                    productsLoaded = false
                }
            } catch (e: Exception) {
                // Используем fallback данные
                cachedProducts = fallbackProductsDatabase
                productsLoaded = false
            }
        }
    }

    private fun extractSkinTypes(product: com.beutystore.pearl.data.model.Product): List<String> {
        // Извлекаем типы кожи из описания и особенностей
        val description = product.description.lowercase()
        val features = product.features.joinToString(" ").lowercase()
        val combined = "$description $features"
        
        val skinTypes = mutableListOf<String>()
        if (combined.contains("сух") || combined.contains("увлажн")) skinTypes.add("сухая")
        if (combined.contains("жирн") || combined.contains("матир")) skinTypes.add("жирная")
        if (combined.contains("чувствит") || combined.contains("гипоаллерг")) skinTypes.add("чувствительная")
        if (combined.contains("зрел") || combined.contains("омолож")) skinTypes.add("зрелая")
        if (combined.contains("комбин")) skinTypes.add("комбинированная")
        if (skinTypes.isEmpty()) skinTypes.add("все типы")
        
        return skinTypes
    }

    private fun extractKeywords(product: com.beutystore.pearl.data.model.Product): List<String> {
        val keywords = mutableListOf<String>()
        keywords.add(product.name.lowercase())
        product.brand?.let { keywords.add(it.lowercase()) }
        keywords.add(product.category.lowercase())
        keywords.addAll(product.features.map { it.lowercase() })
        keywords.addAll(extractSkinTypes(product))
        return keywords.distinct()
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        val userMessage = ChatMessage(text = text.trim(), isUser = true)
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(userMessage)
        _messages.value = currentMessages

        // Имитация ответа AI
        _isLoading.value = true
        viewModelScope.launch {
            val delayMs = 1500L + (text.length * 15L).coerceAtMost(3000L)
            delay(delayMs) // Имитация задержки для более реалистичного опыта
            val aiResponse = generateAIResponse(text, currentMessages)
            val aiMessage = ChatMessage(text = aiResponse, isUser = false)
            val updatedMessages = _messages.value.toMutableList()
            updatedMessages.add(aiMessage)
            _messages.value = updatedMessages
            _isLoading.value = false
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                text = "Привет! Я ваш AI-консультант по косметике. Могу помочь с выбором средств, уходом за кожей, макияжем и другими вопросами. " +
                        "Я знаю все товары в нашем магазине и могу порекомендовать конкретные продукты от L'Oreal, Maybelline, Pearl и других брендов. " +
                        "Что вас интересует?",
                isUser = false
            )
        )
    }

    private fun generateAIResponse(userMessage: String, conversationHistory: List<ChatMessage> = emptyList()): String {
        val lowerMessage = userMessage.lowercase()
        
        // Анализируем контекст из истории разговора
        val context = analyzeConversationContext(conversationHistory, lowerMessage)
        
        // Определяем тип запроса для более точного ответа
        val queryType = analyzeQueryType(lowerMessage, context)
        
        // Поиск конкретных продуктов с учетом контекста
        var matchingProducts = findMatchingProducts(lowerMessage, context)
        
        // Если не нашли продукты, но есть контекст (тип кожи или тип продукта), ищем более широко
        if (matchingProducts.isEmpty() && (context.skinType != null || context.productType != null)) {
            matchingProducts = findMatchingProductsByContext(context)
        }
        
        if (matchingProducts.isNotEmpty()) {
            return formatProductRecommendation(matchingProducts, lowerMessage, context, queryType)
        }

        // Поиск информации о брендах
        val matchingBrand = findMatchingBrand(lowerMessage)
        if (matchingBrand != null) {
            return formatBrandInfo(matchingBrand, lowerMessage, context)
        }

        return when {
            lowerMessage.contains("крем") || lowerMessage.contains("увлажн") -> {
                val productsToSearch = if (productsLoaded && cachedProducts.isNotEmpty()) cachedProducts else fallbackProductsDatabase
                
                // Если указан тип кожи, ищем кремы для этого типа
                var creamProducts = productsToSearch.filter { 
                    it.name.lowercase().contains("крем") || it.category.lowercase().contains("крем")
                }
                
                if (context.skinType != null) {
                    creamProducts = creamProducts.filter { 
                        it.skinTypes.any { st -> st.contains(context.skinType!!, ignoreCase = true) } || 
                        it.skinTypes.contains("все типы")
                    }
                }
                
                if (creamProducts.isEmpty()) {
                    val hydrationProducts = productsToSearch.filter { 
                        it.keywords.any { kw -> kw.contains("увлажн") || kw.contains("гиалурон") } 
                    }
                    if (hydrationProducts.isNotEmpty()) {
                        formatProductRecommendation(hydrationProducts, lowerMessage, context, queryType)
                    } else {
                        buildDetailedHydrationResponse(context)
                    }
                } else {
                    formatProductRecommendation(creamProducts, lowerMessage, context, queryType)
                }
            }
            lowerMessage.contains("макияж") || lowerMessage.contains("тональн") -> {
                buildDetailedMakeupResponse(context)
            }
            lowerMessage.contains("сыворотка") || lowerMessage.contains("сыворотк") -> {
                val productsToSearch = if (productsLoaded && cachedProducts.isNotEmpty()) cachedProducts else fallbackProductsDatabase
                val serumProducts = productsToSearch.filter { 
                    it.name.lowercase().contains("сыворотк") || it.keywords.any { kw -> kw.contains("сыворотк") }
                }
                if (serumProducts.isNotEmpty()) {
                    formatProductRecommendation(serumProducts, lowerMessage, context, queryType)
                } else {
                    buildDetailedSerumResponse(context)
                }
            }
            lowerMessage.contains("очищен") || lowerMessage.contains("умыван") -> {
                buildDetailedCleansingResponse(context)
            }
            lowerMessage.contains("spf") || lowerMessage.contains("солнце") || lowerMessage.contains("защит") -> {
                val productsToSearch = if (productsLoaded && cachedProducts.isNotEmpty()) cachedProducts else fallbackProductsDatabase
                val spfProducts = productsToSearch.filter { 
                    it.keywords.any { kw -> kw.contains("spf") || kw.contains("защит") } 
                }
                if (spfProducts.isNotEmpty()) {
                    formatProductRecommendation(spfProducts, lowerMessage, context, queryType)
                } else {
                    buildDetailedSPFResponse(context)
                }
            }
            lowerMessage.contains("волос") || lowerMessage.contains("шампунь") -> {
                "Для здоровых волос важен правильный подбор шампуня под тип волос и кожи головы. " +
                        "Используйте кондиционер или маску после каждого мытья. Не мойте волосы слишком часто - " +
                        "это может пересушить кожу головы. Для окрашенных волос используйте специальные средства."
            }
            lowerMessage.contains("парфюм") || lowerMessage.contains("духи") -> {
                "Парфюм наносится на пульсирующие точки: запястья, за ушами, на шее. " +
                        "Не трите запястья друг о друга - это разрушает аромат. " +
                        "Для стойкости наносите на увлажненную кожу или используйте парфюмированное масло."
            }
            lowerMessage.contains("цена") || lowerMessage.contains("стоимость") || lowerMessage.contains("сколько") -> {
                val productsToSearch = if (productsLoaded && cachedProducts.isNotEmpty()) cachedProducts else fallbackProductsDatabase
                if (context.skinType != null || context.productType != null) {
                    val relevantProducts = findMatchingProductsByContext(context)
                    if (relevantProducts.isNotEmpty()) {
                        formatProductRecommendation(relevantProducts, lowerMessage, context.copy(wantsAlternatives = true), queryType)
                    } else {
                        "Цены на косметику в нашем магазине варьируются в зависимости от бренда и типа продукта. " +
                                "У нас есть товары разных ценовых категорий - от бюджетных до премиум. " +
                                "Могу помочь подобрать оптимальный вариант в вашем бюджете!"
                    }
                } else {
                    "Цены на косметику в нашем магазине варьируются в зависимости от бренда и типа продукта. " +
                            "У нас есть товары разных ценовых категорий - от бюджетных до премиум. " +
                            "Могу помочь подобрать оптимальный вариант в вашем бюджете!"
                }
            }
            lowerMessage.contains("скидк") || lowerMessage.contains("акция") || lowerMessage.contains("распродаж") -> {
                "Следите за нашими акциями и специальными предложениями! У нас регулярно проходят распродажи " +
                        "с скидками до 50%. Также действует система бонусных баллов. " +
                        "Проверьте раздел 'Специальные предложения' на главной странице."
            }
            lowerMessage.contains("рекоменд") || lowerMessage.contains("посовет") || lowerMessage.contains("что выбрать") -> {
                // Если есть конкретный контекст (тип кожи или продукта), ищем продукты
                if (context.skinType != null || context.productType != null) {
                    val relevantProducts = findMatchingProductsByContext(context)
                    if (relevantProducts.isNotEmpty()) {
                        formatProductRecommendation(relevantProducts, lowerMessage, context.copy(wantsAlternatives = true), queryType)
                    } else {
                        buildDetailedRecommendationsResponse(context)
                    }
                } else {
                    buildDetailedRecommendationsResponse(context)
                }
            }
            lowerMessage.contains("привет") || lowerMessage.contains("здравств") -> {
                "Привет! Рад помочь вам с выбором косметики. Расскажите, что вас интересует?"
            }
            lowerMessage.contains("спасибо") || lowerMessage.contains("благодар") -> {
                "Пожалуйста! Всегда рад помочь. Если возникнут еще вопросы - обращайтесь!"
            }
            else -> {
                buildDetailedGeneralResponse(lowerMessage, context)
            }
        }
    }

    // Анализ контекста разговора
    data class ConversationContext(
        val skinType: String? = null,
        val mentionedBrands: List<String> = emptyList(),
        val mentionedProducts: List<String> = emptyList(),
        val productType: String? = null, // крем, сыворотка, маска и т.д.
        val priceRange: String? = null,
        val concerns: List<String> = emptyList(),
        val wantsAlternatives: Boolean = false // хочет ли пользователь альтернативные варианты
    )
    
    private fun analyzeConversationContext(history: List<ChatMessage>, currentMessage: String): ConversationContext {
        val allText = (history.map { it.text } + currentMessage).joinToString(" ").lowercase()
        
        val skinTypes = listOf("сухая", "жирная", "комбинированная", "чувствительная", "зрелая", "нормальная", "проблемная")
        val detectedSkinType = skinTypes.firstOrNull { allText.contains(it) }
        
        val brands = listOf("l'oreal", "maybelline", "pearl", "loreal")
        val mentionedBrands = brands.filter { allText.contains(it) }
        
        // Определяем тип продукта
        val productTypes = mapOf(
            "крем" to listOf("крем", "крема", "кремом"),
            "сыворотка" to listOf("сыворотка", "сыворотки", "сыворотку", "сыворотк"),
            "маска" to listOf("маска", "маски", "маску"),
            "тоник" to listOf("тоник", "тоники", "тоником"),
            "скраб" to listOf("скраб", "скрабы", "скрабом"),
            "гель" to listOf("гель", "гели", "гелем"),
            "лосьон" to listOf("лосьон", "лосьоны", "лосьоном"),
            "спрей" to listOf("спрей", "спреи", "спреем"),
            "масло" to listOf("масло", "масла", "маслом"),
            "эмульсия" to listOf("эмульсия", "эмульсии", "эмульсией")
        )
        val detectedProductType = productTypes.entries.firstOrNull { (_, keywords) ->
            keywords.any { allText.contains(it) }
        }?.key
        
        val concerns = listOf("акне", "прыщ", "морщин", "пигмент", "покраснен", "раздражен", "сухость", "жирность", "расширен", "пор")
        val detectedConcerns = concerns.filter { allText.contains(it) }
        
        val priceKeywords = listOf("бюджет", "дешев", "дорог", "премиум", "эконом", "люкс", "люксов", "бюджетн", "недорог", "дешевле")
        val priceRange = when {
            allText.contains("бюджет") || allText.contains("дешев") || allText.contains("эконом") || 
            allText.contains("бюджетн") || allText.contains("недорог") || allText.contains("дешевле") -> "бюджетный"
            allText.contains("премиум") || allText.contains("дорог") || allText.contains("люкс") || 
            allText.contains("люксов") || allText.contains("дороже") -> "премиум"
            else -> null
        }
        
        // Определяем, хочет ли пользователь альтернативные варианты
        val wantsAlternatives = allText.contains("альтернатив") || allText.contains("вариант") || 
                               allText.contains("еще") || allText.contains("друг") ||
                               allText.contains("бюджетн") || allText.contains("люкс") ||
                               allText.contains("дешев") || allText.contains("дорог")
        
        return ConversationContext(
            skinType = detectedSkinType,
            mentionedBrands = mentionedBrands,
            productType = detectedProductType,
            priceRange = priceRange,
            concerns = detectedConcerns,
            wantsAlternatives = wantsAlternatives
        )
    }
    
    // Определение типа запроса
    data class QueryType(
        val isProductSearch: Boolean = false,
        val isAdviceRequest: Boolean = false,
        val isComparison: Boolean = false,
        val isGeneralQuestion: Boolean = false
    )
    
    private fun analyzeQueryType(message: String, context: ConversationContext): QueryType {
        val lower = message.lowercase()
        return QueryType(
            isProductSearch = lower.contains("найди") || lower.contains("есть") || lower.contains("купить") || 
                             lower.contains("продает") || lower.contains("товар") || lower.contains("подбери") ||
                             lower.contains("подобрать") || lower.contains("подбор") || lower.contains("рекомендуй") ||
                             lower.contains("посоветуй") || lower.contains("дай") || lower.contains("покажи"),
            isAdviceRequest = lower.contains("как") || lower.contains("что делать") || lower.contains("совет") || 
                            lower.contains("рекоменд") || lower.contains("посовет"),
            isComparison = lower.contains("лучше") || lower.contains("сравн") || lower.contains("разница") || 
                          lower.contains("чем отличается"),
            isGeneralQuestion = lower.contains("что такое") || lower.contains("объясн") || lower.contains("расскажи")
        )
    }
    
    // Поиск подходящих продуктов по запросу с учетом контекста
    private fun findMatchingProducts(query: String, context: ConversationContext): List<ProductInfo> {
        val productsToSearch = if (productsLoaded && cachedProducts.isNotEmpty()) {
            cachedProducts
        } else {
            fallbackProductsDatabase
        }
        
        var filtered = productsToSearch
        
        // Если указан тип продукта, фильтруем по нему
        if (context.productType != null) {
            filtered = filtered.filter { product ->
                product.name.lowercase().contains(context.productType!!) ||
                product.category.lowercase().contains(context.productType!!) ||
                product.keywords.any { it.contains(context.productType!!) } ||
                product.description.lowercase().contains(context.productType!!)
            }
        }
        
        // Если указан тип кожи, фильтруем по нему
        if (context.skinType != null) {
            filtered = filtered.filter { 
                it.skinTypes.any { st -> st.contains(context.skinType!!, ignoreCase = true) } || 
                it.skinTypes.contains("все типы")
            }
        }
        
        // Если указаны бренды, фильтруем по ним
        if (context.mentionedBrands.isNotEmpty()) {
            filtered = filtered.filter { 
                it.brand?.lowercase() in context.mentionedBrands.map { it.lowercase() }
            }
        }
        
        // Если указан ценовой диапазон, фильтруем по нему
        if (context.priceRange != null) {
            filtered = when (context.priceRange) {
                "бюджетный" -> filtered.filter { it.price < 2000 }
                "премиум" -> filtered.filter { it.price >= 2500 }
                else -> filtered
            }
        }
        
        // Если нет фильтров, но есть ключевые слова в запросе
        if (filtered.isEmpty() || (context.productType == null && context.skinType == null)) {
            val queryFiltered = productsToSearch.filter { product ->
                product.keywords.any { keyword -> query.contains(keyword) } ||
                product.name.lowercase().contains(query) ||
                (product.brand?.lowercase()?.contains(query) == true) ||
                product.description.lowercase().contains(query) ||
                product.skinTypes.any { skinType -> query.contains(skinType) } ||
                product.features.any { feature -> query.contains(feature.lowercase()) }
            }
            
            // Объединяем результаты
            filtered = (filtered + queryFiltered).distinctBy { it.id }
        }
        
        return filtered
    }

    // Поиск продуктов только по контексту (когда запрос не содержит ключевых слов)
    private fun findMatchingProductsByContext(context: ConversationContext): List<ProductInfo> {
        val productsToSearch = if (productsLoaded && cachedProducts.isNotEmpty()) {
            cachedProducts
        } else {
            fallbackProductsDatabase
        }
        
        var filtered = productsToSearch
        
        // Фильтруем по типу продукта
        if (context.productType != null) {
            filtered = filtered.filter { product ->
                product.name.lowercase().contains(context.productType!!) ||
                product.category.lowercase().contains(context.productType!!) ||
                product.keywords.any { it.contains(context.productType!!) } ||
                product.description.lowercase().contains(context.productType!!)
            }
        }
        
        // Фильтруем по типу кожи
        if (context.skinType != null) {
            filtered = filtered.filter { 
                it.skinTypes.any { st -> st.contains(context.skinType!!, ignoreCase = true) } || 
                it.skinTypes.contains("все типы")
            }
        }
        
        // Фильтруем по брендам
        if (context.mentionedBrands.isNotEmpty()) {
            filtered = filtered.filter { 
                it.brand?.lowercase() in context.mentionedBrands.map { it.lowercase() }
            }
        }
        
        // Фильтруем по ценовому диапазону
        if (context.priceRange != null) {
            filtered = when (context.priceRange) {
                "бюджетный" -> filtered.filter { it.price < 2000 }
                "премиум" -> filtered.filter { it.price >= 2500 }
                else -> filtered
            }
        }
        
        return filtered
    }
    
    // Поиск информации о бренде
    private fun findMatchingBrand(query: String): BrandInfo? {
        return brandsDatabase.firstOrNull { brand ->
            query.contains(brand.name.lowercase()) ||
            brand.name.lowercase().contains(query)
        }
    }

    // Форматирование рекомендации продукта с учетом контекста
    private fun formatProductRecommendation(
        products: List<ProductInfo>, 
        query: String = "",
        context: ConversationContext = ConversationContext(),
        queryType: QueryType = QueryType()
    ): String {
        if (products.isEmpty()) {
            return buildDetailedNoProductsResponse(query, context)
        }

        val response = StringBuilder()
        
        // Разделяем продукты по ценовым категориям
        val budgetProducts = products.filter { it.price < 2000 }.sortedBy { it.price }
        val midRangeProducts = products.filter { it.price >= 2000 && it.price < 2500 }.sortedBy { it.price }
        val premiumProducts = products.filter { it.price >= 2500 }.sortedBy { it.price }
        
        // Если пользователь хочет альтернативы или не указал ценовой диапазон, показываем все категории
        val showAlternatives = context.wantsAlternatives || context.priceRange == null
        
        if (products.size == 1 && !showAlternatives) {
            // Один продукт без запроса альтернатив
            val product = products.first()
            response.append("✨ Отличный выбор! Вот подробная информация:\n\n")
            response.append("📦 ${product.name}\n")
            if (product.brand != null) {
                response.append("🏷️ Бренд: ${product.brand}\n")
            }
            response.append("💰 Цена: ${product.price} ₽")
            when {
                product.price < 1500 -> response.append(" (отличное соотношение цена/качество!)\n")
                product.price < 2500 -> response.append(" (средний ценовой сегмент)\n")
                else -> response.append(" (премиум сегмент)\n")
            }
            response.append("⭐ Рейтинг: ${product.rating}/5.0\n\n")
            response.append("📝 Описание:\n${product.description}\n\n")
            response.append("✨ Ключевые особенности:\n")
            product.features.forEach { feature ->
                response.append("• $feature\n")
            }
            response.append("\n👤 Подходит для: ${product.skinTypes.joinToString(", ")} кожи\n\n")
            
            // Добавляем советы по использованию
            response.append("💡 Как использовать:\n")
            when {
                product.name.lowercase().contains("крем") -> {
                    response.append("• Наносите на очищенную кожу утром и вечером\n")
                    response.append("• Используйте легкими массажными движениями\n")
                    response.append("• Для лучшего эффекта нанесите сыворотку перед кремом\n")
                }
                product.name.lowercase().contains("сыворотк") -> {
                    response.append("• Наносите на очищенную кожу перед кремом\n")
                    response.append("• Используйте 2-3 капли, аккуратно вбивая в кожу\n")
                    response.append("• Подождите 2-3 минуты перед нанесением крема\n")
                }
                product.keywords.any { it.contains("spf") } -> {
                    response.append("• Наносите за 15-20 минут до выхода на солнце\n")
                    response.append("• Обновляйте каждые 2 часа при активном солнце\n")
                    response.append("• Используйте ежедневно, даже в пасмурную погоду\n")
                }
            }
            response.append("\n📱 Этот продукт доступен в нашем каталоге. Хотите узнать больше о других товарах?")
        } else {
            // Несколько продуктов или запрос альтернатив
            val hasBudget = budgetProducts.isNotEmpty()
            val hasMidRange = midRangeProducts.isNotEmpty()
            val hasPremium = premiumProducts.isNotEmpty()
            
            response.append("✨ Нашел отличные варианты для вас!\n\n")
            
            // Показываем бюджетные варианты
            if (hasBudget && (showAlternatives || context.priceRange == "бюджетный")) {
                response.append("💰 Бюджетные варианты (до 2000 ₽):\n\n")
                budgetProducts.take(2).forEachIndexed { index, product ->
                    val brandText = if (product.brand != null) " от ${product.brand}" else ""
                    response.append("${index + 1}. ${product.name}${brandText}\n")
                    response.append("   💰 ${product.price} ₽ | ⭐ ${product.rating}/5.0\n")
                    response.append("   📝 ${product.description}\n")
                    response.append("   👤 Для: ${product.skinTypes.joinToString(", ")} кожи\n")
                    if (product.features.isNotEmpty()) {
                        response.append("   ✨ ${product.features.take(2).joinToString(", ")}\n")
                    }
                    response.append("\n")
                }
            }
            
            // Показываем средний ценовой сегмент
            if (hasMidRange && (showAlternatives || context.priceRange == null)) {
                response.append("💎 Средний ценовой сегмент (2000-2500 ₽):\n\n")
                midRangeProducts.take(2).forEachIndexed { index, product ->
                    val brandText = if (product.brand != null) " от ${product.brand}" else ""
                    response.append("${index + 1}. ${product.name}${brandText}\n")
                    response.append("   💰 ${product.price} ₽ | ⭐ ${product.rating}/5.0\n")
                    response.append("   📝 ${product.description}\n")
                    response.append("   👤 Для: ${product.skinTypes.joinToString(", ")} кожи\n")
                    if (product.features.isNotEmpty()) {
                        response.append("   ✨ ${product.features.take(2).joinToString(", ")}\n")
                    }
                    response.append("\n")
                }
            }
            
            // Показываем премиум варианты
            if (hasPremium && (showAlternatives || context.priceRange == "премиум")) {
                response.append("👑 Премиум варианты (от 2500 ₽):\n\n")
                premiumProducts.take(2).forEachIndexed { index, product ->
                    val brandText = if (product.brand != null) " от ${product.brand}" else ""
                    response.append("${index + 1}. ${product.name}${brandText}\n")
                    response.append("   💰 ${product.price} ₽ | ⭐ ${product.rating}/5.0\n")
                    response.append("   📝 ${product.description}\n")
                    response.append("   👤 Для: ${product.skinTypes.joinToString(", ")} кожи\n")
                    if (product.features.isNotEmpty()) {
                        response.append("   ✨ ${product.features.take(2).joinToString(", ")}\n")
                    }
                    response.append("\n")
                }
            }
            
            // Если не показали альтернативы, но они есть
            if (!showAlternatives && (hasBudget && hasPremium)) {
                response.append("💡 Хотите увидеть более бюджетные или люксовые варианты? Просто спросите!\n\n")
            }
            
            val totalShown = (if (hasBudget) budgetProducts.take(2).size else 0) + 
                           (if (hasMidRange) midRangeProducts.take(2).size else 0) + 
                           (if (hasPremium) premiumProducts.take(2).size else 0)
            
            if (products.size > totalShown) {
                response.append("...и еще ${products.size - totalShown} вариантов\n\n")
            }
            
            response.append("🛍️ Все эти товары доступны в нашем магазине. Могу рассказать подробнее о любом из них или помочь сравнить варианты!")
        }

        return response.toString()
    }
    
    // Развернутый ответ об увлажнении
    private fun buildDetailedHydrationResponse(context: ConversationContext): String {
        val response = StringBuilder()
        response.append("💧 Увлажнение кожи - это основа здорового ухода!\n\n")
        
        response.append("📚 Почему это важно:\n")
        response.append("• Увлажненная кожа выглядит здоровой и сияющей\n")
        response.append("• Предотвращает преждевременное старение\n")
        response.append("• Улучшает текстуру и эластичность кожи\n")
        response.append("• Создает защитный барьер от внешних факторов\n\n")
        
        response.append("🔑 Ключевые ингредиенты для увлажнения:\n")
        response.append("• Гиалуроновая кислота - удерживает влагу в коже\n")
        response.append("• Глицерин - притягивает влагу из воздуха\n")
        response.append("• Церамиды - восстанавливают защитный барьер\n")
        response.append("• Масла (жожоба, аргановое) - питают и увлажняют\n\n")
        
        if (context.skinType != null) {
            response.append("👤 Для ${context.skinType} кожи рекомендую:\n")
            when {
                context.skinType.contains("сух") -> {
                    response.append("• Плотные кремы с маслами и церамидами\n")
                    response.append("• Наносите утром и вечером на очищенную кожу\n")
                    response.append("• Дополнительно используйте сыворотку с гиалуроновой кислотой\n")
                }
                context.skinType.contains("жирн") -> {
                    response.append("• Легкие гелевые текстуры без масел\n")
                    response.append("• Некомедогенные формулы\n")
                    response.append("• Сыворотки с гиалуроновой кислотой вместо тяжелых кремов\n")
                }
                context.skinType.contains("чувствит") -> {
                    response.append("• Гипоаллергенные средства без отдушек\n")
                    response.append("• Формулы с минимальным количеством ингредиентов\n")
                    response.append("• Тестируйте на небольшом участке перед использованием\n")
                }
            }
            response.append("\n")
        }
        
        response.append("💡 Как правильно увлажнять:\n")
        response.append("1. Очистите кожу мягким средством\n")
        response.append("2. Нанесите сыворотку (если используете)\n")
        response.append("3. Подождите 2-3 минуты\n")
        response.append("4. Нанесите крем легкими массажными движениями\n")
        response.append("5. Утром добавьте SPF защиту\n\n")
        
        response.append("🛍️ В нашем магазине есть отличные средства для увлажнения! Хотите, чтобы я подобрал конкретные продукты под ваш тип кожи?")
        
        return response.toString()
    }
    
    // Развернутый ответ, когда продукты не найдены
    private fun buildDetailedNoProductsResponse(query: String, context: ConversationContext): String {
        val response = StringBuilder()
        response.append("К сожалению, не нашел точных совпадений по запросу \"$query\".\n\n")
        response.append("💡 Попробуйте:\n")
        response.append("• Уточнить тип продукта (крем, сыворотка, маска)\n")
        response.append("• Указать тип кожи (сухая, жирная, чувствительная)\n")
        response.append("• Назвать конкретный бренд (L'Oreal, Maybelline, Pearl)\n")
        response.append("• Описать проблему, которую хотите решить\n\n")
        response.append("Или задайте общий вопрос, например:\n")
        response.append("• \"Что выбрать для сухой кожи?\"\n")
        response.append("• \"Рекомендуй крем для увлажнения\"\n")
        response.append("• \"Расскажи о продуктах L'Oreal\"\n\n")
        response.append("Я всегда готов помочь с выбором! 😊")
        return response.toString()
    }

    // Форматирование информации о бренде с учетом контекста
    private fun formatBrandInfo(brand: BrandInfo, query: String = "", context: ConversationContext = ConversationContext()): String {
        val response = StringBuilder()
        response.append("🏷️ О бренде ${brand.name}:\n\n")
        response.append("${brand.description}\n\n")
        response.append("✨ Специализация бренда:\n")
        brand.specialties.forEach { specialty ->
            response.append("• $specialty\n")
        }
        response.append("\n💰 Ценовой сегмент: ${brand.priceRange}\n")
        response.append("⭐ Популярные категории: ${brand.popularProducts.joinToString(", ")}\n\n")
        
        // Добавляем конкретные продукты этого бренда
        val productsToSearch = if (productsLoaded && cachedProducts.isNotEmpty()) cachedProducts else fallbackProductsDatabase
        var brandProducts = productsToSearch.filter { it.brand.equals(brand.name, ignoreCase = true) }
        
        // Фильтруем по контексту, если есть
        if (context.skinType != null) {
            brandProducts = brandProducts.filter { 
                it.skinTypes.any { st -> st.contains(context.skinType!!, ignoreCase = true) } || 
                it.skinTypes.contains("все типы")
            }
        }
        
        if (brandProducts.isNotEmpty()) {
            response.append("🛍️ В нашем магазине доступны:\n\n")
            brandProducts.take(5).forEach { product ->
                response.append("• ${product.name}\n")
                response.append("  💰 ${product.price} ₽ | ⭐ ${product.rating}/5.0\n")
                response.append("  📝 ${product.description}\n")
                response.append("  👤 Для: ${product.skinTypes.joinToString(", ")} кожи\n\n")
            }
            if (brandProducts.size > 5) {
                response.append("...и еще ${brandProducts.size - 5} товаров\n\n")
            }
            response.append("💡 Хотите узнать подробнее о каком-то конкретном продукте? Или помочь подобрать что-то под ваш тип кожи?")
        } else {
            response.append("🛍️ В нашем магазине представлены различные продукты этого бренда. ")
            response.append("Могу помочь подобрать конкретные средства под ваши потребности!")
        }

        return response.toString()
    }
    
    // Развернутый ответ о макияже
    private fun buildDetailedMakeupResponse(context: ConversationContext): String {
        val response = StringBuilder()
        response.append("💄 Идеальный макияж начинается с правильной подготовки!\n\n")
        response.append("📋 Пошаговая инструкция:\n\n")
        response.append("1️⃣ Подготовка кожи:\n")
        response.append("• Очистите и увлажните кожу\n")
        response.append("• Нанесите праймер для выравнивания текстуры\n")
        response.append("• Подождите 2-3 минуты перед следующим шагом\n\n")
        response.append("2️⃣ Тональная основа:\n")
        response.append("• Подберите оттенок, максимально близкий к тону кожи\n")
        response.append("• Для жирной кожи - матирующие формулы\n")
        response.append("• Для сухой кожи - увлажняющие с сиянием\n")
        response.append("• Наносите кистью, спонжем или пальцами\n\n")
        response.append("3️⃣ Закрепление:\n")
        response.append("• Используйте пудру для матирования (для жирной кожи)\n")
        response.append("• Или фиксирующий спрей для стойкости\n")
        response.append("• Для сухой кожи можно пропустить пудру\n\n")
        response.append("💡 Полезные советы:\n")
        response.append("• Всегда тестируйте тональный крем на линии челюсти\n")
        response.append("• Для естественного вида смешивайте несколько оттенков\n")
        response.append("• Не забывайте про шею - наносите крем и туда\n")
        response.append("• Обновляйте макияж в течение дня при необходимости\n\n")
        response.append("🛍️ В нашем магазине есть отличные средства для макияжа! Хотите конкретные рекомендации?")
        return response.toString()
    }
    
    // Развернутый ответ о сыворотках
    private fun buildDetailedSerumResponse(context: ConversationContext): String {
        val response = StringBuilder()
        response.append("✨ Сыворотки - это концентраты активных ингредиентов!\n\n")
        response.append("📚 Что такое сыворотка:\n")
        response.append("Сыворотки содержат высокую концентрацию активных веществ и проникают глубже в кожу, " +
                "чем обычные кремы. Они решают конкретные проблемы и работают быстрее.\n\n")
        response.append("🔑 Типы сывороток и их действие:\n")
        response.append("• Витамин C - осветляет, выравнивает тон, защищает от свободных радикалов\n")
        response.append("• Ретинол - омолаживает, разглаживает морщины, обновляет кожу\n")
        response.append("• Гиалуроновая кислота - интенсивно увлажняет, разглаживает\n")
        response.append("• Ниацинамид - сужает поры, контролирует жирность, успокаивает\n")
        response.append("• Пептиды - стимулируют выработку коллагена, подтягивают\n\n")
        response.append("💡 Как использовать:\n")
        response.append("1. Очистите кожу\n")
        response.append("2. Нанесите сыворотку (2-3 капли достаточно)\n")
        response.append("3. Аккуратно вбейте подушечками пальцев\n")
        response.append("4. Подождите 2-3 минуты для впитывания\n")
        response.append("5. Нанесите крем для закрепления эффекта\n\n")
        response.append("⚠️ Важно:\n")
        response.append("• Не смешивайте ретинол и витамин C в одном уходе\n")
        response.append("• Начинайте с низкой концентрации активных веществ\n")
        response.append("• Используйте SPF при применении ретинола и витамина C\n")
        response.append("• Вводите новые сыворотки постепенно\n\n")
        response.append("🛍️ Рекомендую обратить внимание на 'Восстанавливающую сыворотку' от Pearl - " +
                "она подходит для всех типов кожи и содержит комплекс активных ингредиентов!")
        return response.toString()
    }
    
    // Развернутый ответ об очищении
    private fun buildDetailedCleansingResponse(context: ConversationContext): String {
        val response = StringBuilder()
        response.append("🧼 Очищение - основа здоровой кожи!\n\n")
        response.append("📚 Почему это важно:\n")
        response.append("• Удаляет загрязнения, макияж и излишки кожного сала\n")
        response.append("• Подготавливает кожу к нанесению уходовых средств\n")
        response.append("• Предотвращает закупорку пор и воспаления\n")
        response.append("• Улучшает впитывание активных ингредиентов\n\n")
        response.append("💧 Правильная техника:\n")
        response.append("1. Снимите макияж специальным средством (если используете)\n")
        response.append("2. Нанесите очищающее средство на влажную кожу\n")
        response.append("3. Массируйте круговыми движениями 30-60 секунд\n")
        response.append("4. Смойте теплой (не горячей!) водой\n")
        response.append("5. Промокните лицо мягким полотенцем\n\n")
        response.append("👤 Выбор средства по типу кожи:\n")
        response.append("• Сухая кожа: кремовые или масляные текстуры, без спирта\n")
        response.append("• Жирная кожа: гелевые формулы, с салициловой кислотой\n")
        response.append("• Чувствительная: мягкие средства без отдушек и парабенов\n")
        response.append("• Комбинированная: адаптивные формулы для разных зон\n\n")
        response.append("⏰ Когда очищать:\n")
        response.append("• Утром - легкое очищение для удаления ночных выделений\n")
        response.append("• Вечером - тщательное очищение для удаления макияжа и загрязнений\n")
        response.append("• После тренировки - обязательно очистите кожу\n\n")
        response.append("⚠️ Чего избегать:\n")
        response.append("• Горячей воды - она сушит и раздражает кожу\n")
        response.append("• Агрессивного трения - это травмирует кожу\n")
        response.append("• Пересушивающих средств - они нарушают защитный барьер\n")
        response.append("• Слишком частого очищения - достаточно 2 раза в день\n\n")
        response.append("🛍️ В нашем магазине есть отличные средства для очищения! Хотите подобрать что-то конкретное?")
        return response.toString()
    }
    
    // Развернутый ответ о SPF защите
    private fun buildDetailedSPFResponse(context: ConversationContext): String {
        val response = StringBuilder()
        response.append("☀️ SPF защита - обязательный элемент ежедневного ухода!\n\n")
        response.append("📚 Почему это критически важно:\n")
        response.append("• Защищает от вредного УФ-излучения (UVA и UVB)\n")
        response.append("• Предотвращает преждевременное старение и морщины\n")
        response.append("• Снижает риск пигментации и темных пятен\n")
        response.append("• Защищает от солнечных ожогов\n")
        response.append("• Снижает риск развития рака кожи\n\n")
        response.append("🔢 Какой SPF выбрать:\n")
        response.append("• SPF 15-20 - минимальная защита, для коротких прогулок\n")
        response.append("• SPF 30 - стандартная защита для города (рекомендуется)\n")
        response.append("• SPF 50+ - максимальная защита для пляжа и активного солнца\n\n")
        response.append("💡 Правильное применение:\n")
        response.append("• Наносите за 15-20 минут до выхода на солнце\n")
        response.append("• Используйте достаточное количество (примерно 1/4 чайной ложки для лица)\n")
        response.append("• Обновляйте каждые 2 часа при активном солнце\n")
        response.append("• Наносите даже в пасмурную погоду (80% УФ проходит через облака)\n")
        response.append("• Не забывайте про шею, уши и зону декольте\n\n")
        response.append("🌍 Когда использовать:\n")
        response.append("• Круглый год, каждый день\n")
        response.append("• Особенно важно весной и летом\n")
        response.append("• Обязательно на пляже и в горах\n")
        response.append("• При работе у окна (стекло не защищает от UVA)\n\n")
        response.append("👤 Выбор по типу кожи:\n")
        response.append("• Жирная кожа: легкие текстуры, матирующие формулы\n")
        response.append("• Сухая кожа: увлажняющие с SPF\n")
        response.append("• Чувствительная: минеральные фильтры (оксид цинка, диоксид титана)\n")
        response.append("• Склонная к акне: некомедогенные формулы\n\n")
        response.append("🛍️ Рекомендую 'Защитный крем с SPF 50' от Maybelline - отличная защита, " +
                "доступная цена и подходит для всех типов кожи!")
        return response.toString()
    }
    
    // Развернутый ответ с рекомендациями
    private fun buildDetailedRecommendationsResponse(context: ConversationContext): String {
        val response = StringBuilder()
        response.append("💡 Персональные рекомендации для вас!\n\n")
        
        response.append("📋 Для начала рекомендую:\n")
        response.append("• Пройти тест кожи в приложении - это поможет определить ваш тип кожи\n")
        response.append("• Рассказать о ваших основных проблемах и целях\n")
        response.append("• Указать предпочтения по брендам и бюджету\n\n")
        
        if (context.skinType != null) {
            response.append("👤 Учитывая ваш тип кожи (${context.skinType}), рекомендую:\n\n")
            val productsToSearch = if (productsLoaded && cachedProducts.isNotEmpty()) cachedProducts else fallbackProductsDatabase
            val skinProducts = productsToSearch.filter { 
                it.skinTypes.any { st -> st.contains(context.skinType!!, ignoreCase = true) } || 
                it.skinTypes.contains("все типы")
            }
            
            if (skinProducts.isNotEmpty()) {
                skinProducts.take(3).forEach { product ->
                    response.append("✨ ${product.name}")
                    if (product.brand != null) response.append(" от ${product.brand}")
                    response.append("\n")
                    response.append("   💰 ${product.price} ₽ | ⭐ ${product.rating}/5.0\n")
                    response.append("   📝 ${product.description}\n\n")
                }
            }
        } else {
            response.append("🛍️ Популярные рекомендации:\n\n")
            response.append("• Для сухой кожи:\n")
            response.append("  'Увлажняющий крем с гиалуроновой кислотой' от L'Oreal - 1200 ₽\n")
            response.append("  Интенсивное увлажнение на 24 часа\n\n")
            response.append("• Для жирной кожи:\n")
            response.append("  'Матирующий крем' от Pearl - 1800 ₽\n")
            response.append("  Контроль жирности и матирование\n\n")
            response.append("• Для чувствительной кожи:\n")
            response.append("  'Успокаивающий крем' от L'Oreal - 1500 ₽\n")
            response.append("  Нежное успокоение и защита\n\n")
            response.append("• Для зрелой кожи:\n")
            response.append("  'Питательный крем' от Maybelline - 2500 ₽\n")
            response.append("  Глубокое питание и регенерация\n\n")
            response.append("• Для защиты от солнца:\n")
            response.append("  'Защитный крем с SPF 50' от Maybelline - 2000 ₽\n")
            response.append("  Максимальная защита от УФ-лучей\n\n")
            response.append("• Для восстановления:\n")
            response.append("  'Восстанавливающая сыворотка' от Pearl - 3000 ₽\n")
            response.append("  Интенсивное восстановление и регенерация\n\n")
        }
        
        response.append("💬 Расскажите больше о ваших потребностях, и я подберу идеальный вариант!")
        return response.toString()
    }
    
    // Развернутый общий ответ
    private fun buildDetailedGeneralResponse(message: String, context: ConversationContext): String {
        val response = StringBuilder()
        response.append("Спасибо за вопрос! Я ваш AI-консультант по косметике и уходу за собой. " +
                "Специализируюсь на помощи с выбором средств для лица, макияжа, ухода за волосами, парфюмерии и многого другого.\n\n")
        
        response.append("🎯 Чем я могу помочь:\n")
        response.append("• Подобрать продукты под ваш тип кожи\n")
        response.append("• Дать советы по уходу и применению средств\n")
        response.append("• Рассказать о брендах и их особенностях\n")
        response.append("• Помочь с выбором в рамках вашего бюджета\n")
        response.append("• Ответить на вопросы о косметике и уходе\n\n")
        
        response.append("📚 Я знаю все товары в нашем магазине и могу порекомендовать конкретные продукты!\n\n")
        response.append("💡 Задайте вопрос, например:\n")
        response.append("• \"Что выбрать для сухой кожи?\"\n")
        response.append("• \"Расскажи о продуктах L'Oreal\"\n")
        response.append("• \"Как правильно увлажнять кожу?\"\n")
        response.append("• \"Рекомендуй крем для жирной кожи\"\n")
        response.append("• \"Что такое сыворотка и как её использовать?\"\n")
        response.append("• \"Нужен SPF крем, что посоветуешь?\"\n\n")
        
        // Если есть контекст, добавляем персональные предложения
        if (context.skinType != null || context.mentionedBrands.isNotEmpty()) {
            response.append("👤 Учитывая ваш запрос")
            if (context.skinType != null) {
                response.append(" (тип кожи: ${context.skinType})")
            }
            if (context.mentionedBrands.isNotEmpty()) {
                response.append(" (бренды: ${context.mentionedBrands.joinToString(", ")})")
            }
            response.append(", могу дать более конкретные рекомендации!\n\n")
        }
        
        response.append("Задайте более конкретный вопрос, и я дам подробный развернутый ответ с рекомендациями! 😊")
        return response.toString()
    }
}

