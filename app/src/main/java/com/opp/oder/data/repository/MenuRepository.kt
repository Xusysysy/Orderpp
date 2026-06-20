package com.opp.oder.data.repository

import com.opp.oder.data.db.dao.MenuItemDao
import com.opp.oder.data.db.dao.RecipeDao
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.data.db.entity.RecipeIngredientEntity
import com.opp.oder.data.db.entity.RecipeStepEntity
import kotlinx.coroutines.flow.Flow

class MenuRepository(
    private val menuDao: MenuItemDao,
    private val recipeDao: RecipeDao
) {
    fun getAll(): Flow<List<MenuItemEntity>> = menuDao.getAll()

    fun getByCategory(category: String): Flow<List<MenuItemEntity>> = menuDao.getByCategory(category)

    suspend fun getById(id: Long): MenuItemEntity? = menuDao.getById(id)

    suspend fun insert(item: MenuItemEntity): Long = menuDao.insert(item)

    suspend fun update(item: MenuItemEntity) = menuDao.update(item)

    suspend fun delete(id: Long) = menuDao.deleteById(id)

    suspend fun getRecipeSteps(menuItemId: Long): List<RecipeStepEntity> =
        recipeDao.getSteps(menuItemId)

    suspend fun getRecipeIngredients(menuItemId: Long): List<RecipeIngredientEntity> =
        recipeDao.getIngredients(menuItemId)

    suspend fun saveRecipe(
        menuItemId: Long,
        steps: List<RecipeStepEntity>,
        ingredients: List<RecipeIngredientEntity>
    ) {
        recipeDao.deleteSteps(menuItemId)
        recipeDao.deleteIngredients(menuItemId)
        steps.forEach { recipeDao.insertStep(it) }
        ingredients.forEach { recipeDao.insertIngredient(it) }
    }
}
