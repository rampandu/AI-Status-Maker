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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.model.AppLanguage
import com.statusmaker.videoapp.data.model.FestivalPresets
import com.statusmaker.videoapp.data.model.MusicStyle
import com.statusmaker.videoapp.data.model.UserInput
import com.statusmaker.videoapp.databinding.FragmentEditorBinding
import com.statusmaker.videoapp.utils.AppLanguageStore
import com.statusmaker.videoapp.video.PreviewAudioPlayer

class EditorFragment : Fragment() {

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditorViewModel by viewModels {
        EditorViewModel.Factory(requireContext())
    }

    private var selectedPhotoUri: Uri? = null

    // Music picker + audition state
    private var selectedMusicStyle = MusicStyle.CLASSICAL
    private var auditionPlayer: PreviewAudioPlayer? = null
    private var auditioning = false

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
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
                FestivalPresets.forLanguage(AppLanguageStore.current))
        )

        setupMusicPicker()

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
                    musicStyleOrdinal = input.musicStyle.ordinal,
                    appLanguageOrdinal = input.appLanguage.ordinal
                )
            )
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    // ─── Music style chips + audition ────────────────────────────────────────

    private fun setupMusicPicker() {
        val group = binding.chipGroupMusic
        group.removeAllViews()
        for (style in MusicStyle.values()) {
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                tag = style
                text = "${style.emoji} ${style.displayName}"
                isCheckable = true
                isCheckedIconVisible = false
                chipBackgroundColor =
                    ContextCompat.getColorStateList(requireContext(), R.color.chip_music_bg)
                chipStrokeColor =
                    ContextCompat.getColorStateList(requireContext(), R.color.chip_music_stroke)
                chipStrokeWidth = resources.displayMetrics.density
                setTextColor(
                    ContextCompat.getColorStateList(requireContext(), R.color.chip_music_text))
            }
            group.addView(chip)
            if (style == selectedMusicStyle) chip.isChecked = true
        }
        group.setOnCheckedStateChangeListener { g, checkedIds ->
            val checked = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val style = g.findViewById<Chip>(checked)?.tag as? MusicStyle
                ?: return@setOnCheckedStateChangeListener
            if (style != selectedMusicStyle) {
                selectedMusicStyle = style
                binding.btnAuditionMusic.isEnabled = style != MusicStyle.NONE
                val wasListening = auditioning
                stopAudition()
                if (wasListening && style != MusicStyle.NONE) startAudition()
            }
        }
        binding.btnAuditionMusic.setOnClickListener {
            if (auditioning) stopAudition() else startAudition()
        }
    }

    private fun checkMusicChip(style: MusicStyle) {
        for (i in 0 until binding.chipGroupMusic.childCount) {
            val chip = binding.chipGroupMusic.getChildAt(i) as? Chip ?: continue
            if (chip.tag == style) { chip.isChecked = true; break }
        }
    }

    private fun startAudition() {
        if (selectedMusicStyle == MusicStyle.NONE) return
        stopAudition()
        auditioning = true
        binding.btnAuditionMusic.text = "⏳ Loading…"
        val player = PreviewAudioPlayer(selectedMusicStyle)
        auditionPlayer = player
        player.prepare(viewLifecycleOwner.lifecycleScope) {
            if (_binding != null && auditioning && auditionPlayer === player) {
                player.play()
                binding.btnAuditionMusic.text = "⏹  Stop"
            }
        }
    }

    private fun stopAudition() {
        auditioning = false
        auditionPlayer?.release()
        auditionPlayer = null
        _binding?.btnAuditionMusic?.text = "▶  Listen"
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
            val localizedName = template.displayName(AppLanguageStore.current)
            binding.tvTemplateName.text     = template.name
            binding.tvTemplateTeluguName.text = localizedName
            binding.tvCategoryBadge.text    = "${template.category.emoji} ${template.category.displayName}"

            // FIX: occasion fields (Village/Business/Festival) only make
            // sense for festival/birthday/etc. templates. For quote-mood
            // templates (Good Morning, Love, Attitude...) they're hidden
            // entirely — previously the Telugu quote text was getting
            // auto-stuffed into the Festival field, which looked broken
            // even though it was harmlessly ignored at render time.
            if (template.category.isQuoteMood) {
                binding.occasionFieldsGroup.visibility = View.GONE
                binding.tilCustomMessage.hint = "Your own words (optional)"
                if (binding.etCustomMessage.text.isNullOrEmpty()) {
                    binding.etCustomMessage.hint = localizedName
                }
            } else {
                binding.occasionFieldsGroup.visibility = View.VISIBLE
                binding.tilCustomMessage.hint = resources.getString(R.string.hint_custom_message)
                if (binding.actvFestivalName.text.isEmpty()) {
                    binding.actvFestivalName.setText(localizedName, false)
                }
            }

            // Pre-select the template's recommended music style
            checkMusicChip(template.musicStyle)
        }
    }

    private fun buildUserInput(): UserInput? {
        val name    = binding.etPersonName.text?.toString()?.trim() ?: ""
        val village = binding.etVillageName.text?.toString()?.trim() ?: ""
        val biz     = binding.etBusinessName.text?.toString()?.trim() ?: ""
        val fest    = binding.actvFestivalName.text?.toString()?.trim() ?: ""
        val msg     = binding.etCustomMessage.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            binding.tilPersonName.error = when (AppLanguageStore.current) {
                AppLanguage.HINDI   -> "कृपया नाम दर्ज करें (Enter name)"
                AppLanguage.ENGLISH -> "Please enter a name"
                else                -> "పేరు నమోదు చేయండి (Enter name)"
            }
            binding.tilPersonName.requestFocus()
            return null
        }
        binding.tilPersonName.error = null

        return UserInput(
            personName    = name,
            personPhotoUri = selectedPhotoUri?.toString(),
            villageName   = village,
            businessName  = biz,
            festivalName  = fest,
            customMessage = msg,
            musicStyle    = selectedMusicStyle,
            appLanguage   = AppLanguageStore.current
        )
    }

    override fun onPause() {
        super.onPause()
        stopAudition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAudition()
        _binding = null
    }
}
