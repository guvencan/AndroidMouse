package com.godofcodes.androidmouse.domain.usecase

import com.godofcodes.androidmouse.domain.model.JigglerConfig
import com.godofcodes.androidmouse.domain.repository.JigglerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJigglerConfigUseCase @Inject constructor(
    private val repository: JigglerRepository,
) {
    operator fun invoke(): Flow<JigglerConfig> = repository.config
}
