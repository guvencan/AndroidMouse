package com.godofcodes.androidmouse.domain.usecase

import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import javax.inject.Inject

class GetPairedDevicesUseCase @Inject constructor(
    private val repository: BluetoothRepository,
) {
    operator fun invoke(): List<BtDevice> = repository.getPairedDevices()
}
