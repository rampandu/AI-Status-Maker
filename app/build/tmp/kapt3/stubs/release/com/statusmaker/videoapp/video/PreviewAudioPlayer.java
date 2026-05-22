package com.statusmaker.videoapp.video;

/**
 * Streams synthesized audio via AudioTrack during preview and export.
 * Generates one pattern loop (~4s), then loops it indefinitely using
 * AudioTrack.setLoopPoints() for zero-copy gapless repetition.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u00152\u00020\u0001:\u0001\u0015B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u000b\u001a\u00020\fJ\u0006\u0010\r\u001a\u00020\fJ\u001c\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\f0\u0012J\u0006\u0010\u0013\u001a\u00020\fJ\u0006\u0010\u0014\u001a\u00020\fR\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/statusmaker/videoapp/video/PreviewAudioPlayer;", "", "style", "Lcom/statusmaker/videoapp/data/model/MusicStyle;", "(Lcom/statusmaker/videoapp/data/model/MusicStyle;)V", "audioTrack", "Landroid/media/AudioTrack;", "prepareJob", "Lkotlinx/coroutines/Job;", "ready", "", "pause", "", "play", "prepare", "scope", "Lkotlinx/coroutines/CoroutineScope;", "onReady", "Lkotlin/Function0;", "release", "resume", "Companion", "app_release"})
public final class PreviewAudioPlayer {
    @org.jetbrains.annotations.NotNull()
    private final com.statusmaker.videoapp.data.model.MusicStyle style = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "PreviewAudioPlayer";
    private static final int LOOP_DURATION_SEC = 4;
    @org.jetbrains.annotations.Nullable()
    private android.media.AudioTrack audioTrack;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job prepareJob;
    private boolean ready = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.statusmaker.videoapp.video.PreviewAudioPlayer.Companion Companion = null;
    
    public PreviewAudioPlayer(@org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.MusicStyle style) {
        super();
    }
    
    /**
     * Pre-generate samples and load into AudioTrack (static mode).
     * Call once after template is known; fires [onReady] when playback can start.
     */
    public final void prepare(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.CoroutineScope scope, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onReady) {
    }
    
    public final void play() {
    }
    
    public final void pause() {
    }
    
    public final void resume() {
    }
    
    public final void release() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/statusmaker/videoapp/video/PreviewAudioPlayer$Companion;", "", "()V", "LOOP_DURATION_SEC", "", "TAG", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}