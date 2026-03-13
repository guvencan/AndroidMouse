package com.godofcodes.androidmouse.presentation.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godofcodes.androidmouse.data.local.AppPreferencesDataStore
import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import com.godofcodes.androidmouse.domain.usecase.ConnectDeviceUseCase
import com.godofcodes.androidmouse.domain.usecase.GetPairedDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val getPairedDevices: GetPairedDevicesUseCase,
    private val connectDevice: ConnectDeviceUseCase,
    private val bluetoothRepository: BluetoothRepository,
    private val appPrefs: AppPreferencesDataStore,
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = bluetoothRepository.connectionState
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectionState.Idle)

    val pairedDevices: List<BtDevice> get() = getPairedDevices()

    fun connect(device: BtDevice) {
        viewModelScope.launch { connectDevice(device) }
    }
}
