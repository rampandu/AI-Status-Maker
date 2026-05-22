package com.statusmaker.videoapp.ui.editor

import android.content.Context
import androidx.lifecycle.*
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.repository.TemplateRepository

class EditorViewModel(private val context: Context) : ViewModel() {

    private val repository = TemplateRepository(context)

    private val _template = MutableLiveData<Template?>()
    val template: LiveData<Template?> = _template

    fun loadTemplate(templateId: String) {
        _template.value = repository.getTemplateById(templateId)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditorViewModel(context) as T
    }
}
