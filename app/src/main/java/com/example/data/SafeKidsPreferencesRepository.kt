package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppMode
import com.example.model.DailyUsageSummary
import com.example.model.RestrictionsConfig
import com.example.model.SafeKidsPersistedState
import com.example.util.TimeUtils
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

private val Context.safeKidsDataStore: DataStore<Preferences> by preferencesDataStore(name = "safekids_prefs")

class SafeKidsPreferencesRepository(private val context: Context) {

    private val stateKey = stringPreferencesKey("safekids_state_json")

    suspend fun loadState(): SafeKidsPersistedState {
        val prefs = context.safeKidsDataStore.data.first()
        val raw = prefs[stateKey]
        val todayKey = TimeUtils.currentDateKey()
        val loaded = if (raw.isNullOrBlank()) {
            SafeKidsPersistedState(
                allowedAppIds = MockAppsCatalog.defaultAllowedAppIds,
                todayDateKey = todayKey,
                weeklyHistory = createInitialMockHistory()
            )
        } else {
            deserializeState(raw, todayKey)
        }
        return rollOverMidnightIfNeeded(loaded, todayKey)
    }

    suspend fun saveState(state: SafeKidsPersistedState) {
        context.safeKidsDataStore.edit { prefs ->
            prefs[stateKey] = serializeState(state)
        }
    }

    fun rollOverMidnightIfNeeded(
        state: SafeKidsPersistedState,
        currentDateKey: String = TimeUtils.currentDateKey()
    ): SafeKidsPersistedState {
        if (state.todayDateKey == currentDateKey) return state
        if (state.todayDateKey.isBlank()) {
            return state.copy(todayDateKey = currentDateKey)
        }

        val yesterdayMinutes = (state.todayUsageSecondsByApp.values.sum() + 30) / 60
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayEntry = DailyUsageSummary(
            dateKey = state.todayDateKey,
            dayLabel = TimeUtils.dayShortLabel(cal),
            totalMinutes = yesterdayMinutes,
            blockedAttempts = state.blockedAttemptsToday
        )
        val updatedHistory = (state.weeklyHistory + yesterdayEntry).takeLast(6)

        return state.copy(
            todayDateKey = currentDateKey,
            todayUsageSecondsByApp = emptyMap(),
            extraMinutesToday = 0,
            blockedAttemptsToday = 0,
            weeklyHistory = updatedHistory
        )
    }

    private fun createInitialMockHistory(): List<DailyUsageSummary> {
        val sampleMinutes = listOf(42, 68, 55, 84, 49, 73)
        val sampleBlocked = listOf(1, 0, 2, 1, 0, 1)
        return (6 downTo 1).mapIndexed { index, daysAgo ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            DailyUsageSummary(
                dateKey = TimeUtils.currentDateKey(cal),
                dayLabel = TimeUtils.dayShortLabel(cal),
                totalMinutes = sampleMinutes[index],
                blockedAttempts = sampleBlocked[index]
            )
        }
    }

    private fun serializeState(state: SafeKidsPersistedState): String {
        val root = JSONObject()
        root.put("isSetupComplete", state.isSetupComplete)
        root.put("pinHash", state.pinHash)
        root.put("recoveryQuestion", state.recoveryQuestion)
        root.put("recoveryAnswerHash", state.recoveryAnswerHash)
        root.put("activeMode", state.activeMode.name)
        root.put("allowedAppIds", JSONArray(state.allowedAppIds.toList()))
        root.put("dailyLimitMinutes", state.dailyLimitMinutes)
        root.put("extraMinutesToday", state.extraMinutesToday)
        root.put("bedtimeEnabled", state.bedtimeEnabled)
        root.put("bedtimeStartMinutes", state.bedtimeStartMinutes)
        root.put("bedtimeEndMinutes", state.bedtimeEndMinutes)
        root.put("bedtimeOverrideUntilMillis", state.bedtimeOverrideUntilMillis)

        val restrictionsJson = JSONObject().apply {
            put("blockCalls", state.restrictions.blockCalls)
            put("blockSms", state.restrictions.blockSms)
            put("blockInternet", state.restrictions.blockInternet)
            put("blockPlayStore", state.restrictions.blockPlayStore)
            put("blockSettings", state.restrictions.blockSettings)
            put("blockUninstall", state.restrictions.blockUninstall)
        }
        root.put("restrictions", restrictionsJson)

        root.put("todayDateKey", state.todayDateKey)
        val usageJson = JSONObject()
        state.todayUsageSecondsByApp.forEach { (appId, seconds) ->
            usageJson.put(appId, seconds)
        }
        root.put("todayUsageSecondsByApp", usageJson)
        root.put("blockedAttemptsToday", state.blockedAttemptsToday)

        val historyArray = JSONArray()
        state.weeklyHistory.forEach { day ->
            historyArray.put(
                JSONObject().apply {
                    put("dateKey", day.dateKey)
                    put("dayLabel", day.dayLabel)
                    put("totalMinutes", day.totalMinutes)
                    put("blockedAttempts", day.blockedAttempts)
                }
            )
        }
        root.put("weeklyHistory", historyArray)

        root.put("failedPinAttempts", state.failedPinAttempts)
        root.put("pinLockoutUntilMillis", state.pinLockoutUntilMillis)
        return root.toString()
    }

