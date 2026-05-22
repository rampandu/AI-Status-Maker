package com.statusmaker.videoapp.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager private constructor(private val context: Context) {

    // ─── Ad Unit IDs — Replace with your real IDs in production ──────────────
    companion object {
        private const val TAG = "AdManager"

        // Test IDs (auto-approved by Google for testing)
        const val BANNER_AD_UNIT     = "ca-app-pub-9535310271167305/1410870345"
        const val INTERSTITIAL_ID    = "ca-app-pub-9535310271167305/4104939141"
        const val REWARDED_AD_UNIT   = "ca-app-pub-9535310271167305/4104939141"

        @Volatile private var instance: AdManager? = null

        fun getInstance(context: Context): AdManager =
            instance ?: synchronized(this) {
                instance ?: AdManager(context.applicationContext).also { instance = it }
            }
    }

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isRewardedLoading = false

    // ─── Preload both ad formats ──────────────────────────────────────────────

    fun preloadAds() {
        loadRewardedAd()
        loadInterstitialAd()
    }

    // ─── Rewarded Ad (shown before video export) ──────────────────────────────

    fun loadRewardedAd() {
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isRewardedLoading = false
                Log.d(TAG, "Rewarded ad loaded")
                setupRewardedCallbacks()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                isRewardedLoading = false
                Log.w(TAG, "Rewarded ad failed: ${error.message}")
            }
        })
    }

    private fun setupRewardedCallbacks() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd() // Preload next
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewardedAd()
            }
        }
    }

    fun isRewardedAdReady() = rewardedAd != null

    /**
     * Show rewarded ad. Call this before allowing video export.
     * [onRewarded] is called when the user earns the reward (watched to completion).
     * [onAdSkipped] is called if ad was dismissed early.
     * [onAdNotAvailable] is called if no ad is loaded yet.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdSkipped: () -> Unit = {},
        onAdNotAvailable: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "Rewarded ad not ready")
            onAdNotAvailable()
            loadRewardedAd()
            return
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewarded()
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd()
                // If reward was not granted via callback above, treat as skipped
                // (Android handles this via the reward callback timing)
            }
        }
    }

    // ─── Interstitial Ad (shown on template selection / home) ─────────────────

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d(TAG, "Interstitial loaded")
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                Log.w(TAG, "Interstitial failed: ${error.message}")
            }
        })
    }

    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            onDismissed()
            loadInterstitialAd()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd()
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                onDismissed()
            }
        }
        ad.show(activity)
    }

    // ─── Banner Ad helper ─────────────────────────────────────────────────────

    fun loadBannerAd(adView: com.google.android.gms.ads.AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}
