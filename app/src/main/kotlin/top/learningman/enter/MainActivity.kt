package top.learningman.enter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import com.samsung.android.sdk.penremote.ButtonEvent
import com.samsung.android.sdk.penremote.SpenRemote
import com.samsung.android.sdk.penremote.SpenUnit
import com.samsung.android.sdk.penremote.SpenUnitManager
import top.learningman.enter.AccessibilityUtil.isAccessibilitySettingsOn
import top.learningman.enter.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCenter.start(
            application, "bdb6695f-b9af-49dd-ae56-2a2bdd4232c8",
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )
        Distribute.setEnabledForDebuggableBuild(true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.grant.setOnClickListener {
            // grant overlay permission
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        binding.accessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Please enable accessibility", Toast.LENGTH_SHORT).show()
        }

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

    override fun onResume() {
        super.onResume()
        if (!Settings.canDrawOverlays(this)) {
            binding.waitForPermission.visibility = View.VISIBLE
            binding.waitForAccessibility.visibility = View.GONE
            binding.waitForStartup.visibility = View.GONE
        } else if (!isAccessibilitySettingsOn(this)) {
            binding.waitForAccessibility.visibility = View.VISIBLE
            binding.waitForPermission.visibility = View.GONE
            binding.waitForStartup.visibility = View.GONE
        } else {
            binding.waitForStartup.visibility = View.VISIBLE
            binding.waitForAccessibility.visibility = View.GONE
            binding.waitForPermission.visibility = View.GONE

            try {
                with(SpenRemote.getInstance()) {
                    if (isFeatureEnabled(SpenRemote.FEATURE_TYPE_BUTTON)) {
                        if (!isConnected) {
                            connect(
                                this@MainActivity,
                                object : SpenRemote.ConnectionResultCallback {
                                    override fun onSuccess(manager: SpenUnitManager) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "SPen Checked. Advanced feature enabled.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        val button: SpenUnit = manager.getUnit(SpenUnit.TYPE_BUTTON)
                                        manager.registerSpenEventListener({ event ->
                                            val buttonEvent = ButtonEvent(event)
                                            when (buttonEvent.action) {
                                                ButtonEvent.ACTION_UP -> {
                                                    startService(
                                                        Intent(
                                                            this@MainActivity,
                                                            ButtonAccessibilityService::class.java
                                                        ).apply {
                                                            putExtra(
                                                                ButtonAccessibilityService.TYPE_KEY,
                                                                ButtonAccessibilityService.PRESS_ENTER
                                                            )
                                                        })
                                                }
                                            }
                                        }, button)
                                    }

                                    override fun onFailure(code: Int) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Failed to connect to S Pen.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is SecurityException) {
                    Toast.makeText(
                        this,
                        "Failed to connect to S Pen. System error.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Crashes.trackError(e);
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}