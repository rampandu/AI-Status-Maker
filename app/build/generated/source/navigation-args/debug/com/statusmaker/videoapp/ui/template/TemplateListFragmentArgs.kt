package com.statusmaker.videoapp.ui.template

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import kotlin.String
import kotlin.jvm.JvmStatic

public data class TemplateListFragmentArgs(
  public val category: String? = null,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("category", this.category)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("category", this.category)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): TemplateListFragmentArgs {
      bundle.setClassLoader(TemplateListFragmentArgs::class.java.classLoader)
      val __category : String?
      if (bundle.containsKey("category")) {
        __category = bundle.getString("category")
      } else {
        __category = null
      }
      return TemplateListFragmentArgs(__category)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): TemplateListFragmentArgs {
      val __category : String?
      if (savedStateHandle.contains("category")) {
        __category = savedStateHandle["category"]
      } else {
        __category = null
      }
      return TemplateListFragmentArgs(__category)
    }
  }
}
