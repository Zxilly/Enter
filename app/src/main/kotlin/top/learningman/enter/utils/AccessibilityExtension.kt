package top.learningman.enter.utils

import android.accessibilityservice.AccessibilityService
import top.learningman.enter.view.ButtonAction
import top.learningman.enter.noAction

fun AccessibilityService.shortClick() {
    tryFunctions(
        this,
        ButtonAction.clickEnter,
        ButtonAction.clickKnow,
        ButtonAction.clickNext,
        ::noAction
    )
}

fun AccessibilityService.longClick() {
    tryFunctions(
        this,
        ButtonAction.clickSpell1,
        ButtonAction.clickSpell2,
        ButtonAction.clickUnKnow,
        ButtonAction.clickMistaken,
        ::noAction
    )
}