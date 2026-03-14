package com.godofcodes.androidmouse.presentation.ui.scan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.godofcodes.androidmouse.R
import com.godofcodes.androidmouse.domain.model.BtDevice
import com.godofcodes.androidmouse.domain.model.ConnectionState

@Composable
fun DeviceScanScreen(
    onConnected: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()
    val isDiscovering by viewModel.isDiscovering.collectAsStateWithLifecycle()
    val computersOnly by viewModel.computersOnly.collectAsStateWithLifecycle()
    var deviceToUnpair by remember { mutableStateOf<BtDevice?>(null) }

    LaunchedEffect(Unit) {
        viewModel.connectionState.collect { state ->
            if (state is ConnectionState.Connected) onConnected()
        }
    }

    deviceToUnpair?.let { device ->
        AlertDialog(
            onDismissRequest = { deviceToUnpair = null },
            title = { Text(stringResource(R.string.scan_unpair_title)) },
            text = { Text(stringResource(R.string.scan_unpair_message, device.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.unpair(device)
                    deviceToUnpair = null
                }) { Text(stringResource(R.string.scan_unpair), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deviceToUnpair = null }) {
                    Text(stringResource(R.string.scan_cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.scan_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        when (connectionState) {
            is ConnectionState.Connecting -> Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.connecting))
            }
            is ConnectionState.Error -> Text(
                text = (connectionState as ConnectionState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            else -> Unit
        }

        FilterChip(
            selected = computersOnly,
            onClick = viewModel::toggleComputersOnly,
            label = { Text(stringResource(R.string.scan_computers_only)) },
            leadingIcon = if (computersOnly) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                }
            } else null,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text(
                    text = stringResource(R.string.scan_paired_devices),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            if (pairedDevices.isEmpty()) {
                item {
                    Text(
                        text = stringResource(
                            if (computersOnly) R.string.scan_no_paired_computers
                            else R.string.scan_no_paired_devices
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            } else {
                items(pairedDevices, key = { it.address }) { device ->
                    PairedDeviceItem(
                        device = device,
                        onClick = { viewModel.connect(device) },
                        onUnpair = { deviceToUnpair = device },
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.scan_nearby_devices),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    if (isDiscovering) {
                        TextButton(onClick = viewModel::stopDiscovery) {
                            Text(stringResource(R.string.scan_stop))
                        }
                    } else {
                        TextButton(onClick = viewModel::startDiscovery) {
                            Text(stringResource(R.string.scan_scan))
                        }
                    }
                }
                if (isDiscovering) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
            }

            if (discoveredDevices.isEmpty() && !isDiscovering) {
                item {
                    Text(
                        text = stringResource(R.string.scan_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            items(discoveredDevices, key = { it.address }) { device ->
                DiscoveredDeviceItem(
                    device = device,
                    onPair = { viewModel.pair(device) },
                )
            }
        }
    }
}

@Composable
private fun PairedDeviceItem(
    device: BtDevice,
    onClick: () -> Unit,
    onUnpair: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (device.isComputer) Icons.Default.Computer else Icons.Default.Bluetooth,
                contentDescription = null,
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick),
            ) {
                Text(device.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onUnpair) {
                Icon(
                    Icons.Default.LinkOff,
                    contentDescription = stringResource(R.string.scan_cd_unpair),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun DiscoveredDeviceItem(
    device: BtDevice,
    onPair: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (device.isComputer) Icons.Default.Computer else Icons.Default.Bluetooth,
                contentDescription = null,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(onClick = onPair) {
                Text(stringResource(R.string.scan_pair))
            }
        }
    }
}
