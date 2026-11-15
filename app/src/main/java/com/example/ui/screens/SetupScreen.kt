package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.PinPadInput

private enum class SetupStep {
    WELCOME,
    CREATE_PIN,
    CONFIRM_PIN,
    RECOVERY_QUESTION
}

@Composable
fun SetupScreen(
    onSetupComplete: (pin: String, recoveryQuestion: String, recoveryAnswer: String) -> Unit
) {
    var step by remember { mutableStateOf(SetupStep.WELCOME) }
    var createdPin by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var shakeTrigger by remember { mutableStateOf(0) }

    val presetQuestions = remember {
        listOf(
            "What is your first pet's name?",
            "What city were you born in?",
            "What is your child's favorite toy?",
            "What is your mother's maiden name?"
        )
    }
    var recoveryQuestion by remember { mutableStateOf(presetQuestions.first()) }
    var recoveryAnswer by remember { mutableStateOf("") }

    LaunchedEffect(pinInput, step) {
        if (pinInput.length == 4) {
            when (step) {
                SetupStep.CREATE_PIN -> {
                    createdPin = pinInput
                    pinInput = ""
                    pinError = null
                    step = SetupStep.CONFIRM_PIN
                }
                SetupStep.CONFIRM_PIN -> {
                    if (pinInput == createdPin) {
                        pinInput = ""
                        pinError = null
                        step = SetupStep.RECOVERY_QUESTION
                    } else {
                        pinInput = ""
                        createdPin = ""
                        pinError = "PINs didn't match. Please choose your PIN again."
                        shakeTrigger++
                        step = SetupStep.CREATE_PIN
                    }
                }
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEEF2FF),
                        MaterialTheme.colorScheme.background,
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header & Step Indicator
            if (step != SetupStep.WELCOME) {
                val progress = when (step) {
                    SetupStep.WELCOME -> 0f
                    SetupStep.CREATE_PIN -> 0.33f
                    SetupStep.CONFIRM_PIN -> 0.66f
                    SetupStep.RECOVERY_QUESTION -> 1f
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            pinInput = ""
                            pinError = null
                            step = when (step) {
                                SetupStep.CREATE_PIN -> SetupStep.WELCOME
                                SetupStep.CONFIRM_PIN -> SetupStep.CREATE_PIN
                                SetupStep.RECOVERY_QUESTION -> SetupStep.CREATE_PIN
                                SetupStep.WELCOME -> SetupStep.WELCOME
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f),
                label = "setup_step_transition"
            ) { currentStep ->
                when (currentStep) {
                    SetupStep.WELCOME -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Shield,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SafeKids Launcher",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(18.dp))

                                Card(
                                    shape = RoundedCornerShape(28.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_welcome_kids_1794744747154),
                                        contentDescription = "Happy child exploring safe apps",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "A playful, safe space for kids to explore",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pick approved apps, set healthy screen time limits, and lock settings behind your Parent PIN.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    WelcomeFeatureRow(
                                        icon = Icons.Rounded.Apps,
                                        title = "Curated App Grid",
                                        subtitle = "Only the apps you allow are visible"
                                    )
                                    WelcomeFeatureRow(
                                        icon = Icons.Rounded.Schedule,
                                        title = "Daily Limits & Bedtime",
                                        subtitle = "Automatic gentle lock when time runs out"
                                    )
                                    WelcomeFeatureRow(
                                        icon = Icons.Rounded.Lock,
                                        title = "SHA-256 PIN Security",
                                        subtitle = "Protected exit & recovery question"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { step = SetupStep.CREATE_PIN },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("setup_get_started_button")
                            ) {
                                Text(
                                    text = "Get Started",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    SetupStep.CREATE_PIN,
                    SetupStep.CONFIRM_PIN -> {
                        val isConfirm = currentStep == SetupStep.CONFIRM_PIN
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isConfirm) "Confirm Parent PIN" else "Create Parent PIN",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isConfirm) {
                                    "Re-enter your 4-digit PIN to confirm"
                                } else {
                                    "Choose a 4-digit PIN to protect Parent Settings"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PinPadInput(
                                pin = pinInput,
                                onPinChange = {
                                    pinError = null
                                    pinInput = it
                                },
                                errorMessage = pinError,
                                shakeTrigger = shakeTrigger,
                                enabled = true
                            )
                        }
                    }

                    SetupStep.RECOVERY_QUESTION -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "PIN Recovery Question",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "In case you forget your Parent PIN, this security question lets you reset it safely.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "Quick presets",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    presetQuestions.forEach { preset ->
                                        FilterChip(
                                            selected = recoveryQuestion == preset,
                                            onClick = { recoveryQuestion = preset },
                                            label = { Text(preset) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedTextField(
                                    value = recoveryQuestion,
                                    onValueChange = { recoveryQuestion = it },
                                    label = { Text("Security Question") },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_recovery_question_input")
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedTextField(
                                    value = recoveryAnswer,
                                    onValueChange = { recoveryAnswer = it },
                                    label = { Text("Your Secret Answer") },
                                    placeholder = { Text("e.g. Buddy") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("setup_recovery_answer_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Both your PIN and recovery answer are hashed locally with SHA-256 and never stored in plain text.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    onSetupComplete(
                                        createdPin,
                                        recoveryQuestion,
                                        recoveryAnswer
                                    )
                                },
                                enabled = recoveryQuestion.isNotBlank() && recoveryAnswer.isNotBlank(),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("setup_finish_button")
                            ) {
                                Text(
                                    text = "Continue to App Selection",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
