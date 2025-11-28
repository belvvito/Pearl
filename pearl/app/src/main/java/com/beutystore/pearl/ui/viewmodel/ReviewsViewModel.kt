package com.beutystore.pearl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beutystore.pearl.data.api.RetrofitInstance
import com.beutystore.pearl.data.api.AvailableOrder
import com.beutystore.pearl.data.api.CanReviewResponse
import com.beutystore.pearl.data.api.CreateReviewRequest
import com.beutystore.pearl.data.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewsViewModel : ViewModel() {

    private val apiService = RetrofitInstance.api
    private val _reviews = MutableStateFlow<Map<Int, List<Review>>>(emptyMap())
    private val _canReview = MutableStateFlow<Map<Int, CanReviewResponse>>(emptyMap())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    val reviews: StateFlow<Map<Int, List<Review>>> = _reviews.asStateFlow()
    val canReview: StateFlow<Map<Int, CanReviewResponse>> = _canReview.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadReviewsForProduct(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getReviews(productId)
                if (response.isSuccessful && response.body() != null) {
                    val currentReviews = _reviews.value.toMutableMap()
                    currentReviews[productId] = response.body()!!
                    _reviews.value = currentReviews
                } else {
                    _error.value = "Не удалось загрузить отзывы: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки отзывов: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkCanReviewProduct(productId: Int, accessToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.canReviewProduct("Bearer $accessToken", productId)
                if (response.isSuccessful && response.body() != null) {
                    val currentCanReview = _canReview.value.toMutableMap()
                    currentCanReview[productId] = response.body()!!
                    _canReview.value = currentCanReview
                } else {
                    val currentCanReview = _canReview.value.toMutableMap()
                    currentCanReview[productId] = CanReviewResponse(false, emptyList())
                    _canReview.value = currentCanReview
                }
            } catch (e: Exception) {
                val currentCanReview = _canReview.value.toMutableMap()
                currentCanReview[productId] = CanReviewResponse(false, emptyList())
                _canReview.value = currentCanReview
                _error.value = "Ошибка проверки возможности оставить отзыв: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getReviewsForProduct(productId: Int): List<Review> {
        return _reviews.value[productId] ?: emptyList()
    }

    fun getAverageRating(productId: Int): Float {
        val productReviews = getReviewsForProduct(productId)
        return if (productReviews.isEmpty()) {
            0f
        } else {
            productReviews.map { it.rating }.average().toFloat()
        }
    }

    fun canReviewProduct(productId: Int): Boolean {
        return _canReview.value[productId]?.can_review ?: false
    }

    fun getAvailableOrdersForProduct(productId: Int): List<AvailableOrder> {
        return _canReview.value[productId]?.available_orders ?: emptyList()
    }

    fun createReview(
        productId: Int,
        orderId: Int,
        rating: Int,
        comment: String,
        title: String?,
        accessToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = CreateReviewRequest(
                    product_id = productId,
                    order_id = orderId,
                    rating = rating,
                    comment = comment,
                    title = title
                )
                val response = apiService.createReview("Bearer $accessToken", request)
                if (response.isSuccessful && response.body() != null) {
                    // Обновляем список отзывов для товара
                    loadReviewsForProduct(productId)
                    // Обновляем информацию о возможности оставить отзыв
                    checkCanReviewProduct(productId, accessToken)
                    onSuccess()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Не удалось создать отзыв"
                    _error.value = errorMessage
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = "Ошибка создания отзыва: ${e.message}"
                _error.value = errorMessage
                onError(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likeReview(reviewId: String, productId: Int, accessToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.likeReview("Bearer $accessToken", reviewId)
                if (response.isSuccessful) {
                    // Обновляем список отзывов для товара
                    loadReviewsForProduct(productId)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка лайка отзыва: ${e.message}"
            }
        }
    }

    fun unlikeReview(reviewId: String, productId: Int, accessToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.unlikeReview("Bearer $accessToken", reviewId)
                if (response.isSuccessful) {
                    // Обновляем список отзывов для товара
                    loadReviewsForProduct(productId)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка удаления лайка: ${e.message}"
            }
        }
    }
}

