package com.statusmaker.videoapp.ui.myvideos

import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.statusmaker.videoapp.databinding.ItemMyVideoBinding
import com.statusmaker.videoapp.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MyVideosAdapter(
    private val onPlay:     (File) -> Unit,
    private val onShare:    (File) -> Unit,
    private val onWhatsApp: (File) -> Unit
) : ListAdapter<File, MyVideosAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemMyVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMyVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val file = currentList[position]
        val b    = holder.binding

        // Generate thumbnail on bind (fast for small grids)
        try {
            val thumb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ThumbnailUtils.createVideoThumbnail(file, Size(320, 180), null)
            else
                @Suppress("DEPRECATION")
                ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Images.Thumbnails.MINI_KIND)
            b.ivThumbnail.setImageBitmap(thumb)
        } catch (_: Exception) {}

        b.tvVideoName.text = file.nameWithoutExtension
        b.tvFileSize.text  = FileUtils.formatFileSize(file.length())
        b.tvVideoDate.text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(file.lastModified()))

        b.cardVideo.setOnClickListener  { onPlay(file) }
        b.btnShare.setOnClickListener   { onShare(file) }
        b.btnWhatsApp.setOnClickListener { onWhatsApp(file) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(a: File, b: File)    = a.absolutePath == b.absolutePath
            override fun areContentsTheSame(a: File, b: File) = a.lastModified() == b.lastModified()
        }
    }
}
