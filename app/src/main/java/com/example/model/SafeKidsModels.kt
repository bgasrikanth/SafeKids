package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppMode {
    SETUP,
    PARENT_SETTINGS,
    KIDS_MODE
}

enum class KidsSubScreen {
    HOME,
    APP_RUNNING,
    BLOCKED,
    TIME_UP
}

enum class TimeUpReason {
    DAILY_LIMIT,
    BEDTIME
}

enum class ParentTab(val label: String) {
    APPS("Apps"),
    LIMITS("Limits"),
    RULES("Rules"),
    REPORT("Report"),
    SECURITY("Security")
}

enum class AppCategory(val displayName: String) {
    VIDEO("Video"),
    BROWSER("Browser"),
    UTILITY("Utility"),
    CREATIVE("Creative"),
    LEARNING("Learning"),
    MUSIC("Music"),
    GAMES("Games"),
    COMMUNICATION("Social"),
    SYSTEM("System")
}

data class MockAppInfo(
    val id: String,
    val name: String,
    val category: AppCategory,
    val gradientStart: Color,
    val gradientEnd: Color,
    val icon: ImageVector,
    val shortDescription: String
)

data class RestrictionsConfig(
    val blockCalls: Boolean = true,
    val blockSms: Boolean = true,
    val blockInternet: Boolean = true,
    val blockPlayStore: Boolean = true,
    val blockSettings: Boolean = true,
    val blockUninstall: Boolean = true
)

data class DailyUsageSummary(
    val dateKey: String,
    val dayLabel: String,
    val totalMinutes: Int,
    val blockedAttempts: Int
)

data class SafeKidsPersistedState(
    val isSetupComplete: Boolean = false,
    val pinHash: String = "",
    val recoveryQuestion: String = "",
    val recoveryAnswerHash: String = "",
    val activeMode: AppMode = AppMode.SETUP,
    val allowedAppIds: Set<String> = emptySet(),
    val dailyLimitMinutes: Int = 120,
    val extraMinutesToday: Int = 0,
    val bedtimeEnabled: Boolean = false,
    val bedtimeStartMinutes: Int = 20 * 60, // 8:00 PM
    val bedtimeEndMinutes: Int = 7 * 60,    // 7:00 AM
    val bedtimeOverrideUntilMillis: Long = 0L,
    val restrictions: RestrictionsConfig = RestrictionsConfig(),
    val todayDateKey: String = "",
    val todayUsageSecondsByApp: Map<String, Int> = emptyMap(),
    val blockedAttemptsToday: Int = 0,
    val weeklyHistory: List<DailyUsageSummary> = emptyList(),
    val failedPinAttempts: Int = 0,
    val pinLockoutUntilMillis: Long = 0L
)

enum class PinPurpose {
    OPEN_PARENT_SETTINGS,
    EXIT_KIDS_MODE,
    UNLOCK_TIME_UP,
    CHANGE_PIN,
    CHANGE_RECOVERY
}

data class SafeKidsUiState(
    val isLoaded: Boolean = false,
    val persisted: SafeKidsPersistedState = SafeKidsPersistedState(),
    val kidsSubScreen: KidsSubScreen = KidsSubScreen.HOME,
    val runningAppId: String? = null,
    val runningAppSessionSeconds: Int = 0,
    val blockedAppName: String? = null,
    val timeUpReason: TimeUpReason = TimeUpReason.DAILY_LIMIT,
    val currentTimeFormatted: String = "",
    val currentMinutesOfDay: Int = 0,
    val remainingSecondsToday: Int = 0,
    val totalAllowedSecondsToday: Int = 0,
    val totalUsedSecondsToday: Int = 0,
    val lockoutRemainingSeconds: Int = 0,
    val activePinPurpose: PinPurpose? = null,
    val showExtraTimeSheet: Boolean = false,
    val selectedParentTab: ParentTab = ParentTab.APPS,
    val appSearchQuery: String = "",
    val isKidSimulatorExpanded: Boolean = false,
    val toastMessage: String? = null
)
