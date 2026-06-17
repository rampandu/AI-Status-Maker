package com.statusmaker.videoapp.ui.premium;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\u0018\u0000 ,2\u00020\u00012\u00020\u0002:\u0001,B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0016\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010H\u0002J\u0010\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0018\u0010\u0015\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0017\u001a\u00020\u0011H\u0002J$\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010 \u001a\u00020\u000eH\u0016J \u0010!\u001a\u00020\u000e2\u0006\u0010\"\u001a\u00020#2\u000e\u0010$\u001a\n\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u0010H\u0016J\u001a\u0010%\u001a\u00020\u000e2\u0006\u0010&\u001a\u00020\u00192\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010\'\u001a\u00020\u000eH\u0002J\b\u0010(\u001a\u00020\u000eH\u0002J\b\u0010)\u001a\u00020\u000eH\u0002J\b\u0010*\u001a\u00020\u000eH\u0002J\b\u0010+\u001a\u00020\u000eH\u0002R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\u00020\u00058BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\nR\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lcom/statusmaker/videoapp/ui/premium/PremiumFragment;", "Landroidx/fragment/app/Fragment;", "Lcom/android/billingclient/api/PurchasesUpdatedListener;", "()V", "_binding", "Lcom/statusmaker/videoapp/databinding/FragmentPremiumBinding;", "billingClient", "Lcom/android/billingclient/api/BillingClient;", "binding", "getBinding", "()Lcom/statusmaker/videoapp/databinding/FragmentPremiumBinding;", "prefManager", "Lcom/statusmaker/videoapp/utils/PreferenceManager;", "applyPurchaseBenefit", "", "productIds", "", "", "handlePurchase", "purchase", "Lcom/android/billingclient/api/Purchase;", "launchBillingFlow", "productId", "productType", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onPurchasesUpdated", "result", "Lcom/android/billingclient/api/BillingResult;", "purchases", "onViewCreated", "view", "queryProductDetails", "restorePurchases", "restorePurchasesQuietly", "setupBillingClient", "setupUI", "Companion", "app_debug"})
public final class PremiumFragment extends androidx.fragment.app.Fragment implements com.android.billingclient.api.PurchasesUpdatedListener {
    @org.jetbrains.annotations.Nullable()
    private com.statusmaker.videoapp.databinding.FragmentPremiumBinding _binding;
    private com.android.billingclient.api.BillingClient billingClient;
    private com.statusmaker.videoapp.utils.PreferenceManager prefManager;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SKU_PREMIUM_MONTHLY = "premium_monthly";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SKU_PREMIUM_ANNUAL = "premium_annual";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SKU_WATERMARK_REMOVE = "remove_watermark";
    @org.jetbrains.annotations.NotNull()
    public static final com.statusmaker.videoapp.ui.premium.PremiumFragment.Companion Companion = null;
    
    public PremiumFragment() {
        super();
    }
    
    private final com.statusmaker.videoapp.databinding.FragmentPremiumBinding getBinding() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupUI() {
    }
    
    private final void setupBillingClient() {
    }
    
    private final void queryProductDetails() {
    }
    
    private final void launchBillingFlow(java.lang.String productId, java.lang.String productType) {
    }
    
    @java.lang.Override()
    public void onPurchasesUpdated(@org.jetbrains.annotations.NotNull()
    com.android.billingclient.api.BillingResult result, @org.jetbrains.annotations.Nullable()
    java.util.List<? extends com.android.billingclient.api.Purchase> purchases) {
    }
    
    private final void handlePurchase(com.android.billingclient.api.Purchase purchase) {
    }
    
    /**
     * FIX #3: grant correct benefit per product.
     * Subscriptions → full premium. Watermark IAP → watermark only.
     */
    private final void applyPurchaseBenefit(java.util.List<java.lang.String> productIds) {
    }
    
    private final void restorePurchasesQuietly() {
    }
    
    private final void restorePurchases() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/statusmaker/videoapp/ui/premium/PremiumFragment$Companion;", "", "()V", "SKU_PREMIUM_ANNUAL", "", "SKU_PREMIUM_MONTHLY", "SKU_WATERMARK_REMOVE", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}