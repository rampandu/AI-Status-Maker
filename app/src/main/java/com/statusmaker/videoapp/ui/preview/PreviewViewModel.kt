package com.statusmaker.videoapp.ui.preview

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.model.UserInput
import com.statusmaker.videoapp.data.repository.TemplateRepository
import com.statusmaker.videoapp.utils.PreferenceManager
import com.statusmaker.videoapp.video.FrameRenderer
import com.statusmaker.videoapp.video.VideoGenState
import com.statusmaker.videoapp.video.VideoGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviewViewModel(private val context: Context) : ViewModel() {

    private val repository = TemplateRepository(context)
    private val prefManager = PreferenceManager(context)
    val videoGenerator = VideoGenerator(context)

    private val _template = MutableLiveData<Template?>(null)
    val template: LiveData<Template?> = _template

    private val _userInput = MutableLiveData<UserInput>()

    private val _exportState = MutableLiveData<VideoGenState?>(null)
    val exportState: LiveData<VideoGenState?> = _exportState

    private val _isPremium = MutableLiveData(false)
    val isPremium: LiveData<Boolean> = _isPremium

    // Cached user photo (loaded once, reused for every preview frame)
    private var cachedUserPhoto: Bitmap? = null

    init {
        viewModelScope.launch {
            _isPremium.value = prefManager.isPremium.first()
        }
    }

    fun loadTemplate(templateId: String) {
        _template.value = repository.getTemplateById(templateId)
    }

    fun setUserInput(input: UserInput) {
        _userInput.value = input
        // Pre-load user photo in background
        input.personPhotoUri?.let { uriStr ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val stream = context.contentResolver.openInputStream(
                        android.net.Uri.parse(uriStr)
                    )
                    cachedUserPhoto = android.graphics.BitmapFactory.decodeStream(stream)
                    stream?.close()
                } catch (e: Exception) {
                    cachedUserPhoto = null
                }
            }
        }
    }

    fun exportVideo(addWatermark: Boolean) {
        val template = _template.value ?: return
        val input = _userInput.value ?: return

        viewModelScope.launch {
            videoGenerator.generateVideo(template, input, addWatermark)
                .collect { state ->
                    _exportState.postValue(state)
                    if (state is VideoGenState.Success) {
                        prefManager.incrementVideosCreated()
                    }
                }
        }
    }

    /**
     * Renders a single preview frame at the given animation index.
     * Called repeatedly by the fragment at ~10fps for the animated preview.
     */
    fun renderPreviewFrameAt(frameIndex: Int, onReady: (Bitmap) -> Unit) {
        val template = _template.value ?: return
        val input = _userInput.value ?: return

        val totalPreviewFrames = template.durationSeconds * 10  // at 10fps
        val wrappedFrame = frameIndex % maxOf(1, totalPreviewFrames)
        val progressRatio = wrappedFrame.toFloat() / maxOf(1, totalPreviewFrames)

        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = FrameRenderer.renderFrame(
                context = context,
                template = template,
                userInput = input,
                userPhoto = cachedUserPhoto,
                frameIndex = wrappedFrame,
                totalFrames = totalPreviewFrames,
                progressRatio = progressRatio,
                addWatermark = !(_isPremium.value ?: false),
                width = 360,
                height = 640
            )
            withContext(Dispatchers.Main) { onReady(bitmap) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cachedUserPhoto?.recycle()
        cachedUserPhoto = null
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PreviewViewModel(context) as T
    }
}
