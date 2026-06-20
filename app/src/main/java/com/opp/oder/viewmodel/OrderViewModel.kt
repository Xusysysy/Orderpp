package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opp.oder.data.repository.OrderRepository
import com.opp.oder.data.db.dao.OrderWithItems
import com.opp.oder.data.db.entity.OrderEntity
import com.opp.oder.data.db.entity.OrderItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    private val _currentOrder = MutableStateFlow<OrderWithItems?>(null)
    val currentOrder: StateFlow<OrderWithItems?> = _currentOrder

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice

    fun loadOrder(tableId: Long) {
        viewModelScope.launch {
            val order = repository.getActiveOrder(tableId)
            if (order != null) {
                val withItems = repository.getOrderWithItems(order.id)
                _currentOrder.value = withItems
                _totalPrice.value = withItems?.items?.sumOf { it.price * it.quantity } ?: 0.0
            } else {
                _currentOrder.value = null
                _totalPrice.value = 0.0
            }
        }
    }

    fun addItem(tableId: Long, menuItemId: Long, name: String, price: Double, quantity: Int = 1) {
        viewModelScope.launch {
            val order = _currentOrder.value?.order ?: repository.createOrder(tableId)
            if (_currentOrder.value == null) {
                _currentOrder.value = OrderWithItems(order, emptyList())
            }
            repository.addItem(order.id, OrderItemEntity(
                orderId = order.id,
                menuItemId = menuItemId,
                name = name,
                quantity = quantity,
                price = price
            ))
            reloadOrder(order.id)
        }
    }

    fun updateQuantity(item: OrderItemEntity, delta: Int) {
        viewModelScope.launch {
            val newQty = (item.quantity + delta).coerceAtLeast(0)
            if (newQty <= 0) {
                repository.removeItem(item.id)
            } else {
                repository.updateItemQuantity(item.copy(quantity = newQty))
            }
            _currentOrder.value?.order?.let { reloadOrder(it.id) }
        }
    }

    fun settleOrder() {
        viewModelScope.launch {
            _currentOrder.value?.order?.let {
                repository.updateOrderStatus(it.id, OrderEntity.STATUS_SETTLED)
                _currentOrder.value = null
                _totalPrice.value = 0.0
            }
        }
    }

    private suspend fun reloadOrder(orderId: Long) {
        val updated = repository.getOrderWithItems(orderId)
        _currentOrder.value = updated
        _totalPrice.value = updated?.items?.sumOf { it.price * it.quantity } ?: 0.0
    }
}
