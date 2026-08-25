package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SmaliCodeCard
import com.example.ui.components.StatusHeader
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSlateBg
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberTertiary

private const val ORIGINAL_SMALI = """.method static constructor <clinit>()V
    .registers 1

    .line 1
    sget-boolean v0, Lcom/miui/utils/configs/MiuiDebugConfig;->DEBUG_NOTIFICATION:Z

    .line 3
    sput-boolean v0, Lcom/miui/systemui/notification/NotificationSettingsManager;->DEBUG:Z

    .line 5
    sget-boolean v0, Lcom/miui/utils/configs/MiuiConfigs;->IS_INTERNATIONAL_BUILD:Z

    .line 7
    xor-int/lit8 v0, v0, 0x1

    .line 9
    sput-boolean v0, Lcom/miui/systemui/notification/NotificationSettingsManager;->USE_WHITE_LISTS:Z

    .line 11
    return-void
.end method"""

private const val PATCHED_SMALI = """.method static constructor <clinit>()V
    .registers 1

    .line 1
    sget-boolean v0, Lcom/miui/utils/configs/MiuiDebugConfig;->DEBUG_NOTIFICATION:Z

    .line 3
    sput-boolean v0, Lcom/miui/systemui/notification/NotificationSettingsManager;->DEBUG:Z

    # [HOOK OVERRIDE]: Forced USE_WHITE_LISTS to always be 1 (true)
    const/4 v0, 0x1

    sput-boolean v0, Lcom/miui/systemui/notification/NotificationSettingsManager;->USE_WHITE_LISTS:Z

    return-void
.end method"""

