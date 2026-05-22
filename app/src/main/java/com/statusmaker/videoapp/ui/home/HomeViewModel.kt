package com.statusmaker.videoapp.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.statusmaker.videoapp.data.repository.TemplateRepository
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(private val context: Context) : ViewModel() {

    private val prefManager = PreferenceManager(context)
    private val repository = TemplateRepository(context)

    private val _totalVideosCreated = MutableLiveData(0)
    val totalVideosCreated: LiveData<Int> = _totalVideosCreated

    private val _isPremium = MutableLiveData(false)
    val isPremium: LiveData<Boolean> = _isPremium

    init {
        viewModelScope.launch {
            _isPremium.value = prefManager.isPremium.first()
            _totalVideosCreated.value = prefManager.videosCreated.first()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(context) as T
    }
}
