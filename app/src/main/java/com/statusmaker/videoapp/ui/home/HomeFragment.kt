package com.statusmaker.videoapp.ui.home

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryGrid()
        setupBannerAd()
        observeViewModel()

        binding.btnCreateNew.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_templateListFragment)
        }

        binding.btnPremium.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_premiumFragment)
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
        AdManager.getInstance(requireContext()).loadBannerAd(adView)
    }

    private fun observeViewModel() {
        viewModel.totalVideosCreated.observe(viewLifecycleOwner) { count ->
            binding.tvVideosCount.text = "$count videos created"
        }
        viewModel.isPremium.observe(viewLifecycleOwner) { isPremium ->
            binding.btnPremium.visibility = if (isPremium) View.GONE else View.VISIBLE
            binding.tvPremiumBadge.visibility = if (isPremium) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
