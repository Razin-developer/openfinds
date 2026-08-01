package com.openfinds.app.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.openfinds.app.core.ui.components.BackIconButton

private const val REPO_URL = "https://github.com/Razin-developer/openfinds"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceScreen(
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text("Open source") }, navigationIcon = { BackIconButton(onBack) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "OpenFind is free and open source under the Apache License 2.0. Contributions, bug reports, and feature requests are welcome.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REPO_URL))) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Code, contentDescription = null)
                Text("  View source on GitHub")
            }
            OutlinedButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$REPO_URL/issues/new"))) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.BugReport, contentDescription = null)
                Text("  Report an issue")
            }
            OutlinedButton(onClick = onOpenLicenses, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Description, contentDescription = null)
                Text("  Third-party licenses")
            }
        }
    }
}
