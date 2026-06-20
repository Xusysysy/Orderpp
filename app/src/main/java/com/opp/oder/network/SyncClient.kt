package com.opp.oder.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class SyncClient(private val hostIp: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val baseUrl get() = "http://$hostIp:8765"

    suspend fun getTables(): List<ApiTable> =
        client.get("$baseUrl/api/tables").body()

    suspend fun getMenu(): List<ApiMenuItem> =
        client.get("$baseUrl/api/menu").body()

    suspend fun getOrders(tableId: Long): List<ApiOrder> =
        client.get("$baseUrl/api/orders/$tableId").body()

    suspend fun getRecipeSteps(menuItemId: Long): List<ApiRecipeStep> =
        client.get("$baseUrl/api/recipes/$menuItemId/steps").body()

    suspend fun getRecipeIngredients(menuItemId: Long): List<ApiRecipeIngredient> =
        client.get("$baseUrl/api/recipes/$menuItemId/ingredients").body()
}
