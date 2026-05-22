package com.statusmaker.videoapp.ui.myvideos

import android.content.Context
import androidx.lifecycle.*
import com.statusmaker.videoapp.utils.FileUtils
import java.io.File

class MyVideosViewModel(private val context: Context) : ViewModel() {

    private val _videos = MutableLiveData<List<File>>(emptyList())
    val videos: LiveData<List<File>> = _videos

    init { loadVideos() }

    fun loadVideos() {
        _videos.value = FileUtils.getSavedVideos(context)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MyVideosViewModel(context) as T
    }
}