private const val XPOSED_KOTLIN_HOOK = """// LSPosed / Xposed Hook Implementation
XposedHelpers.findAndHookMethod(
    "android.app.Application",
    lpparam.classLoader,
    "onCreate",
    object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val clazz = XposedHelpers.findClass(
                "com.miui.systemui.notification.NotificationSettingsManager",
                lpparam.classLoader
            )
            // Force USE_WHITE_LISTS = true (1)
            XposedHelpers.setStaticBooleanField(clazz, "USE_WHITE_LISTS", true)
        }
    }
)"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val tabs = listOf(
        TabItem("Overview", Icons.Default.Dashboard),
        TabItem("Smali & Hook", Icons.Default.Code),
        TabItem("Guide", Icons.Default.Layers),
        TabItem("Diagnostics", Icons.Default.BugReport)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberSlateBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(CyberPrimary, CyberSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MIUI Whitelist Enabler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "LSPosed Module • Notification Hook",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberPrimary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.refreshStatus()
                            Toast.makeText(context, "Status refreshed", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CyberPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSlateBg
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = CyberSlateBg,
                contentColor = CyberPrimary,
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CyberBorder)
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.setTab(index) },
                        modifier = Modifier.testTag("tab_$index"),
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                0 -> OverviewTab(uiState = uiState, viewModel = viewModel)
                1 -> SmaliCodeTab()
                2 -> SetupGuideTab(uiState = uiState)
                3 -> DiagnosticsTab(uiState = uiState)
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)

@Composable
fun OverviewTab(
    uiState: MainUiState,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Card
        item {
            StatusHeader(isModuleActive = uiState.isModuleActive)
        }

        // Action Quick Restart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = CyberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Restart SystemUI (Root Helper)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                val cmd = "su -c 'pkill -f com.android.systemui'"
                                clipboardManager.setText(AnnotatedString(cmd))
                                Toast.makeText(context, "Copied shell command to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy command",
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy Command",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "After enabling the module in LSPosed Manager, you can soft-reboot or kill the SystemUI process to immediately apply the hook.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070A12))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "pkill -f com.android.systemui",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = CyberPrimary
                        )
                    }
                }
            }
        }

        // Toggles Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hook Controls & Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleItem(
                        title = "Force USE_WHITE_LISTS = 1",
                        description = "Enforces NotificationSettingsManager.USE_WHITE_LISTS = true upon SystemUI startup",
                        checked = uiState.preferences.isHookEnabled,
                        onCheckedChange = { viewModel.toggleHook(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleItem(
                        title = "Aggressive Multi-Point Enforcement",
                        description = "Re-applies whitelist = 1 during constructor calls and notification update events",
                        checked = uiState.preferences.isAggressiveMode,
                        onCheckedChange = { viewModel.toggleAggressiveMode(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleItem(
                        title = "Hook on Constructor (<init>)",
                        description = "Hooks all constructor instances of NotificationSettingsManager",
                        checked = uiState.preferences.hookOnConstructor,
                        onCheckedChange = { viewModel.toggleHookOnConstructor(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleItem(
                        title = "Verbose Xposed Logcat",
                        description = "Outputs detailed hook traces and confirmation logs to LSPosed/logcat",
                        checked = uiState.preferences.isVerboseLogging,
                        onCheckedChange = { viewModel.toggleVerboseLogging(it) }
                    )
                }
            }
        }

        // Target Scope Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Configured Hook Scope",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ScopeItem(name = "com.android.systemui", desc = "Primary SystemUI Process (Notification Center & Status Bar)", isPrimary = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    ScopeItem(name = "com.miui.systemui", desc = "Xiaomi MIUI SystemUI Plugin Framework", isPrimary = false)
                    Spacer(modifier = Modifier.height(6.dp))
                    ScopeItem(name = "android (system_server)", desc = "Android System Framework & Notification Manager Service", isPrimary = false)
                }
            }
        }
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CyberCardSurface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CyberSuccess,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = Color(0xFF1E2638)
            )
        )
    }
}

@Composable
fun ScopeItem(name: String, desc: String, isPrimary: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070A12))
            .border(1.dp, if (isPrimary) CyberPrimary.copy(alpha = 0.3f) else Color(0xFF1E2638), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isPrimary) CyberSuccess else CyberPrimary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = name,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) CyberPrimary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SmaliCodeTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Smali Disassembly Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "In MIUI/HyperOS, NotificationSettingsManager.<clinit>() sets USE_WHITE_LISTS by inverting IS_INTERNATIONAL_BUILD (xor-int/lit8 v0, v0, 0x1). On International builds, this turns off whitelist management. This module hooks the class initialization to force USE_WHITE_LISTS = 1 (true) consistently.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            SmaliCodeCard(
                title = "Original Smali (<clinit>()V)",
                subtitle = "Target method in NotificationSettingsManager",
                code = ORIGINAL_SMALI,
                isPatched = false
            )
        }

        item {
            SmaliCodeCard(
                title = "Patched Logic (USE_WHITE_LISTS = 1)",
                subtitle = "Target bytecode behavior injected by Hook",
                code = PATCHED_SMALI,
                isPatched = true
            )
        }

        item {
            SmaliCodeCard(
                title = "Xposed Hook Code (Java / Kotlin)",
                subtitle = "Executed at runtime via LSPosed framework",
                code = XPOSED_KOTLIN_HOOK,
                isPatched = true
            )
        }
    }
}

@Composable
fun SetupGuideTab(uiState: MainUiState) {
    val steps = listOf(
        GuideStep(
            number = 1,
            title = "Install LSPosed / Zygisk",
            description = "Ensure your device is rooted with Magisk, KernelSU, or APatch, and that the LSPosed (Zygisk) framework module is active.",
            icon = Icons.Default.Shield
        ),
        GuideStep(
            number = 2,
            title = "Enable MIUI Whitelist Enabler",
            description = "Open LSPosed Manager app -> Go to 'Modules' -> Locate 'MIUI Whitelist Enabler' and turn the switch ON.",
            icon = Icons.Default.ToggleOn
        ),
        GuideStep(
            number = 3,
            title = "Check Target Scope",
            description = "Ensure 'System Framework' (android) and 'System UI' (com.android.systemui) are selected in the module scope checklist.",
            icon = Icons.Default.Layers
        ),
        GuideStep(
            number = 4,
            title = "Restart SystemUI",
            description = "Reboot your device or perform a soft restart of the SystemUI process to load the hook into the notification engine.",
            icon = Icons.Default.Refresh
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = CyberPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "LSPosed Quick Setup Guide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Follow these steps to activate the module on MIUI / HyperOS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(steps) { step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyberSecondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.number}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

data class GuideStep(val number: Int, val title: String, val description: String, val icon: ImageVector)

@Composable
fun DiagnosticsTab(uiState: MainUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Device telemetry
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Device & ROM Environment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TelemetryRow("Device Model", "${uiState.deviceInfo.manufacturer} ${uiState.deviceInfo.model}")
                    TelemetryRow("Brand", uiState.deviceInfo.brand)
                    TelemetryRow("ROM Flavor", uiState.deviceInfo.miuiVersionName)
                    TelemetryRow("Android Version", "Android ${uiState.deviceInfo.androidVersion} (API ${uiState.deviceInfo.sdkInt})")
                    TelemetryRow("Security Patch", uiState.deviceInfo.securityPatch)
                    TelemetryRow(
                        "LSPosed Bridge",
                        if (uiState.isModuleActive) "Connected (Active)" else "Awaiting Framework Hook"
                    )
                }
            }
        }

        // Live Log Output
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hook Initialization Logs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyberPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "LOGCAT",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF070A12))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.hookLogs.forEach { log ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = log.timestamp,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "[${log.level}]",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (log.level) {
                                            "SUCCESS" -> CyberSuccess
                                            "INFO" -> CyberPrimary
                                            else -> CyberTertiary
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.message,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
