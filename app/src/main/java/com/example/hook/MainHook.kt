package com.example.hook

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * High-performance, zero-logging LSPosed/Xposed Hook.
 * Exclusively hooks com.android.systemui to force
 * com.miui.systemui.notification.NotificationSettingsManager.USE_WHITE_LISTS to false (0).
 */
class MainHook : IXposedHookLoadPackage {

    companion object {
        private const val TARGET_PACKAGE = "com.android.systemui"
        private const val TARGET_CLASS = "com.miui.systemui.notification.NotificationSettingsManager"
        private const val TARGET_FIELD = "USE_WHITE_LISTS"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != TARGET_PACKAGE) {
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
            // 1. Set static boolean / int field USE_WHITE_LISTS = false (0)
            try {
                XposedHelpers.setStaticBooleanField(clazz, TARGET_FIELD, false)
            } catch (_: Throwable) {
                try {
                    XposedHelpers.setStaticIntField(clazz, TARGET_FIELD, 0)
                } catch (_: Throwable) {}
            }

            // 2. Hook constructors to maintain USE_WHITE_LISTS = false
            XposedBridge.hookAllConstructors(clazz, object : XC_MethodHook() {
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

            // 3. Override query methods
            val booleanMethods = arrayOf(
                "canUseWhiteList",
                "isUseWhiteLists",
                "isWhiteListEnabled",
                "useWhiteLists",
                "isWhiteListNotification"
            )

            for (method in booleanMethods) {
                try {
                    XposedHelpers.findAndHookMethod(clazz, method, XC_MethodReplacement.returnConstant(false))
                } catch (_: Throwable) {}
            }

            // 4. Hook static initializer <clinit>
            try {
                XposedBridge.hookAllMethods(clazz, "<clinit>", object : XC_MethodHook() {
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
            } catch (_: Throwable) {}

        } catch (_: Throwable) {}
    }
}
