package com.godofcodes.androidmouse.domain.model

enum class MouseButton { LEFT, RIGHT, MIDDLE }

sealed interface MouseEvent {
    data class Move(val dx: Int, val dy: Int) : MouseEvent
    data class ButtonDown(val button: MouseButton) : MouseEvent
    data class ButtonUp(val button: MouseButton) : MouseEvent
    data class Scroll(val delta: Int) : MouseEvent
}
