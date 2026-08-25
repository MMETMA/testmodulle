package com.example.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * High-performance LSPosed/Xposed Hook with dynamic MIUIX configuration.
 * Exclusively hooks com.android.systemui to configure
 * com.miui.systemui.notification.NotificationSettingsManager.USE_WHITE_LISTS.
 */
class MainHook : IXposedHookLoadPackage {

    companion object {
        private const val TARGET_PACKAGE = "com.android.systemui"
        private const val TARGET_CLASS = "com.miui.systemui.notification.NotificationSettingsManager"
        private const val TARGET_FIELD = "USE_WHITE_LISTS"
        private const val MODULE_PACKAGE = "com.aistudio.miuiwhitelist.hook"
        private const val PREFS_NAME = "module_prefs"
    }

    private var xPrefs: XSharedPreferences? = null

    private fun getPreferences(): XSharedPreferences {
        val prefs = xPrefs ?: XSharedPreferences(MODULE_PACKAGE, PREFS_NAME).also {
            it.makeWorldReadable()
            xPrefs = it
        }
        prefs.reload()
        return prefs
    }

    private fun isHookEnabled(): Boolean {
        return try {
            getPreferences().getBoolean("hook_enabled", true)
        } catch (_: Throwable) {
            true
        }
    }

    private fun getTargetValue(): Boolean {
        return try {
            // Default to false (0) as requested for whitelist bypass
            getPreferences().getBoolean("use_white_lists_val", false)
        } catch (_: Throwable) {
            false
        }
    }

    private fun isHookConstructorEnabled(): Boolean {
        return try {
            getPreferences().getBoolean("hook_constructor", true)
        } catch (_: Throwable) {
            true
        }
    }

    private fun isHookMethodsEnabled(): Boolean {
        return try {
            getPreferences().getBoolean("hook_methods", true)
        } catch (_: Throwable) {
            true
        }
    }

    private fun isHookClinitEnabled(): Boolean {
        return try {
            getPreferences().getBoolean("hook_clinit", true)
        } catch (_: Throwable) {
            true
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != TARGET_PACKAGE) {
            return
        }

        if (!isHookEnabled()) {
            return
        }

        try {
            val notifManagerClass = XposedHelpers.findClassIfExists(TARGET_CLASS, lpparam.classLoader)
            if (notifManagerClass != null) {
                applyOverride(notifManagerClass)
            } else {
                XposedHelpers.findAndHookMethod(
                    "android.app.Application",
                    lpparam.classLoader,
                    "onCreate",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val clazz = XposedHelpers.findClassIfExists(TARGET_CLASS, lpparam.classLoader)
                            if (clazz != null) {
                                applyOverride(clazz)
                            }
                        }
                    }
                )
            }
        } catch (_: Throwable) {}
    }

    private fun applyOverride(clazz: Class<*>) {
        try {
            val targetVal = getTargetValue()
            val targetInt = if (targetVal) 1 else 0

            // 1. Direct field assignment
            try {
                XposedHelpers.setStaticBooleanField(clazz, TARGET_FIELD, targetVal)
            } catch (_: Throwable) {
                try {
                    XposedHelpers.setStaticIntField(clazz, TARGET_FIELD, targetInt)
                } catch (_: Throwable) {}
            }

            // 2. Hook constructors if enabled
            if (isHookConstructorEnabled()) {
                XposedBridge.hookAllConstructors(clazz, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val currentVal = getTargetValue()
                            val currentInt = if (currentVal) 1 else 0
                            try {
                                XposedHelpers.setStaticBooleanField(clazz, TARGET_FIELD, currentVal)
                            } catch (_: Throwable) {
                                XposedHelpers.setStaticIntField(clazz, TARGET_FIELD, currentInt)
                            }
                        } catch (_: Throwable) {}
                    }
                })
            }

            // 3. Override query methods if enabled
            if (isHookMethodsEnabled()) {
                val booleanMethods = arrayOf(
                    "canUseWhiteList",
                    "isUseWhiteLists",
                    "isWhiteListEnabled",
                    "useWhiteLists",
                    "isWhiteListNotification"
                )

                for (method in booleanMethods) {
                    try {
                        XposedHelpers.findAndHookMethod(clazz, method, object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                param.result = getTargetValue()
                            }
                        })
                    } catch (_: Throwable) {}
                }
            }

            // 4. Hook static initializer <clinit> if enabled
            if (isHookClinitEnabled()) {
                try {
                    XposedBridge.hookAllMethods(clazz, "<clinit>", object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            try {
                                val currentVal = getTargetValue()
                                val currentInt = if (currentVal) 1 else 0
                                try {
                                    XposedHelpers.setStaticBooleanField(clazz, TARGET_FIELD, currentVal)
                                } catch (_: Throwable) {
                                    XposedHelpers.setStaticIntField(clazz, TARGET_FIELD, currentInt)
                                }
                            } catch (_: Throwable) {}
                        }
                    })
                } catch (_: Throwable) {}
            }

        } catch (_: Throwable) {}
    }
}
