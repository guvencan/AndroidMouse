package com.godofcodes.androidmouse.presentation.ui.touchpad

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.godofcodes.androidmouse.R
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.presentation.ui.components.TouchpadSurface

private val JigglerActiveColor = Color(0xFF4CAF50)
private val JigglerInactiveColor = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchpadScreen(
    onNavigateToJiggler: () -> Unit,
    onDisconnected: () -> Unit,
    viewModel: TouchpadViewModel = hiltViewModel(),
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val jigglerEnabled by viewModel.jigglerEnabled.collectAsStateWithLifecycle()
    val showJigglerTooltip by viewModel.showJigglerTooltip.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToScan.collect { onDisconnected() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val label = (connectionState as? ConnectionState.Connected)?.device?.name
                        ?: stringResource(R.string.app_name)
                    Text(label)
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.dismissJigglerTooltip()
                        onNavigateToJiggler()
                    }) {
                        Icon(
                            Icons.Default.MotionPhotosOn,
                            contentDescription = stringResource(R.string.cd_jiggler),
                            tint = if (jigglerEnabled) JigglerActiveColor else JigglerInactiveColor,
                        )
                    }
                    IconButton(onClick = { viewModel.disconnect() }) {
                        Icon(
                            Icons.Default.BluetoothDisabled,
                            contentDescription = stringResource(R.string.cd_disconnect),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TouchpadSurface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                onMouseEvent = viewModel::onMouseEvent,
            )

            if (connectionState is ConnectionState.Connecting) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(stringResource(R.string.connecting), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AnimatedVisibility(
                visible = showJigglerTooltip,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                JigglerWalkthrough(
                    onTry = {
                        viewModel.dismissJigglerTooltip()
                        onNavigateToJiggler()
                    },
                    onDismiss = viewModel::dismissJigglerTooltip,
                )
            }
        }
    }
}

@Composable
private fun JigglerWalkthrough(
    onTry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cardColor = MaterialTheme.colorScheme.primaryContainer
    val onCardColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            horizontalAlignment = Alignment.End,
        ) {
            Canvas(
                modifier = Modifier
                    .size(width = 20.dp, height = 12.dp)
                    .padding(end = 8.dp),
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = cardColor)
            }

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.widthIn(max = 280.dp),
            ) {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MotionPhotosOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = stringResource(R.string.walkthrough_jiggler_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = onCardColor,
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.walkthrough_jiggler_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onCardColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.walkthrough_jiggler_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = onCardColor.copy(alpha = 0.8f),
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                stringResource(R.string.walkthrough_ok),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        FilledTonalButton(onClick = onTry) {
                            Text(
                                stringResource(R.string.walkthrough_try),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}
