package com.godofcodes.androidmouse.data.repository

import com.godofcodes.androidmouse.data.local.JigglerConfigDataStore
import com.godofcodes.androidmouse.domain.model.JigglerConfig
import com.godofcodes.androidmouse.domain.repository.JigglerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JigglerRepositoryImpl @Inject constructor(
    private val dataStore: JigglerConfigDataStore,
) : JigglerRepository {

    override val config: Flow<JigglerConfig> = dataStore.config

    override suspend fun saveConfig(config: JigglerConfig) = dataStore.save(config)
}