    private fun deserializeState(raw: String, fallbackDateKey: String): SafeKidsPersistedState {
        return try {
            val root = JSONObject(raw)
            val allowedArray = root.optJSONArray("allowedAppIds")
            val allowedIds = buildSet {
                if (allowedArray != null) {
                    for (i in 0 until allowedArray.length()) {
                        add(allowedArray.optString(i))
                    }
                }
            }

            val rJson = root.optJSONObject("restrictions")
            val restrictions = if (rJson != null) {
                RestrictionsConfig(
                    blockCalls = rJson.optBoolean("blockCalls", true),
                    blockSms = rJson.optBoolean("blockSms", true),
                    blockInternet = rJson.optBoolean("blockInternet", true),
                    blockPlayStore = rJson.optBoolean("blockPlayStore", true),
                    blockSettings = rJson.optBoolean("blockSettings", true),
                    blockUninstall = rJson.optBoolean("blockUninstall", true)
                )
            } else {
                RestrictionsConfig()
            }

            val usageMap = mutableMapOf<String, Int>()
            val usageJson = root.optJSONObject("todayUsageSecondsByApp")
            if (usageJson != null) {
                val keys = usageJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    usageMap[key] = usageJson.optInt(key, 0)
                }
            }

            val historyList = mutableListOf<DailyUsageSummary>()
            val historyArray = root.optJSONArray("weeklyHistory")
            if (historyArray != null) {
                for (i in 0 until historyArray.length()) {
                    val obj = historyArray.optJSONObject(i) ?: continue
                    historyList.add(
                        DailyUsageSummary(
                            dateKey = obj.optString("dateKey", ""),
                            dayLabel = obj.optString("dayLabel", ""),
                            totalMinutes = obj.optInt("totalMinutes", 0),
                            blockedAttempts = obj.optInt("blockedAttempts", 0)
                        )
                    )
                }
            }

            val modeStr = root.optString("activeMode", AppMode.SETUP.name)
            val mode = runCatching { AppMode.valueOf(modeStr) }.getOrDefault(AppMode.SETUP)

            SafeKidsPersistedState(
                isSetupComplete = root.optBoolean("isSetupComplete", false),
                pinHash = root.optString("pinHash", ""),
                recoveryQuestion = root.optString("recoveryQuestion", ""),
                recoveryAnswerHash = root.optString("recoveryAnswerHash", ""),
                activeMode = mode,
                allowedAppIds = allowedIds,
                dailyLimitMinutes = root.optInt("dailyLimitMinutes", 120).coerceIn(15, 360),
                extraMinutesToday = root.optInt("extraMinutesToday", 0).coerceAtLeast(0),
                bedtimeEnabled = root.optBoolean("bedtimeEnabled", false),
                bedtimeStartMinutes = root.optInt("bedtimeStartMinutes", 20 * 60),
                bedtimeEndMinutes = root.optInt("bedtimeEndMinutes", 7 * 60),
                bedtimeOverrideUntilMillis = root.optLong("bedtimeOverrideUntilMillis", 0L),
                restrictions = restrictions,
                todayDateKey = root.optString("todayDateKey", fallbackDateKey),
                todayUsageSecondsByApp = usageMap,
                blockedAttemptsToday = root.optInt("blockedAttemptsToday", 0),
                weeklyHistory = historyList.ifEmpty { createInitialMockHistory() },
                failedPinAttempts = root.optInt("failedPinAttempts", 0),
                pinLockoutUntilMillis = root.optLong("pinLockoutUntilMillis", 0L)
            )
        } catch (_: Exception) {
            SafeKidsPersistedState(
                allowedAppIds = MockAppsCatalog.defaultAllowedAppIds,
                todayDateKey = fallbackDateKey,
                weeklyHistory = createInitialMockHistory()
            )
        }
    }
}
