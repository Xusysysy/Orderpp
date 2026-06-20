package com.opp.oder.network

import com.opp.oder.data.db.DatabaseHelper
import com.opp.oder.data.db.dao.MenuItemDao
import com.opp.oder.data.db.dao.OrderDao
import com.opp.oder.data.db.dao.RecipeDao
import com.opp.oder.data.db.dao.TableDao
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ApiTable(val id: Long, val name: String, val zone: String, val status: String)

@Serializable
data class ApiMenuItem(val id: Long, val name: String, val price: Double, val category: String, val hasRecipe: Boolean)

@Serializable
data class ApiOrder(val id: Long, val tableId: Long, val status: String, val createdAt: Long, val items: List<ApiOrderItem>)

@Serializable
data class ApiOrderItem(val id: Long, val orderId: Long, val menuItemId: Long, val name: String, val quantity: Int, val price: Double)

@Serializable
data class ApiRecipeStep(val id: Long, val menuItemId: Long, val stepNumber: Int, val description: String)

@Serializable
data class ApiRecipeIngredient(val id: Long, val menuItemId: Long, val name: String, val amount: String, val unit: String)

class HostServer(helper: DatabaseHelper) {
    private val tableDao = TableDao(helper)
    private val menuDao = MenuItemDao(helper)
    private val orderDao = OrderDao(helper)
    private val recipeDao = RecipeDao(helper)

    private val server by lazy {
        embeddedServer(CIO, port = 8765) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                get("/api/tables") {
                    val tables = withContext(Dispatchers.IO) { tableDao.getAll() }
                    call.respond(tables.map { ApiTable(it.id, it.name, it.zone, it.status) })
                }
                get("/api/menu") {
                    val items = withContext(Dispatchers.IO) { menuDao.getAll() }
                    call.respond(items.map { ApiMenuItem(it.id, it.name, it.price, it.category, it.hasRecipe) })
                }
                get("/api/orders/{tableId}") {
                    val tableId = call.parameters["tableId"]?.toLongOrNull() ?: 0L
                    val order = withContext(Dispatchers.IO) { orderDao.getActiveOrder(tableId) }
                    if (order != null) {
                        val items = withContext(Dispatchers.IO) { orderDao.getItems(order.id) }
                        call.respond(listOf(ApiOrder(order.id, order.tableId, order.status, order.createdAt, items.map {
                            ApiOrderItem(it.id, it.orderId, it.menuItemId, it.name, it.quantity, it.price)
                        })))
                    } else {
                        call.respond(emptyList<ApiOrder>())
                    }
                }
                get("/api/recipes/{menuItemId}/steps") {
                    val id = call.parameters["menuItemId"]?.toLongOrNull() ?: 0L
                    val steps = withContext(Dispatchers.IO) { recipeDao.getSteps(id) }
                    call.respond(steps.map { ApiRecipeStep(it.id, it.menuItemId, it.stepNumber, it.description) })
                }
                get("/api/recipes/{menuItemId}/ingredients") {
                    val id = call.parameters["menuItemId"]?.toLongOrNull() ?: 0L
                    val ingredients = withContext(Dispatchers.IO) { recipeDao.getIngredients(id) }
                    call.respond(ingredients.map { ApiRecipeIngredient(it.id, it.menuItemId, it.name, it.amount, it.unit) })
                }
            }
        }
    }

    fun start() {
        server.start(wait = false)
    }

    fun stop() {
        server.stop(1000, 2000)
    }
}
