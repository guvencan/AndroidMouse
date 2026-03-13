package com.godofcodes.androidmouse.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import com.godofcodes.androidmouse.data.bluetooth.BluetoothHidManager
import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.domain.model.MouseEvent
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothRepositoryImpl @Inject constructor(
    private val hidManager: BluetoothHidManager,
    private val bluetoothAdapter: BluetoothAdapter,
) : BluetoothRepository {

    override val connectionState: StateFlow<ConnectionState> = hidManager.connectionState

    override fun getPairedDevices(): List<BtDevice> = hidManager.getPairedDevices()

    override suspend fun connect(device: BtDevice) {
        val btDevice = bluetoothAdapter.getRemoteDevice(device.address)
        hidManager.registerAndConnect(btDevice)
    }

    override fun disconnect() = hidManager.disconnect()

    override suspend fun sendEvent(event: MouseEvent) = hidManager.sendEvent(event)
}
