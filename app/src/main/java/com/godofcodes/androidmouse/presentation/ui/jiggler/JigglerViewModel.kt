package com.godofcodes.androidmouse.presentation.ui.jiggler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godofcodes.androidmouse.domain.jiggler.JigglerController
import com.godofcodes.androidmouse.domain.model.JigglerConfig
import com.godofcodes.androidmouse.domain.usecase.GetJigglerConfigUseCase
import com.godofcodes.androidmouse.domain.usecase.SaveJigglerConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JigglerViewModel @Inject constructor(
    private val getJigglerConfig: GetJigglerConfigUseCase,
    private val saveJigglerConfig: SaveJigglerConfigUseCase,
    private val jigglerController: JigglerController,
) : ViewModel() {

    val config: StateFlow<JigglerConfig> = getJigglerConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JigglerConfig())

    val isRunning: StateFlow<Boolean> = jigglerController.isRunning
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val updated = config.value.copy(enabled = enabled)
            saveJigglerConfig(updated)
            if (enabled) jigglerController.start(updated) else jigglerController.stop()
        }
    }

    fun setInterval(intervalMs: Long) {
        viewModelScope.launch {
            val updated = config.value.copy(intervalMs = intervalMs)
            saveJigglerConfig(updated)
            if (jigglerController.isRunning.value) jigglerController.start(updated)
        }
    }

    fun setMoveRange(range: Int) {
        viewModelScope.launch {
            val updated = config.value.copy(moveRange = range)
            saveJigglerConfig(updated)
            if (jigglerController.isRunning.value) jigglerController.start(updated)
        }
    }
}
