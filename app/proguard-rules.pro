# Keep Xposed entry point
-keep class com.example.hook.MainHook { *; }
-keep class com.example.MainActivity { *; }
-keepattributes *Annotation*
-dontwarn de.robv.android.xposed.**

# Strip all logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

-assumenosideeffects class de.robv.android.xposed.XposedBridge {
    public static void log(java.lang.String);
    public static void log(java.lang.Throwable);
}
