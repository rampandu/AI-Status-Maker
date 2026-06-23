package com.statusmaker.videoapp.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.databinding.FragmentFavoritesBinding
import com.statusmaker.videoapp.ui.template.TemplateAdapter

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels {
        FavoritesViewModel.Factory(requireContext())
    }

    private lateinit var adapter: TemplateAdapter

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
