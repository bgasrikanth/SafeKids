package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppMode
import com.example.model.PinPurpose
import com.example.ui.components.ExtraTimeBottomSheet
import com.example.ui.components.PinVerificationBottomSheet
import com.example.ui.screens.KidsModeScreen
import com.example.ui.screens.ParentSettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.SafeKidsTheme
import com.example.viewmodel.SafeKidsViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeKidsTheme {
                SafeKidsLauncherRoot()
            }
        }
    }
}

@Composable
fun SafeKidsLauncherRoot(
    viewModel: SafeKidsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            delay(2400L)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!uiState.isLoaded) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            AnimatedContent(
                targetState = uiState.persisted.activeMode,
                transitionSpec = {
                    fadeIn(tween(260)) togetherWith fadeOut(tween(200))
                },
                label = "app_mode_transition"
            ) { mode ->
                when (mode) {
                    AppMode.SETUP -> SetupScreen(
                        onSetupComplete = { pin, question, answer ->
                            viewModel.completeSetup(pin, question, answer)
                        }
                    )

                    AppMode.PARENT_SETTINGS -> ParentSettingsScreen(
                        uiState = uiState,
                        sevenDayReport = viewModel.getSevenDayReport(),
                        onSelectTab = viewModel::selectParentTab,
                        onSearchQueryChange = viewModel::updateAppSearchQuery,
                        onToggleApp = viewModel::toggleAppAllowed,
                        onSelectAllApps = viewModel::selectAllApps,
                        onClearAllApps = viewModel::clearAllApps,
                        onDailyLimitChange = viewModel::setDailyLimitMinutes,
                        onBedtimeEnabledChange = viewModel::setBedtimeEnabled,
                        onBedtimeTimesChange = viewModel::setBedtimeTimes,
                        onResetTodayUsage = viewModel::resetTodayUsage,
                        onRestrictionsChange = viewModel::updateRestrictions,
                        onChangePin = viewModel::changePin,
                        onUpdateRecovery = viewModel::updateRecoveryQuestion,
                        onStartKidsMode = viewModel::startKidsMode
                    )

                    AppMode.KIDS_MODE -> KidsModeScreen(
                        uiState = uiState,
                        onOpenApp = viewModel::openAllowedApp,
                        onReturnHome = viewModel::returnToKidsHome,
                        onSimulatedRecents = viewModel::handleSimulatedNavRecents,
                        onRequestPin = viewModel::requestPin,
                        onToggleSimulator = viewModel::toggleKidSimulatorExpanded,
                        onTryOpenBlockedApp = viewModel::tryOpenBlockedAppDemo
                    )
                }
            }

            // PIN Verification Modal Bottom Sheet
            val pinPurpose = uiState.activePinPurpose
            if (pinPurpose != null) {
                val (title, subtitle) = when (pinPurpose) {
                    PinPurpose.OPEN_PARENT_SETTINGS -> "Parent Settings" to "Enter your 4-digit Parent PIN to open settings"
                    PinPurpose.EXIT_KIDS_MODE -> "Exit Kids Mode" to "Enter your 4-digit Parent PIN to exit Kids Mode"
                    PinPurpose.UNLOCK_TIME_UP -> "Parent Unlock" to "Enter your Parent PIN to add extra time or unlock"
                    PinPurpose.CHANGE_PIN -> "Verify PIN" to "Enter your current Parent PIN"
                    PinPurpose.CHANGE_RECOVERY -> "Verify PIN" to "Enter your Parent PIN to continue"
                }
                PinVerificationBottomSheet(
                    title = title,
                    subtitle = subtitle,
                    recoveryQuestion = uiState.persisted.recoveryQuestion,
                    failedAttempts = uiState.persisted.failedPinAttempts,
                    lockoutRemainingSeconds = uiState.lockoutRemainingSeconds,
                    onVerifyPin = viewModel::verifyPin,
                    onVerifyRecoveryAnswer = viewModel::verifyRecoveryAnswer,
                    onResetPinFromRecovery = viewModel::resetPinFromRecovery,
                    onDismiss = viewModel::dismissPinSheet
                )
            }

            // Extra Time Bottom Sheet (after Time's Up Parent Unlock)
            if (uiState.showExtraTimeSheet) {
                ExtraTimeBottomSheet(
                    onAddMinutes = viewModel::grantExtraMinutes,
                    onOpenParentSettings = viewModel::goToParentSettingsFromTimeUp,
                    onDismiss = viewModel::dismissExtraTimeSheet
                )
            }
        }

        // Floating Toast Banner
        AnimatedVisibility(
            visible = uiState.toastMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 68.dp, start = 24.dp, end = 24.dp)
        ) {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = uiState.toastMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }
}
