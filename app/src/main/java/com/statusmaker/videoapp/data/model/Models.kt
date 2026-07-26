package com.statusmaker.videoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// ─── App Language ─────────────────────────────────────────────────────────────

/**
 * User-selected content language — drives which name/quote text is shown
 * and rendered into videos for every template. Independent of the Android
 * system locale (see AppLanguageStore / LanguagePickerBottomSheet); TELUGU
 * is first/default so its ordinal (0) matches this app's original identity
 * for anyone who never explicitly picks a language.
 */
enum class AppLanguage(val code: String, val nativeName: String) {
    TELUGU("te", "తెలుగు"),
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी");

    companion object {
        val DEFAULT = TELUGU
        fun fromCode(code: String?): AppLanguage = values().find { it.code == code } ?: DEFAULT
        fun fromOrdinal(ordinal: Int): AppLanguage = values().getOrElse(ordinal) { DEFAULT }
    }
}

// ─── Template Category ────────────────────────────────────────────────────────

enum class TemplateCategory(
    val displayName: String,
    val localizedNames: Map<AppLanguage, String>,
    val emoji: String
) {
    BIRTHDAY("Birthday", mapOf(AppLanguage.TELUGU to "పుట్టినరోజు", AppLanguage.HINDI to "जन्मदिन"), "🎂"),
    FESTIVAL("Festival", mapOf(AppLanguage.TELUGU to "పండుగ", AppLanguage.HINDI to "त्योहार"), "🎉"),
    DEVOTIONAL("Devotional", mapOf(AppLanguage.TELUGU to "భక్తి", AppLanguage.HINDI to "भक्ति"), "🙏"),
    POLITICAL("Political", mapOf(AppLanguage.TELUGU to "రాజకీయం", AppLanguage.HINDI to "राजनीति"), "🗳️"),
    BABY("Baby Welcome", mapOf(AppLanguage.TELUGU to "శిశువు స్వాగతం", AppLanguage.HINDI to "शिशु स्वागत"), "👶"),
    WEDDING("Wedding", mapOf(AppLanguage.TELUGU to "వివాహం", AppLanguage.HINDI to "विवाह"), "💍"),
    BUSINESS("Business", mapOf(AppLanguage.TELUGU to "వ్యాపారం", AppLanguage.HINDI to "व्यापार"), "🏪"),
    HOUSEWARMING("Housewarming", mapOf(AppLanguage.TELUGU to "గృహప్రవేశం", AppLanguage.HINDI to "गृह प्रवेश"), "🏠"),

    // ── Quote/mood categories — daily-status content, Crafto-style ───────────
    GOOD_MORNING("Good Morning", mapOf(AppLanguage.TELUGU to "శుభోదయం", AppLanguage.HINDI to "सुप्रभात"), "🌅"),
    GOOD_NIGHT("Good Night", mapOf(AppLanguage.TELUGU to "శుభరాత్రి", AppLanguage.HINDI to "शुभरात्रि"), "🌙"),
    LOVE("Love", mapOf(AppLanguage.TELUGU to "ప్రేమ", AppLanguage.HINDI to "प्रेम"), "❤️"),
    FRIENDSHIP("Friendship", mapOf(AppLanguage.TELUGU to "మిత్రత్వం", AppLanguage.HINDI to "मित्रता"), "🤝"),
    ATTITUDE("Attitude", mapOf(AppLanguage.TELUGU to "అటిట్యూడ్", AppLanguage.HINDI to "एटीट्यूड"), "🔥"),
    MOTIVATIONAL("Motivational", mapOf(AppLanguage.TELUGU to "ప్రేరణ", AppLanguage.HINDI to "प्रेरणा"), "💪");

    /** True for the daily quote-style categories (text-forward, no festival/business fields). */
    val isQuoteMood: Boolean
        get() = this in setOf(GOOD_MORNING, GOOD_NIGHT, LOVE, FRIENDSHIP, ATTITUDE, MOTIVATIONAL)

    fun localizedName(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) displayName else localizedNames[lang] ?: displayName
}

// ─── Template ─────────────────────────────────────────────────────────────────

data class Template(
    val id: String,
    val name: String,
    val localizedNames: Map<AppLanguage, String>,
    val category: TemplateCategory,
    val thumbnailResId: Int,          // drawable resource id
    val durationSeconds: Int,
    val isPremium: Boolean = false,
    val musicStyle: MusicStyle = MusicStyle.CLASSICAL,
    val primaryColor: String = "#FF6B35",
    val accentColor: String = "#F7C59F",
    val fontStyle: FontStyle = FontStyle.DECORATIVE,
    val animationStyle: AnimationStyle = AnimationStyle.FADE
) {
    /**
     * The name/quote shown for [lang]. For quote-mood categories this is the
     * actual quote text rendered into the video, not just a label. Falls
     * back to the English [name] when [lang] has no translation yet.
     */
    fun displayName(lang: AppLanguage): String =
        if (lang == AppLanguage.ENGLISH) name else localizedNames[lang] ?: name
}

