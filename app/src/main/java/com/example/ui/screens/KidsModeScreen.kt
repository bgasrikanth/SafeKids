package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MockAppsCatalog
import com.example.model.AppCategory
import com.example.model.KidsSubScreen
import com.example.model.MockAppInfo
import com.example.model.PinPurpose
import com.example.model.SafeKidsUiState
import com.example.model.TimeUpReason
import com.example.ui.components.AppIconBadge
import com.example.ui.components.KidsAppGridTile
import com.example.ui.theme.KidsGradientOrange
import com.example.ui.theme.KidsGradientPink
import com.example.ui.theme.KidsGradientPurple
import com.example.ui.theme.TwilightBottom
import com.example.ui.theme.TwilightMid
import com.example.ui.theme.TwilightTop
import com.example.util.TimeUtils

@Composable
fun KidsModeScreen(
    uiState: SafeKidsUiState,
    onOpenApp: (String) -> Unit,
    onReturnHome: () -> Unit,
    onSimulatedRecents: () -> Unit,
    onRequestPin: (PinPurpose) -> Unit,
    onToggleSimulator: () -> Unit,
    onTryOpenBlockedApp: () -> Unit
) {
    // Hardware back always stays inside Kids Mode
    BackHandler(enabled = true) {
        if (uiState.kidsSubScreen != KidsSubScreen.TIME_UP) {
            onReturnHome()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = uiState.kidsSubScreen,
                transitionSpec = {
                    (fadeIn(tween(240)) + slideInVertically { it / 16 }) togetherWith
                        (fadeOut(tween(180)) + slideOutVertically { -it / 16 })
                },
                label = "kids_subscreen_transition"
            ) { subScreen ->
                when (subScreen) {
                    KidsSubScreen.HOME -> KidsHomeContent(
                        uiState = uiState,
                        onOpenApp = onOpenApp,
                        onRequestPin = onRequestPin,
                        onToggleSimulator = onToggleSimulator,
                        onTryOpenBlockedApp = onTryOpenBlockedApp
                    )

                    KidsSubScreen.APP_RUNNING -> {
                        val runningApp = uiState.runningAppId?.let { MockAppsCatalog.findById(it) }
                        if (runningApp != null) {
                            SimulatedAppWindow(
                                app = runningApp,
                                sessionSeconds = uiState.runningAppSessionSeconds,
                                remainingTodaySeconds = uiState.remainingSecondsToday,
                                isInternetBlocked = uiState.persisted.restrictions.blockInternet,
                                onHomeClick = onReturnHome
                            )
                        } else {
                            KidsHomeContent(
                                uiState = uiState,
                                onOpenApp = onOpenApp,
                                onRequestPin = onRequestPin,
                                onToggleSimulator = onToggleSimulator,
                                onTryOpenBlockedApp = onTryOpenBlockedApp
                            )
                        }
                    }

                    KidsSubScreen.BLOCKED -> BlockedAppScreen(
                        blockedAppName = uiState.blockedAppName ?: "This app",
                        onBackToHome = onReturnHome
                    )

                    KidsSubScreen.TIME_UP -> TimeUpScreen(
                        reason = uiState.timeUpReason,
                        bedtimeEndMinutes = uiState.persisted.bedtimeEndMinutes,
                        onParentUnlock = { onRequestPin(PinPurpose.UNLOCK_TIME_UP) }
                    )
                }
            }
        }

        // Simulated Android Navigation Bar (Back, Home, Recents)
        if (uiState.kidsSubScreen != KidsSubScreen.TIME_UP) {
            SimulatedPhoneNavBar(
                onBackClick = onReturnHome,
                onHomeClick = onReturnHome,
                onRecentsClick = onSimulatedRecents
            )
        }
    }
}

