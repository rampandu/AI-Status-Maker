package com.statusmaker.videoapp.ui.template

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.model.TemplateCategory
import com.statusmaker.videoapp.databinding.FragmentTemplateListBinding

class TemplateListFragment : Fragment() {

    private var _binding: FragmentTemplateListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TemplateListViewModel by viewModels {
        TemplateListViewModel.Factory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplateListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryArg = arguments?.getString("category")
        val category = categoryArg?.let {
            try { TemplateCategory.valueOf(it) } catch (e: Exception) { null }
        }

        viewModel.loadTemplates(category)

        val adapter = TemplateAdapter { template -> onTemplateSelected(template) }
        binding.rvTemplates.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
        }

        setupCategoryChips()

        viewModel.templates.observe(viewLifecycleOwner) { templates ->
            adapter.submitList(templates)
            binding.tvTemplateCount.text = "${templates.size} Templates"
            binding.emptyView.visibility = if (templates.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.selectedCategory.observe(viewLifecycleOwner) { cat ->
            binding.tvCategoryTitle.text = cat?.displayName ?: "All Templates"
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupCategoryChips() {
        val allCategories = listOf(null) + TemplateCategory.values().toList()
        binding.chipGroupCategories.removeAllViews()

        allCategories.forEach { category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = if (category == null) "All" else "${category.emoji} ${category.displayName}"
                isCheckable = true
                isChecked = category == viewModel.selectedCategory.value
                setOnClickListener { viewModel.loadTemplates(category) }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun onTemplateSelected(template: Template) {
        val action = TemplateListFragmentDirections
            .actionTemplateListFragmentToEditorFragment(template.id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
