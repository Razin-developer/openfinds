package com.openfinds.app.feature.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.ui.components.EmptyState
import com.openfinds.app.core.ui.components.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onDeviceClick: (String) -> Unit,
    onAddDevice: () -> Unit,
    viewModel: DevicesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Devices") },
                actions = {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.Outlined.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        DropdownMenuItem(text = { Text("Online first") }, onClick = { viewModel.onSortOrderChange(DeviceSortOrder.ONLINE_FIRST); sortMenuOpen = false })
                        DropdownMenuItem(text = { Text("Name") }, onClick = { viewModel.onSortOrderChange(DeviceSortOrder.NAME); sortMenuOpen = false })
                        DropdownMenuItem(text = { Text("Last seen") }, onClick = { viewModel.onSortOrderChange(DeviceSortOrder.LAST_SEEN); sortMenuOpen = false })
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddDevice, icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) }, text = { Text("Pair device") })
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search devices") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )

            when {
                uiState.isLoading -> FullScreenLoading(modifier = Modifier.fillMaxSize())
                uiState.allDevices.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Devices,
                    title = "No trusted devices yet",
                    message = "Pair your first device to see it here.",
                )
                uiState.visibleDevices.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Devices,
                    title = "No matches",
                    message = "Nothing matches \"${uiState.query}\".",
                )
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.visibleDevices, key = { it.id }) { device ->
                        DeviceListItem(device = device, onClick = { onDeviceClick(device.id) })
                    }
                }
            }
        }
    }
}
