package com.opp.oder.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double = 0.0,
    val category: String = CATEGORY_COCKTAIL,
    val hasRecipe: Boolean = false
) {
    companion object {
        const val CATEGORY_COCKTAIL = "cocktail"
        const val CATEGORY_DRINK = "drink"
        const val CATEGORY_SNACK = "snack"
    }
}
