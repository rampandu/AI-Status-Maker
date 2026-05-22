package com.statusmaker.videoapp.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    private const val OUTPUT_DIR = "StatusMaker"

    fun getOutputDir(context: Context): File {
        val dir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
            File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), OUTPUT_DIR)
        else
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), OUTPUT_DIR)
        if (!dir.exists()) dir.mkdirs()  // FIX #10: always create
        return dir
    }

    fun getOutputVideoFile(context: Context): File {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(getOutputDir(context), "STATUS_$ts.mp4")
    }

    fun getThumbnailFile(context: Context): File {
        val dir = File(context.cacheDir, "thumbnails")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "thumb_${System.currentTimeMillis()}.jpg")
    }

    // FIX #10: uses getOutputDir which now always creates directory
    fun getSavedVideos(context: Context): List<File> =
        getOutputDir(context)
            .listFiles { f -> f.extension == "mp4" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1_024       -> "$bytes B"
        bytes < 1_048_576   -> "${bytes / 1_024} KB"
        else                -> String.format("%.1f MB", bytes / 1_048_576.0)
    }
}
