package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class ButtonAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // id:
        // cn.com.langeasy.LangEasyLexis:id/et_spell_word (preview)
        // cn.com.langeasy.LangEasyLexis:id/et_spell_input (final)
        rootInActiveWindow?.let {

        }?: Log.d("ButtonAccessibilityService", "rootInActiveWindow is null")
        return super.onStartCommand(intent, flags, startId)
    }
}