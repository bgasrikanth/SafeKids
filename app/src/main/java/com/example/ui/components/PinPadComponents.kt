package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun PinPadInput(
    pin: String,
    onPinChange: (String) -> Unit,
    errorMessage: String?,
    shakeTrigger: Int,
    enabled: Boolean = true,
    darkMode: Boolean = false,
    showForgotPin: Boolean = false,
    onForgotPinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            val sequence = listOf(-24f, 24f, -18f, 18f, -10f, 10f, 0f)
            for (target in sequence) {
                shakeOffset.animateTo(target, animationSpec = tween(durationMillis = 45))
            }
        }
    }

    val dotFilledColor = if (darkMode) Color.White else MaterialTheme.colorScheme.primary
    val dotEmptyColor = if (darkMode) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant
    val keyBgColor = if (darkMode) Color.White.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
    val keyTextColor = if (darkMode) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 4 PIN Dots with Shake
        Row(
            modifier = Modifier
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val filled = index < pin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (filled) dotFilledColor else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (errorMessage != null) {
                                MaterialTheme.colorScheme.error
                            } else if (filled) {
                                dotFilledColor
                            } else {
                                dotEmptyColor
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Numeric Keypad
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { digit ->
                        PinDigitButton(
                            label = digit,
                            backgroundColor = keyBgColor,
                            contentColor = keyTextColor,
                            enabled = enabled,
                            onClick = {
                                if (pin.length < 4) {
                                    onPinChange(pin + digit)
                                }
                            },
                            testTagName = "pin_digit_$digit"
                        )
                    }
                }
            }

            // Bottom Row: Forgot / 0 / Backspace
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showForgotPin) {
                        TextButton(
                            onClick = onForgotPinClick,
                            modifier = Modifier.testTag("forgot_pin_button")
                        ) {
                            Text(
                                text = "Forgot?",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                PinDigitButton(
                    label = "0",
                    backgroundColor = keyBgColor,
                    contentColor = keyTextColor,
                    enabled = enabled,
                    onClick = {
                        if (pin.length < 4) {
                            onPinChange(pin + "0")
                        }
                    },
                    testTagName = "pin_digit_0"
                )

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .minimumInteractiveComponentSize()
                        .clickable(enabled = enabled && pin.isNotEmpty()) {
                            onPinChange(pin.dropLast(1))
                        }
                        .testTag("pin_backspace"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = "Backspace",
                        tint = if (enabled && pin.isNotEmpty()) keyTextColor else keyTextColor.copy(alpha = 0.35f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PinDigitButton(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    testTagName: String
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.45f),
        modifier = Modifier
            .size(68.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTagName)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) contentColor else contentColor.copy(alpha = 0.4f)
            )
        }
    }
}

private enum class PinSheetStage {
    ENTER_PIN,
    RECOVERY_CHALLENGE,
    RESET_NEW_PIN,
    CONFIRM_NEW_PIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinVerificationBottomSheet(
    title: String,
    subtitle: String,
    recoveryQuestion: String,
    failedAttempts: Int,
    lockoutRemainingSeconds: Int,
    onVerifyPin: (String) -> Boolean,
    onVerifyRecoveryAnswer: (String) -> Boolean,
    onResetPinFromRecovery: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var stage by remember { mutableStateOf(PinSheetStage.ENTER_PIN) }
    var pinInput by remember { mutableStateOf("") }
    var newPinDraft by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var shakeTrigger by remember { mutableStateOf(0) }
    var recoveryAnswer by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf<String?>(null) }

    val isLockedOut = lockoutRemainingSeconds > 0

