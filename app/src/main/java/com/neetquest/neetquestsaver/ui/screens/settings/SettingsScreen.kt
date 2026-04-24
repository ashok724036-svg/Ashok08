package com.neetquest.neetquestsaver.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neetquest.neetquestsaver.service.FloatingBubbleService
import com.neetquest.neetquestsaver.utils.BackupManager
import com.neetquest.neetquestsaver.utils.PreferencesManager
import com.neetquest.neetquestsaver.viewmodel.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val backupManager: BackupManager
) : ViewModel() {

    val isBubbleEnabled = preferencesManager.isBubbleEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setBubbleEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setBubbleEnabled(enabled) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isBubbleEnabled by settingsViewModel.isBubbleEnabled.collectAsStateWithLifecycle()
    val totalCount by mainViewModel.totalCount.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle bubble toggle & overlay permission
    fun handleBubbleToggle(enabled: Boolean) {
        if (enabled) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            } else {
                settingsViewModel.setBubbleEnabled(true)
                FloatingBubbleService.start(context)
            }
        } else {
            settingsViewModel.setBubbleEnabled(false)
            FloatingBubbleService.stopService(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Floating Bubble ───────────────────────────────────────────────
            SettingsSection("Capture") {
                SettingsSwitch(
                    icon = Icons.Default.BubbleChart,
                    title = "Floating Bubble",
                    subtitle = if (isBubbleEnabled) "Active — tap bubble to capture" else "Disabled — enable to capture from any app",
                    checked = isBubbleEnabled,
                    onCheckedChange = { handleBubbleToggle(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Overlay Permission",
                    subtitle = "Required for floating bubble",
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                )
            }

            // ── Data ──────────────────────────────────────────────────────────
            SettingsSection("Data & Backup") {
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "Export All Data",
                    subtitle = "Save $totalCount questions as JSON backup",
                    onClick = {
                        // Export triggered in coroutine
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "Import Backup",
                    subtitle = "Restore questions from JSON file",
                    onClick = { /* Launch file picker */ }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "Clear All Data",
                    subtitle = "Remove all saved questions and images",
                    onClick = { showClearDialog = true },
                    isDestructive = true
                )
            }

            // ── Stats ─────────────────────────────────────────────────────────
            SettingsSection("Statistics") {
                val physicsCount by mainViewModel.physicsCount.collectAsStateWithLifecycle()
                val chemistryCount by mainViewModel.chemistryCount.collectAsStateWithLifecycle()
                val botanyCount by mainViewModel.botanyCount.collectAsStateWithLifecycle()
                val zoologyCount by mainViewModel.zoologyCount.collectAsStateWithLifecycle()

                StatRow("Total Questions", "$totalCount")
                StatRow("Physics", "$physicsCount questions")
                StatRow("Chemistry", "$chemistryCount questions")
                StatRow("Botany", "$botanyCount questions")
                StatRow("Zoology", "$zoologyCount questions")
            }

            // ── About ─────────────────────────────────────────────────────────
            SettingsSection("About") {
                StatRow("App", "NEETQuestSaver")
                StatRow("Version", "1.0.0")
                StatRow("Package", "com.neetquest.neetquestsaver")
                StatRow("Purpose", "NEET exam question capture")
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all $totalCount saved questions and their images. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { showClearDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete All") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val textColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = textColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
