package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.microsoft.appcenter.analytics.Analytics
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
                if (!it.text.isNullOrBlank()) {
                    it.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
                    Log.d("AccessibilityService", "apply enter")
                } else {
                    Toast.makeText(this, "empty input", Toast.LENGTH_SHORT).show()
                    Log.d("AccessibilityService", "empty input")
                }
                it.recycle()
                rootWindow.recycle()
            }
            ?: Log.d(
                "AccessibilityService",
                "Not found should focus input"
            )
    } ?: Log.d("AccessibilityService", "rootInActiveWindow is null")
}

fun AccessibilityService.clickVoice() {
    rootInActiveWindow?.let { rootWindow ->
        val nodes = rootWindow.findAccessibilityNodeInfosByViewId(
            "cn.com.langeasy.LangEasyLexis:id/iv_spell_prompt"
        )
        if (nodes.isNotEmpty()) {
            nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("AccessibilityService", "click voice")
            if (nodes.size > 1) {
                Log.e("AccessibilityService", "more than one voice")
                Analytics.trackEvent("more than one voice")
            }
        } else {
            Log.d("AccessibilityService", "not found voice")
            Toast.makeText(this, "Not found voice button", Toast.LENGTH_SHORT).show()
        }
        nodes.forEach { it.recycle() }
        rootWindow.recycle()
    } ?: Log.d("AccessibilityService", "rootInActiveWindow is null")
}

fun isButtonAvailable(context: Context): Boolean {
    return AccessibilityUtil.isAccessibilitySettingsOn(context) and Settings.canDrawOverlays(context)
}

fun showErrorNotification(context: Context, err: Throwable) {
    fun createNotificationChannel() {
        val name = "Error Notification"
        val descriptionText = "Error Notification"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel =
            NotificationChannel(ButtonAccessibilityService.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    createNotificationChannel()
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(context, ButtonAccessibilityService.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_pen_24px)
        .setContentTitle("Error")
        .setContentText(err.message)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(err.stackTraceToString())
        )
        .build()
    notificationManager.notify(Random().nextInt(), notification)
}