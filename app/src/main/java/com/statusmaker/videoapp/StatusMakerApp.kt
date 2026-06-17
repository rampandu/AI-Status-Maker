package com.statusmaker.videoapp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.statusmaker.videoapp.ads.AdManager

class StatusMakerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        MobileAds.initialize(this) {
            // FIX: route through onSdkInitialized() so AdManager knows the
            // SDK is actually ready, and flushes any banner loads that were
            // requested before this callback fired.
            AdManager.getInstance(this).onSdkInitialized()
        }
    }

    companion object {
        lateinit var instance: StatusMakerApp
            private set
    }
}
