// ui/viewmodel/OrdersViewModel.kt
package com.beutystore.pearl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.beutystore.pearl.data.model.Order
import com.beutystore.pearl.data.model.OrderStatus
import com.beutystore.pearl.data.model.OrderItem
import com.beutystore.pearl.data.model.Product
import com.beutystore.pearl.data.api.RetrofitInstance
import com.beutystore.pearl.data.api.CreateOrderRequest
import com.beutystore.pearl.data.api.OrderItemRequest
import java.util.UUID
import kotlin.random.Random

class OrdersViewModel : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // Сортированные заказы (новые первыми)
    val sortedOrders: StateFlow<List<Order>> = _orders.map { orders ->
        orders.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Количество заказов
    val ordersCount: StateFlow<Int> = _orders.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Загрузка заказов из API
    fun loadOrders(accessToken: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMyOrders("Bearer $accessToken")
                if (response.isSuccessful && response.body() != null) {
                    _orders.value = response.body()!!
                } else {
                    _orders.value = emptyList()
                }
            } catch (e: Exception) {
                _orders.value = emptyList()
            }
        }
    }

    fun createOrderFromCart(
        cartItems: List<CartViewModel.CartItem>,
        shippingAddress: String,
        customerEmail: String,
        customerPhone: String,
        bonusPointsUsed: Int = 0,
        accessToken: String,
        onSuccess: (Order) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val orderItems = cartItems.map { cartItem ->
                    OrderItemRequest(
                        product = cartItem.product.id,
                        quantity = cartItem.quantity,
                        unit_price = cartItem.product.price.toString()
                    )
                }
                
                val request = CreateOrderRequest(
                    items = orderItems,
                    shipping_address = shippingAddress,
                    customer_email = customerEmail,
                    customer_phone = customerPhone,
                    customer_notes = null,
                    bonus_points_used = bonusPointsUsed
                )
                
                val response = RetrofitInstance.api.createOrder("Bearer $accessToken", request)
                
                if (response.isSuccessful && response.body() != null) {
                    val order = response.body()!!
                    // Добавляем заказ в локальный список
                    val currentOrders = _orders.value.toMutableList()
                    currentOrders.add(0, order)
                    _orders.value = currentOrders
                    // Перезагружаем заказы из API для получения актуальных данных
                    if (accessToken.isNotEmpty()) {
                        loadOrders(accessToken)
                    }
                    onSuccess(order)
                } else {
                    val errorBody = response.errorBody()?.string() ?: response.message()
                    val errorMsg = try {
                        errorBody ?: "Ошибка создания заказа"
                    } catch (e: Exception) {
                        "Ошибка создания заказа: ${response.message()}"
                    }
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                onError("Ошибка создания заказа: ${e.message}")
            }
        }
    }
    
    // Старый метод для обратной совместимости (если нужен)
    fun createOrderFromCart(cartItems: List<CartViewModel.CartItem>): Order {
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                product = cartItem.product,
                quantity = cartItem.quantity,
                price = cartItem.product.price
            )
        }
        
        val totalPrice = orderItems.sumOf { it.price * it.quantity }
        val orderNumber = "ORD-${Random.nextInt(1000, 9999)}"
        
        val order = Order(
            id = UUID.randomUUID().toString(),
            orderNumber = orderNumber,
            date = System.currentTimeMillis(),
            status = "pending",
            items = orderItems,
            totalPrice = totalPrice,
            deliveryAddress = "г. Москва, ул. Примерная, д. 1, кв. 10",
            paymentMethod = "Карта"
        )
        
        viewModelScope.launch {
            val currentOrders = _orders.value.toMutableList()
            currentOrders.add(0, order)
            _orders.value = currentOrders
        }
        
        return order
    }

    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            val currentOrders = _orders.value.toMutableList()
            val orderIndex = currentOrders.indexOfFirst { it.id == orderId }
            if (orderIndex != -1) {
                val order = currentOrders[orderIndex]
                if (order.statusEnum == OrderStatus.PENDING || order.statusEnum == OrderStatus.CONFIRMED) {
                    currentOrders[orderIndex] = order.copy(status = "cancelled")
                    _orders.value = currentOrders
                }
            }
        }
    }
}

