package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Abc
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.ui.graphics.Color
import com.example.model.AppCategory
import com.example.model.MockAppInfo

object MockAppsCatalog {

    val allApps: List<MockAppInfo> = listOf(
        MockAppInfo(
            id = "youtube_kids",
            name = "YouTube Kids",
            category = AppCategory.VIDEO,
            gradientStart = Color(0xFFFF4B5C),
            gradientEnd = Color(0xFFFF758C),
            icon = Icons.Rounded.PlayCircle,
            shortDescription = "Curated kid-friendly videos and shows"
        ),
        MockAppInfo(
            id = "chrome",
            name = "Chrome",
            category = AppCategory.BROWSER,
            gradientStart = Color(0xFF38BDF8),
            gradientEnd = Color(0xFF2563EB),
            icon = Icons.Rounded.Public,
            shortDescription = "Web browser"
        ),
        MockAppInfo(
            id = "camera",
            name = "Camera",
            category = AppCategory.UTILITY,
            gradientStart = Color(0xFF14B8A6),
            gradientEnd = Color(0xFF0D9488),
            icon = Icons.Rounded.CameraAlt,
            shortDescription = "Snap fun photos and stickers"
        ),
        MockAppInfo(
            id = "gallery",
            name = "Gallery",
            category = AppCategory.UTILITY,
            gradientStart = Color(0xFFF59E0B),
            gradientEnd = Color(0xFFEA580C),
            icon = Icons.Rounded.PhotoLibrary,
            shortDescription = "View albums and drawings"
        ),
        MockAppInfo(
            id = "calculator",
            name = "Calculator",
            category = AppCategory.UTILITY,
            gradientStart = Color(0xFF6366F1),
            gradientEnd = Color(0xFF4338CA),
            icon = Icons.Rounded.Calculate,
            shortDescription = "Quick numbers & arithmetic"
        ),
        MockAppInfo(
            id = "drawing_pad",
            name = "Drawing Pad",
            category = AppCategory.CREATIVE,
            gradientStart = Color(0xFFEC4899),
            gradientEnd = Color(0xFFBE185D),
            icon = Icons.Rounded.Brush,
            shortDescription = "Doodle, paint & color canvases"
        ),
        MockAppInfo(
            id = "learn_abc",
            name = "Learn ABC",
            category = AppCategory.LEARNING,
            gradientStart = Color(0xFF10B981),
            gradientEnd = Color(0xFF059669),
            icon = Icons.Rounded.Abc,
            shortDescription = "Phonics, letters & reading"
        ),
        MockAppInfo(
            id = "math_fun",
            name = "Math Fun",
            category = AppCategory.LEARNING,
            gradientStart = Color(0xFF8B5CF6),
            gradientEnd = Color(0xFF6D28D9),
            icon = Icons.Rounded.Functions,
            shortDescription = "Puzzles & playful math challenges"
        ),
        MockAppInfo(
            id = "story_time",
            name = "Story Time",
            category = AppCategory.LEARNING,
            gradientStart = Color(0xFFFBBF24),
            gradientEnd = Color(0xFFD97706),
            icon = Icons.Rounded.AutoStories,
            shortDescription = "Illustrated bedtime & adventure books"
        ),
        MockAppInfo(
            id = "music",
            name = "Music",
            category = AppCategory.MUSIC,
            gradientStart = Color(0xFFD946EF),
            gradientEnd = Color(0xFF9333EA),
            icon = Icons.Rounded.MusicNote,
            shortDescription = "Sing-alongs & calm melodies"
        ),
        MockAppInfo(
            id = "games_zone",
            name = "Games Zone",
            category = AppCategory.GAMES,
            gradientStart = Color(0xFF06B6D4),
            gradientEnd = Color(0xFF0284C7),
            icon = Icons.Rounded.SportsEsports,
            shortDescription = "Arcade mini-games & puzzles"
        ),
        MockAppInfo(
            id = "clock",
            name = "Clock",
            category = AppCategory.UTILITY,
            gradientStart = Color(0xFF64748B),
            gradientEnd = Color(0xFF334155),
            icon = Icons.Rounded.AccessTime,
            shortDescription = "Telling time & timers"
        ),
        MockAppInfo(
            id = "maps",
            name = "Maps",
            category = AppCategory.UTILITY,
            gradientStart = Color(0xFF22C55E),
            gradientEnd = Color(0xFF15803D),
            icon = Icons.Rounded.Map,
            shortDescription = "Explore places & geography"
        ),
        MockAppInfo(
            id = "whatsapp",
            name = "WhatsApp",
            category = AppCategory.COMMUNICATION,
            gradientStart = Color(0xFF4ADE80),
            gradientEnd = Color(0xFF16A34A),
            icon = Icons.Rounded.Chat,
            shortDescription = "Messaging & calls"
        ),
        MockAppInfo(
            id = "play_store",
            name = "Play Store",
            category = AppCategory.SYSTEM,
            gradientStart = Color(0xFF2DD4BF),
            gradientEnd = Color(0xFF2563EB),
            icon = Icons.Rounded.ShoppingBag,
            shortDescription = "App store & downloads"
        ),
        MockAppInfo(
            id = "settings",
            name = "Settings",
            category = AppCategory.SYSTEM,
            gradientStart = Color(0xFF475569),
            gradientEnd = Color(0xFF1E293B),
            icon = Icons.Rounded.Settings,
            shortDescription = "System preferences"
        )
    )

    val defaultAllowedAppIds: Set<String> = setOf(
        "youtube_kids",
        "camera",
        "gallery",
        "drawing_pad",
        "learn_abc",
        "math_fun",
        "story_time",
        "music"
    )

    fun findById(id: String): MockAppInfo? = allApps.find { it.id == id }
}
