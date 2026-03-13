package com.godofcodes.androidmouse.domain.usecase

import com.godofcodes.androidmouse.domain.model.JigglerConfig
import com.godofcodes.androidmouse.domain.repository.JigglerRepository
import javax.inject.Inject

class SaveJigglerConfigUseCase @Inject constructor(
    private val repository: JigglerRepository,
) {
    suspend operator fun invoke(config: JigglerConfig) = repository.saveConfig(config)
}
