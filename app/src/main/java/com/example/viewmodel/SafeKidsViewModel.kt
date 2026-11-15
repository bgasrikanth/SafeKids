package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MockAppsCatalog
import com.example.data.SafeKidsPreferencesRepository
import com.example.model.AppMode
import com.example.model.DailyUsageSummary
import com.example.model.KidsSubScreen
import com.example.model.ParentTab
import com.example.model.PinPurpose
import com.example.model.RestrictionsConfig
import com.example.model.SafeKidsPersistedState
import com.example.model.SafeKidsUiState
import com.example.model.TimeUpReason
import com.example.util.SecurityUtils
import com.example.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class SafeKidsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SafeKidsPreferencesRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(SafeKidsUiState())
    val uiState: StateFlow<SafeKidsUiState> = _uiState.asStateFlow()

    private var unsavedAppTicks = 0

    init {
        loadInitialState()
        startTicker()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val loaded = repository.loadState()
            val resolvedMode = when {
                !loaded.isSetupComplete -> AppMode.SETUP
                else -> loaded.activeMode
            }
            val normalized = loaded.copy(activeMode = resolvedMode)
            if (normalized != loaded) {
                repository.saveState(normalized)
            }
            _uiState.update { current ->
                recomputeDerivedState(
                    current.copy(
                        isLoaded = true,
                        persisted = normalized
                    )
                )
            }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                tickOneSecond()
            }
        }
    }

    private fun tickOneSecond() {
        val current = _uiState.value
        if (!current.isLoaded) return

        val nowCal = Calendar.getInstance()
        val nowMillis = System.currentTimeMillis()
        val todayKey = TimeUtils.currentDateKey(nowCal)

        var persisted = repository.rollOverMidnightIfNeeded(current.persisted, todayKey)
        val rolledOver = persisted != current.persisted

        var nextSubScreen = current.kidsSubScreen
        var nextRunningAppId = current.runningAppId
        var nextSessionSeconds = current.runningAppSessionSeconds

        val isAppRunning = persisted.activeMode == AppMode.KIDS_MODE &&
            current.kidsSubScreen == KidsSubScreen.APP_RUNNING &&
            current.runningAppId != null

        if (isAppRunning && !rolledOver) {
            val appId = current.runningAppId!!
            val updatedUsage = persisted.todayUsageSecondsByApp.toMutableMap()
            updatedUsage[appId] = (updatedUsage[appId] ?: 0) + 1
            persisted = persisted.copy(todayUsageSecondsByApp = updatedUsage)
            nextSessionSeconds += 1
            unsavedAppTicks += 1
        }

        // Check Time's Up conditions in Kids Mode
        val totalAllowedSeconds = (persisted.dailyLimitMinutes + persisted.extraMinutesToday) * 60
        val totalUsedSeconds = persisted.todayUsageSecondsByApp.values.sum()
        val remainingSeconds = (totalAllowedSeconds - totalUsedSeconds).coerceAtLeast(0)
        val nowMinutes = TimeUtils.currentMinutesOfDay(nowCal)
        val inBedtime = persisted.bedtimeEnabled &&
            TimeUtils.isWithinBedtime(
                nowMinutesOfDay = nowMinutes,
                startMinutes = persisted.bedtimeStartMinutes,
                endMinutes = persisted.bedtimeEndMinutes
            ) &&
            nowMillis > persisted.bedtimeOverrideUntilMillis

        var timeUpReason = current.timeUpReason
        if (persisted.activeMode == AppMode.KIDS_MODE) {
            if (remainingSeconds <= 0) {
                if (nextSubScreen != KidsSubScreen.TIME_UP) {
                    nextSubScreen = KidsSubScreen.TIME_UP
                    nextRunningAppId = null
                    nextSessionSeconds = 0
                    flushUsageSave(persisted)
                }
                timeUpReason = TimeUpReason.DAILY_LIMIT
            } else if (inBedtime) {
                if (nextSubScreen != KidsSubScreen.TIME_UP) {
                    nextSubScreen = KidsSubScreen.TIME_UP
                    nextRunningAppId = null
                    nextSessionSeconds = 0
                    flushUsageSave(persisted)
                }
                timeUpReason = TimeUpReason.BEDTIME
            } else if (nextSubScreen == KidsSubScreen.TIME_UP) {
                // Daily time is available and bedtime ended
                nextSubScreen = KidsSubScreen.HOME
            }
        }

        if (rolledOver) {
            unsavedAppTicks = 0
            nextSubScreen = if (persisted.activeMode == AppMode.KIDS_MODE && inBedtime) {
                KidsSubScreen.TIME_UP
            } else {
                KidsSubScreen.HOME
            }
            nextRunningAppId = null
            nextSessionSeconds = 0
            viewModelScope.launch { repository.saveState(persisted) }
        } else if (unsavedAppTicks >= 10) {
            flushUsageSave(persisted)
        }

        _uiState.update { state ->
            recomputeDerivedState(
                state.copy(
                    persisted = persisted,
                    kidsSubScreen = nextSubScreen,
                    runningAppId = nextRunningAppId,
                    runningAppSessionSeconds = nextSessionSeconds,
                    timeUpReason = timeUpReason
                ),
                calendar = nowCal,
                nowMillis = nowMillis
            )
        }
    }

    private fun flushUsageSave(persisted: SafeKidsPersistedState) {
        if (unsavedAppTicks > 0 || persisted.activeMode == AppMode.KIDS_MODE) {
            unsavedAppTicks = 0
            viewModelScope.launch {
                repository.saveState(persisted)
            }
        }
    }

    private fun recomputeDerivedState(
        state: SafeKidsUiState,
        calendar: Calendar = Calendar.getInstance(),
        nowMillis: Long = System.currentTimeMillis()
    ): SafeKidsUiState {
        val p = state.persisted
        val totalAllowed = (p.dailyLimitMinutes + p.extraMinutesToday) * 60
        val totalUsed = p.todayUsageSecondsByApp.values.sum()
        val remaining = (totalAllowed - totalUsed).coerceAtLeast(0)
        val nowMinutes = TimeUtils.currentMinutesOfDay(calendar)
        val inBedtime = p.bedtimeEnabled &&
            TimeUtils.isWithinBedtime(nowMinutes, p.bedtimeStartMinutes, p.bedtimeEndMinutes) &&
            nowMillis > p.bedtimeOverrideUntilMillis

        val lockoutSeconds = if (p.pinLockoutUntilMillis > nowMillis) {
            ((p.pinLockoutUntilMillis - nowMillis + 999L) / 1000L).toInt()
        } else {
            0
        }

        val resolvedSubScreen = if (p.activeMode == AppMode.KIDS_MODE) {
            when {
                remaining <= 0 || inBedtime -> KidsSubScreen.TIME_UP
                state.kidsSubScreen == KidsSubScreen.TIME_UP -> KidsSubScreen.HOME
                else -> state.kidsSubScreen
            }
        } else {
            KidsSubScreen.HOME
        }

        val resolvedReason = when {
            remaining <= 0 -> TimeUpReason.DAILY_LIMIT
            inBedtime -> TimeUpReason.BEDTIME
            else -> state.timeUpReason
        }

        return state.copy(
            kidsSubScreen = resolvedSubScreen,
            timeUpReason = resolvedReason,
            currentTimeFormatted = TimeUtils.currentFormattedClock(calendar),
            currentMinutesOfDay = nowMinutes,
            remainingSecondsToday = remaining,
            totalAllowedSecondsToday = totalAllowed,
            totalUsedSecondsToday = totalUsed,
            lockoutRemainingSeconds = lockoutSeconds
        )
    }

    // --- First-run Setup ---

    fun completeSetup(
        pin: String,
        recoveryQuestion: String,
        recoveryAnswer: String
    ) {
        val pinHash = SecurityUtils.sha256(pin)
        val answerHash = SecurityUtils.hashRecoveryAnswer(recoveryAnswer)
        val current = _uiState.value.persisted
        val updated = current.copy(
            isSetupComplete = true,
            pinHash = pinHash,
            recoveryQuestion = recoveryQuestion.trim(),
            recoveryAnswerHash = answerHash,
            activeMode = AppMode.PARENT_SETTINGS,
            failedPinAttempts = 0,
            pinLockoutUntilMillis = 0L
        )
        persistAndRefresh(updated) { state ->
            state.copy(
                selectedParentTab = ParentTab.APPS,
                toastMessage = "Setup complete! Choose allowed apps to start."
            )
        }
    }

    // --- Kids Mode Navigation & App Simulation ---

    fun openAllowedApp(appId: String) {
        val state = _uiState.value
        val persisted = state.persisted
        if (persisted.activeMode != AppMode.KIDS_MODE) return

        // Check restriction overrides if an allowed system app has an explicit block rule
        val app = MockAppsCatalog.findById(appId) ?: return
        val blockedByRule = when (appId) {
            "play_store" -> persisted.restrictions.blockPlayStore
            "settings" -> persisted.restrictions.blockSettings
            else -> false
        }

        if (!persisted.allowedAppIds.contains(appId) || blockedByRule) {
            triggerBlockedApp(app.name)
            return
        }

        _uiState.update {
            it.copy(
                kidsSubScreen = KidsSubScreen.APP_RUNNING,
                runningAppId = appId,
                runningAppSessionSeconds = 0
            )
        }
    }

    fun returnToKidsHome() {
        val current = _uiState.value
        if (unsavedAppTicks > 0) {
            flushUsageSave(current.persisted)
        }
        _uiState.update {
            if (it.kidsSubScreen == KidsSubScreen.TIME_UP) {
                it
            } else {
                it.copy(
                    kidsSubScreen = KidsSubScreen.HOME,
                    runningAppId = null,
                    runningAppSessionSeconds = 0,
                    blockedAppName = null
                )
            }
        }
    }

    fun handleSimulatedNavRecents() {
        showToast("Not allowed in Kids Mode")
    }

    fun toggleKidSimulatorExpanded() {
        _uiState.update { it.copy(isKidSimulatorExpanded = !it.isKidSimulatorExpanded) }
    }

    fun tryOpenBlockedAppDemo() {
        val persisted = _uiState.value.persisted
        val disallowed = MockAppsCatalog.allApps.firstOrNull { !persisted.allowedAppIds.contains(it.id) }
        val targetName = disallowed?.name ?: "Play Store"
        triggerBlockedApp(targetName)
    }

    private fun triggerBlockedApp(appName: String) {
        val current = _uiState.value
        if (unsavedAppTicks > 0) {
            flushUsageSave(current.persisted)
        }
        val updated = current.persisted.copy(
            blockedAttemptsToday = current.persisted.blockedAttemptsToday + 1
        )
        persistAndRefresh(updated) { state ->
            state.copy(
                kidsSubScreen = KidsSubScreen.BLOCKED,
                runningAppId = null,
                runningAppSessionSeconds = 0,
                blockedAppName = appName
            )
        }
    }

    // --- PIN Pad & Recovery ---

    fun requestPin(purpose: PinPurpose) {
        _uiState.update { it.copy(activePinPurpose = purpose) }
    }

    fun dismissPinSheet() {
        _uiState.update { it.copy(activePinPurpose = null) }
    }

    fun verifyPin(enteredPin: String): Boolean {
        val now = System.currentTimeMillis()
        val persisted = _uiState.value.persisted

        if (persisted.pinLockoutUntilMillis > now) {
            return false
        }

        val enteredHash = SecurityUtils.sha256(enteredPin)
        if (enteredHash == persisted.pinHash) {
            val purpose = _uiState.value.activePinPurpose
            val cleared = persisted.copy(failedPinAttempts = 0, pinLockoutUntilMillis = 0L)

            when (purpose) {
                PinPurpose.OPEN_PARENT_SETTINGS,
                PinPurpose.EXIT_KIDS_MODE -> {
                    unsavedAppTicks = 0
                    val exited = cleared.copy(activeMode = AppMode.PARENT_SETTINGS)
                    persistAndRefresh(exited) { state ->
                        state.copy(
                            activePinPurpose = null,
                            kidsSubScreen = KidsSubScreen.HOME,
                            runningAppId = null,
                            runningAppSessionSeconds = 0,
                            isKidSimulatorExpanded = false
                        )
                    }
                }

                PinPurpose.UNLOCK_TIME_UP -> {
                    persistAndRefresh(cleared) { state ->
                        state.copy(
                            activePinPurpose = null,
                            showExtraTimeSheet = true
                        )
                    }
                }

                PinPurpose.CHANGE_PIN,
                PinPurpose.CHANGE_RECOVERY,
                null -> {
                    persistAndRefresh(cleared) { state ->
                        state.copy(activePinPurpose = null)
                    }
                }
            }
            return true
        } else {
            val attempts = persisted.failedPinAttempts + 1
            val updated = if (attempts >= 5) {
                persisted.copy(
                    failedPinAttempts = 0,
                    pinLockoutUntilMillis = now + 30_000L
                )
            } else {
                persisted.copy(failedPinAttempts = attempts)
            }
            persistAndRefresh(updated)
            return false
        }
    }

    fun verifyRecoveryAnswer(answer: String): Boolean {
        val persisted = _uiState.value.persisted
        return SecurityUtils.hashRecoveryAnswer(answer) == persisted.recoveryAnswerHash
    }

    fun resetPinFromRecovery(newPin: String) {
        val updated = _uiState.value.persisted.copy(
            pinHash = SecurityUtils.sha256(newPin),
            failedPinAttempts = 0,
            pinLockoutUntilMillis = 0L
        )
        persistAndRefresh(updated) { state ->
            state.copy(toastMessage = "Parent PIN updated")
        }
    }

    // --- Time's Up Extra Time ---

    fun dismissExtraTimeSheet() {
        _uiState.update { it.copy(showExtraTimeSheet = false) }
    }

    fun grantExtraMinutes(minutes: Int) {
        val now = System.currentTimeMillis()
        val persisted = _uiState.value.persisted
        val updated = persisted.copy(
            extraMinutesToday = persisted.extraMinutesToday + minutes,
            bedtimeOverrideUntilMillis = if (persisted.bedtimeEnabled) {
                now + minutes * 60_000L
            } else {
                persisted.bedtimeOverrideUntilMillis
            }
        )
        persistAndRefresh(updated) { state ->
            state.copy(
                showExtraTimeSheet = false,
                kidsSubScreen = KidsSubScreen.HOME,
                toastMessage = "Added $minutes minutes of extra screen time"
            )
        }
    }

    fun goToParentSettingsFromTimeUp() {
        val updated = _uiState.value.persisted.copy(activeMode = AppMode.PARENT_SETTINGS)
        persistAndRefresh(updated) { state ->
            state.copy(
                showExtraTimeSheet = false,
                kidsSubScreen = KidsSubScreen.HOME
            )
        }
    }

    // --- Parent Settings Mutations ---

    fun selectParentTab(tab: ParentTab) {
        _uiState.update { it.copy(selectedParentTab = tab) }
    }

    fun updateAppSearchQuery(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun toggleAppAllowed(appId: String) {
        val current = _uiState.value.persisted
        val nextAllowed = current.allowedAppIds.toMutableSet()
        if (nextAllowed.contains(appId)) {
            nextAllowed.remove(appId)
        } else {
            nextAllowed.add(appId)
        }
        persistAndRefresh(current.copy(allowedAppIds = nextAllowed))
    }

    fun selectAllApps() {
        val current = _uiState.value.persisted
        val allIds = MockAppsCatalog.allApps.map { it.id }.toSet()
        persistAndRefresh(current.copy(allowedAppIds = allIds)) {
            it.copy(toastMessage = "All 16 apps allowed")
        }
    }

    fun clearAllApps() {
        val current = _uiState.value.persisted
        persistAndRefresh(current.copy(allowedAppIds = emptySet())) {
            it.copy(toastMessage = "Cleared all allowed apps")
        }
    }

    fun setDailyLimitMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(15, 360)
        val current = _uiState.value.persisted
        persistAndRefresh(current.copy(dailyLimitMinutes = clamped))
    }

    fun setBedtimeEnabled(enabled: Boolean) {
        val current = _uiState.value.persisted
        persistAndRefresh(current.copy(bedtimeEnabled = enabled))
    }

    fun setBedtimeTimes(startMinutes: Int, endMinutes: Int) {
        val current = _uiState.value.persisted
        persistAndRefresh(
            current.copy(
                bedtimeStartMinutes = startMinutes,
                bedtimeEndMinutes = endMinutes,
                bedtimeOverrideUntilMillis = 0L
            )
        ) {
            it.copy(toastMessage = "Bedtime schedule updated")
        }
    }

    fun resetTodayUsage() {
        val current = _uiState.value.persisted
        unsavedAppTicks = 0
        persistAndRefresh(
            current.copy(
                todayUsageSecondsByApp = emptyMap(),
                extraMinutesToday = 0,
                blockedAttemptsToday = 0,
                bedtimeOverrideUntilMillis = 0L
            )
        ) {
            it.copy(toastMessage = "Today's usage has been reset")
        }
    }

    fun updateRestrictions(config: RestrictionsConfig) {
        val current = _uiState.value.persisted
        persistAndRefresh(current.copy(restrictions = config))
    }

    fun changePin(currentPin: String, newPin: String): Boolean {
        val persisted = _uiState.value.persisted
        if (SecurityUtils.sha256(currentPin) != persisted.pinHash) {
            return false
        }
        persistAndRefresh(persisted.copy(pinHash = SecurityUtils.sha256(newPin))) {
            it.copy(toastMessage = "Parent PIN changed successfully")
        }
        return true
    }

    fun updateRecoveryQuestion(
        currentPin: String,
        newQuestion: String,
        newAnswer: String
    ): Boolean {
        val persisted = _uiState.value.persisted
        if (SecurityUtils.sha256(currentPin) != persisted.pinHash) {
            return false
        }
        persistAndRefresh(
            persisted.copy(
                recoveryQuestion = newQuestion.trim(),
                recoveryAnswerHash = SecurityUtils.hashRecoveryAnswer(newAnswer)
            )
        ) {
            it.copy(toastMessage = "Recovery question updated")
        }
        return true
    }

    fun startKidsMode() {
        val current = _uiState.value.persisted
        val updated = current.copy(activeMode = AppMode.KIDS_MODE)
        persistAndRefresh(updated) { state ->
            state.copy(
                kidsSubScreen = KidsSubScreen.HOME,
                runningAppId = null,
                runningAppSessionSeconds = 0,
                blockedAppName = null,
                isKidSimulatorExpanded = false
            )
        }
    }

    fun getSevenDayReport(): List<DailyUsageSummary> {
        val persisted = _uiState.value.persisted
        val todayMinutes = (persisted.todayUsageSecondsByApp.values.sum() + 30) / 60
        val todayEntry = DailyUsageSummary(
            dateKey = persisted.todayDateKey,
            dayLabel = "Today",
            totalMinutes = todayMinutes,
            blockedAttempts = persisted.blockedAttemptsToday
        )
        return (persisted.weeklyHistory.takeLast(6) + todayEntry)
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun persistAndRefresh(
        updatedPersisted: SafeKidsPersistedState,
        extraTransform: (SafeKidsUiState) -> SafeKidsUiState = { it }
    ) {
        _uiState.update { state ->
            recomputeDerivedState(extraTransform(state.copy(persisted = updatedPersisted)))
        }
        viewModelScope.launch {
            repository.saveState(updatedPersisted)
        }
    }
}
