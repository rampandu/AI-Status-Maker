package com.statusmaker.videoapp.ui.editor

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class EditorFragmentArgs(
  public val templateId: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("templateId", this.templateId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("templateId", this.templateId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): EditorFragmentArgs {
      bundle.setClassLoader(EditorFragmentArgs::class.java.classLoader)
      val __templateId : String?
      if (bundle.containsKey("templateId")) {
        __templateId = bundle.getString("templateId")
        if (__templateId == null) {
          throw IllegalArgumentException("Argument \"templateId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"templateId\" is missing and does not have an android:defaultValue")
      }
      return EditorFragmentArgs(__templateId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): EditorFragmentArgs {
      val __templateId : String?
      if (savedStateHandle.contains("templateId")) {
        __templateId = savedStateHandle["templateId"]
        if (__templateId == null) {
          throw IllegalArgumentException("Argument \"templateId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"templateId\" is missing and does not have an android:defaultValue")
      }
      return EditorFragmentArgs(__templateId)
    }
  }
}
