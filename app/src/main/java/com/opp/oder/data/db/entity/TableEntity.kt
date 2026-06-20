package com.opp.oder.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val zone: String = "",
    val status: String = STATUS_IDLE
) {
    companion object {
        const val STATUS_IDLE = "IDLE"
        const val STATUS_ORDERED = "ORDERED"
    }
}
