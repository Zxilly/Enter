package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.microsoft.appcenter.analytics.Analytics


class ButtonAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val type = intent?.extras?.getInt(TYPE_KEY)
        if (type != null) {
            when (type) {
                ADD_VIEW -> {
                    Analytics.trackEvent("Show Button")
                    showButton()
                }
                REMOVE_VIEW -> {
                    Analytics.trackEvent("Hide Button")
                    hideButton()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        val name = "ButtonAccessibilityService"
        val descriptionText = "ButtonAccessibilityService Notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showButton() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant overlay permission.", Toast.LENGTH_LONG).show()
        }

        if (ButtonWindowManager.isShowing()) {
            return
        }

        ButtonWindowManager.addView(this)

        createNotificationChannel()
        val intent = Intent(this, ButtonAccessibilityService::class.java).apply {
            putExtra(TYPE_KEY, REMOVE_VIEW)
        }
        val pIntent = PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pen_24px)
            .setContentTitle("Enter")
            .setContentText("Enter is showing.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(R.drawable.ic_pen_24px, "Hide", pIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    private fun hideButton() {
        if (!ButtonWindowManager.isShowing()) {
            return
        }

        ButtonWindowManager.removeView(this)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

    companion object {
        const val ADD_VIEW = 0
        const val REMOVE_VIEW = 1

        const val TYPE_KEY = "type"

        const val CHANNEL_ID = "ButtonAccessibilityService"
    }
}