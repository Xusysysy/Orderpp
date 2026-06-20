package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RoleViewModel : ViewModel() {
    enum class Role { STAFF, GUEST, NONE }

    private val _role = MutableStateFlow(Role.NONE)
    val role: StateFlow<Role> = _role

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError

    fun selectRole(role: Role) {
        _role.value = role
    }

    fun verifyPin(pin: String): Boolean {
        val valid = pin == "0000"
        _pinError.value = !valid
        return valid
    }

    fun resetPinError() {
        _pinError.value = false
    }
}
