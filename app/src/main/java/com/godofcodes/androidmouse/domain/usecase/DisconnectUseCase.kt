package com.godofcodes.androidmouse.domain.usecase

import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import javax.inject.Inject

class DisconnectUseCase @Inject constructor(
    private val repository: BluetoothRepository,
) {
    operator fun invoke() = repository.disconnect()
}
