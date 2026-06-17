package com.statusmaker.videoapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object RatingHelper {

    private const val PREFS    = "rating_prefs"
    private const val KEY_ASKS = "ask_count"
    private const val KEY_LAST = "last_ask_time"
    private const val MAX_ASKS = 3
    private const val COOLDOWN = 30L * 24 * 60 * 60 * 1000L

    fun maybeAskForRating(activity: Activity) {
        val prefs   = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val asks    = prefs.getInt(KEY_ASKS, 0)
        val lastAsk = prefs.getLong(KEY_LAST, 0L)
        val now     = System.currentTimeMillis()

        if (asks >= MAX_ASKS) return
        if (asks > 0 && now - lastAsk < COOLDOWN) return

        CoroutineScope(Dispatchers.IO).launch {
            val count = PreferenceManager(activity).videosCreated.first()
            if (count >= 3) {
                withContext(Dispatchers.Main) {
                    showRatingDialog(activity, prefs, asks)
                }
            }
        }
    }

    private fun showRatingDialog(
        activity: Activity,
        prefs: android.content.SharedPreferences,
        currentAsks: Int
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        AlertDialog.Builder(activity)
            .setTitle("Enjoying Status Maker? ⭐")
            .setMessage("You've created some great videos!\nWould you like to rate us on the Play Store?")
            .setPositiveButton("Rate Now ⭐") { _, _ ->
                openPlayStore(activity)
                prefs.edit()
                    .putInt(KEY_ASKS, currentAsks + 1)
                    .putLong(KEY_LAST, System.currentTimeMillis())
                    .apply()
            }
            .setNeutralButton("Later") { _, _ ->
                prefs.edit().putLong(KEY_LAST, System.currentTimeMillis()).apply()
            }
            .setNegativeButton("No Thanks") { _, _ ->
                prefs.edit().putInt(KEY_ASKS, MAX_ASKS).apply()
            }
            .show()
    }

    private fun openPlayStore(activity: Activity) {
        val pkg = activity.packageName
        try {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
            )
        } catch (_: Exception) {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
            )
        }
    }
}
