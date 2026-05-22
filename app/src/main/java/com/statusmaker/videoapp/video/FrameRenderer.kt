package com.statusmaker.videoapp.video

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import com.statusmaker.videoapp.data.model.*
import kotlin.math.*

/**
 * Renders a single video frame to a Bitmap using Android Canvas.
 * Each template category has its own drawing logic.
 */
object FrameRenderer {

    fun renderFrame(
        context: Context,
        template: Template,
        userInput: UserInput,
        userPhoto: Bitmap?,
        frameIndex: Int,
        totalFrames: Int,
        progressRatio: Float,          // 0f..1f
        addWatermark: Boolean,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val t = progressRatio  // shorthand

        when (template.category) {
            TemplateCategory.BIRTHDAY ->
                drawBirthdayFrame(canvas, template, userInput, userPhoto, t, width, height)
            TemplateCategory.FESTIVAL ->
                drawFestivalFrame(canvas, template, userInput, userPhoto, t, width, height)
            TemplateCategory.DEVOTIONAL ->
                drawDevotionalFrame(canvas, template, userInput, userPhoto, t, width, height)
            TemplateCategory.POLITICAL ->
                drawPoliticalFrame(canvas, template, userInput, userPhoto, t, width, height)
            TemplateCategory.BABY ->
                drawBabyFrame(canvas, template, userInput, userPhoto, t, width, height)
            TemplateCategory.WEDDING ->
                drawWeddingFrame(canvas, template, userInput, userPhoto, t, width, height)
            TemplateCategory.BUSINESS ->
                drawBusinessFrame(canvas, template, userInput, userPhoto, t, width, height)
        }

        if (addWatermark) drawWatermark(canvas, width, height)

        return bitmap
    }

    // ─── Background Helpers ───────────────────────────────────────────────────

