package com.beutystore.pearl.data.repository

import com.beutystore.pearl.data.api.PearlApiService
import com.beutystore.pearl.data.model.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OrdersRepository(private val apiService: PearlApiService) {
    
    suspend fun getMyOrders(accessToken: String): Flow<Result<List<Order>>> = flow {
        try {
            val response = apiService.getMyOrders("Bearer $accessToken")
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch orders: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun getDeliveredOrders(accessToken: String): Flow<Result<List<Order>>> = flow {
        try {
            val response = apiService.getDeliveredOrders("Bearer $accessToken")
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch delivered orders: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

