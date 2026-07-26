package com.statusmaker.videoapp.ui.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.model.AppLanguage
import com.statusmaker.videoapp.utils.AppLanguageStore

/**
 * Content-language picker — shown non-cancelable on first launch (see
 * HomeFragment) and cancelable from the 🌐 button in Home's header.
 * Tapping a row applies + persists immediately and dismisses; there's no
 * separate confirm step.
 *
 * Applying [AppCompatDelegate.setApplicationLocales] triggers AppCompat's
 * automatic host-Activity recreate, which is what refreshes every
 * already-open screen — Fragments/adapters read [AppLanguageStore.current]
 * fresh when they're recreated, so no manual live-update plumbing is needed
 * beyond that recreate.
 */
class LanguagePickerBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "LanguagePickerBottomSheet"
        private const val ARG_FORCE = "force_choice"

        /** No-ops if a picker is already showing (avoids first-launch + manual-tap racing). */
        fun show(fragmentManager: FragmentManager, forceChoice: Boolean) {
            if (fragmentManager.findFragmentByTag(TAG) != null) return
            LanguagePickerBottomSheet().apply {
                arguments = bundleOf(ARG_FORCE to forceChoice)
                isCancelable = !forceChoice
            }.show(fragmentManager, TAG)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_language_picker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val optionsContainer = view.findViewById<LinearLayout>(R.id.languageOptionsContainer)
        val current = AppLanguageStore.current

        for (lang in AppLanguage.values()) {
            val row = layoutInflater.inflate(R.layout.item_language_option, optionsContainer, false)
            row.findViewById<TextView>(R.id.tvLanguageNative).text = lang.nativeName
            row.findViewById<TextView>(R.id.tvLanguageEnglish).text =
                lang.name.lowercase().replaceFirstChar { it.uppercase() }

            if (lang == current) {
                (row as MaterialCardView).apply {
                    strokeColor = ContextCompat.getColor(requireContext(), R.color.accent_gold)
                    strokeWidth = (2 * resources.displayMetrics.density).toInt()
                }
            }

            row.setOnClickListener { selectLanguage(lang) }
            optionsContainer.addView(row)
        }
    }

    private fun selectLanguage(lang: AppLanguage) {
        // Update the cache first (safe, synchronous), then close the sheet
        // BEFORE triggering the locale change — setApplicationLocales can
        // recreate the host Activity, and this fragment must be detached
        // from it before that happens.
        AppLanguageStore.select(requireContext(), lang)
        dismiss()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang.code))
    }
}
