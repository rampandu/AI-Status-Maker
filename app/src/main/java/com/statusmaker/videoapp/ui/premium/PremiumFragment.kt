package com.statusmaker.videoapp.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.statusmaker.videoapp.databinding.FragmentPremiumBinding

/**
 * Billing temporarily removed — see TODO below. This screen still renders
 * (it's a live bottom-nav destination), but none of the buttons complete a
 * purchase right now. Re-enable by restoring the Play Billing Library
 * (com.android.billingclient:billing-ktx) in app/build.gradle and wiring
 * BillingClient back into setupUI()/launchBillingFlow() — the previous
 * implementation handled SUBS (monthly/annual) + INAPP (watermark removal)
 * with purchase acknowledgement and restore-purchases support.
 */
class PremiumFragment : Fragment() {

    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!

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
        setupUI()
    }

    private fun setupUI() {
        binding.btnMonthlyPlan.setOnClickListener     { showComingSoon() }
        binding.btnAnnualPlan.setOnClickListener      { showComingSoon() }
        binding.btnRemoveWatermark.setOnClickListener { showComingSoon() }
        binding.btnRestorePurchases.setOnClickListener { showComingSoon() }
    }

    private fun showComingSoon() {
        Toast.makeText(requireContext(), "Premium purchases aren't available yet — coming soon!", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
