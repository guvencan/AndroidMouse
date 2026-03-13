package com.godofcodes.androidmouse.domain.usecase

import com.godofcodes.androidmouse.domain.model.MouseEvent
import com.godofcodes.androidmouse.domain.repository.BluetoothRepository
import javax.inject.Inject

class SendMouseEventUseCase @Inject constructor(
    private val repository: BluetoothRepository,
) {
    suspend operator fun invoke(event: MouseEvent) = repository.sendEvent(event)
}
