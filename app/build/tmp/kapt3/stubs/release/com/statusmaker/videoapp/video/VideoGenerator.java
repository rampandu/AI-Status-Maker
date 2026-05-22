package com.statusmaker.videoapp.video;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u0000 \u00122\u00020\u0001:\u0001\u0012B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J&\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/statusmaker/videoapp/video/VideoGenerator;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "bitmapToYUV420", "", "bitmap", "Landroid/graphics/Bitmap;", "generateVideo", "Lkotlinx/coroutines/flow/Flow;", "Lcom/statusmaker/videoapp/video/VideoGenState;", "template", "Lcom/statusmaker/videoapp/data/model/Template;", "userInput", "Lcom/statusmaker/videoapp/data/model/UserInput;", "addWatermark", "", "Companion", "app_release"})
public final class VideoGenerator {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "VideoGenerator";
    public static final int VIDEO_WIDTH = 720;
    public static final int VIDEO_HEIGHT = 1280;
    private static final int FPS = 30;
    private static final int VIDEO_BIT_RATE = 3000000;
    private static final int I_FRAME_INTERVAL = 1;
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNELS = 1;
    private static final int AUDIO_BIT_RATE = 128000;
    private static final int AUDIO_FRAME_SAMPLES = 1024;
    @org.jetbrains.annotations.NotNull()
    public static final com.statusmaker.videoapp.video.VideoGenerator.Companion Companion = null;
    
    public VideoGenerator(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.statusmaker.videoapp.video.VideoGenState> generateVideo(@org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.Template template, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.UserInput userInput, boolean addWatermark) {
        return null;
    }
    
    private final byte[] bitmapToYUV420(android.graphics.Bitmap bitmap) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/statusmaker/videoapp/video/VideoGenerator$Companion;", "", "()V", "AUDIO_BIT_RATE", "", "AUDIO_CHANNELS", "AUDIO_FRAME_SAMPLES", "AUDIO_SAMPLE_RATE", "FPS", "I_FRAME_INTERVAL", "TAG", "", "VIDEO_BIT_RATE", "VIDEO_HEIGHT", "VIDEO_WIDTH", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}