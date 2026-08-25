package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrefManager
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val prefs = remember { PrefManager.getPrefs(context) }

    // States initialized from SharedPreferences
    var isHookEnabled by remember {
        mutableStateOf(prefs.getBoolean(PrefManager.KEY_HOOK_ENABLED, true))
    }
    // false = USE_WHITE_LISTS is 0 (false), true = USE_WHITE_LISTS is 1 (true)
    var useWhiteListsVal by remember {
        mutableStateOf(prefs.getBoolean(PrefManager.KEY_USE_WHITE_LISTS_VALUE, false))
    }
    var hookConstructor by remember {
        mutableStateOf(prefs.getBoolean(PrefManager.KEY_HOOK_CONSTRUCTOR, true))
    }
    var hookMethods by remember {
        mutableStateOf(prefs.getBoolean(PrefManager.KEY_HOOK_METHODS, true))
    }
    var hookClinit by remember {
        mutableStateOf(prefs.getBoolean(PrefManager.KEY_HOOK_CLINIT, true))
    }

    fun savePref(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
        PrefManager.makePrefsReadable(context)
    }

    MiuixTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = "MIUI Whitelist"
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Header Card
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isHookEnabled) Color(0xFF34C759).copy(alpha = 0.15f)
                                        else Color(0xFFFF9500).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isHookEnabled) Icons.Default.CheckCircle else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isHookEnabled) Color(0xFF34C759) else Color(0xFFFF9500),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isHookEnabled) "Hook Active & Enforcing" else "Hook Paused",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isHookEnabled)
                                        "Targeting com.android.systemui"
                                    else
                                        "Override is currently disabled in settings",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Current Injected Value Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2C2C2E).copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Target Value:",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = if (!isHookEnabled) "BYPASSED (Disabled)"
                                    else if (!useWhiteListsVal) "USE_WHITE_LISTS = 0 (false)"
                                    else "USE_WHITE_LISTS = 1 (true)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (!isHookEnabled) Color.Gray
                                    else if (!useWhiteListsVal) Color(0xFF007AFF)
                                    else Color(0xFF34C759)
                                )
                            }
                        }
                    }
                }

                // Core Whitelist Feature Switch Section
                SmallTitle(text = "NOTIFICATION WHITELIST SWITCH")

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Master Hook Switch
                    SuperSwitch(
                        title = "Enable Whitelist Hook",
                        summary = "Master toggle to enable or bypass the LSPosed hook in SystemUI",
                        checked = isHookEnabled,
                        onCheckedChange = {
                            isHookEnabled = it
                            savePref(PrefManager.KEY_HOOK_ENABLED, it)
                            Toast.makeText(
                                context,
                                if (it) "Hook enabled" else "Hook disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    // The requested USE_WHITE_LISTS Feature Switch
                    SuperSwitch(
                        title = "Force USE_WHITE_LISTS = false (0)",
                        summary = if (!useWhiteListsVal)
                            "Forcing USE_WHITE_LISTS = 0 (Disables MIUI Whitelist restrictions)"
                        else
                            "Forcing USE_WHITE_LISTS = 1 (Enables MIUI Whitelist restrictions)",
                        checked = !useWhiteListsVal,
                        onCheckedChange = { checked ->
                            val newVal = !checked // checked = force false (0)
                            useWhiteListsVal = newVal
                            savePref(PrefManager.KEY_USE_WHITE_LISTS_VALUE, newVal)
                            Toast.makeText(
                                context,
                                if (!newVal) "Set USE_WHITE_LISTS = 0 (false)" else "Set USE_WHITE_LISTS = 1 (true)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                // Advanced Hook Configurations
                SmallTitle(text = "HOOK INJECTION POINTS")

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SuperSwitch(
                        title = "Static Initializer (<clinit>)",
                        summary = "Overrides sput-boolean in NotificationSettingsManager.<clinit>()",
                        checked = hookClinit,
                        onCheckedChange = {
                            hookClinit = it
                            savePref(PrefManager.KEY_HOOK_CLINIT, it)
                        }
                    )

                    SuperSwitch(
                        title = "Constructor Hook (<init>)",
                        summary = "Re-applies USE_WHITE_LISTS value during every instance construction",
                        checked = hookConstructor,
                        onCheckedChange = {
                            hookConstructor = it
                            savePref(PrefManager.KEY_HOOK_CONSTRUCTOR, it)
                        }
                    )

                    SuperSwitch(
                        title = "Method Return Overrides",
                        summary = "Intercepts canUseWhiteList(), isUseWhiteLists(), isWhiteListEnabled()",
                        checked = hookMethods,
                        onCheckedChange = {
                            hookMethods = it
                            savePref(PrefManager.KEY_HOOK_METHODS, it)
                        }
                    )
                }

                // Smali Bytecode Preview Card
                SmallTitle(text = "BYTECODE PREVIEW")

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NotificationSettingsManager.<clinit>()",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E1E1E))
                                .padding(12.dp)
                        ) {
                            val targetHex = if (!useWhiteListsVal) "0x0" else "0x1"
                            val comment = if (!useWhiteListsVal) "# Forced to false (0)" else "# Forced to true (1)"

                            Text(
                                text = """.method static constructor <clinit>()V
    .registers 2

    # [XPOSED HOOK OVERRIDE] $comment
    const/4 v0, $targetHex

    sput-boolean v0, Lcom/miui/systemui/notification/NotificationSettingsManager;->USE_WHITE_LISTS:Z

    return-void
.end method""",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                color = Color(0xFF4AF626),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Scope Information & Actions
                SmallTitle(text = "MODULE INFORMATION")

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SuperArrow(
                        title = "Target Scope",
                        summary = "com.android.systemui",
                        onClick = {
                            Toast.makeText(context, "Scope is locked to com.android.systemui", Toast.LENGTH_SHORT).show()
                        }
                    )

                    SuperArrow(
                        title = "Restart SystemUI",
                        summary = "Tap to attempt SystemUI restart to apply changes immediately",
                        onClick = {
                            try {
                                Runtime.getRuntime().exec(arrayOf("su", "-c", "pkill -f com.android.systemui"))
                                Toast.makeText(context, "Restarting SystemUI...", Toast.LENGTH_SHORT).show()
                            } catch (_: Throwable) {
                                Toast.makeText(context, "Root required to auto-kill SystemUI. Please reboot or restart via Magisk/KernelSU.", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
