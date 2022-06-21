package top.learningman.enter.utils

import com.microsoft.appcenter.crashes.Crashes
import top.learningman.enter.view.ButtonAction

fun <T> tryFunctions(arg: T, vararg functions: (T) -> Unit): Boolean {
    for (function in functions) {
        val result = runCatching {
            function(arg)
        }
        if (result.isSuccess) {
            return true
        } else {
            val exception = result.exceptionOrNull()!!
            if (exception !is ButtonAction.ActionFailedException) {
                exception.printStackTrace()
                Crashes.trackError(exception)
            }
        }
    }
    return false
}