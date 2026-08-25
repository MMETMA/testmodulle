package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class ModulePreferences(
    val isHookEnabled: Boolean = true,
    val isAggressiveMode: Boolean = true,
    val isVerboseLogging: Boolean = true,
    val hookOnConstructor: Boolean = true,
    val overrideInternationalCheck: Boolean = true
)

class ModuleConfigRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("miui_whitelist_prefs", Context.MODE_PRIVATE)

    fun getPreferences(): ModulePreferences {
        return ModulePreferences(
            isHookEnabled = prefs.getBoolean("hook_enabled", true),
            isAggressiveMode = prefs.getBoolean("aggressive_mode", true),
            isVerboseLogging = prefs.getBoolean("verbose_logging", true),
            hookOnConstructor = prefs.getBoolean("hook_on_constructor", true),
            overrideInternationalCheck = prefs.getBoolean("override_intl_check", true)
        )
    }

    fun setHookEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("hook_enabled", enabled).apply()
    }

    fun setAggressiveMode(enabled: Boolean) {
        prefs.edit().putBoolean("aggressive_mode", enabled).apply()
    }

    fun setVerboseLogging(enabled: Boolean) {
        prefs.edit().putBoolean("verbose_logging", enabled).apply()
    }

    fun setHookOnConstructor(enabled: Boolean) {
        prefs.edit().putBoolean("hook_on_constructor", enabled).apply()
    }

    fun setOverrideInternationalCheck(enabled: Boolean) {
        prefs.edit().putBoolean("override_intl_check", enabled).apply()
    }
}
