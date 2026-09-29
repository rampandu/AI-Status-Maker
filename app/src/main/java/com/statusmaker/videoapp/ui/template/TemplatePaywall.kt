package com.statusmaker.videoapp.ui.template

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.utils.AppLanguageStore

/**
 * Shared PRO-template gate for Home/TemplateList/Favorites. Until now
 * [Template.isPremium] only drove a decorative badge — nothing stopped a
 * free user from opening and fully exporting any PRO template. [openEditor]
 * runs immediately for free templates or already-premium users; otherwise
 * an upgrade prompt is shown and [openPremium] only runs on confirmation.
 */
fun Fragment.openTemplateOrShowPaywall(
    template: Template,
    isPremiumUser: Boolean,
    openEditor: () -> Unit,
    openPremium: () -> Unit
) {
    if (!template.isPremium || isPremiumUser) {
        openEditor()
        return
    }
    val name = template.displayName(AppLanguageStore.current)
    AlertDialog.Builder(requireContext())
        .setTitle("👑 PRO Template")
        .setMessage("\"$name\" is a Premium template. Upgrade to unlock every PRO template, remove all ads, and drop the export watermark.")
        .setPositiveButton("Upgrade") { _, _ -> openPremium() }
        .setNegativeButton("Not Now", null)
        .show()
}
