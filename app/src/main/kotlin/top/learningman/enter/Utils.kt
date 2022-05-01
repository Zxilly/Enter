package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import java.util.*


object AccessibilityUtil {
    fun sendEnter(service: AccessibilityService) {
        // get input text

    }

    fun isAccessibilitySettingsOn(context: Context): Boolean {
        var accessibilityEnabled = 0
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        if (accessibilityEnabled == 1) {
            val services: String = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return services.lowercase(Locale.getDefault())
                .contains(context.packageName.lowercase(Locale.ROOT))
        }
        return false
    }
}