    private fun drawGradientBg(
        canvas: Canvas,
        color1: String,
        color2: String,
        width: Int,
        height: Int,
        t: Float = 0f,
        rotate: Boolean = false
    ) {
        val paint = Paint()
        val colors = intArrayOf(
            Color.parseColor(color1),
            Color.parseColor(color2)
        )
        val shader = if (rotate) {
            val angle = t * 360f
            val cx = width / 2f
            val cy = height / 2f
            val rad = Math.toRadians(angle.toDouble())
            val x1 = cx + cos(rad).toFloat() * width
            val y1 = cy + sin(rad).toFloat() * height
            LinearGradient(cx, cy, x1.toFloat(), y1.toFloat(), colors, null, Shader.TileMode.CLAMP)
        } else {
            LinearGradient(0f, 0f, 0f, height.toFloat(), colors, null, Shader.TileMode.CLAMP)
        }
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    // ─── Birthday Frame ───────────────────────────────────────────────────────

    private fun drawBirthdayFrame(
        canvas: Canvas,
        template: Template,
        userInput: UserInput,
        photo: Bitmap?,
        t: Float,
        w: Int, h: Int
    ) {
        val primary = template.primaryColor
        val accent = template.accentColor

        // Animated gradient background
        drawGradientBg(canvas, primary, "#1a1a2e", w, h, t, rotate = true)

        // Sparkle/confetti effect
        drawConfetti(canvas, t, w, h)

        // Circular photo frame (zoom in animation)
        val photoScale = 0.6f + easeInOut(t) * 0.4f
        val photoCenterX = w / 2f
        val photoCenterY = h * 0.35f
        val photoRadius = w * 0.3f * photoScale
        drawCircularPhoto(canvas, photo, photoCenterX, photoCenterY, photoRadius, Color.parseColor(accent))

        // "Happy Birthday" text (slide in from bottom)
        val titleY = h * 0.58f + (1f - easeInOut(min(t * 3f, 1f))) * 200f
        drawStyledText(
            canvas,
            "Happy Birthday!",
            photoCenterX, titleY,
            textSize = w * 0.07f,
            color = Color.WHITE,
            bold = true,
            shadow = true
        )

        // Telugu subtitle
        drawStyledText(
            canvas,
            "పుట్టినరోజు శుభాకాంక్షలు",
            photoCenterX, titleY + w * 0.09f,
            textSize = w * 0.045f,
            color = Color.parseColor(accent),
            bold = false
        )

        // Name (fade in)
        val nameAlpha = (easeInOut(min(t * 2f - 0.2f, 1f)) * 255).toInt().coerceIn(0, 255)
        val namePaint = Paint().apply { alpha = nameAlpha }
        drawStyledText(
            canvas,
            userInput.personName.ifEmpty { "Your Name" },
            photoCenterX, titleY + w * 0.22f,
            textSize = w * 0.08f,
            color = Color.parseColor(primary),
            bold = true,
            shadow = true,
            paint = namePaint
        )

        // Village/location
        if (userInput.villageName.isNotEmpty()) {
            drawStyledText(
                canvas,
                "From: ${userInput.villageName}",
                photoCenterX, h * 0.82f,
                textSize = w * 0.038f,
                color = Color.WHITE.withAlpha(180)
            )
        }

        // Bottom decorative border
        drawDecorativeBorder(canvas, Color.parseColor(accent), w, h)
    }

    // ─── Festival Frame ───────────────────────────────────────────────────────

    private fun drawFestivalFrame(
        canvas: Canvas,
        template: Template,
        userInput: UserInput,
        photo: Bitmap?,
        t: Float,
        w: Int, h: Int
    ) {
        drawGradientBg(canvas, "#1a0a00", template.primaryColor, w, h, t)

        // Diya/lamp glow effect
        drawGlowCircles(canvas, t, w, h, Color.parseColor(template.primaryColor))

        // Festival name (center, large)
        val festivalName = userInput.festivalName.ifEmpty { template.teluguName }
        val festivalScale = 0.8f + sin(t * PI.toFloat() * 2) * 0.05f
        val titleY = h * 0.25f

        drawStyledText(
            canvas,
            festivalName,
            w / 2f, titleY,
            textSize = w * 0.065f * festivalScale,
            color = Color.parseColor(template.accentColor),
            bold = true,
            shadow = true
        )

        // Photo in an ornate frame
        val photoRadius = w * 0.28f
        drawCircularPhoto(canvas, photo, w / 2f, h * 0.45f, photoRadius, Color.parseColor(template.accentColor))

        // Wishes text
        drawStyledText(
            canvas,
            "శుభాకాంక్షలు",
            w / 2f, h * 0.65f,
            textSize = w * 0.06f,
            color = Color.WHITE,
            bold = true
        )

        // Person & Village
        drawStyledText(
            canvas,
            userInput.personName.ifEmpty { "మీ పేరు" },
            w / 2f, h * 0.72f,
            textSize = w * 0.055f,
            color = Color.parseColor(template.primaryColor),
            bold = true
        )
        if (userInput.villageName.isNotEmpty()) {
            drawStyledText(
                canvas,
                userInput.villageName,
                w / 2f, h * 0.78f,
                textSize = w * 0.04f,
                color = Color.WHITE.withAlpha(200)
            )
        }

        // Custom message
        if (userInput.customMessage.isNotEmpty()) {
            drawStyledText(
                canvas,
                userInput.customMessage,
                w / 2f, h * 0.86f,
                textSize = w * 0.035f,
                color = Color.WHITE.withAlpha(180)
            )
        }

        drawDecorativeBorder(canvas, Color.parseColor(template.accentColor), w, h)
    }

    // ─── Devotional Frame ─────────────────────────────────────────────────────

    private fun drawDevotionalFrame(
        canvas: Canvas,
        template: Template,
        userInput: UserInput,
        photo: Bitmap?,
        t: Float,
        w: Int, h: Int
    ) {
        // Deep spiritual gradient
        drawGradientBg(canvas, "#0D0D1A", template.primaryColor, w, h)

        // Om symbol / halo glow
        val glowPaint = Paint().apply {
            color = Color.parseColor(template.accentColor)
            alpha = (50 + sin(t * 2 * PI.toFloat()) * 30).toInt()
            maskFilter = BlurMaskFilter(120f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(w / 2f, h * 0.35f, w * 0.38f, glowPaint)

        // Divine photo / deity image
        drawCircularPhoto(canvas, photo, w / 2f, h * 0.32f, w * 0.28f, Color.parseColor(template.accentColor), borderWidth = 8f)

        // Om symbol
        drawStyledText(canvas, "🕉", w / 2f, h * 0.08f, w * 0.09f, Color.parseColor(template.accentColor), bold = false)

        // Temple/deity name
        val deityName = userInput.festivalName.ifEmpty { template.teluguName }
        drawStyledText(
            canvas, deityName,
            w / 2f, h * 0.60f,
            w * 0.065f, Color.parseColor(template.accentColor), bold = true, shadow = true
        )

        // Devotional slogan
        drawStyledText(
            canvas, "జయ జయ",
            w / 2f, h * 0.67f,
            w * 0.045f, Color.WHITE, bold = false
        )

        // Person name
        drawStyledText(
            canvas,
            userInput.personName.ifEmpty { "భక్తుడు పేరు" },
            w / 2f, h * 0.77f,
            w * 0.048f, Color.WHITE, bold = true
        )

        // Village
        if (userInput.villageName.isNotEmpty()) {
            drawStyledText(
                canvas, userInput.villageName,
                w / 2f, h * 0.83f,
                w * 0.036f, Color.WHITE.withAlpha(180), bold = false
            )
        }

        // Flickering candle effect at bottom
        drawCandleEffect(canvas, t, w, h, Color.parseColor(template.accentColor))
        drawDecorativeBorder(canvas, Color.parseColor(template.accentColor), w, h)
    }

    // ─── Political Frame ──────────────────────────────────────────────────────

    private fun drawPoliticalFrame(
        canvas: Canvas,
        template: Template,
        userInput: UserInput,
        photo: Bitmap?,
        t: Float,
        w: Int, h: Int
    ) {
        drawGradientBg(canvas, "#050510", template.primaryColor, w, h)

        // Dramatic light rays
        drawLightRays(canvas, t, w, h, Color.parseColor(template.primaryColor))

        // Large leader photo - oval/presidential style
        val photoW = (w * 0.70f).toInt()
        val photoH = (h * 0.42f).toInt()
        val photoLeft = (w - photoW) / 2f
        val photoTop = h * 0.10f
        drawOvalPhoto(canvas, photo, photoLeft, photoTop, photoW.toFloat(), photoH.toFloat(),
            Color.parseColor(template.accentColor))

        // Party/event name
        val festivalName = userInput.festivalName.ifEmpty { "మా పార్టీ" }
        drawStyledText(
            canvas, festivalName,
            w / 2f, h * 0.58f,
            w * 0.055f, Color.parseColor(template.accentColor), bold = true, shadow = true
        )

        // Leader name
        drawStyledText(
            canvas,
            userInput.personName.ifEmpty { "నాయకుడు పేరు" },
            w / 2f, h * 0.65f,
            w * 0.072f, Color.WHITE, bold = true, shadow = true
        )

        // Designation / village
        if (userInput.villageName.isNotEmpty()) {
            drawStyledText(
                canvas, userInput.villageName,
                w / 2f, h * 0.72f,
                w * 0.04f, Color.parseColor(template.accentColor), bold = false
            )
        }

        // Custom message / slogan
        if (userInput.customMessage.isNotEmpty()) {
            drawStyledText(
                canvas, userInput.customMessage,
                w / 2f, h * 0.80f,
                w * 0.038f, Color.WHITE.withAlpha(200), bold = false
            )
        }

        drawDecorativeBorder(canvas, Color.parseColor(template.accentColor), w, h)
    }

    // ─── Baby Frame ───────────────────────────────────────────────────────────

    private fun drawBabyFrame(
        canvas: Canvas,
        template: Template,
        userInput: UserInput,
        photo: Bitmap?,
        t: Float,
        w: Int, h: Int
    ) {
        drawGradientBg(canvas, template.accentColor, template.primaryColor, w, h)

        // Floating bubbles
        drawBubbles(canvas, t, w, h, Color.WHITE)

        // Baby photo
        drawCircularPhoto(canvas, photo, w / 2f, h * 0.35f, w * 0.29f, Color.WHITE)

        // Title
        val title = if (template.id == "baby_boy") "It's a Boy! 👦"
                    else if (template.id == "baby_girl") "It's a Girl! 👧"
                    else "New Baby! 🍼"
        drawStyledText(
            canvas, title,
            w / 2f, h * 0.60f,
            w * 0.065f, Color.WHITE, bold = true, shadow = true
        )
        drawStyledText(
            canvas, template.teluguName,
            w / 2f, h * 0.67f,
            w * 0.045f, Color.WHITE.withAlpha(220), bold = false
        )

        // Baby/parent name
        drawStyledText(
            canvas, userInput.personName.ifEmpty { "శిశువు పేరు" },
            w / 2f, h * 0.76f,
            w * 0.06f, Color.WHITE, bold = true
        )
        if (userInput.villageName.isNotEmpty()) {
            drawStyledText(
                canvas, userInput.villageName,
                w / 2f, h * 0.83f,
                w * 0.038f, Color.WHITE.withAlpha(200), bold = false
            )
        }

        drawDecorativeBorder(canvas, Color.WHITE.withAlpha(180), w, h)
    }

    // ─── Wedding Frame ────────────────────────────────────────────────────────

    private fun drawWeddingFrame(
        canvas: Canvas,
        template: Template,
        userInput: UserInput,
        photo: Bitmap?,
        t: Float,
        w: Int, h: Int
    ) {
        drawGradientBg(canvas, "#2C0E0E", template.primaryColor, w, h)

        // Rose petals falling
        drawRosePetals(canvas, t, w, h)

        drawCircularPhoto(canvas, photo, w / 2f, h * 0.33f, w * 0.28f, Color.parseColor(template.accentColor))

        drawStyledText(canvas, "💍 వివాహ శుభాకాంక్షలు", w / 2f, h * 0.60f, w * 0.055f, Color.parseColor(template.accentColor), bold = true, shadow = true)
        drawStyledText(canvas, userInput.personName.ifEmpty { "Bride & Groom" }, w / 2f, h * 0.67f, w * 0.065f, Color.WHITE, bold = true)

        if (userInput.villageName.isNotEmpty()) {
            drawStyledText(canvas, userInput.villageName, w / 2f, h * 0.75f, w * 0.038f, Color.WHITE.withAlpha(200))
        }
        if (userInput.customMessage.isNotEmpty()) {
            drawStyledText(canvas, userInput.customMessage, w / 2f, h * 0.83f, w * 0.035f, Color.parseColor(template.accentColor))
        }

        drawDecorativeBorder(canvas, Color.parseColor(template.accentColor), w, h)
    }

    // ─── Business Frame ───────────────────────────────────────────────────────

    private fun drawBusinessFrame(
        canvas: Canvas,
        template: Template,
        userInput: UserInput,
        photo: Bitmap?,
        t: Float,
        w: Int, h: Int
    ) {
        drawGradientBg(canvas, "#0A0A14", template.primaryColor, w, h)

        drawLightRays(canvas, t, w, h, Color.parseColor(template.primaryColor))

        drawCircularPhoto(canvas, photo, w / 2f, h * 0.32f, w * 0.26f, Color.parseColor(template.accentColor))

        val bizTitle = if (template.id == "biz_opening") "🎊 Grand Opening 🎊" else "🔥 Big Sale 🔥"
        drawStyledText(canvas, bizTitle, w / 2f, h * 0.58f, w * 0.058f, Color.parseColor(template.accentColor), bold = true, shadow = true)

        val bizName = userInput.businessName.ifEmpty { userInput.personName.ifEmpty { "Business Name" } }
        drawStyledText(canvas, bizName, w / 2f, h * 0.66f, w * 0.065f, Color.WHITE, bold = true)

        if (userInput.villageName.isNotEmpty()) {
            drawStyledText(canvas, "📍 ${userInput.villageName}", w / 2f, h * 0.74f, w * 0.04f, Color.WHITE.withAlpha(200))
        }
        if (userInput.customMessage.isNotEmpty()) {
            drawStyledText(canvas, userInput.customMessage, w / 2f, h * 0.82f, w * 0.038f, Color.parseColor(template.accentColor))
        }

        drawDecorativeBorder(canvas, Color.parseColor(template.accentColor), w, h)
    }

    // ─── Drawing Helpers ──────────────────────────────────────────────────────

    private fun drawCircularPhoto(
        canvas: Canvas,
        photo: Bitmap?,
        cx: Float, cy: Float,
        radius: Float,
        borderColor: Int,
        borderWidth: Float = 6f
    ) {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            maskFilter = BlurMaskFilter(borderWidth * 2, BlurMaskFilter.Blur.OUTER)
        }
        canvas.drawCircle(cx, cy, radius + borderWidth / 2, borderPaint)

        val clipPath = Path().apply {
            addCircle(cx, cy, radius, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)

        if (photo != null) {
            val scaledPhoto = scaleBitmapToFit(photo, (radius * 2).toInt(), (radius * 2).toInt())
            val left = cx - scaledPhoto.width / 2f
            val top = cy - scaledPhoto.height / 2f
            canvas.drawBitmap(scaledPhoto, left, top, null)
        } else {
            // Placeholder gradient
            val placeholderPaint = Paint().apply {
                shader = RadialGradient(cx, cy, radius, intArrayOf(Color.GRAY, Color.DKGRAY), null, Shader.TileMode.CLAMP)
            }
            canvas.drawCircle(cx, cy, radius, placeholderPaint)
            // Person icon placeholder
            drawStyledText(canvas, "👤", cx, cy + radius * 0.15f, radius * 0.7f, Color.WHITE)
        }

        canvas.restore()
    }

    private fun drawOvalPhoto(
        canvas: Canvas,
        photo: Bitmap?,
        left: Float, top: Float,
        photoW: Float, photoH: Float,
        borderColor: Int
    ) {
        val rectF = RectF(left, top, left + photoW, top + photoH)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.save()
        val clipPath = Path().apply { addOval(rectF, Path.Direction.CW) }
        canvas.clipPath(clipPath)

        if (photo != null) {
            val scaled = scaleBitmapToFit(photo, photoW.toInt(), photoH.toInt())
            val ox = left + (photoW - scaled.width) / 2f
            val oy = top + (photoH - scaled.height) / 2f
            canvas.drawBitmap(scaled, ox, oy, null)
        } else {
            val paint = Paint().apply { color = Color.DKGRAY }
            canvas.drawOval(rectF, paint)
            drawStyledText(canvas, "👤", left + photoW / 2, top + photoH / 2 + photoH * 0.1f, photoH * 0.4f, Color.WHITE)
        }
        canvas.restore()
        canvas.drawOval(rectF, borderPaint)
    }

    private fun drawStyledText(
        canvas: Canvas,
        text: String,
        cx: Float,
        cy: Float,
        textSize: Float,
        color: Int,
        bold: Boolean = false,
        shadow: Boolean = false,
        paint: Paint? = null
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.color = color
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
            if (shadow) setShadowLayer(8f, 0f, 4f, Color.BLACK.withAlpha(180))
        }
        if (paint != null) textPaint.alpha = paint.alpha
        canvas.drawText(text, cx, cy, textPaint)
    }

    private fun drawWatermark(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE.withAlpha(100)
            textSize = w * 0.028f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Status Maker App", w / 2f, h * 0.955f, paint)
    }

    private fun drawDecorativeBorder(canvas: Canvas, color: Int, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 6f
            alpha = 120
        }
        val margin = 16f
        canvas.drawRoundRect(
            RectF(margin, margin, w - margin, h - margin),
            24f, 24f, paint
        )
    }

    private fun drawConfetti(canvas: Canvas, t: Float, w: Int, h: Int) {
        val rng = java.util.Random(42)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colors = intArrayOf(
            Color.YELLOW, Color.RED, Color.CYAN, Color.GREEN, Color.MAGENTA
        )
        repeat(40) { i ->
            val x = rng.nextFloat() * w
            val baseY = rng.nextFloat() * h
            val speed = 0.3f + rng.nextFloat() * 0.7f
            val y = ((baseY + t * h * speed) % h)
            paint.color = colors[i % colors.size]
            paint.alpha = 180
            val size = 8f + rng.nextFloat() * 12f
            canvas.drawRect(x, y, x + size, y + size * 0.5f, paint)
        }
    }

    private fun drawGlowCircles(canvas: Canvas, t: Float, w: Int, h: Int, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            maskFilter = BlurMaskFilter(80f, BlurMaskFilter.Blur.NORMAL)
        }
        for (i in 0..2) {
            val phase = (t + i / 3f) % 1f
            val alpha = (sin(phase * PI.toFloat()) * 60).toInt()
            paint.alpha = alpha.coerceIn(0, 80)
            val radius = 100f + phase * 200f
            canvas.drawCircle(w / 2f, h * 0.4f, radius, paint)
        }
    }

