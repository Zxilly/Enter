package top.learningman.enter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationCompat
import top.learningman.enter.services.ButtonAccessibilityService
import java.util.*


fun noAction(context: Context) {
    Toast.makeText(context, "No action execute.", Toast.LENGTH_SHORT).show()
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