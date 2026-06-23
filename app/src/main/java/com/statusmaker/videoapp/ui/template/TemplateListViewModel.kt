package com.statusmaker.videoapp.ui.template

import android.content.Context
import androidx.lifecycle.*
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.model.TemplateCategory
import com.statusmaker.videoapp.data.repository.TemplateRepository
import kotlinx.coroutines.launch

class TemplateListViewModel(private val context: Context) : ViewModel() {

    private val repository = TemplateRepository(context)

    private val _templates = MutableLiveData<List<Template>>()
    val templates: LiveData<List<Template>> = _templates

    private val _selectedCategory = MutableLiveData<TemplateCategory?>(null)
    val selectedCategory: LiveData<TemplateCategory?> = _selectedCategory

    val favoriteIds: LiveData<Set<String>> = repository.getFavoriteIdsFlow().asLiveData()

    fun loadTemplates(category: TemplateCategory? = null) {
        _selectedCategory.value = category
        _templates.value = if (category == null) {
            repository.getAllTemplates()
        } else {
            repository.getTemplatesByCategory(category)
        }
    }

    fun toggleFavorite(template: Template, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(template.id, currentlyFavorite)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TemplateListViewModel(context) as T
    }
}
