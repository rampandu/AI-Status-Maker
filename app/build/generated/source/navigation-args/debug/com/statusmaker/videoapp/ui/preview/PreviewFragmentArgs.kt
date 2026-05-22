package com.statusmaker.videoapp.ui.preview

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmStatic

public data class PreviewFragmentArgs(
  public val templateId: String,
  public val personName: String,
  public val villageName: String,
  public val businessName: String,
  public val festivalName: String,
  public val customMessage: String,
  public val photoUri: String,
  public val musicStyleOrdinal: Int = 0,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("templateId", this.templateId)
    result.putString("personName", this.personName)
    result.putString("villageName", this.villageName)
    result.putString("businessName", this.businessName)
    result.putString("festivalName", this.festivalName)
    result.putString("customMessage", this.customMessage)
    result.putString("photoUri", this.photoUri)
    result.putInt("musicStyleOrdinal", this.musicStyleOrdinal)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("templateId", this.templateId)
    result.set("personName", this.personName)
    result.set("villageName", this.villageName)
    result.set("businessName", this.businessName)
    result.set("festivalName", this.festivalName)
    result.set("customMessage", this.customMessage)
    result.set("photoUri", this.photoUri)
    result.set("musicStyleOrdinal", this.musicStyleOrdinal)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): PreviewFragmentArgs {
      bundle.setClassLoader(PreviewFragmentArgs::class.java.classLoader)
      val __templateId : String?
      if (bundle.containsKey("templateId")) {
        __templateId = bundle.getString("templateId")
        if (__templateId == null) {
          throw IllegalArgumentException("Argument \"templateId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"templateId\" is missing and does not have an android:defaultValue")
      }
      val __personName : String?
      if (bundle.containsKey("personName")) {
        __personName = bundle.getString("personName")
        if (__personName == null) {
          throw IllegalArgumentException("Argument \"personName\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"personName\" is missing and does not have an android:defaultValue")
      }
      val __villageName : String?
      if (bundle.containsKey("villageName")) {
        __villageName = bundle.getString("villageName")
        if (__villageName == null) {
          throw IllegalArgumentException("Argument \"villageName\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"villageName\" is missing and does not have an android:defaultValue")
      }
      val __businessName : String?
      if (bundle.containsKey("businessName")) {
        __businessName = bundle.getString("businessName")
        if (__businessName == null) {
          throw IllegalArgumentException("Argument \"businessName\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"businessName\" is missing and does not have an android:defaultValue")
      }
      val __festivalName : String?
      if (bundle.containsKey("festivalName")) {
        __festivalName = bundle.getString("festivalName")
        if (__festivalName == null) {
          throw IllegalArgumentException("Argument \"festivalName\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"festivalName\" is missing and does not have an android:defaultValue")
      }
      val __customMessage : String?
      if (bundle.containsKey("customMessage")) {
        __customMessage = bundle.getString("customMessage")
        if (__customMessage == null) {
          throw IllegalArgumentException("Argument \"customMessage\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"customMessage\" is missing and does not have an android:defaultValue")
      }
      val __photoUri : String?
      if (bundle.containsKey("photoUri")) {
        __photoUri = bundle.getString("photoUri")
        if (__photoUri == null) {
          throw IllegalArgumentException("Argument \"photoUri\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"photoUri\" is missing and does not have an android:defaultValue")
      }
      val __musicStyleOrdinal : Int
      if (bundle.containsKey("musicStyleOrdinal")) {
        __musicStyleOrdinal = bundle.getInt("musicStyleOrdinal")
      } else {
        __musicStyleOrdinal = 0
      }
      return PreviewFragmentArgs(__templateId, __personName, __villageName, __businessName,
          __festivalName, __customMessage, __photoUri, __musicStyleOrdinal)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): PreviewFragmentArgs {
      val __templateId : String?
      if (savedStateHandle.contains("templateId")) {
        __templateId = savedStateHandle["templateId"]
        if (__templateId == null) {
          throw IllegalArgumentException("Argument \"templateId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"templateId\" is missing and does not have an android:defaultValue")
      }
      val __personName : String?
      if (savedStateHandle.contains("personName")) {
        __personName = savedStateHandle["personName"]
        if (__personName == null) {
          throw IllegalArgumentException("Argument \"personName\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"personName\" is missing and does not have an android:defaultValue")
      }
      val __villageName : String?
      if (savedStateHandle.contains("villageName")) {
        __villageName = savedStateHandle["villageName"]
        if (__villageName == null) {
          throw IllegalArgumentException("Argument \"villageName\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"villageName\" is missing and does not have an android:defaultValue")
      }
      val __businessName : String?
      if (savedStateHandle.contains("businessName")) {
        __businessName = savedStateHandle["businessName"]
        if (__businessName == null) {
          throw IllegalArgumentException("Argument \"businessName\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"businessName\" is missing and does not have an android:defaultValue")
      }
      val __festivalName : String?
      if (savedStateHandle.contains("festivalName")) {
        __festivalName = savedStateHandle["festivalName"]
        if (__festivalName == null) {
          throw IllegalArgumentException("Argument \"festivalName\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"festivalName\" is missing and does not have an android:defaultValue")
      }
      val __customMessage : String?
      if (savedStateHandle.contains("customMessage")) {
        __customMessage = savedStateHandle["customMessage"]
        if (__customMessage == null) {
          throw IllegalArgumentException("Argument \"customMessage\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"customMessage\" is missing and does not have an android:defaultValue")
      }
      val __photoUri : String?
      if (savedStateHandle.contains("photoUri")) {
        __photoUri = savedStateHandle["photoUri"]
        if (__photoUri == null) {
          throw IllegalArgumentException("Argument \"photoUri\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"photoUri\" is missing and does not have an android:defaultValue")
      }
      val __musicStyleOrdinal : Int?
      if (savedStateHandle.contains("musicStyleOrdinal")) {
        __musicStyleOrdinal = savedStateHandle["musicStyleOrdinal"]
        if (__musicStyleOrdinal == null) {
          throw IllegalArgumentException("Argument \"musicStyleOrdinal\" of type integer does not support null values")
        }
      } else {
        __musicStyleOrdinal = 0
      }
      return PreviewFragmentArgs(__templateId, __personName, __villageName, __businessName,
          __festivalName, __customMessage, __photoUri, __musicStyleOrdinal)
    }
  }
}
