package com.example.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * LSPosed / Xposed hook entry point.
 * Exclusively hooks com.android.systemui to force
 * com.miui.systemui.notification.NotificationSettingsManager.USE_WHITE_LISTS to false (0).
 */
class MainHook : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "[MIUI-Notification-Whitelist]"
        private const val TARGET_PACKAGE = "com.android.systemui"
        private const val TARGET_CLASS = "com.miui.systemui.notification.NotificationSettingsManager"
        private const val TARGET_FIELD = "USE_WHITE_LISTS"
        private const val CONFIG_CLASS = "com.miui.utils.configs.MiuiConfigs"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // 1. Self-activation detection hook for our UI app
        if (lpparam.packageName == "com.aistudio.miuiwhitelist.hook" ||
            lpparam.packageName == "com.example"
        ) {
            try {
                val statusClass = XposedHelpers.findClassIfExists("com.example.hook.ModuleStatus", lpparam.classLoader)
                if (statusClass != null) {
                    XposedHelpers.findAndHookMethod(
                        statusClass,
                        "isModuleActive",
                        XC_MethodReplacement.returnConstant(true)
                    )
                    XposedBridge.log("$TAG Self-activation hook active in ${lpparam.packageName}")
                }
            } catch (t: Throwable) {
                XposedBridge.log("$TAG Failed to hook ModuleStatus: ${t.message}")
            }
            return
        }

        // 2. Target ONLY com.android.systemui
        if (lpparam.packageName == TARGET_PACKAGE) {
            hookSystemUiNotificationSettings(lpparam)
        }
    }

    private fun hookSystemUiNotificationSettings(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedBridge.log("$TAG Initializing hook exclusively for package: ${lpparam.packageName}")

            // Hook direct class if currently in class loader
            val notifManagerClass = XposedHelpers.findClassIfExists(TARGET_CLASS, lpparam.classLoader)
            if (notifManagerClass != null) {
                applyNotificationSettingsOverride(notifManagerClass, lpparam.classLoader)
            }

            // Hook Application.onCreate to catch deferred class loading in SystemUI
            try {
                val appClass = XposedHelpers.findClassIfExists("android.app.Application", lpparam.classLoader)
                if (appClass != null) {
                    XposedHelpers.findAndHookMethod(
                        appClass,
                        "onCreate",
                        object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                val lateClass = XposedHelpers.findClassIfExists(TARGET_CLASS, lpparam.classLoader)
                                if (lateClass != null) {
                                    applyNotificationSettingsOverride(lateClass, lpparam.classLoader)
                                }
                            }
                        }
                    )
                }
            } catch (t: Throwable) {
                // Non-critical lifecycle hook
            }

            // Hook MiuiConfigs inspection log
            try {
                val miuiConfigsClass = XposedHelpers.findClassIfExists(CONFIG_CLASS, lpparam.classLoader)
                if (miuiConfigsClass != null) {
                    XposedBridge.log("$TAG Verified presence of $CONFIG_CLASS in ${lpparam.packageName}")
                }
            } catch (t: Throwable) {
                // Non-critical
            }

        } catch (t: Throwable) {
            XposedBridge.log("$TAG Hook error in ${lpparam.packageName}: ${t.message}")
        }
    }

    private fun applyNotificationSettingsOverride(clazz: Class<*>, classLoader: ClassLoader) {
        try {
            // 1. Force the static boolean/int field USE_WHITE_LISTS to false (0)
            try {
                val field = clazz.getDeclaredField(TARGET_FIELD)
                field.isAccessible = true
                XposedHelpers.setStaticBooleanField(clazz, TARGET_FIELD, false)
                XposedBridge.log("$TAG [SUCCESS] Force-set $TARGET_CLASS.$TARGET_FIELD = false (0)")
            } catch (fnf: NoSuchFieldException) {
                XposedBridge.log("$TAG Attempting fallback static field set for $TARGET_FIELD")
                try {
                    XposedHelpers.setStaticBooleanField(clazz, TARGET_FIELD, false)
                } catch (_: Throwable) {
                    try {
                        XposedHelpers.setStaticIntField(clazz, TARGET_FIELD, 0)
                    } catch (_: Throwable) {}
                }
            }

            // 2. Hook all constructors to ensure instance creation maintains USE_WHITE_LISTS = false
            XposedBridge.hookAllConstructors(clazz, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        XposedHelpers.setStaticBooleanField(clazz, TARGET_FIELD, false)
                    } catch (t: Throwable) {
                        try {
                            XposedHelpers.setStaticIntField(clazz, TARGET_FIELD, 0)
                        } catch (_: Throwable) {}
                    }
                }
            })

            // 3. Hook all declared methods in NotificationSettingsManager that query whitelist
            val booleanMethodsToOverride = listOf(
                "canUseWhiteList",
                "isUseWhiteLists",
                "isWhiteListEnabled",
                "useWhiteLists",
                "isWhiteListNotification"
            )

            booleanMethodsToOverride.forEach { methodName ->
                try {
                    XposedHelpers.findAndHookMethod(
                        clazz,
                        methodName,
                        XC_MethodReplacement.returnConstant(false)
                    )
                    XposedBridge.log("$TAG [SUCCESS] Hooked $methodName() -> Forced return false")
                } catch (_: Throwable) {
                    // Method may not exist on this specific ROM build
                }
            }

            clazz.declaredMethods.forEach { method ->
                val name = method.name.lowercase()
                if (name.contains("whitelist") || name.contains("init") || name.contains("update") || name.contains("setting")) {
                    try {
                        XposedBridge.hookMethod(method, object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                try {
                                    XposedHelpers.setStaticBooleanField(clazz, TARGET_FIELD, false)
                                } catch (_: Throwable) {
                                    try {
                                        XposedHelpers.setStaticIntField(clazz, TARGET_FIELD, 0)
                                    } catch (_: Throwable) {}
                                }
                            }
                        })
                    } catch (_: Throwable) {
                    }
                }
            }

            XposedBridge.log("$TAG Overrides and hooks successfully applied to $TARGET_CLASS (USE_WHITE_LISTS = false)")

        } catch (t: Throwable) {
            XposedBridge.log("$TAG Error applying override to $TARGET_CLASS: ${t.message}")
        }
    }
}
