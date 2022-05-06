package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.util.*


object AccessibilityUtil {
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
                .contains(context.packageName.lowercase(Locale.getDefault()))
        }
        return false
    }
}

fun AccessibilityService.clickEnter() {
    rootInActiveWindow?.let { rootWindow ->
        rootWindow.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?.let {
                if (!it.text.isNullOrBlank()){
                    it.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
                    Log.d("AccessibilityService", "apply enter")
                } else {
                    Toast.makeText(this, "empty input", Toast.LENGTH_SHORT).show()
                    Log.d("AccessibilityService", "empty input")
                }
            }
            ?: Log.d(
                "AccessibilityService",
                "Not found should focus input"
            )
    } ?: Log.d("AccessibilityService", "rootInActiveWindow is null")
}

fun buttonAvailable(context: Context): Boolean {
    return AccessibilityUtil.isAccessibilitySettingsOn(context) and Settings.canDrawOverlays(context)
}