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
}

fun TemplateCategory.accentColor(context: Context): Int =
    ContextCompat.getColor(context, accentColorRes())
