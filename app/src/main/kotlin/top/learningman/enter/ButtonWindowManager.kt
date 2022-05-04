package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import android.widget.Toast
import top.learningman.enter.databinding.ButtonBinding


object ButtonWindowManager {
    private var mView: ButtonView? = null
    private var mWindowManager: WindowManager? = null

    fun isShowing(): Boolean {
        return mView != null
    }

    fun addView(service: AccessibilityService) {
        if (mView != null) {
            return
        }
        val wm = getWindowManager(service)

        val bound = wm.currentWindowMetrics.bounds
        val screenWidth = bound.width()
        val screenHeight = bound.height()

        service.setTheme(R.style.Theme_BackSpace)
        val layoutInflater = LayoutInflater.from(service)
        mView = ButtonBinding.inflate(layoutInflater, null, false).apply {
            button.setOnClickListener {
                service.rootInActiveWindow?.let { rootWindow ->
                    rootWindow.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                        ?.let {
                            if (!it.text.isNullOrBlank()){
                                it.performAction(AccessibilityAction.ACTION_IME_ENTER.id)
                                Log.d("ButtonWindowManager", "apply enter")
                            } else {
                                Toast.makeText(service, "empty input", Toast.LENGTH_SHORT).show()
                                Log.d("ButtonWindowManager", "empty input")
                            }
                        }
                        ?: Log.d(
                            "ButtonWindowManager",
                            "Not found should focus input"
                        )
                } ?: Log.d("ButtonWindowManager", "rootInActiveWindow is null")
            }
        }.root

        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            x = screenWidth / 2
            y = screenHeight / 2
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.LEFT or Gravity.TOP
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        mView!!.setWindowLayoutParams(lp)
        wm.addView(mView, lp)
    }

    fun removeView(context: Context) {
        if (mView == null) {
            return
        }
        val wm = getWindowManager(context)
        wm.removeView(mView)
        mView = null
    }

    private fun getWindowManager(context: Context): WindowManager {
        if (mWindowManager == null) {
            mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        return mWindowManager!!
    }
}
