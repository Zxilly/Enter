package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*


object AccessibilityUtil {
    fun isButtonAvailable(context: Context): Boolean {
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

private fun noAction(context: Context) {
    Toast.makeText(context, "No action execute.", Toast.LENGTH_SHORT).show()
}

fun AccessibilityService.shortClick() {
    tryFunctions({ clickEnter() }, { clickKnow() }, { clickNext() }, { noAction(this) })
}

fun AccessibilityService.longClick() {
    tryFunctions({ clickSpell() }, { noAction(this) })
}

private fun tryFunctions(vararg functions: () -> Unit): Boolean {
    for (function in functions) {
        val result = runCatching { function.invoke() }
        if (result.isSuccess) {
            return true
        }
    }
    return false
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
        .setSmallIcon(R.drawable.ic_pen_24px).setContentTitle("Error").setContentText(err.message)
        .setStyle(
            NotificationCompat.BigTextStyle().bigText(err.stackTraceToString())
        ).build()
    notificationManager.notify(Random().nextInt(), notification)
}