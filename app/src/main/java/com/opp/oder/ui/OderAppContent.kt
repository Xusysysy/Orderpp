package com.opp.oder.ui

import android.net.nsd.NsdServiceInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.opp.oder.OderApp
import com.opp.oder.data.repository.MenuRepository
import com.opp.oder.data.repository.OrderRepository
import com.opp.oder.data.repository.TableRepository
import com.opp.oder.network.DiscoveryService
import com.opp.oder.network.HostServer
import com.opp.oder.network.SyncClient
import com.opp.oder.ui.screen.HostSetupScreen
import com.opp.oder.ui.screen.RoleSelectScreen
import com.opp.oder.ui.screen.main.MainScreen
import com.opp.oder.viewmodel.HostViewModel
import com.opp.oder.viewmodel.MenuViewModel
import com.opp.oder.viewmodel.OrderViewModel
import com.opp.oder.viewmodel.RoleViewModel
import com.opp.oder.viewmodel.TableViewModel

enum class NavScreen {
    ROLE_SELECT, HOST_SETUP, MAIN
}

@Composable
fun OderAppContent() {
    val app = LocalContext.current.applicationContext as OderApp
    val db = app.database

    val roleViewModel: RoleViewModel = viewModel()
    val hostViewModel: HostViewModel = viewModel()

    val tableRepository = remember { TableRepository(db.tableDao()) }
    val menuRepository = remember { MenuRepository(db.menuItemDao(), db.recipeDao()) }
    val orderRepository = remember { OrderRepository(db.orderDao()) }

    val tableViewModel: TableViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TableViewModel(tableRepository) as T
            }
        }
    )
    val menuViewModel: MenuViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MenuViewModel(menuRepository) as T
            }
        }
    )
    val orderViewModel: OrderViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return OrderViewModel(orderRepository) as T
            }
        }
    )

    val role by roleViewModel.role.collectAsStateWithLifecycle()
    val hostMode by hostViewModel.mode.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(NavScreen.ROLE_SELECT) }

    val discoveredHosts = remember { mutableStateListOf<NsdServiceInfo>() }
    val discoveryService = remember { DiscoveryService(app) }

    when (currentScreen) {
        NavScreen.ROLE_SELECT -> {
            RoleSelectScreen(
                viewModel = roleViewModel,
                onStaffEnter = { currentScreen = NavScreen.HOST_SETUP },
                onGuestEnter = { currentScreen = NavScreen.HOST_SETUP }
            )
        }
        NavScreen.HOST_SETUP -> {
            HostSetupScreen(
                viewModel = hostViewModel,
                discoveredHosts = discoveredHosts,
                onStartHost = {
                    val server = HostServer(db)
                    hostViewModel.setHostMode(server, discoveryService)
                    currentScreen = NavScreen.MAIN
                },
                onStartClient = {
                    discoveryService.startDiscovery()
                    discoveryService.onHostDiscovered = { info ->
                        discoveredHosts.add(info)
                    }
                },
                onConnectToHost = { ip ->
                    val client = SyncClient(ip)
                    hostViewModel.setClientMode(ip, client, discoveryService)
                    currentScreen = NavScreen.MAIN
                }
            )
        }
        NavScreen.MAIN -> {
            MainScreen(
                tableViewModel = tableViewModel,
                menuViewModel = menuViewModel,
                orderViewModel = orderViewModel,
                roleViewModel = roleViewModel,
                onSwitchRole = { currentScreen = NavScreen.ROLE_SELECT }
            )
        }
    }
}
