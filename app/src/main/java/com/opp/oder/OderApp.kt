package com.opp.oder

import android.app.Application
import android.util.Log
import com.opp.oder.data.db.AppDatabase
import com.opp.oder.data.preset.PresetRecipes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OderApp : Application() {
    val database by lazy {
        try {
            AppDatabase.getInstance(this)
        } catch (e: Exception) {
            Log.e("OderApp", "Database init failed", e)
            throw e
        }
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            try {
                val db = database
                if (db.menuItemDao().getById(1L) == null) {
                    PresetRecipes.cocktails.forEachIndexed { index, c ->
                        val menuId = db.menuItemDao().insert(
                            com.opp.oder.data.db.entity.MenuItemEntity(
                                name = c.name,
                                price = c.price,
                                category = c.category,
                                hasRecipe = true
                            )
                        )
                        c.steps.forEachIndexed { i, step ->
                            db.recipeDao().insertStep(
                                com.opp.oder.data.db.entity.RecipeStepEntity(
                                    menuItemId = menuId,
                                    stepNumber = i + 1,
                                    description = step
                                )
                            )
                        }
                        c.ingredients.forEach { (name, amount) ->
                            val parts = amount.split(Regex("[（(]"))
                            val amt = parts[0].trim()
                            val unit = if (parts.size > 1) parts[1].replace(Regex("[）)]"), "").trim() else ""
                            db.recipeDao().insertIngredient(
                                com.opp.oder.data.db.entity.RecipeIngredientEntity(
                                    menuItemId = menuId,
                                    name = name,
                                    amount = amt,
                                    unit = unit
                                )
                            )
                        }
                    }
                    PresetRecipes.defaultDrinks.forEach { (name, cat, price) ->
                        db.menuItemDao().insert(
                            com.opp.oder.data.db.entity.MenuItemEntity(name = name, price = price, category = cat)
                        )
                    }
                    PresetRecipes.defaultSnacks.forEach { (name, cat, price) ->
                        db.menuItemDao().insert(
                            com.opp.oder.data.db.entity.MenuItemEntity(name = name, price = price, category = cat)
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("OderApp", "Preset data insert failed", e)
            }
        }
    }
}
