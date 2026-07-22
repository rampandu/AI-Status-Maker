package com.statusmaker.videoapp.ui.preview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.ads.AdManager
import com.statusmaker.videoapp.data.model.MusicStyle
import com.statusmaker.videoapp.data.model.UserInput
import com.statusmaker.videoapp.databinding.FragmentPreviewBinding
import com.statusmaker.videoapp.utils.RatingHelper
import com.statusmaker.videoapp.video.PreviewAudioPlayer
import com.statusmaker.videoapp.video.VideoGenState
import java.io.File

class PreviewFragment : Fragment() {

    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PreviewViewModel by viewModels {
        PreviewViewModel.Factory(requireContext())
    }

    private var exportedFilePath: String? = null
    private var selectedMusicStyle = MusicStyle.NONE
    private var previewAudioPlayer: PreviewAudioPlayer? = null
    private var audioReady = false
    private var exoPlayer: ExoPlayer? = null

    private val previewHandler = Handler(Looper.getMainLooper())
    private var previewFrameIndex = 0
    private val previewRunnable = object : Runnable {
        override fun run() {
            if (_binding == null) return
            viewModel.renderPreviewFrameAt(previewFrameIndex) { bitmap ->
                _binding?.ivPreviewFrame?.setImageBitmap(bitmap)
            }
            previewFrameIndex++
            previewHandler.postDelayed(this, 100L) // 10fps
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args           = arguments ?: return
        val templateId     = args.getString("templateId") ?: return
        val musicStyle     = MusicStyle.values()[args.getInt("musicStyleOrdinal", 0)]
        selectedMusicStyle = musicStyle

        val userInput = UserInput(
            personName     = args.getString("personName") ?: "",
            personPhotoUri = args.getString("photoUri")?.ifEmpty { null },
            villageName    = args.getString("villageName") ?: "",
            businessName   = args.getString("businessName") ?: "",
            festivalName   = args.getString("festivalName") ?: "",
            customMessage  = args.getString("customMessage") ?: "",
            musicStyle     = musicStyle
        )

        viewModel.loadTemplate(templateId)
        viewModel.setUserInput(userInput)

        previewAudioPlayer = PreviewAudioPlayer(musicStyle)
        previewAudioPlayer!!.prepare(viewLifecycleOwner.lifecycleScope) {
            audioReady = true
            if (exoPlayer == null) previewAudioPlayer?.play()
        }

        // FIX #9: intercept back press during active export
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.isExporting) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Export in progress")
                            .setMessage("Cancel the export and go back?")
                            .setPositiveButton("Cancel Export") { _, _ ->
                                viewModel.cancelExport()
                                isEnabled = false
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                            .setNegativeButton("Keep Exporting", null)
                            .show()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            })

        setupUI()
        observeViewModel()

        viewModel.template.observe(viewLifecycleOwner) { template ->
            template ?: return@observe
            if (exoPlayer == null) startAnimatedPreview()
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnExport.setOnClickListener      { showRewardedAdThenExport() }
        binding.btnWhatsApp.setOnClickListener    { exportedFilePath?.let { shareToWhatsApp(it) } ?: notExportedYet() }
        binding.btnShare.setOnClickListener       { exportedFilePath?.let { shareVideo(it) }      ?: notExportedYet() }
        binding.btnEditAgain.setOnClickListener {
            // FIX: this previously fired for premium users too, directly
            // contradicting the Premium screen's "no ads" pitch. Now it
            // checks isPremium first — the 3-min cooldown in AdManager
            // still prevents this from feeling spammy for free users.
            if (viewModel.isPremium.value == true) {
                findNavController().navigateUp()
            } else {
                AdManager.getInstance(requireContext()).showInterstitialAd(requireActivity()) {
                    if (isAdded) findNavController().navigateUp()
                }
            }
        }
    }

    private fun notExportedYet() =
        Toast.makeText(requireContext(), "Export the video first!", Toast.LENGTH_SHORT).show()

    private fun startAnimatedPreview() {
        previewFrameIndex = 0
        previewHandler.removeCallbacks(previewRunnable)
        previewHandler.post(previewRunnable)
        if (audioReady) previewAudioPlayer?.play()
    }

    private fun stopAnimatedPreview() {
        previewHandler.removeCallbacks(previewRunnable)
    }

