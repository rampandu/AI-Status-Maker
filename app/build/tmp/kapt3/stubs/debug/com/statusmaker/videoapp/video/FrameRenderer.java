package com.statusmaker.videoapp.video;

/**
 * Renders a single video frame to a Bitmap using Android Canvas.
 * Each template category has its own drawing logic.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0002\b\u0013\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\f\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002JB\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002JB\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J0\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0014\u001a\u00020\u0010H\u0002JB\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J0\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0014\u001a\u00020\u0010H\u0002JD\u0010\u0017\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u000e2\u0006\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u001b\u001a\u00020\u00102\b\b\u0002\u0010\u001c\u001a\u00020\u000eH\u0002J(\u0010\u001d\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J(\u0010\u001e\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00102\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002JB\u0010\u001f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002JB\u0010 \u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J0\u0010!\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0014\u001a\u00020\u0010H\u0002JD\u0010\"\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020$2\u0006\u0010&\u001a\u00020\u00102\u0006\u0010\'\u001a\u00020\u00102\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010(\u001a\u00020)H\u0002J0\u0010*\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0014\u001a\u00020\u0010H\u0002JB\u0010+\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010,\u001a\u00020\u000e2\u0006\u0010-\u001a\u00020\u000e2\u0006\u0010.\u001a\u00020\u000e2\u0006\u0010/\u001a\u00020\u000e2\u0006\u0010\u001b\u001a\u00020\u0010H\u0002JB\u00100\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J(\u00101\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002JX\u00102\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u00103\u001a\u00020$2\u0006\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u000e2\u0006\u00104\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u00102\b\b\u0002\u00105\u001a\u00020)2\b\b\u0002\u00106\u001a\u00020)2\n\b\u0002\u00107\u001a\u0004\u0018\u000108H\u0002J \u00109\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002JB\u0010:\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002J\u0010\u0010;\u001a\u00020\u000e2\u0006\u0010\r\u001a\u00020\u000eH\u0002JX\u0010<\u001a\u00020\f2\u0006\u0010=\u001a\u00020>2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\u0010?\u001a\u0004\u0018\u00010\f2\u0006\u0010@\u001a\u00020\u00102\u0006\u0010A\u001a\u00020\u00102\u0006\u0010B\u001a\u00020\u000e2\u0006\u0010C\u001a\u00020)2\u0006\u0010&\u001a\u00020\u00102\u0006\u0010\'\u001a\u00020\u0010J \u0010D\u001a\u00020\f2\u0006\u0010E\u001a\u00020\f2\u0006\u0010F\u001a\u00020\u00102\u0006\u0010G\u001a\u00020\u0010H\u0002J\u0014\u0010H\u001a\u00020\u0010*\u00020\u00102\u0006\u0010I\u001a\u00020\u0010H\u0002\u00a8\u0006J"}, d2 = {"Lcom/statusmaker/videoapp/video/FrameRenderer;", "", "()V", "drawBabyFrame", "", "canvas", "Landroid/graphics/Canvas;", "template", "Lcom/statusmaker/videoapp/data/model/Template;", "userInput", "Lcom/statusmaker/videoapp/data/model/UserInput;", "photo", "Landroid/graphics/Bitmap;", "t", "", "w", "", "h", "drawBirthdayFrame", "drawBubbles", "color", "drawBusinessFrame", "drawCandleEffect", "drawCircularPhoto", "cx", "cy", "radius", "borderColor", "borderWidth", "drawConfetti", "drawDecorativeBorder", "drawDevotionalFrame", "drawFestivalFrame", "drawGlowCircles", "drawGradientBg", "color1", "", "color2", "width", "height", "rotate", "", "drawLightRays", "drawOvalPhoto", "left", "top", "photoW", "photoH", "drawPoliticalFrame", "drawRosePetals", "drawStyledText", "text", "textSize", "bold", "shadow", "paint", "Landroid/graphics/Paint;", "drawWatermark", "drawWeddingFrame", "easeInOut", "renderFrame", "context", "Landroid/content/Context;", "userPhoto", "frameIndex", "totalFrames", "progressRatio", "addWatermark", "scaleBitmapToFit", "bitmap", "targetW", "targetH", "withAlpha", "alpha", "app_debug"})
public final class FrameRenderer {
    @org.jetbrains.annotations.NotNull()
    public static final com.statusmaker.videoapp.video.FrameRenderer INSTANCE = null;
    
    private FrameRenderer() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.Bitmap renderFrame(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.Template template, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.UserInput userInput, @org.jetbrains.annotations.Nullable()
    android.graphics.Bitmap userPhoto, int frameIndex, int totalFrames, float progressRatio, boolean addWatermark, int width, int height) {
        return null;
    }
    
    private final void drawGradientBg(android.graphics.Canvas canvas, java.lang.String color1, java.lang.String color2, int width, int height, float t, boolean rotate) {
    }
    
    private final void drawBirthdayFrame(android.graphics.Canvas canvas, com.statusmaker.videoapp.data.model.Template template, com.statusmaker.videoapp.data.model.UserInput userInput, android.graphics.Bitmap photo, float t, int w, int h) {
    }
    
    private final void drawFestivalFrame(android.graphics.Canvas canvas, com.statusmaker.videoapp.data.model.Template template, com.statusmaker.videoapp.data.model.UserInput userInput, android.graphics.Bitmap photo, float t, int w, int h) {
    }
    
    private final void drawDevotionalFrame(android.graphics.Canvas canvas, com.statusmaker.videoapp.data.model.Template template, com.statusmaker.videoapp.data.model.UserInput userInput, android.graphics.Bitmap photo, float t, int w, int h) {
    }
    
    private final void drawPoliticalFrame(android.graphics.Canvas canvas, com.statusmaker.videoapp.data.model.Template template, com.statusmaker.videoapp.data.model.UserInput userInput, android.graphics.Bitmap photo, float t, int w, int h) {
    }
    
    private final void drawBabyFrame(android.graphics.Canvas canvas, com.statusmaker.videoapp.data.model.Template template, com.statusmaker.videoapp.data.model.UserInput userInput, android.graphics.Bitmap photo, float t, int w, int h) {
    }
    
    private final void drawWeddingFrame(android.graphics.Canvas canvas, com.statusmaker.videoapp.data.model.Template template, com.statusmaker.videoapp.data.model.UserInput userInput, android.graphics.Bitmap photo, float t, int w, int h) {
    }
    
    private final void drawBusinessFrame(android.graphics.Canvas canvas, com.statusmaker.videoapp.data.model.Template template, com.statusmaker.videoapp.data.model.UserInput userInput, android.graphics.Bitmap photo, float t, int w, int h) {
    }
    
    private final void drawCircularPhoto(android.graphics.Canvas canvas, android.graphics.Bitmap photo, float cx, float cy, float radius, int borderColor, float borderWidth) {
    }
    
    private final void drawOvalPhoto(android.graphics.Canvas canvas, android.graphics.Bitmap photo, float left, float top, float photoW, float photoH, int borderColor) {
    }
    
    private final void drawStyledText(android.graphics.Canvas canvas, java.lang.String text, float cx, float cy, float textSize, int color, boolean bold, boolean shadow, android.graphics.Paint paint) {
    }
    
    private final void drawWatermark(android.graphics.Canvas canvas, int w, int h) {
    }
    
    private final void drawDecorativeBorder(android.graphics.Canvas canvas, int color, int w, int h) {
    }
    
    private final void drawConfetti(android.graphics.Canvas canvas, float t, int w, int h) {
    }
    
    private final void drawGlowCircles(android.graphics.Canvas canvas, float t, int w, int h, int color) {
    }
    
    private final void drawLightRays(android.graphics.Canvas canvas, float t, int w, int h, int color) {
    }
    
    private final void drawBubbles(android.graphics.Canvas canvas, float t, int w, int h, int color) {
    }
    
    private final void drawRosePetals(android.graphics.Canvas canvas, float t, int w, int h) {
    }
    
    private final void drawCandleEffect(android.graphics.Canvas canvas, float t, int w, int h, int color) {
    }
    
    private final float easeInOut(float t) {
        return 0.0F;
    }
    
    private final android.graphics.Bitmap scaleBitmapToFit(android.graphics.Bitmap bitmap, int targetW, int targetH) {
        return null;
    }
    
    private final int withAlpha(int $this$withAlpha, int alpha) {
        return 0;
    }
}