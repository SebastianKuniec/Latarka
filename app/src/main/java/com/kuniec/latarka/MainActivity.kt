package com.kuniec.latarka

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kuniec.latarka.ui.theme.LatarkaTheme
import kotlin.math.roundToInt

/**
 * Main entry point of the application.
 *
 * This activity handles permission requests and hosts the [ConfigurationScreen]
 * within the [LatarkaTheme].
 */
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())

        setContent {
            LatarkaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    ConfigurationScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Composable screen for configuring the flashlight shake detection settings.
 *
 * It allows enabling the service, toggling features like drop prevention and
 * slider snapping, and adjusting sensitivity parameters via sliders or manual entry.
 *
 * @param modifier The modifier to be applied to the layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(FlashlightConfig.load(context)) }
    val scrollState = rememberScrollState()

    // Ensure service state matches config on start
    LaunchedEffect(Unit) {
        updateService(context, config.isEnabled)
    }

    PermissionCheck()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Service Toggle Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.service_enabled),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = config.isEnabled,
                        onCheckedChange = {
                            config = config.copy(isEnabled = it)
                            FlashlightConfig.save(context, config)
                            updateService(context, it)
                        }
                    )
                }
                Text(
                    text = stringResource(R.string.service_enabled_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Feature Toggles
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FeatureToggle(
                label = stringResource(R.string.manual_entry_mode),
                description = stringResource(R.string.manual_entry_mode_desc),
                checked = config.isManualEntryMode,
                onCheckedChange = {
                    config = config.copy(isManualEntryMode = it)
                    FlashlightConfig.save(context, config)
                }
            )

            if (!config.isManualEntryMode) {
                FeatureToggle(
                    label = stringResource(R.string.slider_snapping),
                    description = stringResource(R.string.slider_snapping_desc),
                    checked = config.isSnappingEnabled,
                    onCheckedChange = {
                        config = config.copy(isSnappingEnabled = it)
                        FlashlightConfig.save(context, config)
                    }
                )
            }

            FeatureToggle(
                label = stringResource(R.string.drop_prevention),
                description = stringResource(R.string.drop_prevention_desc),
                checked = config.isDropPreventionEnabled,
                onCheckedChange = {
                    config = config.copy(isDropPreventionEnabled = it)
                    FlashlightConfig.save(context, config)
                }
            )
        }

        HorizontalDivider()

        // Configuration Parameters
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // G-Force Threshold
            ConfigItem(
                label = stringResource(R.string.gforce_threshold, config.gForceThreshold),
                value = config.gForceThreshold,
                onValueChange = {
                    val step = config.snappingStepG
                    val newValue =
                        if (config.isSnappingEnabled) (it / step).roundToInt() * step else it
                    config = config.copy(gForceThreshold = newValue)
                    FlashlightConfig.save(context, config)
                },
                valueRange = 1.0f..5.0f,
                steps = if (config.isSnappingEnabled) ((5f - 1f) / config.snappingStepG).toInt() - 1 else 0,
                isManualMode = config.isManualEntryMode,
                keyboardType = KeyboardType.Decimal,
                onManualEntry = {
                    it.replace(",", ".").toFloatOrNull()?.let { valNum ->
                        if (valNum in 1.0f..5.0f) {
                            config = config.copy(gForceThreshold = valNum)
                            FlashlightConfig.save(context, config)
                        }
                    }
                }
            )

            // Slop Delay
            ConfigItem(
                label = stringResource(R.string.slop_delay, config.slopDelayMs),
                value = config.slopDelayMs.toFloat(),
                onValueChange = {
                    val step = config.snappingStepMs.toFloat()
                    val newValue =
                        if (config.isSnappingEnabled) ((it / step).roundToInt() * step).toLong() else it.toLong()
                    config = config.copy(slopDelayMs = newValue)
                    FlashlightConfig.save(context, config)
                },
                valueRange = 100f..2000f,
                steps = if (config.isSnappingEnabled) ((2000f - 100f) / config.snappingStepMs).toInt() - 1 else 0,
                isManualMode = config.isManualEntryMode,
                onManualEntry = {
                    it.toLongOrNull()?.let { valNum ->
                        if (valNum in 100..2000) {
                            config = config.copy(slopDelayMs = valNum)
                            FlashlightConfig.save(context, config)
                        }
                    }
                }
            )

            // Shake Count
            ConfigItem(
                label = stringResource(R.string.shake_count, config.shakeCount),
                value = config.shakeCount.toFloat(),
                onValueChange = {
                    config = config.copy(shakeCount = it.toInt())
                    FlashlightConfig.save(context, config)
                },
                valueRange = 1f..5f,
                steps = 3,
                isManualMode = config.isManualEntryMode,
                onManualEntry = {
                    it.toIntOrNull()?.let { valNum ->
                        if (valNum in 1..5) {
                            config = config.copy(shakeCount = valNum)
                            FlashlightConfig.save(context, config)
                        }
                    }
                }
            )

            // Cooldown Duration
            ConfigItem(
                label = stringResource(R.string.cooldown_duration, config.cooldownMs),
                value = config.cooldownMs.toFloat(),
                onValueChange = {
                    val step = config.snappingStepMs.toFloat()
                    val newValue =
                        if (config.isSnappingEnabled) ((it / step).roundToInt() * step).toLong() else it.toLong()
                    config = config.copy(cooldownMs = newValue)
                    FlashlightConfig.save(context, config)
                },
                valueRange = 100f..5000f,
                steps = if (config.isSnappingEnabled) ((5000f - 100f) / config.snappingStepMs).toInt() - 1 else 0,
                isManualMode = config.isManualEntryMode,
                onManualEntry = {
                    it.toLongOrNull()?.let { valNum ->
                        if (valNum in 100..5000) {
                            config = config.copy(cooldownMs = valNum)
                            FlashlightConfig.save(context, config)
                        }
                    }
                }
            )

            // Reset Timeout
            ConfigItem(
                label = stringResource(R.string.reset_timeout, config.resetTimeoutMs),
                value = config.resetTimeoutMs.toFloat(),
                onValueChange = {
                    val step = config.snappingStepMs.toFloat()
                    val newValue =
                        if (config.isSnappingEnabled) ((it / step).roundToInt() * step).toLong() else it.toLong()
                    config = config.copy(resetTimeoutMs = newValue)
                    FlashlightConfig.save(context, config)
                },
                valueRange = 100f..5000f,
                steps = if (config.isSnappingEnabled) ((5000f - 100f) / config.snappingStepMs).toInt() - 1 else 0,
                isManualMode = config.isManualEntryMode,
                onManualEntry = {
                    it.toLongOrNull()?.let { valNum ->
                        if (valNum in 100..5000) {
                            config = config.copy(resetTimeoutMs = valNum)
                            FlashlightConfig.save(context, config)
                        }
                    }
                }
            )
        }

        // Snapping Step Configuration (Visible only if snapping is enabled and NOT in manual mode)
        if (config.isSnappingEnabled && !config.isManualEntryMode) {
            HorizontalDivider()
            Text(
                text = stringResource(R.string.snapping_step_desc),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ConfigItem(
                label = stringResource(R.string.snapping_step_g, config.snappingStepG),
                value = config.snappingStepG,
                onValueChange = {
                    config = config.copy(snappingStepG = (it * 100).roundToInt() / 100f)
                    FlashlightConfig.save(context, config)
                },
                valueRange = 0.01f..1.0f,
                isManualMode = config.isManualEntryMode,
                keyboardType = KeyboardType.Decimal,
                onManualEntry = {
                    it.replace(",", ".").toFloatOrNull()?.let { valNum ->
                        if (valNum in 0.01f..1.0f) {
                            config = config.copy(snappingStepG = valNum)
                            FlashlightConfig.save(context, config)
                        }
                    }
                }
            )

            ConfigItem(
                label = stringResource(R.string.snapping_step_ms, config.snappingStepMs),
                value = config.snappingStepMs.toFloat(),
                onValueChange = {
                    config = config.copy(snappingStepMs = it.toInt())
                    FlashlightConfig.save(context, config)
                },
                valueRange = 10f..500f,
                isManualMode = config.isManualEntryMode,
                onManualEntry = {
                    it.toIntOrNull()?.let { valNum ->
                        if (valNum in 10..500) {
                            config = config.copy(snappingStepMs = valNum)
                            FlashlightConfig.save(context, config)
                        }
                    }
                }
            )
        }
    }
}

