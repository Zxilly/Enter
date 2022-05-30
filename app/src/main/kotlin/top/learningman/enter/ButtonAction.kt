package top.learningman.enter

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER
import android.widget.Toast


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

fun AccessibilityService.clickEnter() {
    root {
        findFocus(AccessibilityNodeInfo.FOCUS_INPUT)?.let {
            if (!it.text.isNullOrBlank()) {
                it.performAction(ACTION_IME_ENTER.id)
                Log.d("AccessibilityService", "apply enter")
            } else {
                Toast.makeText(this, "empty input", Toast.LENGTH_SHORT).show()
                Log.d("AccessibilityService", "empty input")
            }
        } ?: throw ActionFailedException()
    }
}

fun AccessibilityService.clickKnow() {
    // if no focus element, try to click first in ans group
    // cn.com.langeasy.LangEasyLexis:id/tv_know
    // cn.com.langeasy.LangEasyLexis:id/tv_dim
    // cn.com.langeasy.LangEasyLexis:id/tv_unknow
    // cn.com.langeasy.LangEasyLexis:id/ll_isknow (group to contain above)
    findSingleNode("cn.com.langeasy.LangEasyLexis:id/ll_isknow") {
        it.clickFirstChild("click know")
    }
}

fun AccessibilityService.clickNext() {
    findSingleNode("cn.com.langeasy.LangEasyLexis:id/ll_sentence_next") {
        it.clickFirstChild("click next")
    }
}

fun AccessibilityService.clickSpell1() {
    findSingleNode("cn.com.langeasy.LangEasyLexis:id/iv_spell_voice") {
        it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}

fun AccessibilityService.clickSpell2() {
    findSingleNode("cn.com.langeasy.LangEasyLexis:id/iv_spell_prompt") {
        it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}
