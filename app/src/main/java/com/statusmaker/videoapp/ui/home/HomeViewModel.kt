package com.statusmaker.videoapp.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.statusmaker.videoapp.data.model.CategorySection
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.repository.TemplateRepository
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.launch

class HomeViewModel(private val context: Context) : ViewModel() {

    private val prefManager = PreferenceManager(context)
    private val repository  = TemplateRepository(context)

    private val _totalVideosCreated = MutableLiveData(0)
    val totalVideosCreated: LiveData<Int> = _totalVideosCreated

    private val _isPremium = MutableLiveData(false)
    val isPremium: LiveData<Boolean> = _isPremium

    val favoriteIds: LiveData<Set<String>> = repository.getFavoriteIdsFlow().asLiveData()

    private val allSections: List<CategorySection> = repository.getCategorySections()

    private val _sections = MutableLiveData(allSections)
    val sections: LiveData<List<CategorySection>> = _sections

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    init {
        viewModelScope.launch {
            prefManager.isPremium.collect { _isPremium.postValue(it) }
        }
        viewModelScope.launch {
            prefManager.videosCreated.collect { _totalVideosCreated.postValue(it) }
        }
    }

    /**
     * Client-side filter across all ~37 templates — small dataset, no need
     * for debouncing or a background dispatcher. Empty query restores the
     * full sectioned feed.
     */
    fun search(query: String) {
        _searchQuery.value = query
        _sections.value = if (query.isBlank()) {
            allSections
        } else {
            val matches = repository.searchTemplates(query).toSet()
            allSections
                .map { section -> section.copy(templates = section.templates.filter { it in matches }) }
                .filter { it.templates.isNotEmpty() }
        }
    }

    fun toggleFavorite(template: Template, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(template.id, currentlyFavorite)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(context) as T
    }
}
