package com.statusmaker.videoapp.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.launch

class HomeViewModel(private val context: Context) : ViewModel() {

    private val prefManager = PreferenceManager(context)

    private val _totalVideosCreated = MutableLiveData(0)
    val totalVideosCreated: LiveData<Int> = _totalVideosCreated

    private val _isPremium = MutableLiveData(false)
    val isPremium: LiveData<Boolean> = _isPremium

    init {
        // FIX #4: collect flows continuously so state updates when user buys premium
        viewModelScope.launch {
            prefManager.isPremium.collect { _isPremium.postValue(it) }
        }
        viewModelScope.launch {
            prefManager.videosCreated.collect { _totalVideosCreated.postValue(it) }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(context) as T
    }
}
