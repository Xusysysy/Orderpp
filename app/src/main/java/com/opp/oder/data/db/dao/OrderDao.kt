package com.opp.oder.data.db.dao

import android.content.ContentValues
import com.opp.oder.data.db.DatabaseHelper
import com.opp.oder.data.db.entity.OrderEntity
import com.opp.oder.data.db.entity.OrderItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OrderWithItems(
    val order: OrderEntity,
    val items: List<OrderItemEntity>
)

class OrderDao(private val helper: DatabaseHelper) {
    suspend fun getActiveOrder(tableId: Long): OrderEntity? = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT * FROM orders WHERE tableId = ? AND status = 'ACTIVE' LIMIT 1", arrayOf(tableId.toString()))
        val result = if (c.moveToFirst()) OrderEntity(c.getLong(0), c.getLong(1), c.getString(2), c.getLong(3)) else null
        c.close()
        result
    }

    suspend fun getOrderWithItems(orderId: Long): OrderWithItems? = withContext(Dispatchers.IO) {
        getById(orderId)?.let { OrderWithItems(it, getItems(orderId, helper.readableDatabase)) }
    }

    suspend fun getAllOrders(): List<OrderEntity> = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT * FROM orders ORDER BY createdAt DESC", null)
        val list = mutableListOf<OrderEntity>()
        while (c.moveToNext()) {
            list.add(OrderEntity(c.getLong(0), c.getLong(1), c.getString(2), c.getLong(3)))
        }
        c.close()
        list
    }

    private fun getById(orderId: Long): OrderEntity? {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT * FROM orders WHERE id = ?", arrayOf(orderId.toString()))
        val result = if (c.moveToFirst()) OrderEntity(c.getLong(0), c.getLong(1), c.getString(2), c.getLong(3)) else null
        c.close()
        return result
    }

    private fun getItems(orderId: Long, db: android.database.sqlite.SQLiteDatabase): List<OrderItemEntity> {
        val c = db.rawQuery("SELECT * FROM order_items WHERE orderId = ?", arrayOf(orderId.toString()))
        val list = mutableListOf<OrderItemEntity>()
        while (c.moveToNext()) {
            list.add(OrderItemEntity(c.getLong(0), c.getLong(1), c.getLong(2), c.getString(3), c.getInt(4), c.getDouble(5)))
        }
        c.close()
        return list
    }

    suspend fun insertOrder(order: OrderEntity): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("tableId", order.tableId); put("status", order.status); put("createdAt", order.createdAt)
        }
        helper.writableDatabase.insert("orders", null, cv)
    }

    suspend fun updateStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        helper.writableDatabase.update("orders", ContentValues().apply { put("status", status) }, "id = ?", arrayOf(id.toString()))
    }

    suspend fun insertItem(item: OrderItemEntity): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("orderId", item.orderId); put("menuItemId", item.menuItemId)
            put("name", item.name); put("quantity", item.quantity); put("price", item.price)
        }
        helper.writableDatabase.insert("order_items", null, cv)
    }

    suspend fun updateItem(item: OrderItemEntity) = withContext(Dispatchers.IO) {
        helper.writableDatabase.update("order_items", ContentValues().apply { put("quantity", item.quantity) }, "id = ?", arrayOf(item.id.toString()))
    }

    suspend fun deleteItem(id: Long) = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete("order_items", "id = ?", arrayOf(id.toString()))
    }

    suspend fun getItems(orderId: Long): List<OrderItemEntity> = withContext(Dispatchers.IO) {
        getItems(orderId, helper.readableDatabase)
    }

    suspend fun getItem(orderId: Long, menuItemId: Long): OrderItemEntity? = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT * FROM order_items WHERE orderId = ? AND menuItemId = ? LIMIT 1", arrayOf(orderId.toString(), menuItemId.toString()))
        val result = if (c.moveToFirst()) OrderItemEntity(c.getLong(0), c.getLong(1), c.getLong(2), c.getString(3), c.getInt(4), c.getDouble(5)) else null
        c.close()
        result
    }
}
