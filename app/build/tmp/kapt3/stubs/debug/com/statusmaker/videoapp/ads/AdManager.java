package com.statusmaker.videoapp.ads;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 \u001e2\u00020\u0001:\u0001\u001eB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\r\u001a\u00020\bJ\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u0006\u0010\u0012\u001a\u00020\u000fJ\u0006\u0010\u0013\u001a\u00020\u000fJ\u0006\u0010\u0014\u001a\u00020\u000fJ\u001e\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0016\u001a\u00020\u00172\u000e\b\u0002\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0019J<\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u0016\u001a\u00020\u00172\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00192\u000e\b\u0002\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00192\u000e\b\u0002\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0019R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/statusmaker/videoapp/ads/AdManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "interstitialAd", "Lcom/google/android/gms/ads/interstitial/InterstitialAd;", "isRewardedLoading", "", "lastInterstitialTime", "", "rewardedAd", "Lcom/google/android/gms/ads/rewarded/RewardedAd;", "isRewardedAdReady", "loadBannerAd", "", "adView", "Lcom/google/android/gms/ads/AdView;", "loadInterstitialAd", "loadRewardedAd", "preloadAds", "showInterstitialAd", "activity", "Landroid/app/Activity;", "onDismissed", "Lkotlin/Function0;", "showRewardedAd", "onRewarded", "onAdSkipped", "onAdNotAvailable", "Companion", "app_debug"})
public final class AdManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "AdManager";
    private static final long INTERSTITIAL_COOLDOWN_MS = 180000L;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BANNER_AD_UNIT = "ca-app-pub-3940256099942544/6300978111";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String REWARDED_AD_UNIT = "ca-app-pub-3940256099942544/5224354917";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.statusmaker.videoapp.ads.AdManager instance;
    @org.jetbrains.annotations.Nullable()
    private com.google.android.gms.ads.rewarded.RewardedAd rewardedAd;
    @org.jetbrains.annotations.Nullable()
    private com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd;
    private boolean isRewardedLoading = false;
    private long lastInterstitialTime = 0L;
    @org.jetbrains.annotations.NotNull()
    public static final com.statusmaker.videoapp.ads.AdManager.Companion Companion = null;
    
    private AdManager(android.content.Context context) {
        super();
    }
    
    public final void preloadAds() {
    }
    
    public final void loadRewardedAd() {
    }
    
    public final boolean isRewardedAdReady() {
        return false;
    }
    
    /**
     * FIX #1: reward flag is set inside the reward callback, then checked in
     * onAdDismissedFullScreenContent — this correctly distinguishes watched vs skipped.
     */
    public final void showRewardedAd(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onRewarded, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAdSkipped, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAdNotAvailable) {
    }
    
    public final void loadInterstitialAd() {
    }
    
    /**
     * FIX #15: cooldown guard — shows at most once every 3 minutes.
     */
    public final void showInterstitialAd(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismissed) {
    }
    
    public final void loadBannerAd(@org.jetbrains.annotations.NotNull()
    com.google.android.gms.ads.AdView adView) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\f\u001a\u00020\u000b2\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/statusmaker/videoapp/ads/AdManager$Companion;", "", "()V", "BANNER_AD_UNIT", "", "INTERSTITIAL_COOLDOWN_MS", "", "INTERSTITIAL_ID", "REWARDED_AD_UNIT", "TAG", "instance", "Lcom/statusmaker/videoapp/ads/AdManager;", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
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