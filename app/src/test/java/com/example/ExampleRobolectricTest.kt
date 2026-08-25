package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.hook.ModuleStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MIUI Whitelist Enabler", appName)
    }

    @Test
    fun `verify module status targets`() {
        assertEquals("com.android.systemui", ModuleStatus.TARGET_PACKAGE)
        assertEquals("com.miui.systemui.notification.NotificationSettingsManager", ModuleStatus.TARGET_CLASS)
        assertEquals("USE_WHITE_LISTS", ModuleStatus.TARGET_FIELD)
        assertNotNull(ModuleStatus.MODULE_VERSION)
    }
}
