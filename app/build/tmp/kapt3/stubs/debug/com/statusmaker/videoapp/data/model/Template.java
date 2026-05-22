package com.statusmaker.videoapp.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b%\b\u0086\b\u0018\u00002\u00020\u0001Bq\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\t\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\u0002\u0010\u0015J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\t\u0010*\u001a\u00020\u0012H\u00c6\u0003J\t\u0010+\u001a\u00020\u0014H\u00c6\u0003J\t\u0010,\u001a\u00020\u0003H\u00c6\u0003J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\t\u0010.\u001a\u00020\u0007H\u00c6\u0003J\t\u0010/\u001a\u00020\tH\u00c6\u0003J\t\u00100\u001a\u00020\tH\u00c6\u0003J\t\u00101\u001a\u00020\fH\u00c6\u0003J\t\u00102\u001a\u00020\u000eH\u00c6\u0003J\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\u0081\u0001\u00104\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00032\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u0014H\u00c6\u0001J\u0013\u00105\u001a\u00020\f2\b\u00106\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00107\u001a\u00020\tH\u00d6\u0001J\t\u00108\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\n\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0017R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010!R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u0017R\u0011\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0017R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u0017R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001d\u00a8\u00069"}, d2 = {"Lcom/statusmaker/videoapp/data/model/Template;", "", "id", "", "name", "teluguName", "category", "Lcom/statusmaker/videoapp/data/model/TemplateCategory;", "thumbnailResId", "", "durationSeconds", "isPremium", "", "musicStyle", "Lcom/statusmaker/videoapp/data/model/MusicStyle;", "primaryColor", "accentColor", "fontStyle", "Lcom/statusmaker/videoapp/data/model/FontStyle;", "animationStyle", "Lcom/statusmaker/videoapp/data/model/AnimationStyle;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/statusmaker/videoapp/data/model/TemplateCategory;IIZLcom/statusmaker/videoapp/data/model/MusicStyle;Ljava/lang/String;Ljava/lang/String;Lcom/statusmaker/videoapp/data/model/FontStyle;Lcom/statusmaker/videoapp/data/model/AnimationStyle;)V", "getAccentColor", "()Ljava/lang/String;", "getAnimationStyle", "()Lcom/statusmaker/videoapp/data/model/AnimationStyle;", "getCategory", "()Lcom/statusmaker/videoapp/data/model/TemplateCategory;", "getDurationSeconds", "()I", "getFontStyle", "()Lcom/statusmaker/videoapp/data/model/FontStyle;", "getId", "()Z", "getMusicStyle", "()Lcom/statusmaker/videoapp/data/model/MusicStyle;", "getName", "getPrimaryColor", "getTeluguName", "getThumbnailResId", "component1", "component10", "component11", "component12", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class Template {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String teluguName = null;
    @org.jetbrains.annotations.NotNull()
    private final com.statusmaker.videoapp.data.model.TemplateCategory category = null;
    private final int thumbnailResId = 0;
    private final int durationSeconds = 0;
    private final boolean isPremium = false;
    @org.jetbrains.annotations.NotNull()
    private final com.statusmaker.videoapp.data.model.MusicStyle musicStyle = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String primaryColor = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String accentColor = null;
    @org.jetbrains.annotations.NotNull()
    private final com.statusmaker.videoapp.data.model.FontStyle fontStyle = null;
    @org.jetbrains.annotations.NotNull()
    private final com.statusmaker.videoapp.data.model.AnimationStyle animationStyle = null;
    
    public Template(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String teluguName, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.TemplateCategory category, int thumbnailResId, int durationSeconds, boolean isPremium, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.MusicStyle musicStyle, @org.jetbrains.annotations.NotNull()
    java.lang.String primaryColor, @org.jetbrains.annotations.NotNull()
    java.lang.String accentColor, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.FontStyle fontStyle, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.AnimationStyle animationStyle) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTeluguName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.TemplateCategory getCategory() {
        return null;
    }
    
    public final int getThumbnailResId() {
        return 0;
    }
    
    public final int getDurationSeconds() {
        return 0;
    }
    
    public final boolean isPremium() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.MusicStyle getMusicStyle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPrimaryColor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAccentColor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.FontStyle getFontStyle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.AnimationStyle getAnimationStyle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.FontStyle component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.AnimationStyle component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.TemplateCategory component4() {
        return null;
    }
    
    public final int component5() {
        return 0;
    }
    
    public final int component6() {
        return 0;
    }
    
    public final boolean component7() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.MusicStyle component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.statusmaker.videoapp.data.model.Template copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String teluguName, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.TemplateCategory category, int thumbnailResId, int durationSeconds, boolean isPremium, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.MusicStyle musicStyle, @org.jetbrains.annotations.NotNull()
    java.lang.String primaryColor, @org.jetbrains.annotations.NotNull()
    java.lang.String accentColor, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.FontStyle fontStyle, @org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.AnimationStyle animationStyle) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}