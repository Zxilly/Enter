package top.learningman.enter.view

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER
import android.widget.Toast


object ButtonAction {
    class ActionFailedException : Exception()

    private fun AccessibilityService.root(block: (root: AccessibilityNodeInfo) -> Unit) {
        if (rootInActiveWindow == null) {
            Toast.makeText(this, "Can not view window.", Toast.LENGTH_SHORT).show()
            throw NullPointerException("rootInActiveWindow is null")
        }
        block(rootInActiveWindow)
        rootInActiveWindow.recycle()
    }

    private fun AccessibilityService.findSingleNode(
        id: String, block: (node: AccessibilityNodeInfo) -> Unit
    ) {
        root {
            val nodes = it.findAccessibilityNodeInfosByViewId(id)
            nodes.firstOrNull()?.let(block) ?: throw ActionFailedException()
            nodes.forEach(AccessibilityNodeInfo::recycle)
        }
    }

    private fun AccessibilityNodeInfo.clickFirstChild(
        logMsg: String = "click first in group", errMsg: String = "empty group"
    ) {
        if (childCount >= 1) {
            val first = getChild(0)
            first.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            first.recycle()
            Log.d("AccessibilityService", logMsg)
        } else {
            Log.w("AccessibilityService", errMsg)
            throw ActionFailedException()
        }
    }

    private fun AccessibilityNodeInfo.clickLastChild(
        logMsg: String = "click last in group", errMsg: String = "empty group"
    ) {
        if (childCount >= 1) {
            val last = getChild(childCount - 1)
            last.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            last.recycle()
            Log.d("AccessibilityService", logMsg)
        } else {
            Log.w("AccessibilityService", errMsg)
            throw ActionFailedException()
        }
    }


    val clickEnter: (AccessibilityService) -> Unit = { service ->
        service.root {
            it.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)?.let {
                if (!it.text.isNullOrBlank()) {
                    it.performAction(ACTION_IME_ENTER.id)
                    Log.d("AccessibilityService", "apply enter")
                } else {
                    Toast.makeText(service, "empty input", Toast.LENGTH_SHORT).show()
                    Log.d("AccessibilityService", "empty input")
                }
            } ?: throw ActionFailedException()
        }
    }

    val clickKnow: (AccessibilityService) -> Unit = { service ->
        // if no focus element, try to click first in ans group
        // cn.com.langeasy.LangEasyLexis:id/tv_know
        // cn.com.langeasy.LangEasyLexis:id/tv_dim
        // cn.com.langeasy.LangEasyLexis:id/tv_unknow
        // cn.com.langeasy.LangEasyLexis:id/ll_isknow (group to contain above)
        service.findSingleNode("cn.com.langeasy.LangEasyLexis:id/ll_isknow") {
            it.clickFirstChild("click know")
        }
    }

    val clickUnKnow: (AccessibilityService) -> Unit = { service ->
        service.findSingleNode("cn.com.langeasy.LangEasyLexis:id/ll_isknow") {
            it.clickLastChild("click unknow")
        }
    }

    val clickNext: (AccessibilityService) -> Unit = { service ->
        service.findSingleNode("cn.com.langeasy.LangEasyLexis:id/ll_sentence_next") {
            it.clickFirstChild("click next")
        }
    }

    val clickMistaken: (AccessibilityService) -> Unit = { service ->
        service.findSingleNode("cn.com.langeasy.LangEasyLexis:id/ll_sentence_next") {
            it.clickLastChild("click mistaken")
        }
    }

    val clickSpell1: (AccessibilityService) -> Unit = { service ->
        service.findSingleNode("cn.com.langeasy.LangEasyLexis:id/iv_spell_voice") {
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    val clickSpell2: (AccessibilityService) -> Unit = { service ->
        service.findSingleNode("cn.com.langeasy.LangEasyLexis:id/iv_spell_prompt") {
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }
}
