package com.statusmaker.videoapp.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\u0018\u0000 \u001e2\u00020\u0001:\u0001\u001eB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u0012\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00100\u000fJ\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u0010J\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00120\u0010J\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00120\u0010J\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u0016\u001a\u00020\u0017J\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00120\u00102\u0006\u0010\u0019\u001a\u00020\u001aJ\u0016\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u001d\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/statusmaker/videoapp/data/repository/TemplateRepository;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "dao", "Lcom/statusmaker/videoapp/data/db/ProjectDao;", "db", "Lcom/statusmaker/videoapp/data/db/AppDatabase;", "deleteProject", "", "project", "Lcom/statusmaker/videoapp/data/model/Project;", "(Lcom/statusmaker/videoapp/data/model/Project;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllProjects", "Lkotlinx/coroutines/flow/Flow;", "", "getAllTemplates", "Lcom/statusmaker/videoapp/data/model/Template;", "getFreeTemplates", "getPremiumTemplates", "getTemplateById", "id", "", "getTemplatesByCategory", "category", "Lcom/statusmaker/videoapp/data/model/TemplateCategory;", "saveProject", "", "updateProject", "Companion", "app_debug"})
public final class TemplateRepository {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.statusmaker.videoapp.data.db.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final com.statusmaker.videoapp.data.db.ProjectDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.statusmaker.videoapp.data.model.Template> TEMPLATES = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.statusmaker.videoapp.data.repository.TemplateRepository.Companion Companion = null;
    
    public TemplateRepository(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.statusmaker.videoapp.data.model.Project>> getAllProjects() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveProject(@org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteProject(@org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateProject(@org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.Project project, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.statusmaker.videoapp.data.model.Template> getAllTemplates() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.statusmaker.videoapp.data.model.Template> getTemplatesByCategory(@org.jetbrains.annotations.NotNull()
    com.statusmaker.videoapp.data.model.TemplateCategory category) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.statusmaker.videoapp.data.model.Template getTemplateById(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.statusmaker.videoapp.data.model.Template> getFreeTemplates() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.statusmaker.videoapp.data.model.Template> getPremiumTemplates() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/statusmaker/videoapp/data/repository/TemplateRepository$Companion;", "", "()V", "TEMPLATES", "", "Lcom/statusmaker/videoapp/data/model/Template;", "getTEMPLATES", "()Ljava/util/List;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.statusmaker.videoapp.data.model.Template> getTEMPLATES() {
            return null;
        }
    }
}