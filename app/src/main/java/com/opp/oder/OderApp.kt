package com.opp.oder

import android.app.Application
import android.util.Log
import com.opp.oder.data.db.AppDatabase
import com.opp.oder.data.preset.PresetRecipes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OderApp : Application() {
    private val _dbState = MutableStateFlow<AppDatabase?>(null)
    val dbState: StateFlow<AppDatabase?> = _dbState

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            try {
                val db = AppDatabase.getInstance(this@OderApp)
                _dbState.value = db
                insertPresetData(db)
            } catch (e: Exception) {
                Log.e("OderApp", "Database init failed", e)
            }
        }
    }

    private suspend fun insertPresetData(db: AppDatabase) {
        try {
            if (db.menuItemDao().getById(1L) != null) return
            for ((index, c) in PresetRecipes.cocktails.withIndex()) {
                val menuId = db.menuItemDao().insert(
                    com.opp.oder.data.db.entity.MenuItemEntity(
                        name = c.name, price = c.price, category = c.category, hasRecipe = true
                    )
                )
                for ((i, step) in c.steps.withIndex()) {
                    db.recipeDao().insertStep(
                        com.opp.oder.data.db.entity.RecipeStepEntity(
                            menuItemId = menuId, stepNumber = i + 1, description = step
                        )
                    )
                }
                for ((name, amount) in c.ingredients) {
                    val parts = amount.split(Regex("[（(]"))
                    val amt = parts[0].trim()
                    val unit = if (parts.size > 1) parts[1].replace(Regex("[）)]"), "").trim() else ""
                    db.recipeDao().insertIngredient(
                        com.opp.oder.data.db.entity.RecipeIngredientEntity(
                            menuItemId = menuId, name = name, amount = amt, unit = unit
                        )
                    )
                }
            }
            for ((name, cat, price) in PresetRecipes.defaultDrinks) {
                db.menuItemDao().insert(
                    com.opp.oder.data.db.entity.MenuItemEntity(name = name, price = price, category = cat)
                )
            }
            for ((name, cat, price) in PresetRecipes.defaultSnacks) {
                db.menuItemDao().insert(
                    com.opp.oder.data.db.entity.MenuItemEntity(name = name, price = price, category = cat)
                )
            }
        } catch (e: Exception) {
            Log.e("OderApp", "Preset insert failed", e)
        }
    }
}
