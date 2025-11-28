package com.beutystore.pearl.data.api

import com.beutystore.pearl.data.model.AuthResponse
import com.beutystore.pearl.data.model.LoginRequest
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.data.model.RegisterRequest
import com.beutystore.pearl.data.model.User
import com.beutystore.pearl.data.model.Order
import com.beutystore.pearl.data.model.Review
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Интерфейс API сервиса для взаимодействия с бэкенд сервером.
 * 
 * Определяет все HTTP endpoints приложения:
 * - Аутентификация и регистрация пользователей
 * - Работа с товарами и категориями
 * - Управление заказами
 * - Работа с отзывами
 * 
 * Все методы являются suspend функциями для работы с корутинами.
 */
interface PearlApiService {
    // ==================== User endpoints (Пользователи) ====================
    
    /**
     * Регистрация нового пользователя.
     * @param request Данные для регистрации (username, email, phone, password и т.д.)
     * @return Ответ с данными пользователя и токенами доступа
     */
    @POST("user/register/")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    /**
     * Вход в систему по телефону и паролю.
     * @param request Данные для входа (phone, password)
     * @return Ответ с данными пользователя и токенами доступа
     */
    @POST("user/login/")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    /**
     * Вход в систему по номеру телефона (отправка кода подтверждения).
     * @param request Номер телефона
     * @return Ответ с данными пользователя и токенами доступа
     */
    @POST("user/phone_login/")
    suspend fun phoneLogin(@Body request: PhoneLoginRequest): Response<AuthResponse>

    /**
     * Подтверждение кода при входе по телефону.
     * @param request Номер телефона и код подтверждения
     * @return Ответ с данными пользователя и токенами доступа
     */
    @POST("user/verify_code/")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<AuthResponse>

    /**
     * Получение профиля текущего пользователя.
     * @param token Токен авторизации в формате "Bearer {token}"
     * @return Данные профиля пользователя
     */
    @GET("user/profile/")
    suspend fun getProfile(@Header("Authorization") token: String): Response<User>

    // ==================== Product endpoints (Товары) ====================
    
    /**
     * Получение списка товаров с возможностью фильтрации.
     * @param category Фильтр по категории (опционально)
     * @param search Поисковый запрос (опционально)
     * @param brand Фильтр по бренду (опционально)
     * @param page Номер страницы для пагинации (опционально)
     * @param pageSize Размер страницы (опционально)
     * @return Список товаров с метаданными пагинации
     */
    @GET("products/")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("brand") brand: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<ProductListResponse>

    /**
     * Получение детальной информации о товаре по ID.
     * @param id ID товара
     * @return Данные товара
     */
    @GET("products/{id}/")
    suspend fun getProduct(@Path("id") id: Int): Response<Product>

    /**
     * Получение популярных товаров.
     * @param limit Максимальное количество товаров (опционально)
     * @return Список популярных товаров
     */
    @GET("products/popular/")
    suspend fun getPopularProducts(@Query("limit") limit: Int? = null): Response<ProductListResponse>

    /**
     * Получение рекомендованных товаров для пользователя.
     * @param token Токен авторизации (опционально, для персонализированных рекомендаций)
     * @param limit Максимальное количество товаров (опционально)
     * @return Список рекомендованных товаров
     */
    @GET("products/recommended/")
    suspend fun getRecommendedProducts(
        @Header("Authorization") token: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<ProductListResponse>

    // ==================== Categories endpoint (Категории) ====================
    
    /**
     * Получение списка всех категорий товаров.
     * @return Список названий категорий
     */
    @GET("categories/")
    suspend fun getCategories(): Response<List<String>>

    // ==================== Orders endpoints (Заказы) ====================
    
    /**
     * Получение всех заказов текущего пользователя.
     * @param token Токен авторизации
     * @return Список заказов пользователя
     */
    @GET("orders/my_orders/")
    suspend fun getMyOrders(@Header("Authorization") token: String): Response<List<Order>>

    /**
     * Получение доставленных заказов текущего пользователя.
     * @param token Токен авторизации
     * @return Список доставленных заказов
     */
    @GET("orders/delivered_orders/")
    suspend fun getDeliveredOrders(@Header("Authorization") token: String): Response<List<Order>>

    /**
     * Создание нового заказа.
     * @param token Токен авторизации
     * @param request Данные заказа (товары, адрес доставки, контакты и т.д.)
     * @return Созданный заказ
     */
    @POST("orders/")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): Response<Order>

    // ==================== Reviews endpoints (Отзывы) ====================
    
