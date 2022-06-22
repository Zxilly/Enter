package top.learningman.enter.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.microsoft.appcenter.crashes.Crashes
import com.samsung.android.sdk.penremote.ButtonEvent
import com.samsung.android.sdk.penremote.SpenRemote
import com.samsung.android.sdk.penremote.SpenUnit
import com.samsung.android.sdk.penremote.SpenUnitManager
import top.learningman.enter.Config
import top.learningman.enter.R
import top.learningman.enter.databinding.ActivityMainBinding
import top.learningman.enter.services.ButtonAccessibilityService
import top.learningman.enter.showErrorNotification
import top.learningman.enter.view.ButtonWindowManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.toggle.setOnClickListener {
            val intent = Intent(this, ButtonAccessibilityService::class.java)
            intent.putExtra(
                ButtonAccessibilityService.TYPE_KEY,
                if (ButtonWindowManager.isShowing()) {
                    ButtonAccessibilityService.REMOVE_VIEW
                } else {
                    ButtonAccessibilityService.ADD_VIEW
                }
            )
            startService(intent)
        }
    }

    private val sPenCallback = object :
        SpenRemote.ConnectionResultCallback {
        override fun onSuccess(manager: SpenUnitManager) {
            try {
                manager.registerSpenEventListener(
                    { event ->
                        when (ButtonEvent(event).action) {
                            ButtonEvent.ACTION_DOWN -> {
                                mClickController.actionDown()
                            }
                            ButtonEvent.ACTION_UP -> {
                                mClickController.actionUp()
                            }
                        }
                    }, manager.getUnit(SpenUnit.TYPE_BUTTON)
                )
            } catch (e: Exception) {
                if (e is SecurityException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to connect to S Pen. System error.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Crashes.trackError(e)
                Log.e(
                    "SPen",
                    "Failed to connect to S Pen. System error.",
                    e
                )
                showErrorNotification(this@MainActivity, e)
            }
        }

        override fun onFailure(code: Int) {
            Toast.makeText(
                this@MainActivity,
                "Failed to connect to S Pen.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.MANUFACTURER.lowercase() == "samsung"){
            with(SpenRemote.getInstance()) {
                if (isFeatureEnabled(SpenRemote.FEATURE_TYPE_BUTTON)) {
                    binding.spen.visibility = View.VISIBLE
                    binding.spenTip.visibility = View.VISIBLE

                    fun spenStatusText(isConnect: Boolean) {
                        binding.spenTip.text = if (isConnect) {
                            "S Pen connected"
                        } else {
                            "S Pen disconnected"
                        }
                    }
                    spenStatusText(isConnected)
                    binding.spen.setOnClickListener {
                        if (!isConnected) {
                            connect(
                                this@MainActivity, sPenCallback
                            )
                            spenStatusText(true)
                            Toast.makeText(
                                this@MainActivity,
                                "Connecting to S Pen.",
                                Toast.LENGTH_LONG
                            ).show()
                            showSPenNotification()
                        } else {
                            disconnect(this@MainActivity)
                            spenStatusText(false)
                            Toast.makeText(
                                this@MainActivity,
                                "Disconnecting from S Pen.",
                                Toast.LENGTH_LONG
                            ).show()
                            hideSPenNotification()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        with(SpenRemote.getInstance()) {
            if (isConnected) {
                disconnect(this@MainActivity)
                Toast.makeText(
                    this@MainActivity,
                    "Disconnecting from S Pen.",
                    Toast.LENGTH_LONG
                ).show()
                hideSPenNotification()
            }
        }
    }

    private fun showSPenNotification() {
        val channelID = "SpenNotification"
        fun createNotificationChannel() {
            val name = "SPen Notification"
            val descriptionText = "SPen Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        createNotificationChannel()
        val notifyIntent = Intent(this, MainActivity::class.java)
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.ic_pen_24px)
            .setContentTitle("Enter")
            .setContentText("SPen connected.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(notifyPendingIntent)
        with(NotificationManagerCompat.from(this)) {
            notify(2, builder.build())
        }
    }

    private fun hideSPenNotification() {
        with(NotificationManagerCompat.from(this)) {
            cancel(2)
        }
    }

    private val mClickController = object {
        private var mLastDown = 0L

        fun actionDown() {
            mLastDown = System.currentTimeMillis()
        }

        fun actionUp() {
            val duration = System.currentTimeMillis() - mLastDown
            Log.d("MainActivity", "Clicked for $duration ms")
            if (duration < Config.CLICK_LIMIT) {
                Log.d("MainActivity", "Pen Clicked")
                ButtonAccessibilityService.triggerAction(
                    this@MainActivity,
                    ButtonAccessibilityService.PRESS_ENTER
                )
            } else {
                Log.d("MainActivity", "Pen Long Clicked")
                ButtonAccessibilityService.triggerAction(
                    this@MainActivity,
                    ButtonAccessibilityService.PRESS_VOICE
                )
            }
        }
    }
}