package com.statusmaker.videoapp.ui.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.ads.AdManager
import com.statusmaker.videoapp.data.model.TemplateCategory
import com.statusmaker.videoapp.databinding.FragmentHomeBinding
import com.statusmaker.videoapp.ui.template.CategoryAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory(requireContext()) }
    private var glowAnimator: ObjectAnimator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryGrid()
        observePremiumAndAds()
        startGlowPulse()

        binding.btnCreateNew.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_templateListFragment)
        }

        binding.btnPremium.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_premiumFragment)
        }
    }

    /**
     * The one signature motion on this screen: a slow, restrained pulse on
     * the marigold glow behind the hero card.
     */
    private fun startGlowPulse() {
        glowAnimator = ObjectAnimator.ofFloat(binding.heroGlow, View.ALPHA, 0.55f, 1f).apply {
            duration = 2400L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun setupCategoryGrid() {
        val categories = TemplateCategory.values().toList()
        val adapter = CategoryAdapter(categories) { category ->
            val action = HomeFragmentDirections.actionHomeFragmentToTemplateListFragment(category.name)
            findNavController().navigate(action)
        }
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
        }
    }

    private fun setupBannerAd() {
        val adView = AdView(requireContext()).apply {
            adUnitId = AdManager.BANNER_AD_UNIT
            setAdSize(AdSize.BANNER)
        }
        binding.bannerAdContainer.addView(adView)
        // Banner callbacks are guarded against a destroyed view (FIX), and
        // the AdManager itself now retries on failure with backoff before
        // giving up — see AdManager.loadBannerAd().
        AdManager.getInstance(requireContext()).loadBannerAd(
            adView,
            onLoaded = {
                if (_binding != null) binding.bannerAdContainer.visibility = View.VISIBLE
            },
            onFailed = {
                if (_binding != null) binding.bannerAdContainer.visibility = View.GONE
            }
        )
    }

    /**
     * FIX: the banner previously loaded unconditionally regardless of
     * premium status — directly contradicting the Premium screen's "no
     * ads" pitch. Now the banner is only ever created for non-premium
     * users, and is torn down immediately if the user upgrades while this
     * screen is visible.
     */
    private fun observePremiumAndAds() {
        viewModel.totalVideosCreated.observe(viewLifecycleOwner) { count ->
            binding.tvVideosCount.text = "$count videos created"
        }
        viewModel.isPremium.observe(viewLifecycleOwner) { isPremium ->
            binding.btnPremium.visibility = if (isPremium) View.GONE else View.VISIBLE
            binding.tvPremiumBadge.visibility = if (isPremium) View.VISIBLE else View.GONE
            if (isPremium) {
                binding.bannerAdContainer.removeAllViews()
                binding.bannerAdContainer.visibility = View.GONE
            } else if (binding.bannerAdContainer.childCount == 0) {
                setupBannerAd()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        glowAnimator?.cancel()
        glowAnimator = null
        _binding = null
    }
}