// ─── UserInput ────────────────────────────────────────────────────────────────

data class UserInput(
    val personName: String = "",
    val personPhotoUri: String? = null,
    val villageName: String = "",
    val businessName: String = "",
    val festivalName: String = "",
    val customMessage: String = "",
    val musicStyle: MusicStyle = MusicStyle.CLASSICAL,
    val appLanguage: AppLanguage = AppLanguage.DEFAULT,
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

// ─── Category Section (Home discovery feed grouping — not persisted) ─────────

data class CategorySection(
    val category: TemplateCategory,
    val templates: List<Template>
)

// ─── Favorite Template (Room Entity) ──────────────────────────────────────────

@Entity(tableName = "favorite_templates")
data class FavoriteTemplate(
    @PrimaryKey
    val templateId: String,
    val addedAt: Long = System.currentTimeMillis()
)

// ─── Enums ────────────────────────────────────────────────────────────────────

enum class MusicStyle(val displayName: String, val teluguName: String, val emoji: String) {
    CLASSICAL("Classical", "శాస్త్రీయ సంగీతం", "🎻"),
    FOLK("Folk / Janapada", "జానపద", "🥁"),
    DEVOTIONAL("Devotional / Bhakti", "భక్తి", "🪔"),
    FILMY("Filmy / Tollywood", "టాలీవుడ్", "🎬"),
    INSTRUMENTAL("Instrumental", "వాద్య సంగీతం", "🎹"),
    NONE("No Music", "సంగీతం లేదు", "🔇")
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
    private val TELUGU_LIST = listOf(
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
        "Holi / హోలీ",
        "Raksha Bandhan / రక్షా బంధన్",
        "Karva Chauth / కర్వా చౌత్",
        "Chhath Puja / ఛఠ్ పూజ",
        "Navratri / నవరాత్రి",
        "Eid Mubarak / ఈద్ ముబారక్",
        "Christmas / క్రిస్మస్",
        "New Year / నూతన సంవత్సరం",
        "Independence Day / స్వాతంత్ర్య దినోత్సవం",
        "Republic Day / గణతంత్ర దినోత్సవం",
        "Telugu New Year",
        "Birthday / పుట్టినరోజు",
        "Wedding Anniversary / వివాహ వార్షికోత్సవం",
        "Baby Shower / బేబీ షవర్",
        "Business Opening / వ్యాపార ప్రారంభం",
        "Griha Pravesh / గృహప్రవేశం",
        "Custom"
    )

    private val ENGLISH_LIST = listOf(
        "Ugadi", "Sankranti", "Dasara", "Diwali", "Sri Rama Navami", "Krishnashtami",
        "Bonalu", "Karthika Purnima", "Vinayaka Chavithi", "Bathukamma",
        "Holi", "Raksha Bandhan", "Karva Chauth", "Chhath Puja", "Navratri",
        "Eid Mubarak", "Christmas", "New Year", "Independence Day", "Republic Day",
        "Telugu New Year", "Birthday", "Wedding Anniversary", "Baby Shower",
        "Business Opening", "Griha Pravesh", "Custom"
    )

    private val HINDI_LIST = listOf(
        "Ugadi / उगादी",
        "Sankranti / मकर संक्रांति",
        "Dasara / दशहरा",
        "Diwali / दीपावली",
        "Sri Rama Navami / श्री राम नवमी",
        "Krishnashtami / जन्माष्टमी",
        "Bonalu / बोनालु",
        "Karthika Purnima / कार्तिक पूर्णिमा",
        "Vinayaka Chavithi / गणेश चतुर्थी",
        "Bathukamma / बतुकम्मा",
        "Holi / होली",
        "Raksha Bandhan / रक्षा बंधन",
        "Karva Chauth / करवा चौथ",
        "Chhath Puja / छठ पूजा",
        "Navratri / नवरात्रि",
        "Eid Mubarak / ईद मुबारक",
        "Christmas / क्रिसमस",
        "New Year / नया साल",
        "Independence Day / स्वतंत्रता दिवस",
        "Republic Day / गणतंत्र दिवस",
        "Telugu New Year / तेलुगु नव वर्ष",
        "Birthday / जन्मदिन",
        "Wedding Anniversary / विवाह वर्षगांठ",
        "Baby Shower / बेबी शावर",
        "Business Opening / व्यापार उद्घाटन",
        "Griha Pravesh / गृह प्रवेश",
        "Custom / कस्टम"
    )

    /** Back-compat default (Telugu) — prefer [forLanguage]. */
    val list = TELUGU_LIST

    fun forLanguage(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.TELUGU -> TELUGU_LIST
        AppLanguage.HINDI  -> HINDI_LIST
        AppLanguage.ENGLISH -> ENGLISH_LIST
    }
}
