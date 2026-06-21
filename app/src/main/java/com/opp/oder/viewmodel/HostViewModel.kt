package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opp.oder.network.ApiOrderItemRequest
import com.opp.oder.network.ApiOrderRequest
import com.opp.oder.network.HostServer
import com.opp.oder.network.SyncClient
import com.opp.oder.network.DiscoveryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HostViewModel : ViewModel() {
    enum class Mode { HOST, CLIENT, NONE }
    enum class SyncStatus { DISCONNECTED, CONNECTING, SYNCED, ERROR }

    private val _mode = MutableStateFlow(Mode.NONE)
    val mode: StateFlow<Mode> = _mode

    private val _clientIp = MutableStateFlow("")
    val clientIp: StateFlow<String> = _clientIp

    private val _syncStatus = MutableStateFlow(SyncStatus.DISCONNECTED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    private val _connectedHostIp = MutableStateFlow("")
    val connectedHostIp: StateFlow<String> = _connectedHostIp

    private val _connectedHostId = MutableStateFlow("")
    val connectedHostId: StateFlow<String> = _connectedHostId

    var hostServer: HostServer? = null
    var syncClient: SyncClient? = null
    var discoveryService: DiscoveryService? = null

    data class PendingOrder(val tableId: Long, val items: List<ApiOrderItemRequest>)
    private val _pendingOrders = mutableListOf<PendingOrder>()

    var onSyncTables: (suspend (SyncClient) -> Unit)? = null
    var onSyncMenu: (suspend (SyncClient) -> Unit)? = null
    var onSyncPin: ((String) -> Unit)? = null

    fun setHostMode(server: HostServer, discovery: DiscoveryService) {
        _mode.value = Mode.HOST
        hostServer = server
        discoveryService = discovery
        server.start()
        discovery.startAdvertising(8765)
        discovery.startHeartbeat(8765)
    }

    fun setClientMode(ip: String, client: SyncClient, discovery: DiscoveryService) {
        _mode.value = Mode.CLIENT
        _clientIp.value = ip
        syncClient = client
        discoveryService = discovery
    }

    fun connectAndSync(ip: String, serviceName: String, client: SyncClient, discovery: DiscoveryService) {
        setClientMode(ip, client, discovery)
        _connectedHostIp.value = ip
        _connectedHostId.value = serviceName
        _syncStatus.value = SyncStatus.CONNECTING
        viewModelScope.launch {
            try {
                onSyncTables?.invoke(client)
                onSyncMenu?.invoke(client)
                val pin = client.getPin()
                onSyncPin?.invoke(pin)
                _syncStatus.value = SyncStatus.SYNCED
            } catch (_: Exception) {
                _syncStatus.value = SyncStatus.ERROR
            }
        }
    }

    fun submitOrder(tableId: Long, items: List<ApiOrderItemRequest>) {
        val client = syncClient ?: return
        viewModelScope.launch {
            val result = client.submitOrder(ApiOrderRequest(tableId, items))
            if (result == null) {
                _pendingOrders.add(PendingOrder(tableId, items))
            }
        }
    }

    fun retryPendingOrders() {
        val client = syncClient ?: return
        if (_pendingOrders.isEmpty()) return
        viewModelScope.launch {
            val toRetry = _pendingOrders.toList()
            _pendingOrders.clear()
            toRetry.forEach { order ->
                val result = client.submitOrder(ApiOrderRequest(order.tableId, order.items))
                if (result == null) {
                    _pendingOrders.add(order)
                }
            }
        }
    }

    fun getClient(): SyncClient? = syncClient

    override fun onCleared() {
        super.onCleared()
        discoveryService?.stopHeartbeat()
        discoveryService?.stopAdvertising()
        discoveryService?.stopDiscovery()
        hostServer?.stop()
    }
}
