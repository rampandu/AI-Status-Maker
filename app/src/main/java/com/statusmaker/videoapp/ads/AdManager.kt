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
        private const val INTERSTITIAL_COOLDOWN_MS = 3 * 60 * 1000L

        // ── Replace these with real Ad Unit IDs before release ───────────────
        const val BANNER_AD_UNIT   = "ca-app-pub-9535310271167305/1410870345"
        const val INTERSTITIAL_ID  = "ca-app-pub-9535310271167305/4104939141"
        const val REWARDED_AD_UNIT = "ca-app-pub-9535310271167305/2671981128"

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

    // FIX: previously calling loadRewardedAd() was fire-and-forget — nothing
    // ever found out when the load finished, so the caller was stuck after
    // showing a "Loading ad…" toast with no follow-up. This queue lets any
    // number of callers register interest in "when does the load resolve".
    private val pendingRewardedCallbacks = mutableListOf<Pair<() -> Unit, (String) -> Unit>>()

    // FIX: track whether MobileAds.initialize() has actually completed.
    // Loading ads before this is done is a common cause of silent failures.
    @Volatile private var isSdkInitialized = false
    private val pendingBannerLoads = mutableListOf<AdView>()

    fun onSdkInitialized() {
        isSdkInitialized = true
        Log.d(TAG, "AdMob SDK initialized")
        preloadAds()
        // Flush any banners that tried to load before init finished
        pendingBannerLoads.forEach { it.loadAd(AdRequest.Builder().build()) }
        pendingBannerLoads.clear()
    }

    fun preloadAds() {
        loadRewardedAd()
        loadInterstitialAd()
    }

    // ── Rewarded Ad ───────────────────────────────────────────────────────────

    /**
     * Loads a rewarded ad. Optional [onLoaded]/[onFailed] let the caller find
     * out exactly when this specific load resolves — previously this was
     * fire-and-forget, so a caller showing "Loading ad…" had no way to know
     * when to actually show the ad or fall back.
     *
     * If an ad is already loaded, [onLoaded] fires immediately.
     * If a load is already in progress, this callback is queued onto it
     * instead of starting a duplicate request.
     */
    fun loadRewardedAd(onLoaded: () -> Unit = {}, onFailed: (String) -> Unit = {}) {
        if (rewardedAd != null) { onLoaded(); return }

        pendingRewardedCallbacks.add(onLoaded to onFailed)
        if (isRewardedLoading) return  // callback queued; existing load will resolve it

        isRewardedLoading = true
        RewardedAd.load(context, REWARDED_AD_UNIT, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad; isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded")
                    val callbacks = pendingRewardedCallbacks.toList()
                    pendingRewardedCallbacks.clear()
                    callbacks.forEach { (onLoadedCb, _) -> onLoadedCb() }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null; isRewardedLoading = false
                    Log.w(TAG, "Rewarded failed [${error.code}]: ${error.message}")
                    val callbacks = pendingRewardedCallbacks.toList()
                    pendingRewardedCallbacks.clear()
                    callbacks.forEach { (_, onFailedCb) -> onFailedCb(error.message) }
                }
            })
    }

    fun isRewardedAdReady() = rewardedAd != null

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

        var rewarded = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd()
                if (rewarded) onRewarded() else onAdSkipped()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.w(TAG, "Rewarded failed to show: ${e.message}")
                rewardedAd = null; loadRewardedAd(); onAdNotAvailable()
            }
        }

        ad.show(activity) { _ -> rewarded = true }
    }

    // ── Interstitial Ad ───────────────────────────────────────────────────────

    fun loadInterstitialAd() {
        InterstitialAd.load(context, INTERSTITIAL_ID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial loaded")
                }
                override fun onAdFailedToLoad(e: LoadAdError) {
                    interstitialAd = null
                    Log.w(TAG, "Interstitial failed [${e.code}]: ${e.message}")
                }
            })
    }

    fun isInterstitialReady() = interstitialAd != null

    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        if (now - lastInterstitialTime < INTERSTITIAL_COOLDOWN_MS) {
            Log.d(TAG, "Interstitial skipped (cooldown active)")
            onDismissed(); return
        }
        val ad = interstitialAd ?: run {
            Log.d(TAG, "Interstitial not ready, skipping this time")
            onDismissed(); loadInterstitialAd(); return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null; loadInterstitialAd(); onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.w(TAG, "Interstitial failed to show: ${e.message}")
                interstitialAd = null; onDismissed()
            }
        }
        lastInterstitialTime = now
        ad.show(activity)
    }

    // ── Banner ────────────────────────────────────────────────────────────────

    /**
     * FIX: previously this had no listener at all, so a failed banner load
     * (no fill, network error, SDK not ready) was completely silent — the
     * container just stayed empty with zero diagnostic info.
     *
     * Now: logs every outcome, and if the SDK hasn't finished initializing
     * yet, the load is queued instead of fired immediately (which can fail
     * silently on some SDK versions if called too early).
     */
    fun loadBannerAd(adView: AdView, onLoaded: () -> Unit = {}, onFailed: (String) -> Unit = {}) {
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner loaded")
                onLoaded()
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "Banner failed [${error.code}]: ${error.message} (domain=${error.domain})")
                onFailed(error.message)
            }
        }

        if (!isSdkInitialized) {
            Log.d(TAG, "SDK not ready yet — queuing banner load")
            pendingBannerLoads.add(adView)
            return
        }
        adView.loadAd(AdRequest.Builder().build())
    }
}
