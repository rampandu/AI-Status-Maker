package com.statusmaker.videoapp.ui.template

import android.os.Bundle
import androidx.navigation.NavDirections
import com.statusmaker.videoapp.R
import kotlin.Int
import kotlin.String

public class TemplateListFragmentDirections private constructor() {
  private data class ActionTemplateListFragmentToEditorFragment(
    public val templateId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_templateListFragment_to_editorFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("templateId", this.templateId)
        return result
      }
  }

  public companion object {
    public fun actionTemplateListFragmentToEditorFragment(templateId: String): NavDirections =
        ActionTemplateListFragmentToEditorFragment(templateId)
  }
}
