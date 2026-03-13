package com.godofcodes.androidmouse.presentation.ui.touchpad

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.godofcodes.androidmouse.domain.model.ConnectionState
import com.godofcodes.androidmouse.presentation.ui.components.TouchpadSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchpadScreen(
    onNavigateToJiggler: () -> Unit,
    onDisconnected: () -> Unit,
    viewModel: TouchpadViewModel = hiltViewModel(),
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val jigglerEnabled by viewModel.jigglerEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToScan.collect { onDisconnected() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val label = (connectionState as? ConnectionState.Connected)?.device?.name
                        ?: "AndroidMouse"
                    Text(label)
                },
                actions = {
                    IconButton(onClick = onNavigateToJiggler) {
                        Icon(
                            Icons.Default.MotionPhotosOn,
                            contentDescription = "Jiggler",
                            tint = if (jigglerEnabled) Color(0xFF4CAF50) else LocalContentColor.current,
                        )
                    }
                    IconButton(onClick = { viewModel.disconnect() }) {
                        Icon(Icons.Default.BluetoothDisabled, contentDescription = "Disconnect")
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

            // Connecting overlay
            if (connectionState is ConnectionState.Connecting) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
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
                        Text("Connecting…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