    LaunchedEffect(pinInput, stage) {
        if (pinInput.length == 4) {
            when (stage) {
                PinSheetStage.ENTER_PIN -> {
                    val ok = onVerifyPin(pinInput)
                    if (!ok) {
                        pinInput = ""
                        shakeTrigger++
                        errorMessage = if (lockoutRemainingSeconds > 0) {
                            "PIN pad locked. Wait ${lockoutRemainingSeconds}s"
                        } else {
                            val remainingTries = (5 - (failedAttempts + 1)).coerceAtLeast(0)
                            if (remainingTries == 0) {
                                "Too many wrong attempts. Locked for 30s."
                            } else {
                                "Incorrect PIN. $remainingTries attempts left."
                            }
                        }
                    }
                }
                PinSheetStage.RESET_NEW_PIN -> {
                    newPinDraft = pinInput
                    pinInput = ""
                    errorMessage = null
                    stage = PinSheetStage.CONFIRM_NEW_PIN
                }
                PinSheetStage.CONFIRM_NEW_PIN -> {
                    if (pinInput == newPinDraft) {
                        onResetPinFromRecovery(pinInput)
                        pinInput = ""
                        stage = PinSheetStage.ENTER_PIN
                        errorMessage = null
                    } else {
                        pinInput = ""
                        newPinDraft = ""
                        shakeTrigger++
                        errorMessage = "PINs did not match. Try again."
                        stage = PinSheetStage.RESET_NEW_PIN
                    }
                }
                PinSheetStage.RECOVERY_CHALLENGE -> Unit
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(targetState = stage, label = "pin_sheet_stage") { currentStage ->
                when (currentStage) {
                    PinSheetStage.ENTER_PIN -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Parent PIN lock",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isLockedOut) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Too many wrong attempts. Try again in ${lockoutRemainingSeconds}s",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            PinPadInput(
                                pin = pinInput,
                                onPinChange = {
                                    errorMessage = null
                                    pinInput = it
                                },
                                errorMessage = if (isLockedOut) null else errorMessage,
                                shakeTrigger = shakeTrigger,
                                enabled = !isLockedOut,
                                showForgotPin = recoveryQuestion.isNotBlank(),
                                onForgotPinClick = {
                                    pinInput = ""
                                    errorMessage = null
                                    recoveryAnswer = ""
                                    recoveryError = null
                                    stage = PinSheetStage.RECOVERY_CHALLENGE
                                }
                            )
                        }
                    }

                    PinSheetStage.RECOVERY_CHALLENGE -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shield,
                                        contentDescription = "PIN Recovery",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Recover Parent PIN",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Answer your security question to set a new PIN.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = recoveryQuestion,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = recoveryAnswer,
                                onValueChange = {
                                    recoveryError = null
                                    recoveryAnswer = it
                                },
                                label = { Text("Your Answer") },
                                singleLine = true,
                                isError = recoveryError != null,
                                supportingText = recoveryError?.let { msg -> { Text(msg) } },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recovery_answer_input")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { stage = PinSheetStage.ENTER_PIN },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text("Back")
                                }
                                Button(
                                    onClick = {
                                        if (onVerifyRecoveryAnswer(recoveryAnswer)) {
                                            pinInput = ""
                                            errorMessage = null
                                            stage = PinSheetStage.RESET_NEW_PIN
                                        } else {
                                            recoveryError = "That answer doesn't match."
                                        }
                                    },
                                    enabled = recoveryAnswer.isNotBlank(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("verify_recovery_button")
                                ) {
                                    Text("Verify")
                                }
                            }
                        }
                    }

                    PinSheetStage.RESET_NEW_PIN,
                    PinSheetStage.CONFIRM_NEW_PIN -> {
                        val isConfirm = currentStage == PinSheetStage.CONFIRM_NEW_PIN
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isConfirm) "Confirm New PIN" else "Enter New 4-Digit PIN",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isConfirm) "Enter the new PIN once more to confirm" else "Choose a new 4-digit Parent PIN",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            PinPadInput(
                                pin = pinInput,
                                onPinChange = {
                                    errorMessage = null
                                    pinInput = it
                                },
                                errorMessage = errorMessage,
                                shakeTrigger = shakeTrigger,
                                enabled = true,
                                showForgotPin = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraTimeBottomSheet(
    onAddMinutes: (Int) -> Unit,
    onOpenParentSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTimeFilled,
                        contentDescription = "Add extra time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Parent Unlock",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Grant extra screen time for today or open Parent Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(15, 30, 60).forEach { mins ->
                    Button(
                        onClick = { onAddMinutes(mins) },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("add_extra_${mins}_min")
                    ) {
                        Text(
                            text = "+$mins min",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenParentSettings,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("unlock_to_parent_settings")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Parent Settings")
            }
        }
    }
}
