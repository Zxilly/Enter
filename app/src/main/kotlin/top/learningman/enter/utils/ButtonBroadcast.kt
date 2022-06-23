package top.learningman.enter.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object ButtonBroadcast {
    const val ACTION_ENABLE_ENTER = "top.learningman.enter.action.ENABLE_ENTER_BUTTON"
    const val ACTION_DISABLE_ENTER = "top.learningman.enter.action.DISABLE_ENTER_BUTTON"
    const val ACTION_ENABLE_SPEN = "top.learningman.enter.action.ENABLE_SPEN_BINDING"
    const val ACTION_DISABLE_SPEN = "top.learningman.enter.action.DISABLE_SPEN_BINDING"

    private fun sendAction(context: Context, action: String) {
        val intent = Intent().apply {
            this.action = action
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun enableEnterButton(context: Context) {
        sendAction(context, ACTION_ENABLE_ENTER)
    }

    fun disableEnterButton(context: Context) {
        sendAction(context, ACTION_DISABLE_ENTER)
    }

    fun enableSPenBinding(context: Context) {
        sendAction(context, ACTION_ENABLE_SPEN)
    }

    fun disableSPenBinding(context: Context) {
        sendAction(context, ACTION_DISABLE_SPEN)
    }

    val filter = IntentFilter().apply {
        addAction(ACTION_ENABLE_ENTER)
        addAction(ACTION_DISABLE_ENTER)
        addAction(ACTION_ENABLE_SPEN)
        addAction(ACTION_DISABLE_SPEN)
    }

    abstract class Receiver : BroadcastReceiver() {
        abstract fun onEnableEnterButton()
        abstract fun onDisableEnterButton()
        abstract fun onEnableSpenBinding()
        abstract fun onDisableSpenBinding()

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_ENABLE_ENTER -> onEnableEnterButton()
                ACTION_DISABLE_ENTER -> onDisableEnterButton()
                ACTION_ENABLE_SPEN -> onEnableSpenBinding()
                ACTION_DISABLE_SPEN -> onDisableSpenBinding()
            }
        }
    }
}