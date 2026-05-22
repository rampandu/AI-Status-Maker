package com.statusmaker.videoapp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.statusmaker.videoapp.ads.AdManager

class StatusMakerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        MobileAds.initialize(this) {
            AdManager.getInstance(this).preloadAds()
        }
    }

    companion object {
        lateinit var instance: StatusMakerApp
            private set
    }
}
