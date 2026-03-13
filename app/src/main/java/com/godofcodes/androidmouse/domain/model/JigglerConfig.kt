package com.godofcodes.androidmouse.domain.model

data class JigglerConfig(
    val enabled: Boolean = false,
    val intervalMs: Long = 30_000L,
    val pattern: JigglerPattern = JigglerPattern.RANDOM,
    val moveRange: Int = 10,  // max pixels per jiggle step (1–100)
)
