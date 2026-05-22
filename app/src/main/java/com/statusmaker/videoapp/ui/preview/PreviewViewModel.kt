package com.statusmaker.videoapp.ui.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.*
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.model.UserInput
import com.statusmaker.videoapp.data.repository.TemplateRepository
import com.statusmaker.videoapp.utils.PreferenceManager
import com.statusmaker.videoapp.video.FrameRenderer
import com.statusmaker.videoapp.video.VideoGenState
import com.statusmaker.videoapp.video.VideoGenerator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class PreviewViewModel(private val context: Context) : ViewModel() {

    private val repository  = TemplateRepository(context)
    private val prefManager = PreferenceManager(context)
    val videoGenerator      = VideoGenerator(context)

    private val _template     = MutableLiveData<Template?>(null)
    val template: LiveData<Template?> = _template

    private val _userInput    = MutableLiveData<UserInput>()

    private val _exportState  = MutableLiveData<VideoGenState?>(null)
    val exportState: LiveData<VideoGenState?> = _exportState

    private val _isPremium    = MutableLiveData(false)
    val isPremium: LiveData<Boolean> = _isPremium

    private var cachedUserPhoto: Bitmap? = null

    // FIX #2: single active frame-render job — cancel before launching next
    private var frameRenderJob: Job? = null
    private var exportJob: Job? = null

    init {
        viewModelScope.launch {
            // Observe premium in real-time (FIX #4)
            prefManager.isPremium.collect { _isPremium.postValue(it) }
        }
    }

    fun loadTemplate(id: String) {
        _template.value = repository.getTemplateById(id)
    }

    fun setUserInput(input: UserInput) {
        _userInput.value = input
        input.personPhotoUri?.let { uriStr ->
            viewModelScope.launch(Dispatchers.IO) {
                cachedUserPhoto = try {
                    context.contentResolver.openInputStream(Uri.parse(uriStr))
                        ?.use { BitmapFactory.decodeStream(it) }
                } catch (e: Exception) { null }
            }
        }
    }

    fun exportVideo(addWatermark: Boolean) {
        exportJob?.cancel()
        val template  = _template.value ?: return
        val input     = _userInput.value ?: return
        exportJob = viewModelScope.launch {
            videoGenerator.generateVideo(template, input, addWatermark)
                .collect { state ->
                    _exportState.postValue(state)
                    if (state is VideoGenState.Success) prefManager.incrementVideosCreated()
                }
        }
    }

    fun cancelExport() { exportJob?.cancel() }

    val isExporting: Boolean
        get() = exportJob?.isActive == true

    /** FIX #2: cancel the previous frame job before starting a new one */
    fun renderPreviewFrameAt(frameIndex: Int, onReady: (Bitmap) -> Unit) {
        val template = _template.value ?: return
        val input    = _userInput.value ?: return

        frameRenderJob?.cancel()
        frameRenderJob = viewModelScope.launch(Dispatchers.IO) {
            val totalFrames   = template.durationSeconds * 10
            val wrapped       = frameIndex % maxOf(1, totalFrames)
            val ratio         = wrapped.toFloat() / maxOf(1, totalFrames)
            val bmp = FrameRenderer.renderFrame(
                context       = context,
                template      = template,
                userInput     = input,
                userPhoto     = cachedUserPhoto,
                frameIndex    = wrapped,
                totalFrames   = totalFrames,
                progressRatio = ratio,
                addWatermark  = !(_isPremium.value ?: false),
                width  = 360,
                height = 640
            )
            withContext(Dispatchers.Main) {
                if (isActive) onReady(bmp)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        frameRenderJob?.cancel()
        exportJob?.cancel()
        cachedUserPhoto?.recycle()
        cachedUserPhoto = null
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PreviewViewModel(context) as T
    }
}
