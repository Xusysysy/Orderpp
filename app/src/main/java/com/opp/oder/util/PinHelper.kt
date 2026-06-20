package com.opp.oder.util

object PinHelper {
    const val DEFAULT_PIN = "0000"

    fun verify(pin: String): Boolean = pin == DEFAULT_PIN
}
