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

    companion object {
        private const val TAG = "AdManager"
        private const val INTERSTITIAL_COOLDOWN_MS = 3 * 60 * 1000L // 3 min between interstitials

        // ── Replace these with real Ad Unit IDs before release ───────────────
        const val BANNER_AD_UNIT   = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_ID  = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED_AD_UNIT = "ca-app-pub-3940256099942544/5224354917"

        @Volatile private var instance: AdManager? = null
        fun getInstance(context: Context): AdManager =
            instance ?: synchronized(this) {
                instance ?: AdManager(context.applicationContext).also { instance = it }
            }
    }

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isRewardedLoading = false
    private var lastInterstitialTime = 0L

    fun preloadAds() {
        loadRewardedAd()
        loadInterstitialAd()
    }

    // ── Rewarded Ad ───────────────────────────────────────────────────────────

    fun loadRewardedAd() {
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true
        RewardedAd.load(context, REWARDED_AD_UNIT, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad; isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null; isRewardedLoading = false
                    Log.w(TAG, "Rewarded failed: ${error.message}")
                }
            })
    }

    fun isRewardedAdReady() = rewardedAd != null

    /**
     * FIX #1: reward flag is set inside the reward callback, then checked in
     * onAdDismissedFullScreenContent — this correctly distinguishes watched vs skipped.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdSkipped: () -> Unit = {},
        onAdNotAvailable: () -> Unit = {}
    ) {
        val ad = rewardedAd ?: run {
            loadRewardedAd()
            onAdNotAvailable()
            return
        }

        var rewarded = false  // FIX: track reward inside this scope

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd()
                // Called AFTER reward callback (if rewarded), so flag is correct
                if (rewarded) onRewarded() else onAdSkipped()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                rewardedAd = null; loadRewardedAd(); onAdNotAvailable()
            }
        }

        ad.show(activity) { _ -> rewarded = true }
    }

    // ── Interstitial Ad ───────────────────────────────────────────────────────

    fun loadInterstitialAd() {
        InterstitialAd.load(context, INTERSTITIAL_ID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitialAd = null }
            })
    }

    /**
     * FIX #15: cooldown guard — shows at most once every 3 minutes.
     */
    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        if (now - lastInterstitialTime < INTERSTITIAL_COOLDOWN_MS) {
            onDismissed(); return
        }
        val ad = interstitialAd ?: run { onDismissed(); loadInterstitialAd(); return }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null; loadInterstitialAd(); onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                interstitialAd = null; onDismissed()
            }
        }
        lastInterstitialTime = now
        ad.show(activity)
    }

    // ── Banner ────────────────────────────────────────────────────────────────

    fun loadBannerAd(adView: com.google.android.gms.ads.AdView) {
        adView.loadAd(AdRequest.Builder().build())
    }
}
