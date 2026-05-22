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

    // Play Console product IDs
    companion object {
        const val SKU_PREMIUM_MONTHLY   = "premium_monthly"
        const val SKU_WATERMARK_REMOVE  = "remove_watermark"
        const val SKU_PREMIUM_ANNUAL    = "premium_annual"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
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
        binding.btnMonthlyPlan.setOnClickListener {
            launchBillingFlow(SKU_PREMIUM_MONTHLY, BillingClient.ProductType.SUBS)
        }
        binding.btnAnnualPlan.setOnClickListener {
            launchBillingFlow(SKU_PREMIUM_ANNUAL, BillingClient.ProductType.SUBS)
        }
        binding.btnRemoveWatermark.setOnClickListener {
            launchBillingFlow(SKU_WATERMARK_REMOVE, BillingClient.ProductType.INAPP)
        }
        binding.btnRestorePurchases.setOnClickListener {
            restorePurchases()
        }
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
                }
            }
            override fun onBillingServiceDisconnected() { /* retry */ }
        })
    }

    private fun queryProductDetails() {
        val subProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PREMIUM_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PREMIUM_ANNUAL)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val inappProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_WATERMARK_REMOVE)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(subProducts).build()
        ) { result, products ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                activity?.runOnUiThread {
                    products.forEach { product ->
                        when (product.productId) {
                            SKU_PREMIUM_MONTHLY -> {
                                val price = product.subscriptionOfferDetails?.firstOrNull()
                                    ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "₹99"
                                binding.btnMonthlyPlan.text = "Monthly Premium – $price/month"
                            }
                            SKU_PREMIUM_ANNUAL -> {
                                val price = product.subscriptionOfferDetails?.firstOrNull()
                                    ?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "₹599"
                                binding.btnAnnualPlan.text = "Annual Premium – $price/year (Best Value!)"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchBillingFlow(productId: String, productType: String) {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(productType)
            .build()

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        ) { result, products ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && products.isNotEmpty()) {
                val productDetails = products.first()
                val productDetailsParams = if (productType == BillingClient.ProductType.SUBS) {
                    val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                } else {
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                }

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productDetailsParams))
                    .build()

                activity?.let { billingClient.launchBillingFlow(it, flowParams) }
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Product not available. Check Play Console.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(context, "Purchase cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge purchase
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        lifecycleScope.launch {
                            prefManager.setPremium(true)
                            activity?.runOnUiThread {
                                binding.premiumSuccessGroup.visibility = View.VISIBLE
                                Toast.makeText(context, "🎉 Premium activated! Enjoy!", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun restorePurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActive = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                lifecycleScope.launch {
                    if (hasActive) {
                        prefManager.setPremium(true)
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Premium restored!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(context, "No active purchases found.", Toast.LENGTH_SHORT).show()
                        }
                    }
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
