package top.learningman.enter

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCenter.start(
            this, "bdb6695f-b9af-49dd-ae56-2a2bdd4232c8",
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}