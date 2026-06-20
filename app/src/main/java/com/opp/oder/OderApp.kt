package com.opp.oder

import android.app.Application
import com.opp.oder.data.db.AppDatabase

class OderApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
}
