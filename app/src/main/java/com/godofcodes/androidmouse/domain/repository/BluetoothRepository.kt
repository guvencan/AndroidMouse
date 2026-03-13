package com.godofcodes.androidmouse.domain.repository

import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.domain.model.MouseEvent
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {
    val connectionState: StateFlow<ConnectionState>
    fun getPairedDevices(): List<BtDevice>
    suspend fun connect(device: BtDevice)
    fun disconnect()
    suspend fun sendEvent(event: MouseEvent)
}
