package com.openfinds.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.BuildConfig
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatsNewViewModel
    @Inject
    constructor(
        private val preferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        fun markSeen(onDone: () -> Unit) {
            viewModelScope.launch {
                preferencesRepository.setLastSeenWhatsNewVersionCode(BuildConfig.VERSION_CODE)
                onDone()
            }
        }
    }

@Composable
fun WhatsNewScreen(
    onContinue: () -> Unit,
    viewModel: WhatsNewViewModel = hiltViewModel(),
) {
    val latest = changelogEntries.first()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("What's new in v${latest.version}", style = MaterialTheme.typography.headlineSmall)
                Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    latest.highlights.forEach { line ->
                        Text("•  $line", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Button(onClick = { viewModel.markSeen(onContinue) }, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        }
    }
}
