# ─── App-specific rules ───────────────────────────────────────────────────────
-keep class com.statusmaker.videoapp.data.model.** { *; }
-keepclassmembers enum * { *; }

# ─── Google Play Billing ──────────────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }
-keep interface com.android.billingclient.** { *; }

# ─── AdMob / GMS — critical: missing rules here can silently break ad loading
# ─── in release builds even when everything works fine in debug.
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-dontwarn com.google.android.gms.ads.**

# ─── ExoPlayer / Media3 ──────────────────────────────────────────────────────
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ─── Glide ───────────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# ─── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# ─── Navigation Safe Args ─────────────────────────────────────────────────────
-keep class * extends androidx.navigation.NavArgs { *; }
-keep class **Args { *; }
-keep class **Directions { *; }

# ─── Lottie ───────────────────────────────────────────────────────────────────
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# ─── DataStore ────────────────────────────────────────────────────────────────
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
  <fields>;
}

# ─── Kotlin coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ─── General Android ──────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ─── Remove debug logging in release ─────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
