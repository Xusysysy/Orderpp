package com.opp.oder.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.opp.oder.data.db.dao.MenuItemDao
import com.opp.oder.data.db.dao.OrderDao
import com.opp.oder.data.db.dao.RecipeDao
import com.opp.oder.data.db.dao.TableDao
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.data.db.entity.OrderEntity
import com.opp.oder.data.db.entity.OrderItemEntity
import com.opp.oder.data.db.entity.RecipeIngredientEntity
import com.opp.oder.data.db.entity.RecipeStepEntity
import com.opp.oder.data.db.entity.TableEntity

@Database(
    entities = [
        TableEntity::class,
        MenuItemEntity::class,
        RecipeStepEntity::class,
        RecipeIngredientEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tableDao(): TableDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun recipeDao(): RecipeDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oder_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
