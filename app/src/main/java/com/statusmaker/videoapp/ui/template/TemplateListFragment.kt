package com.statusmaker.videoapp.ui.template

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.model.TemplateCategory
import com.statusmaker.videoapp.databinding.FragmentTemplateListBinding

class TemplateListFragment : Fragment() {

    private var _binding: FragmentTemplateListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TemplateListViewModel by viewModels {
        TemplateListViewModel.Factory(requireContext())
    }

    private lateinit var templateAdapter: TemplateAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTemplateListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialCategory = arguments?.getString("category")?.let {
            try { TemplateCategory.valueOf(it) } catch (_: Exception) { null }
        }

        templateAdapter = TemplateAdapter { template -> onTemplateSelected(template) }
        binding.rvTemplates.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = templateAdapter
        }

        // FIX #8: build chips ONCE at setup time, not on every category change
        buildCategoryChips()

        viewModel.loadTemplates(initialCategory)

        viewModel.templates.observe(viewLifecycleOwner) { list ->
            templateAdapter.submitList(list)
            binding.tvTemplateCount.text = "${list.size} Templates"
            binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.selectedCategory.observe(viewLifecycleOwner) { cat ->
            binding.tvCategoryTitle.text = cat?.displayName ?: "All Templates"
            // Update chip check state without recreating chips
            for (i in 0 until binding.chipGroupCategories.childCount) {
                val chip = binding.chipGroupCategories.getChildAt(i) as? Chip ?: continue
                val chipCategory = chip.tag as? TemplateCategory
                chip.isChecked = chipCategory == cat
            }
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun buildCategoryChips() {
        binding.chipGroupCategories.removeAllViews()

        // "All" chip
        val allChip = Chip(requireContext()).apply {
            text = "All"; tag = null; isCheckable = true; isChecked = true
            setOnClickListener { viewModel.loadTemplates(null) }
        }
        binding.chipGroupCategories.addView(allChip)

        // Category chips
        TemplateCategory.values().forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = "${category.emoji} ${category.displayName}"
                tag = category; isCheckable = true
                setOnClickListener { viewModel.loadTemplates(category) }
            }
            binding.chipGroupCategories.addView(chip)
        }
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
