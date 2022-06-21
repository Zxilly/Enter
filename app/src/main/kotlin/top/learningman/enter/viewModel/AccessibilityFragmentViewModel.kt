package top.learningman.enter.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccessibilityFragmentViewModel : ViewModel() {
    private val tryCount = MutableLiveData(0)

    fun haveTryGrant(): Boolean {
        return tryCount.value?.let { it > 0 } ?: false
    }

    fun tryGrant() {
        tryCount.value = tryCount.value?.plus(1)
    }
}