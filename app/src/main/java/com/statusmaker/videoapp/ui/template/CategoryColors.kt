package com.statusmaker.videoapp.ui.template

import android.content.Context
import androidx.core.content.ContextCompat
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.model.TemplateCategory

/**
 * Per-category accent color lookup. Kept separate from the TemplateCategory
 * enum itself (which has no color field) so this is purely a presentation
 * concern — Models.kt / TemplateRepository stay untouched.
 */
fun TemplateCategory.accentColorRes(): Int = when (this) {
    TemplateCategory.BIRTHDAY   -> R.color.cat_birthday
    TemplateCategory.FESTIVAL   -> R.color.cat_festival
    TemplateCategory.DEVOTIONAL -> R.color.cat_devotional
    TemplateCategory.POLITICAL  -> R.color.cat_political
    TemplateCategory.BABY       -> R.color.cat_baby
    TemplateCategory.WEDDING    -> R.color.cat_wedding
    TemplateCategory.BUSINESS   -> R.color.cat_business
    TemplateCategory.GOOD_MORNING -> R.color.cat_good_morning
    TemplateCategory.GOOD_NIGHT   -> R.color.cat_good_night
    TemplateCategory.LOVE         -> R.color.cat_love
    TemplateCategory.FRIENDSHIP   -> R.color.cat_friendship
    TemplateCategory.ATTITUDE     -> R.color.cat_attitude
    TemplateCategory.MOTIVATIONAL -> R.color.cat_motivational
}

fun TemplateCategory.accentColor(context: Context): Int =
    ContextCompat.getColor(context, accentColorRes())
