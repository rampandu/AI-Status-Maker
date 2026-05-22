package com.statusmaker.videoapp.ui.editor

import android.os.Bundle
import androidx.navigation.NavDirections
import com.statusmaker.videoapp.R
import kotlin.Int
import kotlin.String

public class EditorFragmentDirections private constructor() {
  private data class ActionEditorFragmentToPreviewFragment(
    public val templateId: String,
    public val personName: String,
    public val villageName: String,
    public val businessName: String,
    public val festivalName: String,
    public val customMessage: String,
    public val photoUri: String,
    public val musicStyleOrdinal: Int = 0,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_editorFragment_to_previewFragment

    public override val arguments: Bundle
      get() {
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
  }

  public companion object {
    public fun actionEditorFragmentToPreviewFragment(
      templateId: String,
      personName: String,
      villageName: String,
      businessName: String,
      festivalName: String,
      customMessage: String,
      photoUri: String,
      musicStyleOrdinal: Int = 0,
    ): NavDirections = ActionEditorFragmentToPreviewFragment(templateId, personName, villageName,
        businessName, festivalName, customMessage, photoUri, musicStyleOrdinal)
  }
}
