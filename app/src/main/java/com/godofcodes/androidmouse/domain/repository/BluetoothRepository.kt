package com.godofcodes.androidmouse.domain.repository

import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.domain.model.MouseEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {
    val connectionState: StateFlow<ConnectionState>
    val discoveredDevices: StateFlow<List<BtDevice>>
    val isDiscovering: StateFlow<Boolean>
    val bondStateChanged: Flow<Unit>
    fun getPairedDevices(computersOnly: Boolean = false): List<BtDevice>
    suspend fun connect(device: BtDevice)
    fun disconnect()
    suspend fun sendEvent(event: MouseEvent)
    fun startDiscovery()
    fun stopDiscovery()
    fun pair(device: BtDevice)
    fun unpair(device: BtDevice)
}
