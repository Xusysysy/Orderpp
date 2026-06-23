package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opp.oder.data.repository.OrderRepository
import com.opp.oder.data.db.dao.OrderBill
import com.opp.oder.data.db.dao.OrderWithItems
import com.opp.oder.data.db.entity.OrderEntity
import com.opp.oder.data.db.entity.OrderItemEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository
) : ViewModel() {

    private val _currentOrder = MutableStateFlow<OrderWithItems?>(null)
    val currentOrder: StateFlow<OrderWithItems?> = _currentOrder

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice

    private val _allOrders = MutableStateFlow<List<OrderBill>>(emptyList())
    val allOrders: StateFlow<List<OrderBill>> = _allOrders

    private val _selectedBillId = MutableStateFlow<Long?>(null)
    val selectedBillId: StateFlow<Long?> = _selectedBillId

    private val _activeOrderCount = MutableStateFlow(0)
    val activeOrderCount: StateFlow<Int> = _activeOrderCount

    private val _submittedOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val submittedOrders: StateFlow<List<OrderWithItems>> = _submittedOrders

    private val _expandedOrderId = MutableStateFlow<Long?>(null)
    val expandedOrderId: StateFlow<Long?> = _expandedOrderId

    private var autoRefreshJob: Job? = null

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

    fun loadAllOrders() {
        viewModelScope.launch {
            val orders = repository.getAllOrderBills()
            _allOrders.value = orders
            _activeOrderCount.value = orders.count { it.status == OrderEntity.STATUS_ACTIVE }
            _selectedBillId.value = null
        }
    }

    fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                val orders = repository.getAllOrderBills()
                _allOrders.value = orders
                _activeOrderCount.value = orders.count { it.status == OrderEntity.STATUS_ACTIVE }
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun selectBill(orderId: Long) {
        if (_selectedBillId.value == orderId) {
            _selectedBillId.value = null
        } else {
            _selectedBillId.value = orderId
        }
    }

    fun settleBill(orderId: Long) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, OrderEntity.STATUS_SETTLED)
            loadAllOrders()
        }
    }

    fun submitCurrentOrder() {
        val order = _currentOrder.value ?: return
        _submittedOrders.value = _submittedOrders.value + order
        _currentOrder.value = null
        _totalPrice.value = 0.0
    }

    fun toggleExpand(orderId: Long) {
        _expandedOrderId.value = if (_expandedOrderId.value == orderId) null else orderId
    }

    fun resubmitOrder(order: OrderWithItems, hostViewModel: HostViewModel) {
        val items = order.items.map {
            com.opp.oder.network.ApiOrderItemRequest(it.menuItemId, it.name, it.quantity, it.price)
        }
        hostViewModel.submitOrder(order.order.tableId, items)
    }

    fun removeSubmittedOrder(orderId: Long) {
        _submittedOrders.value = _submittedOrders.value.filter { it.order.id != orderId }
    }

    fun addItemToSubmitted(orderId: Long, menuItemId: Long, name: String, price: Double) {
        val idx = _submittedOrders.value.indexOfFirst { it.order.id == orderId }
        if (idx < 0) return
        val order = _submittedOrders.value[idx]
        val updated = OrderWithItems(order.order, order.items + OrderItemEntity(
            orderId = orderId, menuItemId = menuItemId, name = name, quantity = 1, price = price
        ))
        _submittedOrders.value = _submittedOrders.value.toMutableList().also { it[idx] = updated }
    }

    fun updateSubmittedQty(orderId: Long, item: OrderItemEntity, delta: Int) {
        val idx = _submittedOrders.value.indexOfFirst { it.order.id == orderId }
        if (idx < 0) return
        val items = _submittedOrders.value[idx].items.toMutableList()
        val itemIdx = items.indexOfFirst { it.id == item.id }
        if (itemIdx < 0) return
        val newQty = (item.quantity + delta).coerceAtLeast(0)
        if (newQty <= 0) {
            items.removeAt(itemIdx)
        } else {
            items[itemIdx] = item.copy(quantity = newQty)
        }
        _submittedOrders.value = _submittedOrders.value.toMutableList().also { it[idx] = OrderWithItems(_submittedOrders.value[idx].order, items) }
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }

    private suspend fun reloadOrder(orderId: Long) {
        val updated = repository.getOrderWithItems(orderId)
        _currentOrder.value = updated
        _totalPrice.value = updated?.items?.sumOf { it.price * it.quantity } ?: 0.0
    }
}
