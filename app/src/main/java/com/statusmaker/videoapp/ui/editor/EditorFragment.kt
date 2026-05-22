package com.statusmaker.videoapp.ui.editor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.model.FestivalPresets
import com.statusmaker.videoapp.data.model.MusicStyle
import com.statusmaker.videoapp.data.model.UserInput
import com.statusmaker.videoapp.databinding.FragmentEditorBinding

class EditorFragment : Fragment() {

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditorViewModel by viewModels {
        EditorViewModel.Factory(requireContext())
    }

    private var selectedPhotoUri: Uri? = null

    // Photo picker launcher
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedPhotoUri = uri
                Glide.with(this).load(uri).circleCrop().into(binding.ivSelectedPhoto)
                binding.tvPhotoHint.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val templateId = arguments?.getString("templateId") ?: run {
            findNavController().navigateUp()
            return
        }
        viewModel.loadTemplate(templateId)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Photo picker
        binding.cvPhotoSelector.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            photoPickerLauncher.launch(intent)
        }

        // Festival name dropdown
        val festivalAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            FestivalPresets.list
        )
        binding.actvFestivalName.setAdapter(festivalAdapter)

        // Music style spinner
        val musicStyles = MusicStyle.values()
        val musicAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            musicStyles.map { "${it.displayName} • ${it.teluguName}" }
        )
        musicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMusicStyle.adapter = musicAdapter

        // Preview button
        binding.btnPreview.setOnClickListener {
            val input = buildUserInput() ?: return@setOnClickListener
            val action = EditorFragmentDirections.actionEditorFragmentToPreviewFragment(
                templateId = viewModel.template.value!!.id,
                personName = input.personName,
                villageName = input.villageName,
                businessName = input.businessName,
                festivalName = input.festivalName,
                customMessage = input.customMessage,
                photoUri = input.personPhotoUri ?: "",
                musicStyleOrdinal = input.musicStyle.ordinal
            )
            findNavController().navigate(action)
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun observeViewModel() {
        viewModel.template.observe(viewLifecycleOwner) { template ->
            template ?: return@observe
            binding.tvTemplateName.text = template.name
            binding.tvTemplateTeluguName.text = template.teluguName
            binding.tvCategoryBadge.text = "${template.category.emoji} ${template.category.displayName}"

            // Pre-fill festival name for festival-specific templates
            if (binding.actvFestivalName.text.isEmpty()) {
                binding.actvFestivalName.setText(template.teluguName, false)
            }
        }
    }

    private fun buildUserInput(): UserInput? {
        val name = binding.etPersonName.text?.toString()?.trim() ?: ""
        val village = binding.etVillageName.text?.toString()?.trim() ?: ""
        val business = binding.etBusinessName.text?.toString()?.trim() ?: ""
        val festival = binding.actvFestivalName.text?.toString()?.trim() ?: ""
        val message = binding.etCustomMessage.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            binding.tilPersonName.error = "పేరు నమోదు చేయండి (Enter name)"
            return null
        }
        binding.tilPersonName.error = null

        val musicStyle = MusicStyle.values()[binding.spinnerMusicStyle.selectedItemPosition]

        return UserInput(
            personName = name,
            personPhotoUri = selectedPhotoUri?.toString(),
            villageName = village,
            businessName = business,
            festivalName = festival,
            customMessage = message,
            musicStyle = musicStyle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
