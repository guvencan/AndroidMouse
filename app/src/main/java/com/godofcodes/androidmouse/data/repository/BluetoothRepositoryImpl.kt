package com.godofcodes.androidmouse.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import com.godofcodes.androidmouse.data.bluetooth.BluetoothHidManager
import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.domain.model.MouseEvent
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothRepositoryImpl @Inject constructor(
    private val hidManager: BluetoothHidManager,
    private val bluetoothAdapter: BluetoothAdapter,
) : BluetoothRepository {

    override val connectionState: StateFlow<ConnectionState> = hidManager.connectionState
    override val discoveredDevices: StateFlow<List<BtDevice>> = hidManager.discoveredDevices
    override val isDiscovering: StateFlow<Boolean> = hidManager.isDiscovering
    override val isBluetoothEnabled: StateFlow<Boolean> = hidManager.isBluetoothEnabled
    override val bondStateChanged: Flow<Unit> = hidManager.bondStateChanged

    override fun getPairedDevices(computersOnly: Boolean) = hidManager.getPairedDevices(computersOnly)

    override suspend fun connect(device: BtDevice) {
        val btDevice = bluetoothAdapter.getRemoteDevice(device.address)
        hidManager.registerAndConnect(btDevice)
    }

    override fun disconnect() = hidManager.disconnect()

    override suspend fun sendEvent(event: MouseEvent) = hidManager.sendEvent(event)

    override fun startDiscovery() = hidManager.startDiscovery()

    override fun stopDiscovery() = hidManager.stopDiscovery()

    override fun pair(device: BtDevice) = hidManager.pair(device.address)

    override fun unpair(device: BtDevice) = hidManager.unpair(device.address)
}