@Composable
private fun KidsHomeContent(
    uiState: SafeKidsUiState,
    onOpenApp: (String) -> Unit,
    onRequestPin: (PinPurpose) -> Unit,
    onToggleSimulator: () -> Unit,
    onTryOpenBlockedApp: () -> Unit
) {
    val allowedApps = remember(uiState.persisted.allowedAppIds) {
        MockAppsCatalog.allApps.filter { uiState.persisted.allowedAppIds.contains(it.id) }
    }
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        KidsGradientPurple,
                        KidsGradientPink,
                        KidsGradientOrange
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 2000f)
                )
            )
    ) {
        // Subtle decorative translucent orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.width * 0.42f,
                center = Offset(size.width * 0.9f, size.height * 0.12f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.1f, size.height * 0.78f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Top Glass Bar: Greeting, Live Clock, Remaining Time Ring, Lock & Menu
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.30f),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White.copy(alpha = 0.24f),
                                shape = CircleShape
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Kids Mode locked",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "KIDS MODE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.currentTimeFormatted,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.testTag("kids_home_clock")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hi there!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = "${allowedApps.size} fun apps ready",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TimeLeftProgressRing(
                            remainingSeconds = uiState.remainingSecondsToday,
                            totalSeconds = uiState.totalAllowedSecondsToday
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.testTag("kids_overflow_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Parent options",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Parent Settings") },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Settings, contentDescription = null)
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onRequestPin(PinPurpose.OPEN_PARENT_SETTINGS)
                                    },
                                    modifier = Modifier.testTag("menu_parent_settings")
                                )
                                DropdownMenuItem(
                                    text = { Text("Exit Kids Mode") },
                                    leadingIcon = {
                                        Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null)
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onRequestPin(PinPurpose.EXIT_KIDS_MODE)
                                    },
                                    modifier = Modifier.testTag("menu_exit_kids_mode")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Column App Grid
            if (allowedApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Apps,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No apps allowed yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the 3-dot menu above and open Parent Settings to choose apps.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allowedApps, key = { it.id }) { app ->
                        KidsAppGridTile(
                            app = app,
                            onClick = { onOpenApp(app.id) }
                        )
                    }
                }
            }

            // Collapsible Kid Simulator Debug Panel
            KidSimulatorPanel(
                expanded = uiState.isKidSimulatorExpanded,
                onToggle = onToggleSimulator,
                onTryBlockedApp = onTryOpenBlockedApp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TimeLeftProgressRing(
    remainingSeconds: Int,
    totalSeconds: Int
) {
    val fraction = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedSweep by animateFloatAsState(
        targetValue = fraction * 360f,
        animationSpec = tween(500),
        label = "time_ring_sweep"
    )
    val remainingLabel = TimeUtils.formatDurationMinutes((remainingSeconds + 59) / 60)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(58.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = Color.White.copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = stroke
                )
            }
            Text(
                text = remainingLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Time left",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun KidSimulatorPanel(
    expanded: Boolean,
    onToggle: () -> Unit,
    onTryBlockedApp: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A).copy(alpha = 0.72f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("kid_simulator_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.BugReport,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kid Simulator",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                    contentDescription = if (expanded) "Collapse simulator" else "Expand simulator",
                    tint = Color.White
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    Text(
                        text = "Test parental restrictions by simulating a child trying to launch a blocked app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onTryBlockedApp,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF43F5E),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("try_blocked_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Block,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try opening a blocked app")
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedAppWindow(
    app: MockAppInfo,
    sessionSeconds: Int,
    remainingTodaySeconds: Int,
    isInternetBlocked: Boolean,
    onHomeClick: () -> Unit
) {
    var interactionCounter by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(app.gradientStart, app.gradientEnd)
                )
            )
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Session: ${TimeUtils.formatCountdownSeconds(sessionSeconds)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Left: ${TimeUtils.formatCountdownSeconds(remainingTodaySeconds)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Center Simulated Interactive App Window
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppIconBadge(
                        app = app,
                        size = 80.dp,
                        cornerRadius = 24.dp,
                        iconSize = 42.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.shortDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (app.category == AppCategory.BROWSER && isInternetBlocked) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WifiOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Internet access is restricted by Parent Rules.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = app.gradientStart.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val demoActivityText = when (app.id) {
                                    "learn_abc" -> "Letter of the moment: ${('A' + (interactionCounter % 26))}"
                                    "math_fun" -> "Quick Puzzle: ${3 + (interactionCounter % 7)} + ${2 + (interactionCounter % 5)} = ?"
                                    "youtube_kids" -> "Now Playing: Space Rockets for Kids #${interactionCounter + 1}"
                                    "story_time" -> "Chapter ${interactionCounter + 1}: The Friendly Dragon"
                                    "music" -> "Playing Happy Melodies (${interactionCounter + 1}x)"
                                    "drawing_pad" -> "Canvas Brush Strokes: $interactionCounter"
                                    else -> "Interactive Activity Score: ${interactionCounter * 10} pts"
                                }
                                Text(
                                    text = demoActivityText,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { interactionCounter++ },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = app.gradientEnd
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Play / Interact")
                                }
                            }
                        }
                    }
                }
            }

            // Home Button at bottom of simulated app window
            Button(
                onClick = onHomeClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = app.gradientEnd
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("simulated_app_home_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun BlockedAppScreen(
    blockedAppName: String,
    onBackToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF1F2), Color(0xFFFDF2F8), Color(0xFFFFFFFF))
                )
            )
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = CircleShape
            ) {
                Text(
                    text = "$blockedAppName is blocked",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.img_blocked_app_1794744746010),
                contentDescription = "Blocked app illustration",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(32.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "This app isn't available right now",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Only parent-approved apps can be opened in Kids Mode. This attempt has been logged.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onBackToHome,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("blocked_back_to_home_button")
            ) {
                Icon(Icons.Rounded.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to Home",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun TimeUpScreen(
    reason: TimeUpReason,
    bedtimeEndMinutes: Int,
    onParentUnlock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TwilightTop, TwilightMid, TwilightBottom)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_times_up_1794744746903),
                contentDescription = "Moon and hourglass illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(230.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(36.dp))
            )

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Time's up! See you tomorrow",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = when (reason) {
                    TimeUpReason.DAILY_LIMIT -> "You've used all your screen time for today. Great job!"
                    TimeUpReason.BEDTIME -> "Bedtime lock is active until ${TimeUtils.formatMinutesOfDay(bedtimeEndMinutes)}. Sweet dreams!"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                onClick = onParentUnlock,
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = TwilightTop
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("time_up_parent_unlock_button")
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Parent Unlock",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun SimulatedPhoneNavBar(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRecentsClick: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("sim_nav_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Simulated Back",
                    tint = Color.White.copy(alpha = 0.88f)
                )
            }

            IconButton(
                onClick = onHomeClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("sim_nav_home")
            ) {
                Icon(
                    imageVector = Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Simulated Home",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onRecentsClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("sim_nav_recents")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CropSquare,
                    contentDescription = "Simulated Recents",
                    tint = Color.White.copy(alpha = 0.88f)
                )
            }
        }
    }
}
