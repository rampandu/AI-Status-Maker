package com.statusmaker.videoapp.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.statusmaker.videoapp.databinding.FragmentPremiumBinding
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.launch

class PremiumFragment : Fragment(), PurchasesUpdatedListener {

    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!

    private lateinit var billingClient: BillingClient
    private lateinit var prefManager: PreferenceManager

    companion object {
        const val SKU_PREMIUM_MONTHLY  = "premium_monthly"
        const val SKU_PREMIUM_ANNUAL   = "premium_annual"
        const val SKU_WATERMARK_REMOVE = "remove_watermark"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())
        setupBillingClient()
        setupUI()
    }

    private fun setupUI() {
        binding.btnMonthlyPlan.setOnClickListener     { launchBillingFlow(SKU_PREMIUM_MONTHLY,  BillingClient.ProductType.SUBS)  }
        binding.btnAnnualPlan.setOnClickListener      { launchBillingFlow(SKU_PREMIUM_ANNUAL,   BillingClient.ProductType.SUBS)  }
        binding.btnRemoveWatermark.setOnClickListener { launchBillingFlow(SKU_WATERMARK_REMOVE, BillingClient.ProductType.INAPP) }
        binding.btnRestorePurchases.setOnClickListener { restorePurchases() }
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    restorePurchasesQuietly()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Retry handled by next user action
            }
        })
    }

    private fun queryProductDetails() {
        val subs = listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_PREMIUM_MONTHLY).setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder().setProductId(SKU_PREMIUM_ANNUAL).setProductType(BillingClient.ProductType.SUBS).build()
        )
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(subs).build()
        ) { result, products ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            activity?.runOnUiThread {
                products.forEach { p ->
                    val price = p.subscriptionOfferDetails?.firstOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    when (p.productId) {
                        SKU_PREMIUM_MONTHLY -> binding.btnMonthlyPlan.text = "Monthly Premium – ${price ?: "₹99"}/month"
                        SKU_PREMIUM_ANNUAL  -> binding.btnAnnualPlan.text  = "Annual Premium – ${price ?: "₹599"}/year ⭐ Best Value"
                    }
                }
            }
        }
    }

    private fun launchBillingFlow(productId: String, productType: String) {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId).setProductType(productType).build()

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        ) { result, products ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK || products.isEmpty()) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Product not available. Check Play Console setup.", Toast.LENGTH_SHORT).show()
                }
                return@queryProductDetailsAsync
            }
            val pd = products.first()
            val params = if (productType == BillingClient.ProductType.SUBS) {
                val token = pd.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync
                BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(pd).setOfferToken(token).build()
            } else {
                BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(pd).build()
            }
            activity?.let {
                billingClient.launchBillingFlow(it,
                    BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                Toast.makeText(context, "Purchase cancelled", Toast.LENGTH_SHORT).show()
            else ->
                Toast.makeText(context, "Purchase error: ${result.debugMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (purchase.isAcknowledged) {
            applyPurchaseBenefit(purchase.products)
            return
        }
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        ) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                applyPurchaseBenefit(purchase.products)
            }
        }
    }

    /**
     * FIX #3: grant correct benefit per product.
     * Subscriptions → full premium. Watermark IAP → watermark only.
     */
    private fun applyPurchaseBenefit(productIds: List<String>) {
        lifecycleScope.launch {
            val isSubscription = productIds.any { it == SKU_PREMIUM_MONTHLY || it == SKU_PREMIUM_ANNUAL }
            val isWatermark    = productIds.any { it == SKU_WATERMARK_REMOVE }

            when {
                isSubscription -> {
                    prefManager.setPremium(true)
                    activity?.runOnUiThread {
                        binding.premiumSuccessGroup.visibility = View.VISIBLE
                        Toast.makeText(context, "🎉 Premium activated! No ads, no watermark!", Toast.LENGTH_LONG).show()
                    }
                }
                isWatermark -> {
                    prefManager.setWatermarkRemoved(true)
                    activity?.runOnUiThread {
                        Toast.makeText(context, "✅ Watermark removed!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun restorePurchasesQuietly() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .forEach { applyPurchaseBenefit(it.products) }
            }
        }
    }

    private fun restorePurchases() {
        restorePurchasesQuietly()
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasAny = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .forEach { applyPurchaseBenefit(it.products) }
                activity?.runOnUiThread {
                    Toast.makeText(context, if (hasAny) "Purchases restored!" else "No active purchases found.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::billingClient.isInitialized) billingClient.endConnection()
    }
}
