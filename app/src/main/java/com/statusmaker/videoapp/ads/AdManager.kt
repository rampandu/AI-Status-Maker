package com.statusmaker.videoapp.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.statusmaker.videoapp.BuildConfig
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.Date

class AdManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "AdManager"
        private const val INTERSTITIAL_COOLDOWN_MS = 3 * 60 * 1000L

        // App Open ads expire after ~4h per Google's own reference implementation —
        // serving a stale cached ad past that window has a high show-failure rate.
        private const val APP_OPEN_AD_MAX_AGE_MS = 4 * 60 * 60 * 1000L

        // ── Production ad units (used only in release builds) ────────────────
        private const val PROD_BANNER       = "ca-app-pub-9535310271167305/1410870345"
        private const val PROD_INTERSTITIAL = "ca-app-pub-9535310271167305/4104939141"
        private const val PROD_REWARDED     = "ca-app-pub-9535310271167305/2671981128"
        private const val PROD_APP_OPEN     = "ca-app-pub-9535310271167305/8281894822"

        // ── Google's public sample ad units — used in debug builds ───────────
        // Test ads ALWAYS fill, so ads are actually visible during development,
        // and requesting real ads from dev builds (an AdMob policy violation
        // that can get the account limited) is avoided. Real units frequently
        // no-fill on unregistered dev devices, which is why banners appeared
        // "not to work" while debugging.
        private const val TEST_BANNER       = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_REWARDED     = "ca-app-pub-3940256099942544/5224354917"
        private const val TEST_APP_OPEN     = "ca-app-pub-3940256099942544/9257395921"

        val BANNER_AD_UNIT   = if (BuildConfig.DEBUG) TEST_BANNER else PROD_BANNER
        val INTERSTITIAL_ID  = if (BuildConfig.DEBUG) TEST_INTERSTITIAL else PROD_INTERSTITIAL
        val REWARDED_AD_UNIT = if (BuildConfig.DEBUG) TEST_REWARDED else PROD_REWARDED
        val APP_OPEN_AD_UNIT = if (BuildConfig.DEBUG) TEST_APP_OPEN else PROD_APP_OPEN

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

    private val pendingRewardedCallbacks = mutableListOf<Pair<() -> Unit, (String) -> Unit>>()

    @Volatile private var isSdkInitialized = false
    private val pendingBannerLoads = mutableListOf<AdView>()

    // ── App Open ad state ──────────────────────────────────────────────────
    private var appOpenAd: AppOpenAd? = null
    private var appOpenLoadTime: Long = 0L
    private var isAppOpenLoading = false
    private var isShowingAppOpenAd = false

    fun onSdkInitialized() {
        isSdkInitialized = true
        Log.d(TAG, "AdMob SDK initialized")
        preloadAds()
        pendingBannerLoads.forEach { it.loadAd(AdRequest.Builder().build()) }
        pendingBannerLoads.clear()
    }

    fun preloadAds() {
        loadRewardedAd()
        loadInterstitialAd()
        loadAppOpenAd()
    }

    // ── Rewarded Ad ───────────────────────────────────────────────────────────

    fun loadRewardedAd(onLoaded: () -> Unit = {}, onFailed: (String) -> Unit = {}) {
        if (rewardedAd != null) { onLoaded(); return }

        pendingRewardedCallbacks.add(onLoaded to onFailed)
        if (isRewardedLoading) return

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

    /**
     * Shared cooldown applies across ALL interstitial trigger points in the
     * app (Edit Again, Templates→Home, My Videos→Home) — so adding more
     * trigger points increases the *chance* an interstitial fires at a
     * natural break point, without increasing total frequency past one
     * every 3 minutes.
     */
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
     * One-call banner setup shared by all screens: creates an ADAPTIVE banner
     * sized to the actual screen width (better fill rate + full-width look
     * vs. the old fixed 320×50 AdSize.BANNER), attaches it to [container]
     * and starts loading. Returns the AdView so callers can pause/destroy it.
     */
    fun attachAdaptiveBanner(
        container: ViewGroup,
        onLoaded: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ): AdView {
        val ctx = container.context
        val metrics = ctx.resources.displayMetrics
        val widthPx = if (container.width > 0) container.width.toFloat() else metrics.widthPixels.toFloat()
        val adWidthDp = (widthPx / metrics.density).toInt().coerceAtLeast(320)
        val adView = AdView(ctx).apply {
            adUnitId = BANNER_AD_UNIT
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidthDp))
        }
        container.removeAllViews()
        container.addView(adView)
        loadBannerAd(adView, onLoaded, onFailed)
        return adView
    }

    /**
     * FIX: previously a failed banner load was completely silent. Now logs
     * every outcome AND retries with backoff (15s, then 45s) before giving
     * up for this AdView — a transient no-fill or network blip no longer
     * permanently empties the banner slot for the rest of the session.
     */
    fun loadBannerAd(
        adView: AdView,
        onLoaded: () -> Unit = {},
        onFailed: (String) -> Unit = {},
        retriesLeft: Int = 2
    ) {
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner loaded")
                onLoaded()
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "Banner failed [${error.code}]: ${error.message} (domain=${error.domain})")
                if (retriesLeft > 0) {
                    val delayMs = if (retriesLeft == 2) 15_000L else 45_000L
                    Log.d(TAG, "Retrying banner in ${delayMs}ms ($retriesLeft retries left)")
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadBannerAd(adView, onLoaded, onFailed, retriesLeft - 1)
                    }, delayMs)
                } else {
                    onFailed(error.message)
                }
            }
        }

        if (!isSdkInitialized) {
            Log.d(TAG, "SDK not ready yet — queuing banner load")
            pendingBannerLoads.add(adView)
            return
        }
        adView.loadAd(AdRequest.Builder().build())
    }

    // ── App Open Ad ────────────────────────────────────────────────────────────

    fun loadAppOpenAd() {
        if (isAppOpenLoading || isAppOpenAdAvailable()) return
        isAppOpenLoading = true
        AppOpenAd.load(
            context, APP_OPEN_AD_UNIT, AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appOpenLoadTime = Date().time
                    isAppOpenLoading = false
                    Log.d(TAG, "App Open ad loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAppOpenLoading = false
                    Log.w(TAG, "App Open ad failed [${error.code}]: ${error.message}")
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean =
        appOpenAd != null && (Date().time - appOpenLoadTime) < APP_OPEN_AD_MAX_AGE_MS

    /**
     * Shows the App Open ad if one is cached and not stale. [onComplete]
     * always fires exactly once — either after the ad is dismissed, or
     * immediately if no ad is available — so the caller (Splash screen)
     * can always proceed into the app without getting stuck.
     */
    fun showAppOpenAdIfAvailable(activity: Activity, onComplete: () -> Unit) {
        if (isShowingAppOpenAd) { onComplete(); return }
        if (!isAppOpenAdAvailable()) {
            Log.d(TAG, "App Open ad not available — skipping")
            loadAppOpenAd()
            onComplete()
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null; isShowingAppOpenAd = false
                loadAppOpenAd()
                onComplete()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                Log.w(TAG, "App Open ad failed to show: ${e.message}")
                appOpenAd = null; isShowingAppOpenAd = false
                loadAppOpenAd()
                onComplete()
            }
            override fun onAdShowedFullScreenContent() {
                isShowingAppOpenAd = true
            }
        }
        appOpenAd?.show(activity)
    }
}