    private fun initExoPlayer(filePath: String) {
        stopAnimatedPreview()
        previewAudioPlayer?.pause()

        val player = ExoPlayer.Builder(requireContext()).build()
        exoPlayer = player
        binding.playerView.player = player
        binding.ivPreviewFrame.visibility     = View.GONE
        binding.tvLivePreviewBadge.visibility = View.GONE
        binding.playerView.visibility         = View.VISIBLE

        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(filePath))))
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        player.play()
    }

    private fun releaseExoPlayer() {
        exoPlayer?.release(); exoPlayer = null
    }

    private fun observeViewModel() {
        viewModel.template.observe(viewLifecycleOwner) { t ->
            t ?: return@observe
            binding.tvPreviewTemplateName.text = t.name
            binding.tvPreviewCategory.text =
                "${t.category.emoji} ${t.category.displayName}  •  " +
                "${selectedMusicStyle.emoji} ${selectedMusicStyle.displayName}"
        }

        viewModel.exportState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VideoGenState.Progress -> {
                    binding.exportProgress.visibility  = View.VISIBLE
                    binding.exportProgressBar.progress = state.percent
                    binding.tvExportStatus.text        = state.message
                    binding.btnExport.isEnabled        = false
                }
                is VideoGenState.Success -> {
                    exportedFilePath = state.outputPath
                    binding.exportProgress.visibility     = View.GONE
                    binding.btnExport.isEnabled           = true
                    binding.exportSuccessGroup.visibility = View.VISIBLE
                    binding.btnExport.text = "✓ Export Again"
                    Toast.makeText(requireContext(), "🎉 Video saved!", Toast.LENGTH_LONG).show()
                    initExoPlayer(state.outputPath)
                    // FIX #12: ask for rating after successful exports
                    RatingHelper.maybeAskForRating(requireActivity())
                }
                is VideoGenState.Error -> {
                    binding.exportProgress.visibility = View.GONE
                    binding.btnExport.isEnabled       = true
                    Toast.makeText(requireContext(), "Export failed: ${state.message}", Toast.LENGTH_LONG).show()
                }
                null -> {}
            }
        }

        viewModel.isPremium.observe(viewLifecycleOwner) { isPremium ->
            binding.cardAdNotice.visibility = if (isPremium) View.GONE else View.VISIBLE
            if (!isPremium) {
                // Warm the ads THIS screen shows — rewarded (export) and
                // interstitial (Edit Again) — instead of app-launch preloads.
                // By the time the user taps Export, the rewarded ad is ready
                // and the 8-second "Loading ad…" wait rarely happens.
                val adManager = AdManager.getInstance(requireContext())
                adManager.loadRewardedAd()
                adManager.loadInterstitialAd()
            }
        }
    }

    private fun showRewardedAdThenExport() {
        if (viewModel.isPremium.value == true) { startExport(false); return }

        val adManager = AdManager.getInstance(requireContext())

        if (adManager.isRewardedAdReady()) {
            showLoadedRewardedAd(adManager)
            return
        }

        // FIX: previously this branch called loadRewardedAd() and showed a
        // toast, then did nothing — the user was stuck until they manually
        // tapped Export again. Now we actually wait for the load (with a
        // timeout) and automatically continue once it resolves.
        binding.btnExport.isEnabled = false
        binding.btnExport.text = "⏳ Loading ad…"

        var resolved = false
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (resolved) return@Runnable
            resolved = true
            if (_binding != null) {
                resetExportButton()
                startExport(true)
                Toast.makeText(requireContext(), "Ad took too long to load. Exporting with watermark.", Toast.LENGTH_SHORT).show()
            }
        }
        timeoutHandler.postDelayed(timeoutRunnable, 8000L) // 8s max wait

        adManager.loadRewardedAd(
            onLoaded = {
                if (resolved) return@loadRewardedAd
                resolved = true
                timeoutHandler.removeCallbacks(timeoutRunnable)
                if (_binding != null) {
                    resetExportButton()
                    showLoadedRewardedAd(adManager)
                }
            },
            onFailed = {
                if (resolved) return@loadRewardedAd
                resolved = true
                timeoutHandler.removeCallbacks(timeoutRunnable)
                if (_binding != null) {
                    resetExportButton()
                    startExport(true)
                    Toast.makeText(requireContext(), "Ad unavailable. Exporting with watermark.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun resetExportButton() {
        binding.btnExport.isEnabled = true
        binding.btnExport.text = "🎬 Export Video"
    }

    private fun showLoadedRewardedAd(adManager: AdManager) {
        adManager.showRewardedAd(
            activity       = requireActivity(),
            onRewarded     = { if (_binding != null) startExport(false) },
            onAdSkipped    = {
                if (_binding != null) {
                    startExport(true)
                    Toast.makeText(requireContext(), "Watch full ad for watermark-free export!", Toast.LENGTH_LONG).show()
                }
            },
            onAdNotAvailable = {
                if (_binding != null) {
                    startExport(true)
                    Toast.makeText(requireContext(), "Ad unavailable. Exporting with watermark.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun startExport(addWatermark: Boolean) {
        if (exoPlayer != null) {
            releaseExoPlayer()
            binding.playerView.visibility         = View.GONE
            binding.ivPreviewFrame.visibility     = View.VISIBLE
            binding.tvLivePreviewBadge.visibility = View.VISIBLE
            startAnimatedPreview()
        }
        viewModel.exportVideo(addWatermark)
    }

    private fun shareVideo(filePath: String) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", File(filePath))
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Share Status Video"))
    }

    private fun shareToWhatsApp(filePath: String) {
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", File(filePath))
        try {
            startActivity(Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            })
        } catch (_: Exception) { shareVideo(filePath) }
    }

    override fun onPause() {
        super.onPause()
        stopAnimatedPreview()
        previewAudioPlayer?.pause()
        exoPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (exoPlayer != null) exoPlayer?.play()
        else startAnimatedPreview()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAnimatedPreview()
        previewAudioPlayer?.release(); previewAudioPlayer = null
        releaseExoPlayer()
        _binding = null
    }
}
