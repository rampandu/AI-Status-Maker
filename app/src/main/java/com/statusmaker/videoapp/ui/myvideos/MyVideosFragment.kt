package com.statusmaker.videoapp.ui.myvideos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.statusmaker.videoapp.databinding.FragmentMyVideosBinding

class MyVideosFragment : Fragment() {

    private var _binding: FragmentMyVideosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyVideosViewModel by viewModels {
        MyVideosViewModel.Factory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val adapter = MyVideosAdapter(
            onPlay    = { file -> playVideo(file.absolutePath) },
            onShare   = { file -> shareVideo(file.absolutePath) },
            onWhatsApp = { file -> shareToWhatsApp(file.absolutePath) }
        )

        binding.rvMyVideos.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter  = adapter
        }

        viewModel.videos.observe(viewLifecycleOwner) { files ->
            adapter.submitList(files)
            binding.emptyState.visibility  = if (files.isEmpty()) View.VISIBLE else View.GONE
            binding.rvMyVideos.visibility  = if (files.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun playVideo(path: String) {
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider",
            java.io.File(path)
        )
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    }

    private fun shareVideo(path: String) {
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider",
            java.io.File(path)
        )
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Share Video"))
    }

    private fun shareToWhatsApp(path: String) {
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider",
            java.io.File(path)
        )
        try {
            startActivity(Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            })
        } catch (_: Exception) { shareVideo(path) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadVideos()  // refresh when coming back from export
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
