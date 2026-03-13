package com.godofcodes.androidmouse.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.godofcodes.androidmouse.data.local.AppPreferencesDataStore
import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.domain.model.MouseEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("MissingPermission")
class BluetoothHidManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val appPrefs: AppPreferencesDataStore,
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var targetDevice: BluetoothDevice? = null
    private var pollJob: Job? = null
    private val reportBuilder = HidReportBuilder()

    private val sdpSettings = BluetoothHidDeviceAppSdpSettings(
        "AndroidMouse",
        "Bluetooth Mouse",
        "godofcodes",
        BluetoothHidDevice.SUBCLASS1_MOUSE,
        HidDescriptor.MOUSE_REPORT,
    )

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            if (registered) {
                val device = pluggedDevice ?: targetDevice ?: return
                hidDevice?.connect(device)
                // Fallback poll: some devices never fire STATE_CONNECTED callback
                startPolling(device)
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            when (state) {
                BluetoothProfile.STATE_CONNECTING ->
                    _connectionState.value = ConnectionState.Connecting

                BluetoothProfile.STATE_CONNECTED -> {
                    pollJob?.cancel()
                    connectedDevice = device
                    _connectionState.value = ConnectionState.Connected(
                        BtDevice(device.name ?: device.address, device.address)
                    )
                    scope.launch { appPrefs.saveLastDevice(device.address) }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevice = null
                    _connectionState.value = ConnectionState.Idle
                }
            }
        }
    }

    /** Poll every 500 ms for up to 10 s in case onConnectionStateChanged never fires. */
    private fun startPolling(target: BluetoothDevice) {
        pollJob?.cancel()
        pollJob = scope.launch {
            repeat(20) {
                delay(500)
                val hid = hidDevice ?: return@launch
                val isConnected = hid.connectedDevices.any { it.address == target.address }
                if (isConnected && _connectionState.value !is ConnectionState.Connected) {
                    connectedDevice = target
                    _connectionState.value = ConnectionState.Connected(
                        BtDevice(target.name ?: target.address, target.address)
                    )
                    scope.launch { appPrefs.saveLastDevice(target.address) }
                    return@launch
                }
                if (_connectionState.value is ConnectionState.Connected) return@launch
            }
            // 10 s elapsed without connecting
            if (_connectionState.value is ConnectionState.Connecting) {
                _connectionState.value = ConnectionState.Error("Connection timed out")
            }
        }
    }

    fun registerAndConnect(target: BluetoothDevice) {
        targetDevice = target
        _connectionState.value = ConnectionState.Connecting

        hidDevice?.unregisterApp()
        hidDevice = null

        bluetoothAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                hidDevice = proxy as BluetoothHidDevice
                hidDevice?.registerApp(
                    sdpSettings,
                    null,
                    null,
                    Executors.newSingleThreadExecutor(),
                    callback,
                )
            }
            override fun onServiceDisconnected(profile: Int) {
                hidDevice = null
                _connectionState.value = ConnectionState.Idle
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    fun disconnect() {
        pollJob?.cancel()
        connectedDevice?.let { hidDevice?.disconnect(it) }
        hidDevice?.unregisterApp()
        connectedDevice = null
        _connectionState.value = ConnectionState.Idle
    }

    fun sendEvent(event: MouseEvent) {
        val device = connectedDevice ?: return
        val report = reportBuilder.build(event)
        hidDevice?.sendReport(device, 0, report)
    }

    fun getPairedDevices(): List<BtDevice> =
        bluetoothAdapter.bondedDevices.map { BtDevice(it.name ?: it.address, it.address) }
}
