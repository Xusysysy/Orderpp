package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opp.oder.network.HostServer
import com.opp.oder.network.SyncClient
import com.opp.oder.network.DiscoveryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HostViewModel : ViewModel() {
    enum class Mode { HOST, CLIENT, NONE }

    private val _mode = MutableStateFlow(Mode.NONE)
    val mode: StateFlow<Mode> = _mode

    private val _clientIp = MutableStateFlow("")
    val clientIp: StateFlow<String> = _clientIp

    var hostServer: HostServer? = null
    var syncClient: SyncClient? = null
    var discoveryService: DiscoveryService? = null

    fun setHostMode(server: HostServer, discovery: DiscoveryService) {
        _mode.value = Mode.HOST
        hostServer = server
        discoveryService = discovery
        server.start()
        discovery.startAdvertising(8765)
    }

    fun setClientMode(ip: String, client: SyncClient, discovery: DiscoveryService) {
        _mode.value = Mode.CLIENT
        _clientIp.value = ip
        syncClient = client
        discoveryService = discovery
    }

    fun getClient(): SyncClient? = syncClient

    override fun onCleared() {
        super.onCleared()
        discoveryService?.stopAdvertising()
        discoveryService?.stopDiscovery()
        hostServer?.stop()
    }
}
