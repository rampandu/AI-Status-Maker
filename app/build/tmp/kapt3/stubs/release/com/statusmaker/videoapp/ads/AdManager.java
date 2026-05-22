package com.statusmaker.videoapp.ads;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u000b\u001a\u00020\bJ\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u0006\u0010\u0010\u001a\u00020\rJ\u0006\u0010\u0011\u001a\u00020\rJ\u0006\u0010\u0012\u001a\u00020\rJ\b\u0010\u0013\u001a\u00020\rH\u0002J\u001e\u0010\u0014\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\u00162\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\r0\u0018J<\u0010\u0019\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\u00162\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\r0\u00182\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\r0\u00182\u000e\b\u0002\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\r0\u0018R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/statusmaker/videoapp/ads/AdManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "interstitialAd", "Lcom/google/android/gms/ads/interstitial/InterstitialAd;", "isRewardedLoading", "", "rewardedAd", "Lcom/google/android/gms/ads/rewarded/RewardedAd;", "isRewardedAdReady", "loadBannerAd", "", "adView", "Lcom/google/android/gms/ads/AdView;", "loadInterstitialAd", "loadRewardedAd", "preloadAds", "setupRewardedCallbacks", "showInterstitialAd", "activity", "Landroid/app/Activity;", "onDismissed", "Lkotlin/Function0;", "showRewardedAd", "onRewarded", "onAdSkipped", "onAdNotAvailable", "Companion", "app_release"})
public final class AdManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "AdManager";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BANNER_AD_UNIT = "ca-app-pub-9535310271167305/1410870345";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String INTERSTITIAL_ID = "ca-app-pub-9535310271167305/4104939141";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String REWARDED_AD_UNIT = "ca-app-pub-9535310271167305/4104939141";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.statusmaker.videoapp.ads.AdManager instance;
    @org.jetbrains.annotations.Nullable()
    private com.google.android.gms.ads.rewarded.RewardedAd rewardedAd;
    @org.jetbrains.annotations.Nullable()
    private com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd;
    private boolean isRewardedLoading = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.statusmaker.videoapp.ads.AdManager.Companion Companion = null;
    
    private AdManager(android.content.Context context) {
        super();
    }
    
    public final void preloadAds() {
    }
    
    public final void loadRewardedAd() {
    }
    
    private final void setupRewardedCallbacks() {
    }
    
    public final boolean isRewardedAdReady() {
        return false;
    }
    
    /**
     * Show rewarded ad. Call this before allowing video export.
     * [onRewarded] is called when the user earns the reward (watched to completion).
     * [onAdSkipped] is called if ad was dismissed early.
     * [onAdNotAvailable] is called if no ad is loaded yet.
     */
    public final void showRewardedAd(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRewarded, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAdSkipped, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAdNotAvailable) {
    }
    
    public final void loadInterstitialAd() {
    }
    
    public final void showInterstitialAd(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismissed) {
    }
    
    public final void loadBannerAd(@org.jetbrains.annotations.NotNull()
    com.google.android.gms.ads.AdView adView) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/statusmaker/videoapp/ads/AdManager$Companion;", "", "()V", "BANNER_AD_UNIT", "", "INTERSTITIAL_ID", "REWARDED_AD_UNIT", "TAG", "instance", "Lcom/statusmaker/videoapp/ads/AdManager;", "getInstance", "context", "Landroid/content/Context;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.statusmaker.videoapp.ads.AdManager getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}