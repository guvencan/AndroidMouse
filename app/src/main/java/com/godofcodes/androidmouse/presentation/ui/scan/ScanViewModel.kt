package com.godofcodes.androidmouse.presentation.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import com.godofcodes.androidmouse.domain.usecase.ConnectDeviceUseCase
import com.godofcodes.androidmouse.domain.usecase.GetPairedDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val getPairedDevices: GetPairedDevicesUseCase,
    private val connectDevice: ConnectDeviceUseCase,
    private val bluetoothRepository: BluetoothRepository,
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = bluetoothRepository.connectionState
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectionState.Idle)

    val discoveredDevices: StateFlow<List<BtDevice>> = bluetoothRepository.discoveredDevices
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isDiscovering: StateFlow<Boolean> = bluetoothRepository.isDiscovering
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _computersOnly = MutableStateFlow(true)
    val computersOnly: StateFlow<Boolean> = _computersOnly.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BtDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BtDevice>> = _pairedDevices.asStateFlow()

    init {
        refreshPaired()
        viewModelScope.launch {
            bluetoothRepository.bondStateChanged.collect { refreshPaired() }
        }
        viewModelScope.launch {
            _computersOnly.collect { refreshPaired() }
        }
    }

    fun toggleComputersOnly() {
        _computersOnly.value = !_computersOnly.value
    }

    private fun refreshPaired() {
        _pairedDevices.value = getPairedDevices(_computersOnly.value)
    }

    fun connect(device: BtDevice) {
        viewModelScope.launch { connectDevice(device) }
    }

    fun startDiscovery() = bluetoothRepository.startDiscovery()

    fun stopDiscovery() = bluetoothRepository.stopDiscovery()

    fun pair(device: BtDevice) = bluetoothRepository.pair(device)

    fun unpair(device: BtDevice) {
        bluetoothRepository.unpair(device)
        _pairedDevices.value = _pairedDevices.value.filter { it.address != device.address }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothRepository.stopDiscovery()
    }
}
