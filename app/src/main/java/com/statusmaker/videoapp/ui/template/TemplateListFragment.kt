package com.statusmaker.videoapp.ui.template

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.ads.AdManager
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.model.TemplateCategory
import com.statusmaker.videoapp.databinding.FragmentTemplateListBinding
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.launch

class TemplateListFragment : Fragment() {

    private var _binding: FragmentTemplateListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TemplateListViewModel by viewModels {
        TemplateListViewModel.Factory(requireContext())
    }

    private lateinit var templateAdapter: TemplateAdapter
    private var isPremiumUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTemplateListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialCategory = arguments?.getString("category")?.let {
            try { TemplateCategory.valueOf(it) } catch (_: Exception) { null }
        }

        templateAdapter = TemplateAdapter(
            onTemplateClick = { template -> onTemplateSelected(template) },
            onFavoriteToggle = { template, currentlyFavorite ->
                viewModel.toggleFavorite(template, currentlyFavorite)
            }
        )
        binding.rvTemplates.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = templateAdapter
        }

        buildCategoryChips()
        observePremiumAndLoadAds()
        setupBackPressInterstitial()

        viewModel.loadTemplates(initialCategory)

        viewModel.templates.observe(viewLifecycleOwner) { list ->
            templateAdapter.submitList(list)
            binding.tvTemplateCount.text = "${list.size} Templates"
            binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.favoriteIds.observe(viewLifecycleOwner) { ids ->
            templateAdapter.updateFavorites(ids)
        }

        viewModel.selectedCategory.observe(viewLifecycleOwner) { cat ->
            binding.tvCategoryTitle.text = cat?.displayName ?: "All Templates"
            for (i in 0 until binding.chipGroupCategories.childCount) {
                val chip = binding.chipGroupCategories.getChildAt(i) as? Chip ?: continue
                val chipCategory = chip.tag as? TemplateCategory
                chip.isChecked = chipCategory == cat
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /**
     * New revenue surface: a banner on the screen users browse templates on
     * longest. Premium users never see it — the container is never even
     * populated for them, so no ad request is made on their behalf either.
     */
    private fun observePremiumAndLoadAds() {
        viewLifecycleOwner.lifecycleScope.launch {
            PreferenceManager(requireContext()).isPremium.collect { isPremium ->
                isPremiumUser = isPremium
                if (_binding == null) return@collect
                if (isPremium) {
                    binding.bannerAdContainer.removeAllViews()
                    binding.bannerAdContainer.visibility = View.GONE
                } else {
                    // Warm the exit interstitial here — on the screen that
                    // shows it — instead of preloading at app launch.
                    AdManager.getInstance(requireContext()).loadInterstitialAd()
                    if (binding.bannerAdContainer.childCount == 0) setupBannerAd()
                }
            }
        }
    }

    private fun setupBannerAd() {
        AdManager.getInstance(requireContext()).attachAdaptiveBanner(
            binding.bannerAdContainer,
            onLoaded = { if (_binding != null) binding.bannerAdContainer.visibility = View.VISIBLE },
            onFailed = { if (_binding != null) binding.bannerAdContainer.visibility = View.GONE }
        )
    }

    /**
     * New revenue surface: an interstitial on exiting this screen (browsing
     * without converting to a created video). Shares the same 3-minute
     * cooldown as every other interstitial trigger in the app, so adding
     * this trigger point increases the *chance* one fires at a natural
     * break, not the total frequency. Skipped entirely for premium users.
     */
    private fun setupBackPressInterstitial() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    isEnabled = false
                    if (isPremiumUser) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        AdManager.getInstance(requireContext()).showInterstitialAd(requireActivity()) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            })
    }

    private fun buildCategoryChips() {
        binding.chipGroupCategories.removeAllViews()

        val allChip = Chip(requireContext()).apply {
            text = "All"; tag = null; isCheckable = true; isChecked = true
            applyChipStyle()
            setOnClickListener { viewModel.loadTemplates(null) }
        }
        binding.chipGroupCategories.addView(allChip)

        TemplateCategory.values().forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = "${category.emoji} ${category.displayName}"
                tag = category; isCheckable = true
                applyChipStyle()
                setOnClickListener { viewModel.loadTemplates(category) }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    /**
     * Material's Chip only accepts a flat ColorStateList for its background
     * (not an arbitrary gradient drawable), so selected/unselected states
     * are driven by color selectors instead.
     */
    private fun Chip.applyChipStyle() {
        chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_bg_selector)
        setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector))
        chipStrokeWidth = resources.displayMetrics.density   // 1dp
        chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_stroke_selector)
        rippleColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_stroke_selector)
    }

    private fun onTemplateSelected(template: Template) {
        findNavController().navigate(
            TemplateListFragmentDirections.actionTemplateListFragmentToEditorFragment(template.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
