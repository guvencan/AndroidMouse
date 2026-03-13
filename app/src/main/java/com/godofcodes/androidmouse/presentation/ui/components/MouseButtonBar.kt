package com.godofcodes.androidmouse.presentation.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.godofcodes.androidmouse.domain.model.MouseButton
import com.godofcodes.androidmouse.domain.model.MouseEvent

@Composable
fun MouseButtonBar(
    modifier: Modifier = Modifier,
    onMouseEvent: (MouseEvent) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MouseButton(
            label = "Left",
            modifier = Modifier.weight(1f),
            button = MouseButton.LEFT,
            onMouseEvent = onMouseEvent,
        )
        MouseButton(
            label = "Middle",
            modifier = Modifier.weight(0.7f),
            button = MouseButton.MIDDLE,
            onMouseEvent = onMouseEvent,
        )
        MouseButton(
            label = "Right",
            modifier = Modifier.weight(1f),
            button = MouseButton.RIGHT,
            onMouseEvent = onMouseEvent,
        )
    }
}

@Composable
private fun MouseButton(
    label: String,
    button: MouseButton,
    modifier: Modifier = Modifier,
    onMouseEvent: (MouseEvent) -> Unit,
) {
    Button(
        onClick = {},
        modifier = modifier
            .height(64.dp)
            .pointerInput(button) {
                detectTapGestures(
                    onPress = {
                        onMouseEvent(MouseEvent.ButtonDown(button))
                        tryAwaitRelease()
                        onMouseEvent(MouseEvent.ButtonUp(button))
                    },
                )
            },
    ) {
        Text(label)
    }
}
