package com.openfinds.app.feature.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openfinds.app.core.ui.components.BackIconButton
import com.openfinds.app.core.ui.components.EmptyState

private val groupColorPalette =
    listOf(
        0xFF5B6CFF.toInt(),
        0xFF2FA766.toInt(),
        0xFFCC8A1E.toInt(),
        0xFFE05252.toInt(),
        0xFF8B94FF.toInt(),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceGroupsScreen(
    onBack: () -> Unit,
    viewModel: DeviceGroupsViewModel = hiltViewModel(),
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTargetId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Device groups") }, navigationIcon = { BackIconButton(onBack) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("New group") },
            )
        },
    ) { padding ->
        if (groups.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding),
                icon = Icons.Outlined.Groups,
                title = "No groups yet",
                message = "Create a group to organize devices — e.g. \"Family\" or \"Work\".",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(groups, key = { it.id }) { group ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(14.dp).background(Color(group.colorArgb), CircleShape))
                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(group.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${group.deviceCount} device${if (group.deviceCount == 1) "" else "s"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { renameTargetId = group.id }) { Text("Rename") }
                            IconButton(onClick = { viewModel.deleteGroup(group.id) }) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete group")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New group") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true) },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        viewModel.createGroup(name, groupColorPalette[groups.size % groupColorPalette.size])
                        showCreateDialog = false
                    },
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } },
        )
    }

    renameTargetId?.let { groupId ->
        val group = groups.firstOrNull { it.id == groupId }
        var name by remember(groupId) { mutableStateOf(group?.name ?: "") }
        AlertDialog(
            onDismissRequest = { renameTargetId = null },
            title = { Text("Rename group") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true) },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        viewModel.renameGroup(groupId, name)
                        renameTargetId = null
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renameTargetId = null }) { Text("Cancel") } },
        )
    }
}
