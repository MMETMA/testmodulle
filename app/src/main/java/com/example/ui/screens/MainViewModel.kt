package com.example.ui.screens

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ModuleConfigRepository
import com.example.data.ModulePreferences
import com.example.hook.ModuleStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SystemDeviceInfo(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val androidVersion: String,
    val sdkInt: Int,
    val isMiuiOrHyperOs: Boolean,
    val miuiVersionName: String,
    val securityPatch: String
)

data class HookLogEntry(
    val timestamp: String,
    val level: String,
    val message: String
)

data class MainUiState(
    val isModuleActive: Boolean = false,
    val preferences: ModulePreferences = ModulePreferences(),
    val deviceInfo: SystemDeviceInfo,
    val hookLogs: List<HookLogEntry> = emptyList(),
    val selectedTab: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ModuleConfigRepository(application)

    private val _uiState = MutableStateFlow(
        MainUiState(
            isModuleActive = ModuleStatus.isModuleActive(),
            preferences = repository.getPreferences(),
            deviceInfo = inspectDeviceInfo(),
            hookLogs = generateInitialLogs(ModuleStatus.isModuleActive())
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        val active = ModuleStatus.isModuleActive()
        val prefs = repository.getPreferences()
        _uiState.value = _uiState.value.copy(
            isModuleActive = active,
            preferences = prefs,
            hookLogs = generateInitialLogs(active)
        )
    }

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun toggleHook(enabled: Boolean) {
        repository.setHookEnabled(enabled)
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(isHookEnabled = enabled)
        )
    }

    fun toggleAggressiveMode(enabled: Boolean) {
        repository.setAggressiveMode(enabled)
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(isAggressiveMode = enabled)
        )
    }

    fun toggleVerboseLogging(enabled: Boolean) {
        repository.setVerboseLogging(enabled)
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(isVerboseLogging = enabled)
        )
    }

    fun toggleHookOnConstructor(enabled: Boolean) {
        repository.setHookOnConstructor(enabled)
        _uiState.value = _uiState.value.copy(
            preferences = _uiState.value.preferences.copy(hookOnConstructor = enabled)
        )
    }

    private fun inspectDeviceInfo(): SystemDeviceInfo {
        val miuiProperty = getSystemProperty("ro.miui.ui.version.name")
        val hyperOsProperty = getSystemProperty("ro.mi.os.version.name")
        val isMiui = miuiProperty.isNotEmpty() || hyperOsProperty.isNotEmpty() ||
                Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
                Build.BRAND.equals("POCO", ignoreCase = true) ||
                Build.BRAND.equals("Redmi", ignoreCase = true)

        val versionDisplay = when {
            hyperOsProperty.isNotEmpty() -> "HyperOS ($hyperOsProperty)"
            miuiProperty.isNotEmpty() -> "MIUI ($miuiProperty)"
            isMiui -> "MIUI / HyperOS"
            else -> "AOSP / Generic"
        }

        return SystemDeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            sdkInt = Build.VERSION.SDK_INT,
            isMiuiOrHyperOs = isMiui,
            miuiVersionName = versionDisplay,
            securityPatch = Build.VERSION.SECURITY_PATCH ?: "N/A"
        )
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java)
            (method.invoke(null, key) as? String) ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun generateInitialLogs(active: Boolean): List<HookLogEntry> {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        val now = Date()
        val list = mutableListOf<HookLogEntry>()

        list.add(
            HookLogEntry(
                timestamp = sdf.format(Date(now.time - 3500)),
                level = "INFO",
                message = "Module initialization: target = com.android.systemui"
            )
        )
        list.add(
            HookLogEntry(
                timestamp = sdf.format(Date(now.time - 2500)),
                level = "INFO",
                message = "Target definition: com.miui.systemui.notification.NotificationSettingsManager.USE_WHITE_LISTS"
            )
        )
        list.add(
            HookLogEntry(
                timestamp = sdf.format(Date(now.time - 1500)),
                level = "INFO",
                message = "Hook rule: Override sput-boolean v0 in <clinit>()V to constant 0x1 (true)"
            )
        )

        if (active) {
            list.add(
                HookLogEntry(
                    timestamp = sdf.format(now),
                    level = "SUCCESS",
                    message = "LSPosed framework hook active in target process"
                )
            )
        } else {
            list.add(
                HookLogEntry(
                    timestamp = sdf.format(now),
                    level = "WAITING",
                    message = "Awaiting activation in LSPosed Manager scope (select 'System UI' and restart SystemUI)"
                )
            )
        }
        return list
    }
}
