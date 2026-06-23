package com.statusmaker.videoapp.ui.favorites

import android.content.Context
import androidx.lifecycle.*
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.repository.TemplateRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(private val context: Context) : ViewModel() {

    private val repository = TemplateRepository(context)

    val favorites: LiveData<List<Template>> =
        repository.getFavoriteTemplates().asLiveData()

    fun toggleFavorite(template: Template, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(template.id, currentlyFavorite)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FavoritesViewModel(context) as T
    }
}
