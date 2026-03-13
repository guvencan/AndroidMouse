package com.godofcodes.androidmouse.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.godofcodes.androidmouse.domain.model.MouseButton
import com.godofcodes.androidmouse.domain.model.MouseEvent
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private const val TARGET_SCREEN_WIDTH_PX = 1440f
private const val TWO_FINGER_TAP_TIMEOUT_MS = 120L * 4
private const val TAP_TIMEOUT_MS = 200L

@Composable
fun TouchpadSurface(
    modifier: Modifier = Modifier,
    onMouseEvent: (MouseEvent) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    val sensitivity = remember(configuration.screenWidthDp, density) {
        val phoneWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
        TARGET_SCREEN_WIDTH_PX / phoneWidthPx
    }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .indication(interactionSource, ripple())
            .pointerInput(sensitivity) {
                val touchSlop = viewConfiguration.touchSlop

                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(firstDown.position)
                    // launch is not a suspend call — safe inside restricted scope
                    scope.launch { interactionSource.emit(press) }

                    val downTime = System.currentTimeMillis()
                    var lastPosition: Offset = firstDown.position
                    var dragging = false
                    var twoFingerDetected = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }

                        if (!dragging && pressed.size >= 2) {
                            val elapsed = System.currentTimeMillis() - downTime
                            if (elapsed < TWO_FINGER_TAP_TIMEOUT_MS) twoFingerDetected = true
                        }

                        if (pressed.isEmpty()) {
                            scope.launch { interactionSource.emit(PressInteraction.Release(press)) }
                            val elapsed = System.currentTimeMillis() - downTime
                            if (!dragging && elapsed < TAP_TIMEOUT_MS) {
                                if (twoFingerDetected) {
                                    onMouseEvent(MouseEvent.ButtonDown(MouseButton.RIGHT))
                                    onMouseEvent(MouseEvent.ButtonUp(MouseButton.RIGHT))
                                } else {
                                    onMouseEvent(MouseEvent.ButtonDown(MouseButton.LEFT))
                                    onMouseEvent(MouseEvent.ButtonUp(MouseButton.LEFT))
                                }
                            }
                            break
                        }

                        if (!twoFingerDetected) {
                            val change = event.changes.firstOrNull { it.id == firstDown.id }
                                ?: continue
                            val delta = change.position - lastPosition

                            if (!dragging &&
                                (delta.x.absoluteValue > touchSlop || delta.y.absoluteValue > touchSlop)
                            ) {
                                dragging = true
                            }

                            if (dragging) {
                                val dx = (delta.x * sensitivity).roundToInt()
                                val dy = (delta.y * sensitivity).roundToInt()
                                if (dx != 0 || dy != 0) onMouseEvent(MouseEvent.Move(dx, dy))
                                lastPosition = change.position
                                change.consume()
                            }
                        }
                    }
                }
            },
    )
}
