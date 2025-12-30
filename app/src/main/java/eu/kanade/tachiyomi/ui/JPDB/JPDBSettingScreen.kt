package eu.kanade.tachiyomi.ui.JPDB

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.util.LocalBackPress
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.JPDBKey
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.TextButton

class JPDBSettingScreen : Screen() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val handleBack = LocalBackPress.current
        var showDialog by remember { mutableStateOf(false) }
        var text by remember { mutableStateOf(JPDBKey.apiKey) }

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = "JPDB API Key",
                    navigateUp = if (handleBack != null) handleBack::invoke else null,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            ScrollbarLazyColumn(
                contentPadding = contentPadding,
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "JPDB Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Set your JPDB API key to enable parsing of Japanese vocabulary from subtitles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                item {
                    TextPreferenceWidget(
                        title = "Clé JPDB",
                        subtitle = if (JPDBKey.apiKey.isNotEmpty()) "••••••••" else "Not set",
                        icon = Icons.Outlined.VpnKey,
                        onPreferenceClick = { showDialog = true },
                    )
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    ) {
                        Text(
                            text = "How to get your API key?",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "1. Visit jpdb.io\n2. Log in to your account\n3. Go to settings\n4. Copy your API key\n5. Paste it below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Edit JPDB API Key") },
                text = {
                    Column {
                        Text(
                            text = "Enter your JPDB API key:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            placeholder = { Text("Paste your API key here") },
                            singleLine = false,
                            maxLines = 3,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                JPDBKey.apiKey = text
                                Toast.makeText(context, "API key saved successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter an API key", Toast.LENGTH_SHORT).show()
                            }
                            showDialog = false
                        },
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }

}
