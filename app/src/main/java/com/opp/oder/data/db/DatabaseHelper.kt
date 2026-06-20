package com.opp.oder.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context.applicationContext, "oder_database", null, 1
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE tables (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                zone TEXT DEFAULT '',
                status TEXT DEFAULT 'IDLE'
            )
        """)
        db.execSQL("""
            CREATE TABLE menu_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                price REAL DEFAULT 0,
                category TEXT DEFAULT 'cocktail',
                hasRecipe INTEGER DEFAULT 0
            )
        """)
        db.execSQL("""
            CREATE TABLE recipe_steps (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                menuItemId INTEGER NOT NULL,
                stepNumber INTEGER NOT NULL,
                description TEXT NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE recipe_ingredients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                menuItemId INTEGER NOT NULL,
                name TEXT NOT NULL,
                amount TEXT,
                unit TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tableId INTEGER NOT NULL,
                status TEXT DEFAULT 'ACTIVE',
                createdAt INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                orderId INTEGER NOT NULL,
                menuItemId INTEGER NOT NULL,
                name TEXT NOT NULL,
                quantity INTEGER DEFAULT 1,
                price REAL DEFAULT 0
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS order_items")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients")
        db.execSQL("DROP TABLE IF EXISTS recipe_steps")
        db.execSQL("DROP TABLE IF EXISTS menu_items")
        db.execSQL("DROP TABLE IF EXISTS tables")
        onCreate(db)
    }
}
