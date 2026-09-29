package com.statusmaker.videoapp.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.statusmaker.videoapp.billing.BillingManager
import com.statusmaker.videoapp.databinding.FragmentPremiumBinding
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.launch

/**
 * Real Play Billing purchase flows. See BillingManager for the product/base-plan
 * IDs that must exist in Play Console before any of these buttons can complete
 * a purchase — without them Play returns "item unavailable" and the buttons
 * quietly stay non-functional even though the code path is live.
 */
class PremiumFragment : Fragment() {

    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!

    private lateinit var billing: BillingManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billing = BillingManager.getInstance(requireContext())
        setupUI()
        observePremiumState()
        billing.startConnection { if (_binding != null) refreshPrices() }
    }

    private fun setupUI() {
        binding.btnMonthlyPlan.setOnClickListener {
            billing.launchSubscriptionPurchase(requireActivity(), BillingManager.BASE_PLAN_MONTHLY)
        }
        binding.btnAnnualPlan.setOnClickListener {
            billing.launchSubscriptionPurchase(requireActivity(), BillingManager.BASE_PLAN_YEARLY)
        }
        binding.btnRemoveWatermark.setOnClickListener {
            billing.launchWatermarkRemovalPurchase(requireActivity())
        }
        binding.btnRestorePurchases.setOnClickListener {
            binding.btnRestorePurchases.isEnabled = false
            billing.restorePurchases { foundAny ->
                if (_binding == null) return@restorePurchases
                binding.btnRestorePurchases.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    if (foundAny) "Purchase restored!" else "No previous purchase found for this account",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /** Swaps the placeholder ₹ prices for the live ones once Play returns them. */
    private fun refreshPrices() {
        billing.monthlyPrice()?.let { binding.btnMonthlyPlan.text = "Monthly Premium – $it/month" }
        billing.yearlyPrice()?.let { binding.btnAnnualPlan.text = "Annual Premium – $it/year" }
        billing.watermarkRemovalPrice()?.let { binding.btnRemoveWatermark.text = "Remove Watermark Only – $it (one-time)" }
    }

    private fun observePremiumState() {
        viewLifecycleOwner.lifecycleScope.launch {
            PreferenceManager(requireContext()).isPremium.collect { isPremium ->
                if (_binding == null) return@collect
                binding.premiumSuccessGroup.visibility = if (isPremium) View.VISIBLE else View.GONE
                binding.btnMonthlyPlan.isEnabled = !isPremium
                binding.btnAnnualPlan.isEnabled = !isPremium
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
