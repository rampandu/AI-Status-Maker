package com.statusmaker.videoapp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.statusmaker.videoapp.ads.AdManager

class StatusMakerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initAdMob()
    }

    private fun initAdMob() {
        // Test device setup (remove in production)
        val testDeviceIds = listOf("ABCDEF012345") // Replace with your test device ID
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        MobileAds.initialize(this) {
            AdManager.getInstance(this).preloadAds()
        }
    }

    companion object {
        lateinit var instance: StatusMakerApp
            private set
    }
}
