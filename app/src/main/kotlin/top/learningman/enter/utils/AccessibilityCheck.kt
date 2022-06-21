package top.learningman.enter.utils

import android.content.Context
import android.provider.Settings
import android.widget.Toast
import java.util.*

object AccessibilityCheck {
    fun isFloatingButtonAvailable(context: Context): Boolean {
        return isAccessibilitySettingsOn(context) and Settings.canDrawOverlays(
            context
        )
    }

    fun isAccessibilitySettingsOn(context: Context): Boolean {
        var accessibilityEnabled = 0
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, "A18y not supported.", Toast.LENGTH_SHORT).show()
        }
        if (accessibilityEnabled == 1) {
            val services: String = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return services.lowercase(Locale.getDefault())
                .contains(context.packageName.lowercase(Locale.getDefault()))
        }
        return false
    }
}