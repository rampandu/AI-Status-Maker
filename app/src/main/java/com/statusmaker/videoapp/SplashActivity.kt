package com.statusmaker.videoapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.statusmaker.videoapp.ads.AdManager
import com.statusmaker.videoapp.utils.PreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    // Minimum gap between App Open ad impressions — protects UX for a
    // "quick status video" app that may get reopened many times an hour,
    // while still giving a real ad opportunity each genuine session.
    private val APP_OPEN_MIN_INTERVAL_MS = 30L * 60 * 1000L

    private var glowAnimator: ObjectAnimator? = null
    private var navigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<View>(R.id.splashGlow)?.let { glow ->
            glowAnimator = ObjectAnimator.ofFloat(glow, View.ALPHA, 0.55f, 1f).apply {
                duration = 1400L
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                start()
            }
        }

        lifecycleScope.launch {
            delay(1800L)
            if (isFinishing || isDestroyed) return@launch

            val prefManager = PreferenceManager(this@SplashActivity)
            val isPremium = prefManager.isPremium.first()
            val lastShown = prefManager.lastAdShown.first()
            val now = System.currentTimeMillis()
            val cooledDown = (now - lastShown) >= APP_OPEN_MIN_INTERVAL_MS

            if (!isPremium && cooledDown) {
                AdManager.getInstance(this@SplashActivity).showAppOpenAdIfAvailable(this@SplashActivity) {
                    lifecycleScope.launch { prefManager.recordAdShown() }
                    goToMain()
                }
            } else {
                goToMain()
            }
        }
    }

    private fun goToMain() {
        if (navigated || isFinishing || isDestroyed) return
        navigated = true
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        glowAnimator?.cancel()
        glowAnimator = null
    }
}
