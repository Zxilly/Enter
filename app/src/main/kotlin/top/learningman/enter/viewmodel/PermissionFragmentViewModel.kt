package top.learningman.enter.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionFragmentViewModel : ViewModel() {
    private val tryCount = MutableLiveData(0)

    fun haveTryGrant(): Boolean {
        return tryCount.value?.let { it > 0 } ?: false
    }

    fun tryGrant() {
        tryCount.value = tryCount.value?.plus(1)
    }
}