/**
 * A reusable composable for a feature toggle with a description.
 */
@Composable
fun PermissionCheck() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission =
                permissions[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotificationPermission
        }
    }

    if (!hasCameraPermission || !hasNotificationPermission) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.permissions_required))
                }
            },
            text = { Text(stringResource(R.string.permissions_required_desc)) },
            confirmButton = {
                Button(onClick = {
                    val permissions = mutableListOf(Manifest.permission.CAMERA)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    launcher.launch(permissions.toTypedArray())
                }) {
                    Text(stringResource(R.string.grant_permissions))
                }
            }
        )
    }
}

/**
 * A reusable composable for a feature toggle with a description.
 */
@Composable
fun FeatureToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A reusable composable for a configuration item that supports both slider and manual entry.
 */
@Composable
fun ConfigItem(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    isManualMode: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Number,
    onManualEntry: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var localTextValue by remember {
        mutableStateOf(if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString())
    }

    LaunchedEffect(value) {
        val currentParsed = localTextValue.replace(",", ".").toFloatOrNull()
        if (currentParsed != value) {
            localTextValue =
                if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
        }
    }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.clickable { showDialog = true }
        )
        if (isManualMode) {
            OutlinedTextField(
                value = localTextValue,
                onValueChange = {
                    localTextValue = it
                    onManualEntry(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                trailingIcon = { Text("📝") }
            )
        } else {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps
            )
        }
    }

    if (showDialog && !isManualMode) {
        var textValue by remember { mutableStateOf(value.toString()) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.edit_value)) },
            text = {
                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onManualEntry(textValue)
                    showDialog = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    LatarkaTheme {
        ConfigurationScreen()
    }
}

/**
 * Starts or stops the [ShakeDetectionService] based on the provided state.
 */
private fun updateService(context: Context, enabled: Boolean) {
    val intent = Intent(context, ShakeDetectionService::class.java)
    if (enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    } else {
        context.stopService(intent)
    }
}
