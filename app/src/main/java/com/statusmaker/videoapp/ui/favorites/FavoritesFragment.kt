package com.statusmaker.videoapp.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.statusmaker.videoapp.ads.AdManager
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.databinding.FragmentFavoritesBinding
import com.statusmaker.videoapp.ui.template.TemplateAdapter
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModel.Factory(requireContext())
    }

    private lateinit var adapter: TemplateAdapter
    private var isPremiumUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TemplateAdapter(
            onTemplateClick = { template -> onTemplateSelected(template) },
            onFavoriteToggle = { template, currentlyFavorite ->
                viewModel.toggleFavorite(template, currentlyFavorite)
            }
        )
        binding.rvFavorites.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@FavoritesFragment.adapter
        }

        viewModel.favorites.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            adapter.updateFavorites(list.map { it.id }.toSet())
            binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvFavorites.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        observePremiumAndLoadAds()
        setupBackPressInterstitial()
    }

    /**
     * Favorites now occupies the bottom-nav slot Templates used to hold —
     * it needs the same banner + back-press interstitial pattern Templates
     * had, premium-gated exactly the same way. See TemplateListFragment /
     * MyVideosFragment for the identical pattern.
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

    private fun onTemplateSelected(template: Template) {
        findNavController().navigate(
            FavoritesFragmentDirections.actionFavoritesFragmentToEditorFragment(template.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
