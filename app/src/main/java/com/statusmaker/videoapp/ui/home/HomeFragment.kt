package com.statusmaker.videoapp.ui.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.ads.AdManager
import com.statusmaker.videoapp.data.model.CategorySection
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.databinding.FragmentHomeBinding
import com.statusmaker.videoapp.ui.template.TemplateListFragmentDirections

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory(requireContext()) }
    private var glowAnimator: ObjectAnimator? = null
    private lateinit var sectionsAdapter: HomeSectionsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSectionsFeed()
        setupSearch()
        observePremiumAndAds()
        startGlowPulse()

        binding.btnPremium.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_premiumFragment)
        }
    }

    private fun setupSectionsFeed() {
        sectionsAdapter = HomeSectionsAdapter(
            onTemplateClick = { template -> onTemplateSelected(template) },
            onFavoriteToggle = { template, currentlyFavorite ->
                viewModel.toggleFavorite(template, currentlyFavorite)
            },
            onSeeAllClick = { section -> onSeeAllClicked(section) }
        )
        binding.rvSections.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionsAdapter
            setHasFixedSize(true)
        }

        viewModel.sections.observe(viewLifecycleOwner) { sections ->
            sectionsAdapter.submitSections(sections)
            val query = viewModel.searchQuery.value ?: ""
            binding.emptySearchView.visibility =
                if (sections.isEmpty() && query.isNotBlank()) View.VISIBLE else View.GONE
            binding.rvSections.visibility =
                if (sections.isEmpty() && query.isNotBlank()) View.GONE else View.VISIBLE
        }

        viewModel.favoriteIds.observe(viewLifecycleOwner) { ids ->
            sectionsAdapter.updateFavorites(ids)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s?.toString() ?: "")
            }
        })
    }

    private fun onTemplateSelected(template: Template) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToEditorFragment(template.id)
        )
    }

    private fun onSeeAllClicked(section: CategorySection) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToTemplateListFragment(section.category.name)
        )
    }

    /**
     * The one signature motion on this screen: a slow, restrained pulse on
     * the marigold glow behind the header.
     */
    private fun startGlowPulse() {
        glowAnimator = ObjectAnimator.ofFloat(binding.heroGlow, View.ALPHA, 0.5f, 1f).apply {
            duration = 2400L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

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

    private fun setupBannerAd() {
        AdManager.getInstance(requireContext()).attachAdaptiveBanner(
            binding.bannerAdContainer,
            onLoaded = { if (_binding != null) binding.bannerAdContainer.visibility = View.VISIBLE },
            onFailed = { if (_binding != null) binding.bannerAdContainer.visibility = View.GONE }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        glowAnimator?.cancel()
        glowAnimator = null
        _binding = null
    }
}
