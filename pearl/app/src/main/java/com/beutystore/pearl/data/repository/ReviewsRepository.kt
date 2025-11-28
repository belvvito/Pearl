package com.beutystore.pearl.data.repository

import com.beutystore.pearl.data.api.PearlApiService
import com.beutystore.pearl.data.api.CreateReviewRequest
import com.beutystore.pearl.data.model.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReviewsRepository(private val apiService: PearlApiService) {
    
    suspend fun getReviews(productId: Int? = null): Flow<Result<List<Review>>> = flow {
        try {
            val response = apiService.getReviews(productId)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch reviews: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun createReview(
        accessToken: String,
        productId: Int,
        orderId: Int,
        rating: Int,
        comment: String,
        title: String? = null
    ): Flow<Result<Review>> = flow {
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
                emit(Result.success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                emit(Result.failure(Exception("Failed to create review: $errorBody")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun likeReview(accessToken: String, reviewId: String): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.likeReview("Bearer $accessToken", reviewId)
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Failed to like review: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun unlikeReview(accessToken: String, reviewId: String): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.unlikeReview("Bearer $accessToken", reviewId)
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Failed to unlike review: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

