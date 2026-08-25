package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.io.File

object PrefManager {
    const val PREFS_NAME = "module_prefs"
    const val KEY_HOOK_ENABLED = "hook_enabled"
    const val KEY_USE_WHITE_LISTS_VALUE = "use_white_lists_val" // false (0) or true (1)
    const val KEY_HOOK_CONSTRUCTOR = "hook_constructor"
    const val KEY_HOOK_METHODS = "hook_methods"
    const val KEY_HOOK_CLINIT = "hook_clinit"

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun makePrefsReadable(context: Context) {
        try {
            val dataDir = File(context.applicationInfo.dataDir)
            dataDir.setExecutable(true, false)
            dataDir.setReadable(true, false)

            val prefsDir = File(dataDir, "shared_prefs")
            if (prefsDir.exists()) {
                prefsDir.setExecutable(true, false)
                prefsDir.setReadable(true, false)
            }

            val prefsFile = File(prefsDir, "$PREFS_NAME.xml")
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false)
            }
        } catch (_: Throwable) {}
    }
}
