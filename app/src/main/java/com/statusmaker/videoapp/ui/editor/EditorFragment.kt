package com.statusmaker.videoapp.ui.editor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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

    // FIX #13: Permission launcher before photo picker
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openPhotoPicker()
        else Toast.makeText(requireContext(), "Gallery permission needed to pick a photo", Toast.LENGTH_SHORT).show()
    }

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Persist permission across restarts
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {}
                selectedPhotoUri = uri
                Glide.with(this).load(uri).circleCrop().into(binding.ivSelectedPhoto)
                binding.tvPhotoHint.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIX #5: safe navigation if templateId is missing
        val templateId = arguments?.getString("templateId") ?: run {
            findNavController().navigateUp(); return
        }
        viewModel.loadTemplate(templateId)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.cvPhotoSelector.setOnClickListener { checkPermissionAndPick() }

        // Festival dropdown
        binding.actvFestivalName.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, FestivalPresets.list)
        )

        // Music spinner
        val styles = MusicStyle.values()
        binding.spinnerMusicStyle.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            styles.map { "🎵 ${it.displayName}  •  ${it.teluguName}" }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.btnPreview.setOnClickListener {
            val input = buildUserInput() ?: return@setOnClickListener
            // FIX #5: null-safe template access
            val tId = viewModel.template.value?.id ?: run {
                Toast.makeText(requireContext(), "Template not loaded, please wait…", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(
                EditorFragmentDirections.actionEditorFragmentToPreviewFragment(
                    templateId       = tId,
                    personName       = input.personName,
                    villageName      = input.villageName,
                    businessName     = input.businessName,
                    festivalName     = input.festivalName,
                    customMessage    = input.customMessage,
                    photoUri         = input.personPhotoUri ?: "",
                    musicStyleOrdinal = input.musicStyle.ordinal
                )
            )
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun checkPermissionAndPick() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED ->
                openPhotoPicker()
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(requireContext(), "Gallery access needed to add your photo", Toast.LENGTH_LONG).show()
                permissionLauncher.launch(permission)
            }
            else -> permissionLauncher.launch(permission)
        }
    }

    private fun openPhotoPicker() {
        photoPickerLauncher.launch(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )
    }

    private fun observeViewModel() {
        viewModel.template.observe(viewLifecycleOwner) { template ->
            template ?: return@observe
            binding.tvTemplateName.text     = template.name
            binding.tvTemplateTeluguName.text = template.teluguName
            binding.tvCategoryBadge.text    = "${template.category.emoji} ${template.category.displayName}"

            // Pre-fill festival name only if field is empty
            if (binding.actvFestivalName.text.isEmpty()) {
                binding.actvFestivalName.setText(template.teluguName, false)
            }

            // Pre-select matching music style
            val defaultStyle = template.musicStyle
            val idx = MusicStyle.values().indexOfFirst { it == defaultStyle }
            if (idx >= 0) binding.spinnerMusicStyle.setSelection(idx)
        }
    }

    private fun buildUserInput(): UserInput? {
        val name    = binding.etPersonName.text?.toString()?.trim() ?: ""
        val village = binding.etVillageName.text?.toString()?.trim() ?: ""
        val biz     = binding.etBusinessName.text?.toString()?.trim() ?: ""
        val fest    = binding.actvFestivalName.text?.toString()?.trim() ?: ""
        val msg     = binding.etCustomMessage.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            binding.tilPersonName.error = "పేరు నమోదు చేయండి (Enter name)"
            binding.tilPersonName.requestFocus()
            return null
        }
        binding.tilPersonName.error = null

        val music = MusicStyle.values()[binding.spinnerMusicStyle.selectedItemPosition]
        return UserInput(
            personName    = name,
            personPhotoUri = selectedPhotoUri?.toString(),
            villageName   = village,
            businessName  = biz,
            festivalName  = fest,
            customMessage = msg,
            musicStyle    = music
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
