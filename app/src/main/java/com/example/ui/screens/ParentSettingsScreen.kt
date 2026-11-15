package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PublicOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shop
import androidx.compose.material.icons.rounded.SmsFailed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.MockAppsCatalog
import com.example.model.DailyUsageSummary
import com.example.model.MockAppInfo
import com.example.model.ParentTab
import com.example.model.RestrictionsConfig
import com.example.model.SafeKidsUiState
import com.example.ui.components.AppIconBadge
import com.example.util.TimeUtils
import kotlin.math.roundToInt

@Composable
fun ParentSettingsScreen(
    uiState: SafeKidsUiState,
    sevenDayReport: List<DailyUsageSummary>,
    onSelectTab: (ParentTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleApp: (String) -> Unit,
    onSelectAllApps: () -> Unit,
    onClearAllApps: () -> Unit,
    onDailyLimitChange: (Int) -> Unit,
    onBedtimeEnabledChange: (Boolean) -> Unit,
    onBedtimeTimesChange: (Int, Int) -> Unit,
    onResetTodayUsage: () -> Unit,
    onRestrictionsChange: (RestrictionsConfig) -> Unit,
    onChangePin: (currentPin: String, newPin: String) -> Boolean,
    onUpdateRecovery: (currentPin: String, question: String, answer: String) -> Boolean,
    onStartKidsMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Parent Dashboard",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.persisted.allowedAppIds.size} of ${MockAppsCatalog.allApps.size} apps allowed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = uiState.currentTimeFormatted,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ParentTab.entries.forEach { tab ->
                        val icon = when (tab) {
                            ParentTab.APPS -> Icons.Rounded.Apps
                            ParentTab.LIMITS -> Icons.Rounded.Schedule
                            ParentTab.RULES -> Icons.Rounded.Block
                            ParentTab.REPORT -> Icons.Rounded.BarChart
                            ParentTab.SECURITY -> Icons.Rounded.Lock
                        }
                        FilterChip(
                            selected = uiState.selectedParentTab == tab,
                            onClick = { onSelectTab(tab) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(tab.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("parent_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Tab Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.selectedParentTab,
                    label = "parent_tab_content"
                ) { tab ->
                    when (tab) {
                        ParentTab.APPS -> AllowedAppsSection(
                            allowedIds = uiState.persisted.allowedAppIds,
                            searchQuery = uiState.appSearchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            onToggleApp = onToggleApp,
                            onSelectAll = onSelectAllApps,
                            onClearAll = onClearAllApps
                        )

                        ParentTab.LIMITS -> TimeLimitsSection(
                            dailyLimitMinutes = uiState.persisted.dailyLimitMinutes,
                            usedSecondsToday = uiState.totalUsedSecondsToday,
                            bedtimeEnabled = uiState.persisted.bedtimeEnabled,
                            bedtimeStartMinutes = uiState.persisted.bedtimeStartMinutes,
                            bedtimeEndMinutes = uiState.persisted.bedtimeEndMinutes,
                            onDailyLimitChange = onDailyLimitChange,
                            onBedtimeEnabledChange = onBedtimeEnabledChange,
                            onBedtimeTimesChange = onBedtimeTimesChange,
                            onResetTodayUsage = onResetTodayUsage
                        )

                        ParentTab.RULES -> RestrictionsSection(
                            restrictions = uiState.persisted.restrictions,
                            onRestrictionsChange = onRestrictionsChange
                        )

                        ParentTab.REPORT -> UsageReportSection(
                            uiState = uiState,
                            sevenDayReport = sevenDayReport
                        )

                        ParentTab.SECURITY -> SecuritySection(
                            recoveryQuestion = uiState.persisted.recoveryQuestion,
                            onChangePin = onChangePin,
                            onUpdateRecovery = onUpdateRecovery
                        )
                    }
                }
            }
        }

        // Bottom Big CTA: Start Kids Mode
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onStartKidsMode,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .height(56.dp)
                        .testTag("start_kids_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Kids Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun AllowedAppsSection(
    allowedIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleApp: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    val filteredApps = remember(searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) MockAppsCatalog.allApps
        else MockAppsCatalog.allApps.filter {
            it.name.lowercase().contains(q) || it.category.displayName.lowercase().contains(q)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Allowed Apps",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${allowedIds.size} of ${MockAppsCatalog.allApps.size} allowed",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                                    .testTag("allowed_apps_counter")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search apps or categories...") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = "Search apps")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("app_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSelectAll,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("select_all_apps_button")
                        ) {
                            Text("Select all")
                        }
                        OutlinedButton(
                            onClick = onClearAll,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("clear_all_apps_button")
                        ) {
                            Text("Clear all")
                        }
                    }
                }
            }
        }

        items(filteredApps, key = { it.id }) { app ->
            val isAllowed = allowedIds.contains(app.id)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                onClick = { onToggleApp(app.id) },
                modifier = Modifier.testTag("parent_app_row_${app.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconBadge(
                        app = app,
                        size = 48.dp,
                        cornerRadius = 15.dp,
                        iconSize = 26.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = app.category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Switch(
                        checked = isAllowed,
                        onCheckedChange = { onToggleApp(app.id) },
                        modifier = Modifier.testTag("app_toggle_${app.id}")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeLimitsSection(
    dailyLimitMinutes: Int,
    usedSecondsToday: Int,
    bedtimeEnabled: Boolean,
    bedtimeStartMinutes: Int,
    bedtimeEndMinutes: Int,
    onDailyLimitChange: (Int) -> Unit,
    onBedtimeEnabledChange: (Boolean) -> Unit,
    onBedtimeTimesChange: (Int, Int) -> Unit,
    onResetTodayUsage: () -> Unit
) {
    var pickingStartTime by remember { mutableStateOf(false) }
    var pickingEndTime by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Limit Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Screen Time Limit",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "15 min to 6 hours per day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = TimeUtils.formatDurationMinutes(dailyLimitMinutes),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Slider(
                    value = dailyLimitMinutes.toFloat(),
                    onValueChange = { raw ->
                        val snapped = ((raw / 15f).roundToInt() * 15).coerceIn(15, 360)
                        onDailyLimitChange(snapped)
                    },
                    valueRange = 15f..360f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("daily_limit_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("15m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("3h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("6h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Bedtime Schedule Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Bedtime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bedtime Schedule",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Automatically locks device during sleep hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = bedtimeEnabled,
                        onCheckedChange = onBedtimeEnabledChange,
                        modifier = Modifier.testTag("bedtime_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { pickingStartTime = true },
                        enabled = bedtimeEnabled,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("bedtime_start_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Start", style = MaterialTheme.typography.labelSmall)
                            Text(
                                TimeUtils.formatMinutesOfDay(bedtimeStartMinutes),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { pickingEndTime = true },
                        enabled = bedtimeEnabled,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("bedtime_end_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Wake Up", style = MaterialTheme.typography.labelSmall)
                            Text(
                                TimeUtils.formatMinutesOfDay(bedtimeEndMinutes),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Reset Today's Usage Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Today's Usage",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Used ${TimeUtils.formatCountdownSeconds(usedSecondsToday)} so far today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = onResetTodayUsage,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("reset_today_usage_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset today's usage")
                }
            }
        }
    }

    if (pickingStartTime || pickingEndTime) {
        val initialMinutes = if (pickingStartTime) bedtimeStartMinutes else bedtimeEndMinutes
        val pickerState = rememberTimePickerState(
            initialHour = initialMinutes / 60,
            initialMinute = initialMinutes % 60
        )
        AlertDialog(
            onDismissRequest = {
                pickingStartTime = false
                pickingEndTime = false
            },
            title = {
                Text(if (pickingStartTime) "Select Bedtime Start" else "Select Wake-Up Time")
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = pickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosen = pickerState.hour * 60 + pickerState.minute
                        if (pickingStartTime) {
                            onBedtimeTimesChange(chosen, bedtimeEndMinutes)
                        } else {
                            onBedtimeTimesChange(bedtimeStartMinutes, chosen)
                        }
                        pickingStartTime = false
                        pickingEndTime = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pickingStartTime = false
                        pickingEndTime = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RestrictionsSection(
    restrictions: RestrictionsConfig,
    onRestrictionsChange: (RestrictionsConfig) -> Unit
) {
    var showTooltipDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Prototype Enforcement Tooltip Card
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "Simulation info",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Simulated rules: real device-wide blocking requires native Android Device Owner / Lock Task permissions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showTooltipDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.HelpOutline,
                        contentDescription = "More info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                RestrictionToggleRow(
                    icon = Icons.Rounded.Call,
                    title = "Block Calls",
                    subtitle = "Prevent incoming & outgoing phone calls",
                    checked = restrictions.blockCalls,
                    onCheckedChange = { onRestrictionsChange(restrictions.copy(blockCalls = it)) },
                    testTagName = "rule_block_calls"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                RestrictionToggleRow(
                    icon = Icons.Rounded.SmsFailed,
                    title = "Block SMS",
                    subtitle = "Hide text messaging and notifications",
                    checked = restrictions.blockSms,
                    onCheckedChange = { onRestrictionsChange(restrictions.copy(blockSms = it)) },
                    testTagName = "rule_block_sms"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                RestrictionToggleRow(
                    icon = Icons.Rounded.PublicOff,
                    title = "Block Internet",
                    subtitle = "Restrict browser access inside Kids Mode",
                    checked = restrictions.blockInternet,
                    onCheckedChange = { onRestrictionsChange(restrictions.copy(blockInternet = it)) },
                    testTagName = "rule_block_internet"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                RestrictionToggleRow(
                    icon = Icons.Rounded.Shop,
                    title = "Block Play Store / App Install",
                    subtitle = "Prevent new downloads or in-app purchases",
                    checked = restrictions.blockPlayStore,
                    onCheckedChange = { onRestrictionsChange(restrictions.copy(blockPlayStore = it)) },
                    testTagName = "rule_block_play_store"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                RestrictionToggleRow(
                    icon = Icons.Rounded.SettingsApplications,
                    title = "Block Settings",
                    subtitle = "Prevent changing Wi-Fi, Bluetooth, or system settings",
                    checked = restrictions.blockSettings,
                    onCheckedChange = { onRestrictionsChange(restrictions.copy(blockSettings = it)) },
                    testTagName = "rule_block_settings"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                RestrictionToggleRow(
                    icon = Icons.Rounded.DeleteForever,
                    title = "Block Uninstall",
                    subtitle = "Prevent removing SafeKids Launcher or apps",
                    checked = restrictions.blockUninstall,
                    onCheckedChange = { onRestrictionsChange(restrictions.copy(blockUninstall = it)) },
                    testTagName = "rule_block_uninstall"
                )
            }
        }
    }

    if (showTooltipDialog) {
        AlertDialog(
            onDismissRequest = { showTooltipDialog = false },
            title = { Text("About Device Restrictions") },
            text = {
                Text(
                    "All restriction toggles are persisted locally. In this prototype, Block Internet shows an offline restriction inside Chrome, and Block Play Store / Block Settings trigger the Blocked App screen. Full OS-level call, SMS, and uninstall locks require Android Enterprise Device Owner & Accessibility APIs."
                )
            },
            confirmButton = {
                TextButton(onClick = { showTooltipDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun RestrictionToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTagName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTagName)
        )
    }
}

@Composable
private fun UsageReportSection(
    uiState: SafeKidsUiState,
    sevenDayReport: List<DailyUsageSummary>
) {
    val usageEntries = remember(uiState.persisted.todayUsageSecondsByApp) {
        uiState.persisted.todayUsageSecondsByApp
            .entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
            .mapNotNull { (id, secs) ->
                MockAppsCatalog.findById(id)?.let { it to secs }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReportKpiCard(
                label = "Used Today",
                value = TimeUtils.formatCountdownSeconds(uiState.totalUsedSecondsToday),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            ReportKpiCard(
                label = "Daily Limit",
                value = TimeUtils.formatDurationMinutes(uiState.persisted.dailyLimitMinutes + uiState.persisted.extraMinutesToday),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            ReportKpiCard(
                label = "Blocked Tries",
                value = uiState.persisted.blockedAttemptsToday.toString(),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        // Today's Per-App Bar Chart
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Time Spent Per App Today",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Live screen time tracked while apps are open in Kids Mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (usageEntries.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No app activity logged today yet",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Start Kids Mode and open an app to watch usage update live.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    TodayPerAppBarChart(entries = usageEntries)
                }
            }
        }

        // 7-Day Summary Bar Chart
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val weeklyTotalMinutes = sevenDayReport.sumOf { it.totalMinutes }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7-Day Usage Summary",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Past 6 days + live today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Total: ${TimeUtils.formatDurationMinutes(weeklyTotalMinutes)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                SevenDayBarChart(
                    days = sevenDayReport,
                    dailyLimitMinutes = uiState.persisted.dailyLimitMinutes
                )
            }
        }
    }
}

@Composable
private fun ReportKpiCard(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = tint,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TodayPerAppBarChart(
    entries: List<Pair<MockAppInfo, Int>>
) {
    val maxSeconds = entries.maxOf { it.second }.coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        entries.forEach { (app, seconds) ->
            val fraction = (seconds.toFloat() / maxSeconds.toFloat()).coerceIn(0.06f, 1f)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = TimeUtils.formatCountdownSeconds(seconds),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = app.gradientEnd
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(app.gradientStart, app.gradientEnd)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SevenDayBarChart(
    days: List<DailyUsageSummary>,
    dailyLimitMinutes: Int
) {
    val maxChartMinutes = maxOf(
        days.maxOfOrNull { it.totalMinutes } ?: 60,
        dailyLimitMinutes,
        60
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val todayColor = Color(0xFFEC4899)
    val limitLineColor = MaterialTheme.colorScheme.tertiary

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount = days.size.coerceAtLeast(1)
                val slotWidth = size.width / barCount
                val barWidth = (slotWidth * 0.48f).coerceAtMost(36.dp.toPx())

                // Daily limit dashed reference line
                val limitY = size.height * (1f - (dailyLimitMinutes.toFloat() / maxChartMinutes.toFloat()).coerceIn(0f, 1f))
                drawLine(
                    color = limitLineColor.copy(alpha = 0.7f),
                    start = Offset(0f, limitY),
                    end = Offset(size.width, limitY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                days.forEachIndexed { index, summary ->
                    val ratio = (summary.totalMinutes.toFloat() / maxChartMinutes.toFloat()).coerceIn(0.03f, 1f)
                    val barHeight = size.height * ratio
                    val left = index * slotWidth + (slotWidth - barWidth) / 2f
                    val top = size.height - barHeight
                    val barColor = if (summary.dayLabel == "Today") todayColor else primaryColor

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { summary ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${summary.totalMinutes}m",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.dayLabel == "Today") Color(0xFFEC4899) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = summary.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Dashed line = daily limit (${TimeUtils.formatDurationMinutes(dailyLimitMinutes)})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecuritySection(
    recoveryQuestion: String,
    onChangePin: (currentPin: String, newPin: String) -> Boolean,
    onUpdateRecovery: (currentPin: String, question: String, answer: String) -> Boolean
) {
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showChangeRecoveryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Parent PIN", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "4-digit SHA-256 protected PIN",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedButton(
                    onClick = { showChangePinDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("change_pin_button")
                ) {
                    Text("Change PIN")
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Recovery Security Question", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = recoveryQuestion.ifBlank { "Not configured" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { showChangeRecoveryDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("change_recovery_button")
                ) {
                    Text("Change Recovery Question")
                }
            }
        }
    }

    if (showChangePinDialog) {
        ChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onSubmit = { current, next ->
                val ok = onChangePin(current, next)
                if (ok) showChangePinDialog = false
                ok
            }
        )
    }

    if (showChangeRecoveryDialog) {
        ChangeRecoveryDialog(
            initialQuestion = recoveryQuestion,
            onDismiss = { showChangeRecoveryDialog = false },
            onSubmit = { currentPin, q, a ->
                val ok = onUpdateRecovery(currentPin, q, a)
                if (ok) showChangeRecoveryDialog = false
                ok
            }
        )
    }
}

@Composable
private fun ChangePinDialog(
    onDismiss: () -> Unit,
    onSubmit: (currentPin: String, newPin: String) -> Boolean
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Parent PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { currentPin = it; error = null } },
                    label = { Text("Current 4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { newPin = it; error = null } },
                    label = { Text("New 4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { confirmPin = it; error = null } },
                    label = { Text("Confirm New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPin.length != 4 || newPin.length != 4 -> error = "PINs must be 4 digits."
                        newPin != confirmPin -> error = "New PINs do not match."
                        else -> {
                            val ok = onSubmit(currentPin, newPin)
                            if (!ok) error = "Current PIN is incorrect."
                        }
                    }
                }
            ) {
                Text("Update PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ChangeRecoveryDialog(
    initialQuestion: String,
    onDismiss: () -> Unit,
    onSubmit: (currentPin: String, question: String, answer: String) -> Boolean
) {
    var currentPin by remember { mutableStateOf("") }
    var question by remember { mutableStateOf(initialQuestion) }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Recovery Question") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) { currentPin = it; error = null } },
                    label = { Text("Current 4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it; error = null },
                    label = { Text("Security Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it; error = null },
                    label = { Text("New Answer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPin.length != 4 -> error = "Enter your 4-digit current PIN."
                        question.isBlank() || answer.isBlank() -> error = "Question and answer cannot be empty."
                        else -> {
                            val ok = onSubmit(currentPin, question, answer)
                            if (!ok) error = "Current PIN is incorrect."
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
