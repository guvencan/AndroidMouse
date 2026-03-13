package com.godofcodes.androidmouse.data.bluetooth

import com.godofcodes.androidmouse.domain.model.MouseButton
import com.godofcodes.androidmouse.domain.model.MouseEvent

/**
 * Converts [MouseEvent] domain objects into 4-byte HID mouse reports.
 *
 * Report layout:
 *   byte[0] = buttons  (bit0=LEFT, bit1=RIGHT, bit2=MIDDLE)
 *   byte[1] = dx
 *   byte[2] = dy
 *   byte[3] = scroll
 */
class HidReportBuilder {

    // Tracks which buttons are currently held down
    private var buttonMask: Byte = 0x00

    fun build(event: MouseEvent): ByteArray {
        return when (event) {
            is MouseEvent.Move -> report(dx = event.dx.clamp(), dy = event.dy.clamp())
            is MouseEvent.ButtonDown -> {
                buttonMask = (buttonMask.toInt() or event.button.bit).toByte()
                report()
            }
            is MouseEvent.ButtonUp -> {
                buttonMask = (buttonMask.toInt() and event.button.bit.inv()).toByte()
                report()
            }
            is MouseEvent.Scroll -> report(scroll = event.delta.clamp())
        }
    }

    private fun report(dx: Byte = 0, dy: Byte = 0, scroll: Byte = 0): ByteArray =
        byteArrayOf(buttonMask, dx, dy, scroll)

    private val MouseButton.bit: Int
        get() = when (this) {
            MouseButton.LEFT -> 0x01
            MouseButton.RIGHT -> 0x02
            MouseButton.MIDDLE -> 0x04
        }

    private fun Int.clamp(): Byte = coerceIn(-127, 127).toByte()
}
