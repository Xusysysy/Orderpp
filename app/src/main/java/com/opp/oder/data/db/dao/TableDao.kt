package com.opp.oder.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.opp.oder.data.db.entity.TableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM tables ORDER BY zone, name")
    fun getAll(): Flow<List<TableEntity>>

    @Query("SELECT * FROM tables WHERE id = :id")
    suspend fun getById(id: Long): TableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(table: TableEntity): Long

    @Update
    suspend fun update(table: TableEntity)

    @Query("DELETE FROM tables WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE tables SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
