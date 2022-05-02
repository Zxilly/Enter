package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings

import android.view.accessibility.AccessibilityEvent
import android.widget.Toast


class ButtonAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val type = intent?.extras?.getInt(TYPE_KEY)
        if (type != null) {
            when (type) {
                ADD_VIEW -> showButton()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showButton() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant overlay permission.", Toast.LENGTH_LONG).show()
        }

        ButtonWindowManager.addView(this)
    }

    private fun hideButton() {
        ButtonWindowManager.removeView(this)
    }

    companion object {
        const val ADD_VIEW = 0
        const val REMOVE_VIEW = 1

        const val TYPE_KEY = "type"
    }
}