    private fun drawLightRays(canvas: Canvas, t: Float, w: Int, h: Int, color: Int) {
        val cx = w / 2f
        val cy = h * 0.2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 30
            maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
        }
        for (i in 0..7) {
            val angle = Math.toRadians((i * 45 + t * 180).toDouble())
            val endX = cx + cos(angle).toFloat() * h * 0.9f
            val endY = cy + sin(angle).toFloat() * h * 0.9f
            canvas.drawLine(cx, cy, endX, endY, paint.apply { strokeWidth = 80f })
        }
    }

    private fun drawBubbles(canvas: Canvas, t: Float, w: Int, h: Int, color: Int) {
        val rng = java.util.Random(77)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            alpha = 40
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        repeat(20) {
            val x = rng.nextFloat() * w
            val baseY = rng.nextFloat() * h
            val y = ((baseY - t * h * 0.3f + h) % h)
            val r = 15f + rng.nextFloat() * 40f
            canvas.drawCircle(x, y, r, paint)
        }
    }

    private fun drawRosePetals(canvas: Canvas, t: Float, w: Int, h: Int) {
        val rng = java.util.Random(13)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            alpha = 120
        }
        repeat(20) {
            val x = rng.nextFloat() * w
            val baseY = -50f + rng.nextFloat() * h
            val y = ((baseY + t * h * 0.4f) % (h + 100f))
            val r = 8f + rng.nextFloat() * 14f
            canvas.drawOval(RectF(x, y, x + r * 2, y + r), paint)
        }
    }

    private fun drawCandleEffect(canvas: Canvas, t: Float, w: Int, h: Int, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
            alpha = (100 + sin(t * 10 * PI.toFloat()) * 40).toInt().coerceIn(60, 160)
        }
        canvas.drawCircle(w / 2f, h * 0.91f, 18f, paint)
    }

    // ─── Animation Utils ──────────────────────────────────────────────────────

    private fun easeInOut(t: Float): Float {
        val clamped = t.coerceIn(0f, 1f)
        return (3 * clamped * clamped - 2 * clamped * clamped * clamped)
    }

    private fun scaleBitmapToFit(bitmap: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val scale = minOf(targetW.toFloat() / bitmap.width, targetH.toFloat() / bitmap.height)
        val newW = (bitmap.width * scale).toInt()
        val newH = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    private fun Int.withAlpha(alpha: Int): Int {
        return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
    }
}
