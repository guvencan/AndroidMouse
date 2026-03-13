package com.godofcodes.androidmouse.domain.model

sealed interface ConnectionState {
    data object Idle : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val device: BtDevice) : ConnectionState
    data class Error(val message: String) : ConnectionState
}
