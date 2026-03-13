package com.godofcodes.androidmouse.domain.jiggler

import com.godofcodes.androidmouse.domain.model.JigglerConfig
import com.godofcodes.androidmouse.domain.model.MouseEvent
import com.godofcodes.androidmouse.domain.usecase.SendMouseEventUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

@Singleton
class JigglerController @Inject constructor(
    private val sendMouseEvent: SendMouseEventUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun start(config: JigglerConfig) {
        job?.cancel()
        _isRunning.value = true
        job = scope.launch {
            while (true) {
                delay(config.intervalMs)
                val totalDx = Random.nextInt(-config.moveRange, config.moveRange + 1)
                val totalDy = Random.nextInt(-config.moveRange, config.moveRange + 1)
                smoothMove(totalDx, totalDy)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _isRunning.value = false
    }

    /**
     * Splits the total displacement into small ~1-2px steps with 16ms delay
     * so the cursor glides smoothly instead of jumping.
     */
    private suspend fun smoothMove(totalDx: Int, totalDy: Int) {
        val steps = maxOf(abs(totalDx), abs(totalDy)).coerceIn(1, 60)
        var accumDx = 0f
        var accumDy = 0f
        val stepDx = totalDx.toFloat() / steps
        val stepDy = totalDy.toFloat() / steps

        repeat(steps) {
            accumDx += stepDx
            accumDy += stepDy
            val dx = accumDx.roundToInt()
            val dy = accumDy.roundToInt()
            accumDx -= dx
            accumDy -= dy
            if (dx != 0 || dy != 0) sendMouseEvent(MouseEvent.Move(dx, dy))
            delay(16L)
        }
    }
}
