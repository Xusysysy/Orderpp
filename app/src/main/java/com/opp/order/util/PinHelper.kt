package com.opp.order.util

import android.content.Context
import android.content.SharedPreferences

object PinHelper {
    private const val PREFS_NAME = "Order_prefs"
    private const val KEY_PIN = "staff_pin"
    private const val DEFAULT_PIN = "0000"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val currentPin: String
        get() = prefs?.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN

    fun verify(pin: String): Boolean = pin == currentPin

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (!verify(oldPin)) return false
        if (newPin.length != 4) return false
        prefs?.edit()?.putString(KEY_PIN, newPin)?.apply()
        return true
    }

    fun setPin(pin: String) {
        if (pin.length == 4) {
            prefs?.edit()?.putString(KEY_PIN, pin)?.apply()
        }
    }
}
