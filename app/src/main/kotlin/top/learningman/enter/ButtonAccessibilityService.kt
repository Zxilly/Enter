package top.learningman.enter

import android.R.attr
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import top.learningman.enter.databinding.ButtonBinding


class ButtonAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        id:
//        cn.com.langeasy.LangEasyLexis:id/et_spell_word (preview)
//        cn.com.langeasy.LangEasyLexis:id/et_spell_input (final)
//        rootInActiveWindow?.let {
//
//        }?: Log.d("ButtonAccessibilityService", "rootInActiveWindow is null")

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

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val screenWidth = windowManager.defaultDisplay.width
        val screenHeight = windowManager.defaultDisplay.height


        val layoutInflater = LayoutInflater.from(this)
        val view = ButtonBinding.inflate(layoutInflater, null, false).apply {
            button.setOnClickListener {

            }
        }.root
        val layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            x = screenWidth
            y = screenHeight / 2
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.LEFT or Gravity.TOP
            format = PixelFormat.RGBA_8888;
        }
        view.layoutParams = layoutParams
        windowManager.addView(view, layoutParams)
    }

    companion object {
        const val ADD_VIEW = 0
        const val TYPE_KEY = "type"
    }
}