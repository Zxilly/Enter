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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import top.learningman.enter.utils.ButtonBroadcast
import top.learningman.enter.view.ButtonWindowManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.enterButton.setOnClickListener {
            val action = if (ButtonWindowManager.isShowing()) {
                ButtonAccessibilityService.REMOVE_VIEW
            } else {
                ButtonAccessibilityService.ADD_VIEW
            }
            ButtonAccessibilityService.triggerAction(this, action)
        }

        binding.spenButton.setOnClickListener {
            Log.d("Spen Button", "SPen button clicked")
            val sp = SpenRemote.getInstance()
            if (!sp.isConnected) {
                sp.connect(
                    this@MainActivity, sPenCallback
                )
                Toast.makeText(
                    this@MainActivity,
                    "Connecting to S Pen.",
                    Toast.LENGTH_LONG
                ).show()
                showSPenNotification()
                enableSPenButton()
            } else {
                sp.disconnect(this@MainActivity)
                Toast.makeText(
                    this@MainActivity,
                    "Disconnecting from S Pen.",
                    Toast.LENGTH_LONG
                ).show()
                hideSPenNotification()
                disableSPenButton()
            }
        }

        if (!isSPenAvailable()) {
            binding.spenButton.visibility = View.GONE
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

    private fun isSPenAvailable(): Boolean {
        if (!isSamsung()) return false
        val sp = SpenRemote.getInstance()
        if (!sp.isFeatureEnabled(SpenRemote.FEATURE_TYPE_BUTTON)) {
            return false
        }
        return true
    }

    private val buttonReceiver = object : ButtonBroadcast.Receiver() {
        override fun onEnableEnterButton() {
            enableEnterButton()
        }
        override fun onDisableEnterButton() {
            disableEnterButton()
        }
        override fun onEnableSpenBinding() {
            enableSPenButton()
        }
        override fun onDisableSpenBinding() {
            disableSPenButton()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ButtonWindowManager.isShowing()) {
            enableEnterButton()
        } else {
            disableEnterButton()
        }
        val sp = SpenRemote.getInstance()
        if (sp.isConnected) {
            enableSPenButton()
        } else {
            disableSPenButton()
        }

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(buttonReceiver, ButtonBroadcast.filter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(buttonReceiver)
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
            applicationContext, 0, notifyIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
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

    private fun enableEnterButton() {
        binding.enterButton.isChecked = true
    }

    private fun disableEnterButton() {
        binding.enterButton.isChecked = false
    }

    private fun enableSPenButton() {
        binding.spenButton.isChecked = true
    }

    private fun disableSPenButton() {
        binding.spenButton.isChecked = false
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

    companion object {
        private fun isSamsung(): Boolean {
            return Build.MANUFACTURER.lowercase() == "samsung"
        }
    }
}