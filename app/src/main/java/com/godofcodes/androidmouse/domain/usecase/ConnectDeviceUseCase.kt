package com.godofcodes.androidmouse.domain.usecase

import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import javax.inject.Inject

class ConnectDeviceUseCase @Inject constructor(
    private val repository: BluetoothRepository,
) {
    suspend operator fun invoke(device: BtDevice) = repository.connect(device)
}
