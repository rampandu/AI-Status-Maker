package com.statusmaker.videoapp.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Requests an in-app rating after the user exports 3+ videos,
 * at most 3 times total with a 30-day cooldown between asks.
 */
object RatingHelper {

    private const val PREFS    = "rating_prefs"
    private const val KEY_ASKS = "ask_count"
    private const val KEY_LAST = "last_ask_time"
    private const val MAX_ASKS = 3
    private const val COOLDOWN = 30L * 24 * 60 * 60 * 1000L // 30 days ms

    fun maybeAskForRating(activity: Activity) {
        val prefs   = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val asks    = prefs.getInt(KEY_ASKS, 0)
        val lastAsk = prefs.getLong(KEY_LAST, 0L)
        val now     = System.currentTimeMillis()

        if (asks >= MAX_ASKS) return
        if (asks > 0 && now - lastAsk < COOLDOWN) return

        CoroutineScope(Dispatchers.Main).launch {
            val count = PreferenceManager(activity).videosCreated.first()
            if (count >= 3) triggerReview(activity, prefs, asks)
        }
    }

    private fun triggerReview(
        activity: Activity,
        prefs: SharedPreferences,
        currentAsks: Int
    ) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    manager.launchReviewFlow(activity, task.result)
                        .addOnCompleteListener {
                            prefs.edit()
                                .putInt(KEY_ASKS, currentAsks + 1)
                                .putLong(KEY_LAST, System.currentTimeMillis())
                                .apply()
                        }
                }
            }
        } catch (_: Exception) {
            // Review API not available on non-Play devices — fail silently
        }
    }
}
