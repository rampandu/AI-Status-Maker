package com.statusmaker.videoapp.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Requests an in-app rating review after the user has exported 3 or more videos,
 * but at most once every 30 days.
 */
object RatingHelper {

    private const val PREFS    = "rating_prefs"
    private const val KEY_ASKS = "ask_count"
    private const val KEY_LAST = "last_ask_time"
    private const val MAX_ASKS = 3
    private const val COOLDOWN = 30L * 24 * 60 * 60 * 1000 // 30 days

    fun maybeAskForRating(activity: Activity) {
        val prefs   = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val asks    = prefs.getInt(KEY_ASKS, 0)
        val lastAsk = prefs.getLong(KEY_LAST, 0L)
        val now     = System.currentTimeMillis()

        if (asks >= MAX_ASKS) return
        if (now - lastAsk < COOLDOWN && asks > 0) return

        // Check video count via PreferenceManager
        CoroutineScope(Dispatchers.Main).launch {
            val prefManager = PreferenceManager(activity)
            val count = kotlinx.coroutines.flow.first(prefManager.videosCreated)
            if (count >= 3) {
                triggerReview(activity, prefs, asks)
            }
        }
    }

    private fun triggerReview(activity: Activity, prefs: SharedPreferences, currentAsks: Int) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    manager.launchReviewFlow(activity, request.result)
                    prefs.edit()
                        .putInt(KEY_ASKS, currentAsks + 1)
                        .putLong(KEY_LAST, System.currentTimeMillis())
                        .apply()
                }
            }
        } catch (_: Exception) { /* not available on non-Play devices */ }
    }
}
