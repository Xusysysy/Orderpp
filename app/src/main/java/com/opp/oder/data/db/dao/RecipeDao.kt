package com.opp.oder.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.opp.oder.data.db.entity.RecipeIngredientEntity
import com.opp.oder.data.db.entity.RecipeStepEntity

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe_steps WHERE menuItemId = :menuItemId ORDER BY stepNumber")
    suspend fun getSteps(menuItemId: Long): List<RecipeStepEntity>

    @Query("SELECT * FROM recipe_ingredients WHERE menuItemId = :menuItemId")
    suspend fun getIngredients(menuItemId: Long): List<RecipeIngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: RecipeStepEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: RecipeIngredientEntity): Long

    @Query("DELETE FROM recipe_steps WHERE menuItemId = :menuItemId")
    suspend fun deleteSteps(menuItemId: Long)

    @Query("DELETE FROM recipe_ingredients WHERE menuItemId = :menuItemId")
    suspend fun deleteIngredients(menuItemId: Long)
}
