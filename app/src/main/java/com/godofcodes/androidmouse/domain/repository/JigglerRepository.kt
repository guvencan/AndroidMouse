package com.godofcodes.androidmouse.domain.repository

import com.godofcodes.androidmouse.domain.model.JigglerConfig
import kotlinx.coroutines.flow.Flow

interface JigglerRepository {
    val config: Flow<JigglerConfig>
    suspend fun saveConfig(config: JigglerConfig)
}
