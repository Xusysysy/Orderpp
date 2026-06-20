package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import com.opp.oder.util.PinHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RoleViewModel : ViewModel() {
    enum class Role { STAFF, GUEST, NONE }

    private val _role = MutableStateFlow(Role.NONE)
    val role: StateFlow<Role> = _role

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError

    private val _changePinResult = MutableStateFlow<String?>(null)
    val changePinResult: StateFlow<String?> = _changePinResult

    fun selectRole(role: Role) {
        _role.value = role
    }

    fun verifyPin(pin: String): Boolean {
        val valid = PinHelper.verify(pin)
        _pinError.value = !valid
        return valid
    }

    fun resetPinError() {
        _pinError.value = false
    }

    fun changePin(oldPin: String, newPin: String) {
        if (PinHelper.changePin(oldPin, newPin)) {
            _changePinResult.value = "密码修改成功"
        } else {
            _changePinResult.value = "旧密码错误或新密码不是4位"
        }
    }

    fun clearChangePinResult() {
        _changePinResult.value = null
    }
}
