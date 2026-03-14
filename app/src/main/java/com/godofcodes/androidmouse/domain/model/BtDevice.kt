package com.godofcodes.androidmouse.domain.model

data class BtDevice(
    val name: String,
    val address: String,
    val isPaired: Boolean = true,
    val isComputer: Boolean = false,
)
