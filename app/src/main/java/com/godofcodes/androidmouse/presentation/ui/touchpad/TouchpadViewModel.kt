package com.godofcodes.androidmouse.presentation.ui.touchpad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godofcodes.androidmouse.data.local.AppPreferencesDataStore
import com.godofcodes.androidmouse.domain.jiggler.JigglerController
import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.domain.model.MouseEvent
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import com.godofcodes.androidmouse.domain.usecase.ConnectDeviceUseCase
import com.godofcodes.androidmouse.domain.usecase.DisconnectUseCase
import com.godofcodes.androidmouse.domain.usecase.GetPairedDevicesUseCase
import com.godofcodes.androidmouse.domain.usecase.SendMouseEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TouchpadViewModel @Inject constructor(
    private val sendMouseEvent: SendMouseEventUseCase,
    private val disconnect: DisconnectUseCase,
    private val connectDevice: ConnectDeviceUseCase,
    private val getPairedDevices: GetPairedDevicesUseCase,
    private val bluetoothRepository: BluetoothRepository,
    private val jigglerController: JigglerController,
    private val appPrefs: AppPreferencesDataStore,
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = bluetoothRepository.connectionState
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectionState.Idle)

    val jigglerEnabled: StateFlow<Boolean> = jigglerController.isRunning
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val showJigglerTooltip: StateFlow<Boolean> = appPrefs.jigglerTooltipShown
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun dismissJigglerTooltip() {
        viewModelScope.launch { appPrefs.markJigglerTooltipShown() }
    }

    private val _navigateToScan = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToScan = _navigateToScan.asSharedFlow()

    private var wasConnected = false

    init {
        autoConnect()
        viewModelScope.launch {
            bluetoothRepository.connectionState.collect { state ->
                when (state) {
                    is ConnectionState.Connected -> wasConnected = true
                    is ConnectionState.Idle -> if (wasConnected) _navigateToScan.emit(Unit)
                    is ConnectionState.Error -> _navigateToScan.emit(Unit)
                    else -> Unit
                }
            }
        }
    }

    private fun autoConnect() {
        if (bluetoothRepository.connectionState.value is ConnectionState.Connected) return
        viewModelScope.launch {
            val lastAddress = appPrefs.lastDeviceAddress.firstOrNull() ?: run {
                _navigateToScan.emit(Unit)
                return@launch
            }
            val device: BtDevice = getPairedDevices().firstOrNull { it.address == lastAddress }
                ?: run {
                    _navigateToScan.emit(Unit)
                    return@launch
                }
            connectDevice(device)
        }
    }

    fun onMouseEvent(event: MouseEvent) {
        viewModelScope.launch { sendMouseEvent(event) }
    }

    fun disconnect() = disconnect.invoke()
}
