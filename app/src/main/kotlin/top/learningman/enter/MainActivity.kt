package top.learningman.enter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import top.learningman.enter.AccessibilityUtil.isAccessibilitySettingsOn
import top.learningman.enter.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCenter.start(
            application, "bdb6695f-b9af-49dd-ae56-2a2bdd4232c8",
            Analytics::class.java, Crashes::class.java
        )

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