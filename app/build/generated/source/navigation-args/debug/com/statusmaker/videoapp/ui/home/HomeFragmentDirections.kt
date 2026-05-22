package com.statusmaker.videoapp.ui.home

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.statusmaker.videoapp.R
import kotlin.Int
import kotlin.String

public class HomeFragmentDirections private constructor() {
  private data class ActionHomeFragmentToTemplateListFragment(
    public val category: String? = null,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_homeFragment_to_templateListFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("category", this.category)
        return result
      }
  }

  public companion object {
    public fun actionHomeFragmentToTemplateListFragment(category: String? = null): NavDirections =
        ActionHomeFragmentToTemplateListFragment(category)

    public fun actionHomeFragmentToPremiumFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_homeFragment_to_premiumFragment)
  }
}
