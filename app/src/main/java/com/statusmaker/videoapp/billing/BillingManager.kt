package com.statusmaker.videoapp.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Wraps Play Billing (v8) for the two things this app sells:
 *  - A subscription ("premium_subscription") with two base plans — monthly
 *    and yearly — that unlocks: no ads anywhere, no export watermark, every
 *    PRO-badged template, unlimited exports. This is the primary monetization
 *    path — recurring revenue compounds far better than a one-time unlock
 *    for an app people return to for every festival/birthday, not just once.
 *  - A single non-consumable in-app product ("remove_watermark") for the
 *    price-sensitive segment who won't commit to a subscription but will
 *    pay once to drop the export watermark. Deliberately narrower: it does
 *    NOT unlock PRO templates or remove ads, so it can't cannibalize the
 *    subscription's value.
 *
 * ── Play Console setup required (this code alone does nothing without it) ──
 * 1. Create ONE subscription with product ID [SUBSCRIPTION_PRODUCT_ID],
 *    with two base plans: [BASE_PLAN_MONTHLY] and [BASE_PLAN_YEARLY].
 * 2. Create ONE in-app product (one-time, not consumable) with ID
 *    [WATERMARK_REMOVE_PRODUCT_ID].
 * Product/base-plan IDs must match these constants EXACTLY.
 */
class BillingManager private constructor(context: Context) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"

        const val SUBSCRIPTION_PRODUCT_ID = "premium_subscription"
        const val BASE_PLAN_MONTHLY = "monthly"
        const val BASE_PLAN_YEARLY = "yearly"
        const val WATERMARK_REMOVE_PRODUCT_ID = "remove_watermark"

        @Volatile private var instance: BillingManager? = null
        fun getInstance(context: Context): BillingManager =
            instance ?: synchronized(this) {
                instance ?: BillingManager(context.applicationContext).also { instance = it }
            }
    }

    private val appContext = context.applicationContext
    private val prefManager = PreferenceManager(appContext)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    @Volatile private var isReady = false
    private val onReadyCallbacks = mutableListOf<() -> Unit>()

    private var subscriptionDetails: ProductDetails? = null
    private var watermarkRemoveDetails: ProductDetails? = null

    // ── Connection ─────────────────────────────────────────────────────────

    fun startConnection(onConnected: () -> Unit = {}) {
        if (isReady) { onConnected(); return }
        onReadyCallbacks.add(onConnected)
        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    isReady = true
                    queryProductDetails()
                    queryExistingPurchases()
                    val callbacks = onReadyCallbacks.toList()
                    onReadyCallbacks.clear()
                    callbacks.forEach { it() }
                } else {
                    Log.w(TAG, "Billing setup failed [${result.responseCode}]: ${result.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                isReady = false
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    // ── Product details (for both purchase flows and displaying live prices) ──

    private fun queryProductDetails() {
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(SUBSCRIPTION_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )).build()
        billingClient.queryProductDetailsAsync(subParams) { result, queryResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                subscriptionDetails = queryResult.productDetailsList.firstOrNull()
            } else {
                Log.w(TAG, "Subscription product query failed: ${result.debugMessage}")
            }
        }

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(WATERMARK_REMOVE_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )).build()
        billingClient.queryProductDetailsAsync(inAppParams) { result, queryResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                watermarkRemoveDetails = queryResult.productDetailsList.firstOrNull()
            } else {
                Log.w(TAG, "Watermark-removal product query failed: ${result.debugMessage}")
            }
        }
    }

    /** Live formatted price (e.g. "₹149.00") for a base plan, once fetched — null until then. */
    fun monthlyPrice(): String? = offerFor(BASE_PLAN_MONTHLY)?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
    fun yearlyPrice(): String? = offerFor(BASE_PLAN_YEARLY)?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
    fun watermarkRemovalPrice(): String? = watermarkRemoveDetails?.oneTimePurchaseOfferDetails?.formattedPrice

    private fun offerFor(basePlanId: String): ProductDetails.SubscriptionOfferDetails? =
        subscriptionDetails?.subscriptionOfferDetails?.firstOrNull { it.basePlanId == basePlanId }

    // ── Purchase flows ───────────────────────────────────────────────────────

    fun launchSubscriptionPurchase(activity: Activity, basePlanId: String) {
        val details = subscriptionDetails
        val offer = offerFor(basePlanId)
        if (details == null || offer == null) {
            Log.w(TAG, "Subscription offer for '$basePlanId' not ready yet")
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(offer.offerToken)
                    .build()
            )).build()
        billingClient.launchBillingFlow(activity, params)
    }

    fun launchWatermarkRemovalPurchase(activity: Activity) {
        val details = watermarkRemoveDetails
        if (details == null) {
            Log.w(TAG, "Watermark-removal product not ready yet")
            return
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )).build()
        billingClient.launchBillingFlow(activity, params)
    }

    // ── Purchase updates + entitlement sync ──────────────────────────────────

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                Log.d(TAG, "Purchase flow cancelled by user")
            else ->
                Log.w(TAG, "Purchase update failed [${result.responseCode}]: ${result.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        val grantsSubscription = SUBSCRIPTION_PRODUCT_ID in purchase.products
        val grantsWatermarkRemoval = WATERMARK_REMOVE_PRODUCT_ID in purchase.products

        scope.launch {
            if (grantsSubscription) prefManager.setPremium(true)
            if (grantsWatermarkRemoval) prefManager.setWatermarkRemoved(true)
        }

        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) { ackResult ->
                if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "Acknowledge failed: ${ackResult.debugMessage}")
                }
            }
        }
    }

    /**
     * Re-syncs local entitlement with Play's records — called on every
     * connect (catches renewals/cancellations that happened while the app
     * wasn't running) and from the explicit "Restore Purchases" button.
     * Revokes [PreferenceManager.setPremium]/[setWatermarkRemoved] when
     * Play no longer shows an active purchase, so a lapsed subscriber
     * doesn't stay premium forever on this device.
     */
    private fun queryExistingPurchases(onDone: (foundAny: Boolean) -> Unit = {}) {
        var subActive = false
        var watermarkOwned = false
        var pending = 2

        fun finishIfDone() {
            pending--
            if (pending == 0) {
                scope.launch {
                    prefManager.setPremium(subActive)
                    if (!subActive) prefManager.setWatermarkRemoved(watermarkOwned)
                }
                onDone(subActive || watermarkOwned)
            }
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                for (p in purchases) {
                    if (p.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        SUBSCRIPTION_PRODUCT_ID in p.products) {
                        subActive = true
                        handlePurchase(p)
                    }
                }
            }
            finishIfDone()
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                for (p in purchases) {
                    if (p.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        WATERMARK_REMOVE_PRODUCT_ID in p.products) {
                        watermarkOwned = true
                        handlePurchase(p)
                    }
                }
            }
            finishIfDone()
        }
    }

    /** User-triggered "Restore Purchases" — re-syncs and reports whether anything was found. */
    fun restorePurchases(onDone: (foundAny: Boolean) -> Unit) {
        startConnection { queryExistingPurchases(onDone) }
    }
}
