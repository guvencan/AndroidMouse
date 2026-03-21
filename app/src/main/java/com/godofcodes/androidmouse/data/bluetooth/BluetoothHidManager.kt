package com.godofcodes.androidmouse.data.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _discoveredDevices = MutableStateFlow<List<BtDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BtDevice>> = _discoveredDevices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _bondStateChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val bondStateChanged: SharedFlow<Unit> = _bondStateChanged.asSharedFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter.isEnabled)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var targetDevice: BluetoothDevice? = null
    private var pollJob: Job? = null
    private val reportBuilder = HidReportBuilder()
    private var discoveryReceiverRegistered = false

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            val enabled = state == BluetoothAdapter.STATE_ON
            _isBluetoothEnabled.value = enabled
            if (!enabled) {
                pollJob?.cancel()
                connectedDevice = null
                hidDevice?.unregisterApp()
                hidDevice = null
                _connectionState.value = ConnectionState.Idle
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
            val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
            if (bondState == BluetoothDevice.BOND_BONDED) {
                _discoveredDevices.value = _discoveredDevices.value.filter { it.address != device?.address }
            }
            _bondStateChanged.tryEmit(Unit)
        }
    }

    init {
        context.registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        context.registerReceiver(bondStateReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
    }

    private val sdpSettings = BluetoothHidDeviceAppSdpSettings(
        "MX Master 3",
        "Logitech MX Master 3 Wireless Mouse",
        "Logitech",
        BluetoothHidDevice.SUBCLASS1_MOUSE,
        HidDescriptor.MOUSE_REPORT,
    )

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            if (registered) {
                val device = targetDevice ?: pluggedDevice ?: return
                hidDevice?.connect(device)
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

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    device ?: return
                    val btDevice = device.toBtDevice(isPaired = false)
                    val current = _discoveredDevices.value
                    if (current.none { it.address == btDevice.address }) {
                        _discoveredDevices.value = current + btDevice
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isDiscovering.value = false
                }
            }
        }
    }

    private fun startPolling(target: BluetoothDevice) {
        pollJob?.cancel()
        pollJob = scope.launch {
            repeat(20) {
                delay(500)
                val hid = hidDevice ?: return@launch
                if (hid.connectedDevices.any { it.address == target.address } &&
                    _connectionState.value !is ConnectionState.Connected
                ) {
                    connectedDevice = target
                    _connectionState.value = ConnectionState.Connected(
                        BtDevice(target.name ?: target.address, target.address)
                    )
                    scope.launch { appPrefs.saveLastDevice(target.address) }
                    return@launch
                }
                if (_connectionState.value is ConnectionState.Connected) return@launch
            }
            if (_connectionState.value is ConnectionState.Connecting) {
                _connectionState.value = ConnectionState.Error("Connection timed out")
            }
        }
    }

    fun registerAndConnect(target: BluetoothDevice) {
        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = ConnectionState.Error("Bluetooth is disabled")
            return
        }
        if (!hasBluetoothConnectPermission()) {
            _connectionState.value = ConnectionState.Error("Bluetooth permission not granted")
            return
        }
        targetDevice = target
        _connectionState.value = ConnectionState.Connecting
        hidDevice?.unregisterApp()
        hidDevice = null
        bluetoothAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                hidDevice = proxy as BluetoothHidDevice
                hidDevice?.registerApp(
                    sdpSettings, null, null,
                    Executors.newSingleThreadExecutor(),
                    hidCallback,
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

    fun getPairedDevices(computersOnly: Boolean = false): List<BtDevice> {
        if (!bluetoothAdapter.isEnabled || !hasBluetoothConnectPermission()) return emptyList()
        return bluetoothAdapter.bondedDevices
            .filter { !computersOnly || it.isComputer() }
            .map { it.toBtDevice(isPaired = true) }
    }

    fun startDiscovery() {
        if (!bluetoothAdapter.isEnabled || !hasDiscoveryPermission()) return
        _discoveredDevices.value = emptyList()
        _isDiscovering.value = true
        if (!discoveryReceiverRegistered) {
            context.registerReceiver(
                discoveryReceiver,
                IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
            )
            discoveryReceiverRegistered = true
        }
        bluetoothAdapter.cancelDiscovery()
        bluetoothAdapter.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothAdapter.cancelDiscovery()
        _isDiscovering.value = false
        if (discoveryReceiverRegistered) {
            try { context.unregisterReceiver(discoveryReceiver) } catch (_: Exception) {}
            discoveryReceiverRegistered = false
        }
    }

    fun pair(address: String) {
        bluetoothAdapter.getRemoteDevice(address)?.createBond()
    }

    fun unpair(address: String) {
        val device = bluetoothAdapter.getRemoteDevice(address) ?: return
        try {
            device.javaClass.getMethod("removeBond").invoke(device)
        } catch (_: Exception) {}
    }

    private fun hasBluetoothConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED

    private fun hasDiscoveryPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        }

    private fun BluetoothDevice.isComputer(): Boolean =
        bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER

    private fun BluetoothDevice.toBtDevice(isPaired: Boolean) = BtDevice(
        name = name ?: address,
        address = address,
        isPaired = isPaired,
        isComputer = isComputer(),
    )
}
