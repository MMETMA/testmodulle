package com.example.hook

object ModuleStatus {
    /**
     * This method is hooked by MainHook in handleLoadPackage
     * to return true when the module is actively loaded by LSPosed/Xposed.
     */
    @JvmStatic
    fun isModuleActive(): Boolean {
        return false
    }

    const val MODULE_VERSION = "1.0.0"
    const val TARGET_PACKAGE = "com.android.systemui"
    const val TARGET_CLASS = "com.miui.systemui.notification.NotificationSettingsManager"
    const val TARGET_FIELD = "USE_WHITE_LISTS"
}
