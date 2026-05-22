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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.statusmaker.videoapp.ads.AdManager
import com.statusmaker.videoapp.data.model.MusicStyle
import com.statusmaker.videoapp.data.model.UserInput
import com.statusmaker.videoapp.databinding.FragmentPreviewBinding
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

    // ── Audio ────────────────────────────────────────────────────────────────
    private var previewAudioPlayer: PreviewAudioPlayer? = null
    private var audioReady = false

    // ── Video player (post-export) ────────────────────────────────────────────
    private var exoPlayer: ExoPlayer? = null

    // ── Animated frame preview (pre-export) ──────────────────────────────────
    private val previewHandler = Handler(Looper.getMainLooper())
    private var previewFrameIndex = 0
    private val previewFps = 10
    private val previewRunnable = object : Runnable {
        override fun run() {
            if (_binding == null) return
            viewModel.renderPreviewFrameAt(previewFrameIndex) { bitmap ->
                _binding?.ivPreviewFrame?.setImageBitmap(bitmap)
            }
            previewFrameIndex++
            previewHandler.postDelayed(this, (1000L / previewFps))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments ?: return
        val templateId        = args.getString("templateId") ?: return
        val personName        = args.getString("personName") ?: ""
        val villageName       = args.getString("villageName") ?: ""
        val businessName      = args.getString("businessName") ?: ""
        val festivalName      = args.getString("festivalName") ?: ""
        val customMessage     = args.getString("customMessage") ?: ""
        val photoUri          = args.getString("photoUri")?.ifEmpty { null }
        val musicStyleOrdinal = args.getInt("musicStyleOrdinal", 0)
        val musicStyle        = MusicStyle.values()[musicStyleOrdinal]

        val userInput = UserInput(
            personName       = personName,
            personPhotoUri   = photoUri,
            villageName      = villageName,
            businessName     = businessName,
            festivalName     = festivalName,
            customMessage    = customMessage,
            musicStyle       = musicStyle
        )

        viewModel.loadTemplate(templateId)
        viewModel.setUserInput(userInput)

        // Prepare audio player immediately so it's ready when preview starts
        previewAudioPlayer = PreviewAudioPlayer(musicStyle)
        previewAudioPlayer!!.prepare(viewLifecycleOwner.lifecycleScope) {
            audioReady = true
            // Auto-play if preview animation is already running
            if (exoPlayer == null) previewAudioPlayer?.play()
        }

        setupUI()
        observeViewModel()

        // Start visual animation once template loads
        viewModel.template.observe(viewLifecycleOwner) { template ->
            template ?: return@observe
            if (exoPlayer == null) startAnimatedPreview()
        }
    }

    // ─── UI setup ─────────────────────────────────────────────────────────────

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnExport.setOnClickListener       { showRewardedAdThenExport() }
        binding.btnShare.setOnClickListener {
            exportedFilePath?.let { shareVideo(it) }
                ?: Toast.makeText(context, "Export the video first!", Toast.LENGTH_SHORT).show()
        }
        binding.btnWhatsApp.setOnClickListener {
            exportedFilePath?.let { shareToWhatsApp(it) }
                ?: Toast.makeText(context, "Export the video first!", Toast.LENGTH_SHORT).show()
        }
        binding.btnEditAgain.setOnClickListener { findNavController().navigateUp() }
    }

    // ─── Animated preview ─────────────────────────────────────────────────────

    private fun startAnimatedPreview() {
        previewFrameIndex = 0
        previewHandler.removeCallbacks(previewRunnable)
        previewHandler.post(previewRunnable)
        // Audio: play if already prepared, else it will auto-play from onReady callback
        if (audioReady) previewAudioPlayer?.play()
    }

    private fun stopAnimatedPreview() {
        previewHandler.removeCallbacks(previewRunnable)
    }

    // ─── ExoPlayer (post-export) ──────────────────────────────────────────────

    private fun initExoPlayer(filePath: String) {
        // Stop preview animation + preview audio
        stopAnimatedPreview()
        previewAudioPlayer?.pause()   // mute preview audio; ExoPlayer has its own audio

        val player = ExoPlayer.Builder(requireContext()).build()
        exoPlayer = player
        binding.playerView.player = player
        binding.ivPreviewFrame.visibility   = View.GONE
        binding.tvLivePreviewBadge.visibility = View.GONE
        binding.playerView.visibility       = View.VISIBLE

        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(filePath))))
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        player.play()
    }

    private fun releaseExoPlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    // ─── Observe ViewModel ────────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.template.observe(viewLifecycleOwner) { template ->
            template ?: return@observe
            binding.tvPreviewTemplateName.text = template.name
            binding.tvPreviewCategory.text =
                "${template.category.emoji} ${template.category.displayName}"
        }

        viewModel.exportState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VideoGenState.Progress -> {
                    binding.exportProgress.visibility   = View.VISIBLE
                    binding.exportProgressBar.progress  = state.percent
                    binding.tvExportStatus.text         = state.message
                    binding.btnExport.isEnabled         = false
                    // Audio keeps playing during export — nothing to stop
                }
                is VideoGenState.Success -> {
                    exportedFilePath = state.outputPath
                    binding.exportProgress.visibility  = View.GONE
                    binding.btnExport.isEnabled        = true
                    binding.exportSuccessGroup.visibility = View.VISIBLE
                    binding.btnExport.text = "✓ Export Again"
                    Toast.makeText(context, "Video saved! 🎉", Toast.LENGTH_LONG).show()
                    initExoPlayer(state.outputPath)   // switch to real video with audio
                }
                is VideoGenState.Error -> {
                    binding.exportProgress.visibility = View.GONE
                    binding.btnExport.isEnabled       = true
                    Toast.makeText(context,
                        "Export failed: ${state.message}", Toast.LENGTH_LONG).show()
                }
                null -> {}
            }
        }

        viewModel.isPremium.observe(viewLifecycleOwner) { isPremium ->
            binding.cardAdNotice.visibility = if (isPremium) View.GONE else View.VISIBLE
        }
    }

    // ─── Export flow ──────────────────────────────────────────────────────────

    private fun showRewardedAdThenExport() {
        if (viewModel.isPremium.value == true) {
            startExport(addWatermark = false); return
        }
        if (AdManager.getInstance(requireContext()).isRewardedAdReady()) {
            AdManager.getInstance(requireContext()).showRewardedAd(
                activity       = requireActivity(),
                onRewarded     = { startExport(addWatermark = false) },
                onAdSkipped    = {
                    startExport(addWatermark = true)
                    Toast.makeText(context, "Watch full ad for watermark-free export!",
                        Toast.LENGTH_LONG).show()
                },
                onAdNotAvailable = {
                    startExport(addWatermark = true)
                    Toast.makeText(context, "Ad unavailable. Exporting with watermark.",
                        Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            AdManager.getInstance(requireContext()).loadRewardedAd()
            Toast.makeText(context, "Loading ad, please try again in a moment.",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun startExport(addWatermark: Boolean) {
        // If re-exporting: restore animated preview and preview audio
        if (exoPlayer != null) {
            releaseExoPlayer()
            binding.playerView.visibility       = View.GONE
            binding.ivPreviewFrame.visibility   = View.VISIBLE
            binding.tvLivePreviewBadge.visibility = View.VISIBLE
            startAnimatedPreview()  // this also restarts preview audio
        }
        viewModel.exportVideo(addWatermark)
    }

    // ─── Share helpers ────────────────────────────────────────────────────────

    private fun shareVideo(filePath: String) {
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", File(filePath)
        )
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Share Status Video"))
    }

    private fun shareToWhatsApp(filePath: String) {
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", File(filePath)
        )
        try {
            startActivity(Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            })
        } catch (e: Exception) { shareVideo(filePath) }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onPause() {
        super.onPause()
        stopAnimatedPreview()
        previewAudioPlayer?.pause()
        exoPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (exoPlayer != null) {
            exoPlayer?.play()
        } else {
            startAnimatedPreview()   // this also calls previewAudioPlayer?.play() if ready
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAnimatedPreview()
        previewAudioPlayer?.release()
        previewAudioPlayer = null
        releaseExoPlayer()
        _binding = null
    }
}
