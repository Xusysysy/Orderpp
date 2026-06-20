package com.opp.oder

import android.app.Application
import com.opp.oder.data.db.AppDatabase
import com.opp.oder.data.preset.PresetRecipes
import com.opp.oder.util.LogWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DbState {
    data object Loading : DbState()
    data class Ready(val db: AppDatabase) : DbState()
    data class Error(val message: String) : DbState()
}

class OderApp : Application() {
    private val _dbState = MutableStateFlow<DbState>(DbState.Loading)
    val dbState: StateFlow<DbState> = _dbState

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        LogWriter.init(filesDir)
        LogWriter.write("APP", "OderApp v1.0.1 onCreate start")
        appScope.launch {
            try {
                LogWriter.write("DB", "Building Room database...")
                val db = AppDatabase.getInstance(this@OderApp)
                LogWriter.write("DB", "Room database built OK")

                LogWriter.write("DB", "Inserting preset data...")
                insertPresetData(db)
                LogWriter.write("DB", "Preset data insert done")

                _dbState.value = DbState.Ready(db)
                LogWriter.write("APP", "OderApp init SUCCESS")
            } catch (e: Exception) {
                val msg = "${e.javaClass.simpleName}: ${e.message}"
                LogWriter.write("APP", "OderApp init FAILED", e)
                _dbState.value = DbState.Error(msg)
            }
        }
    }

    private suspend fun insertPresetData(db: AppDatabase) {
        try {
            val existing = db.menuItemDao().getById(1L)
            if (existing != null) {
                LogWriter.write("DB", "Preset data already exists, skip insert")
                return
            }
            LogWriter.write("DB", "Inserting ${PresetRecipes.cocktails.size} cocktails")
            for ((index, c) in PresetRecipes.cocktails.withIndex()) {
                val menuId = db.menuItemDao().insert(
                    com.opp.oder.data.db.entity.MenuItemEntity(
                        name = c.name, price = c.price, category = c.category, hasRecipe = true
                    )
                )
                LogWriter.write("DB", "  [$index] $c.name (id=$menuId)")
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
            LogWriter.write("DB", "Inserting drinks...")
            for ((name, cat, price) in PresetRecipes.defaultDrinks) {
                db.menuItemDao().insert(
                    com.opp.oder.data.db.entity.MenuItemEntity(name = name, price = price, category = cat)
                )
            }
            LogWriter.write("DB", "Inserting snacks...")
            for ((name, cat, price) in PresetRecipes.defaultSnacks) {
                db.menuItemDao().insert(
                    com.opp.oder.data.db.entity.MenuItemEntity(name = name, price = price, category = cat)
                )
            }
            LogWriter.write("DB", "All preset data inserted")
        } catch (e: Exception) {
            LogWriter.write("DB", "Preset insert failed", e)
            throw e
        }
    }
}
