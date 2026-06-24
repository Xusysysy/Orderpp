package com.opp.order.data.repository

import com.opp.order.data.db.dao.MenuItemDao
import com.opp.order.data.db.dao.RecipeDao
import com.opp.order.data.db.entity.MenuItemEntity
import com.opp.order.data.db.entity.RecipeIngredientEntity
import com.opp.order.data.db.entity.RecipeStepEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MenuRepository(
    private val menuDao: MenuItemDao,
    private val recipeDao: RecipeDao
) {
    fun getAll(): Flow<List<MenuItemEntity>> = menuDao.getAllFlow()

    fun getByCategory(category: String): Flow<List<MenuItemEntity>> = flow { emit(menuDao.getByCategory(category)) }

    suspend fun getById(id: Long): MenuItemEntity? = menuDao.getById(id)

    suspend fun insert(item: MenuItemEntity): Long = menuDao.insert(item)

    suspend fun update(item: MenuItemEntity) = menuDao.update(item)

    suspend fun delete(id: Long) = menuDao.deleteById(id)

    suspend fun updateSortOrders(ids: List<Long>) = menuDao.updateSortOrders(ids)

    suspend fun updateAllFromApi(items: List<com.opp.order.network.ApiMenuItem>) = menuDao.syncFromApi(items)

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

    suspend fun markHasRecipe(menuItemId: Long, hasRecipe: Boolean) = menuDao.markHasRecipe(menuItemId, hasRecipe)

    suspend fun syncRecipesFromApi(
        menuItemId: Long,
        steps: List<com.opp.order.network.ApiRecipeStep>,
        ingredients: List<com.opp.order.network.ApiRecipeIngredient>
    ) {
        recipeDao.deleteSteps(menuItemId)
        recipeDao.deleteIngredients(menuItemId)
        steps.forEach { s ->
            recipeDao.insertStep(RecipeStepEntity(menuItemId = menuItemId, stepNumber = s.stepNumber, description = s.description))
        }
        ingredients.forEach { ing ->
            recipeDao.insertIngredient(RecipeIngredientEntity(menuItemId = menuItemId, name = ing.name, amount = ing.amount, unit = ing.unit))
        }
    }
}
