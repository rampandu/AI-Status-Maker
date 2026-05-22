package com.statusmaker.videoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// ─── Template Category ────────────────────────────────────────────────────────

enum class TemplateCategory(
    val displayName: String,
    val teluguName: String,
    val emoji: String
) {
    BIRTHDAY("Birthday", "పుట్టినరోజు", "🎂"),
    FESTIVAL("Festival", "పండుగ", "🎉"),
    DEVOTIONAL("Devotional", "భక్తి", "🙏"),
    POLITICAL("Political", "రాజకీయం", "🗳️"),
    BABY("Baby Welcome", "శిశువు స్వాగతం", "👶"),
    WEDDING("Wedding", "వివాహం", "💍"),
    BUSINESS("Business", "వ్యాపారం", "🏪");
}

// ─── Template ─────────────────────────────────────────────────────────────────

data class Template(
    val id: String,
    val name: String,
    val teluguName: String,
    val category: TemplateCategory,
    val thumbnailResId: Int,          // drawable resource id
    val durationSeconds: Int,
    val isPremium: Boolean = false,
    val musicStyle: MusicStyle = MusicStyle.CLASSICAL,
    val primaryColor: String = "#FF6B35",
    val accentColor: String = "#F7C59F",
    val fontStyle: FontStyle = FontStyle.DECORATIVE,
    val animationStyle: AnimationStyle = AnimationStyle.FADE
)

// ─── UserInput ────────────────────────────────────────────────────────────────

data class UserInput(
    val personName: String = "",
    val personPhotoUri: String? = null,
    val villageName: String = "",
    val businessName: String = "",
    val festivalName: String = "",
    val customMessage: String = "",
    val musicStyle: MusicStyle = MusicStyle.CLASSICAL,
    val selectedTemplate: Template? = null
)

// ─── Project (Room Entity) ────────────────────────────────────────────────────

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: String,
    val personName: String,
    val villageName: String,
    val festivalName: String,
    val personPhotoUri: String?,
    val outputVideoPath: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val isShared: Boolean = false
) : Serializable

// ─── Enums ────────────────────────────────────────────────────────────────────

enum class MusicStyle(val displayName: String, val teluguName: String) {
    CLASSICAL("Classical", "శాస్త్రీయ సంగీతం"),
    FOLK("Folk / Janapada", "జానపద"),
    DEVOTIONAL("Devotional / Bhakti", "భక్తి"),
    FILMY("Filmy / Tollywood", "టాలీవుడ్"),
    INSTRUMENTAL("Instrumental", "వాద్య సంగీతం"),
    NONE("No Music", "సంగీతం లేదు")
}

enum class FontStyle(val displayName: String) {
    BOLD("Bold"),
    DECORATIVE("Decorative"),
    ELEGANT("Elegant"),
    TELUGU_STYLE("Telugu Style")
}

enum class AnimationStyle(val displayName: String) {
    FADE("Fade"),
    SLIDE("Slide"),
    ZOOM("Zoom"),
    SPARKLE("Sparkle"),
    ROTATE("Rotate")
}

// ─── Festival Presets ─────────────────────────────────────────────────────────

object FestivalPresets {
    val list = listOf(
        "Ugadi / ఉగాది",
        "Sankranti / సంక్రాంతి",
        "Dasara / దసరా",
        "Diwali / దీపావళి",
        "Sri Rama Navami / శ్రీరామ నవమి",
        "Krishnashtami / కృష్ణాష్టమి",
        "Bonalu / బోనాలు",
        "Karthika Purnima / కార్తీక పూర్ణిమ",
        "Vinayaka Chavithi / వినాయక చవితి",
        "Bathukamma / బతుకమ్మ",
        "Telugu New Year",
        "Birthday / పుట్టినరోజు",
        "Wedding Anniversary / వివాహ వార్షికోత్సవం",
        "Baby Shower / బేబీ షవర్",
        "Business Opening / వ్యాపార ప్రారంభం",
        "Custom"
    )
}