    /**
     * Получение отзывов о товарах.
     * @param productId ID товара для фильтрации (опционально, если null - все отзывы)
     * @return Список отзывов
     */
    @GET("reviews/")
    suspend fun getReviews(@Query("product_id") productId: Int? = null): Response<List<Review>>

    /**
     * Создание нового отзыва о товаре.
     * @param token Токен авторизации
     * @param request Данные отзыва (товар, заказ, рейтинг, комментарий)
     * @return Созданный отзыв
     */
    @POST("reviews/")
    suspend fun createReview(
        @Header("Authorization") token: String,
        @Body request: CreateReviewRequest
    ): Response<Review>

    /**
     * Лайк отзыва.
     * @param token Токен авторизации
     * @param reviewId ID отзыва
     * @return Результат операции
     */
    @POST("reviews/{id}/like/")
    suspend fun likeReview(
        @Header("Authorization") token: String,
        @Path("id") reviewId: String
    ): Response<Map<String, Any>>

    /**
     * Удаление лайка с отзыва.
     * @param token Токен авторизации
     * @param reviewId ID отзыва
     * @return Результат операции
     */
    @DELETE("reviews/{id}/unlike/")
    suspend fun unlikeReview(
        @Header("Authorization") token: String,
        @Path("id") reviewId: String
    ): Response<Map<String, Any>>

    /**
     * Проверка возможности оставить отзыв о товаре.
     * @param token Токен авторизации
     * @param productId ID товара
     * @return Информация о возможности оставить отзыв и доступных заказах
     */
    @GET("reviews/can_review_product/")
    suspend fun canReviewProduct(
        @Header("Authorization") token: String,
        @Query("product_id") productId: Int
    ): Response<CanReviewResponse>

}

/**
 * Запрос на обновление товара.
 */
data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Int? = null,
    val original_price: Int? = null,
    val category: String? = null,
    val article: String? = null,
    val stock_quantity: Int? = null,
    val is_available: Boolean? = null,
    val brand: String? = null,
    val features: List<String>? = null,
    val colors: List<String>? = null,
    val sizes: List<String>? = null,
    val image_url: String? = null  // URL изображения (будет сохранен как строка)
)

/**
 * Запрос на обновление статуса заказа.
 */
data class UpdateOrderStatusRequest(
    val status: String  // "processing" | "shipped" | "delivered" | "cancelled"
)

// ==================== Response models (Модели ответов) ====================

/**
 * Ответ API со списком товаров и метаданными пагинации.
 */
data class ProductListResponse(
    val count: Int? = null,           // Общее количество товаров
    val next: String? = null,         // URL следующей страницы
    val previous: String? = null,     // URL предыдущей страницы
    val results: List<Product>         // Список товаров на текущей странице
)

// ==================== Request models (Модели запросов) ====================

/**
 * Запрос на вход по номеру телефона.
 */
data class PhoneLoginRequest(val phone: String)

/**
 * Запрос на подтверждение кода при входе по телефону.
 */
data class VerifyCodeRequest(val phone: String, val code: String)

/**
 * Запрос на создание отзыва о товаре.
 */
data class CreateReviewRequest(
    val product_id: Int,      // ID товара
    val order_id: Int,        // ID заказа (для проверки покупки)
    val rating: Int,          // Рейтинг (1-5)
    val comment: String,      // Текст отзыва
    val title: String? = null // Заголовок отзыва (опционально)
)

/**
 * Ответ на проверку возможности оставить отзыв.
 */
data class CanReviewResponse(
    val can_review: Boolean,              // Можно ли оставить отзыв
    val available_orders: List<AvailableOrder> // Список доступных заказов для отзыва
)

/**
 * Информация о заказе, доступном для отзыва.
 */
data class AvailableOrder(
    val id: Int,              // ID заказа
    val order_number: String, // Номер заказа
    val date: String,         // Дата заказа
    val total_amount: Int    // Общая сумма заказа
)

/**
 * Запрос на создание заказа.
 */
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,  // Список товаров в заказе
    val shipping_address: String,       // Адрес доставки
    val customer_email: String,         // Email покупателя
    val customer_phone: String,         // Телефон покупателя
    val customer_notes: String? = null, // Примечания к заказу (опционально)
    val bonus_points_used: Int = 0      // Использованные бонусные баллы
)

/**
 * Товар в заказе.
 */
data class OrderItemRequest(
    val product: Int,      // ID товара
    val quantity: Int,    // Количество
    val unit_price: String // Цена за единицу
)