package com.statusmaker.videoapp.data.repository

import android.content.Context
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.db.AppDatabase
import com.statusmaker.videoapp.data.model.*
import kotlinx.coroutines.flow.Flow

class TemplateRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.projectDao()

    fun getAllProjects(): Flow<List<Project>> = dao.getAllProjects()
    suspend fun saveProject(project: Project): Long = dao.insert(project)
    suspend fun deleteProject(project: Project) = dao.delete(project)
    suspend fun updateProject(project: Project) = dao.update(project)

    fun getAllTemplates(): List<Template> = TEMPLATES
    fun getTemplatesByCategory(category: TemplateCategory): List<Template> =
        TEMPLATES.filter { it.category == category }
    fun getTemplateById(id: String): Template? = TEMPLATES.find { it.id == id }
    fun getFreeTemplates(): List<Template> = TEMPLATES.filter { !it.isPremium }
    fun getPremiumTemplates(): List<Template> = TEMPLATES.filter { it.isPremium }

    companion object {
        val TEMPLATES = listOf(

            // ── Birthday ────────────────────────────────────────────────────
            Template(
                id = "bday_gold",
                name = "Golden Birthday",
                teluguName = "బంగారు పుట్టినరోజు",
                category = TemplateCategory.BIRTHDAY,
                thumbnailResId = R.drawable.thumb_bday_gold,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#FFD700",
                accentColor = "#FFA500",
                animationStyle = AnimationStyle.SPARKLE
            ),
            Template(
                id = "bday_rose",
                name = "Rose Birthday",
                teluguName = "రోజ్ పుట్టినరోజు",
                category = TemplateCategory.BIRTHDAY,
                thumbnailResId = R.drawable.thumb_bday_rose,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#FF69B4",
                accentColor = "#FFB6C1",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "bday_blue",
                name = "Blue Sparkle Birthday",
                teluguName = "నీలి మెరుపు పుట్టినరోజు",
                category = TemplateCategory.BIRTHDAY,
                thumbnailResId = R.drawable.thumb_bday_blue,
                durationSeconds = 20,
                isPremium = true,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#2196F3",
                accentColor = "#BBDEFB",
                animationStyle = AnimationStyle.SPARKLE
            ),
            Template(
                id = "bday_kids",
                name = "Kids Fun Birthday",
                teluguName = "పిల్లల పుట్టినరోజు",
                category = TemplateCategory.BIRTHDAY,
                thumbnailResId = R.drawable.thumb_bday_kids,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FOLK,
                primaryColor = "#FF9800",
                accentColor = "#FFE0B2",
                animationStyle = AnimationStyle.ZOOM
            ),

            // ── Festival ────────────────────────────────────────────────────
            Template(
                id = "sankranti",
                name = "Sankranti Wishes",
                teluguName = "సంక్రాంతి శుభాకాంక్షలు",
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_sankranti,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FOLK,
                primaryColor = "#FF9800",
                accentColor = "#FFC107",
                animationStyle = AnimationStyle.SLIDE
            ),
            Template(
                id = "ugadi",
                name = "Ugadi Wishes",
                teluguName = "ఉగాది శుభాకాంక్షలు",
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_ugadi,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#4CAF50",
                accentColor = "#C8E6C9",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "dasara",
                name = "Dasara Wishes",
                teluguName = "దసరా శుభాకాంక్షలు",
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_dasara,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#FF5722",
                accentColor = "#FFCCBC",
                animationStyle = AnimationStyle.ZOOM
            ),
            Template(
                id = "diwali",
                name = "Diwali Wishes",
                teluguName = "దీపావళి శుభాకాంక్షలు",
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_diwali,
                durationSeconds = 20,
                isPremium = true,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#9C27B0",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.SPARKLE
            ),
            Template(
                id = "bathukamma",
                name = "Bathukamma Wishes",
                teluguName = "బతుకమ్మ శుభాకాంక్షలు",
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_bathukamma,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FOLK,
                primaryColor = "#F44336",
                accentColor = "#FFCDD2",
                animationStyle = AnimationStyle.ROTATE
            ),
            Template(
                id = "vinayaka",
                name = "Vinayaka Chavithi",
                teluguName = "వినాయక చవితి శుభాకాంక్షలు",
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_vinayaka,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#FF9800",
                accentColor = "#FFF9C4",
                animationStyle = AnimationStyle.FADE
            ),

            // ── Devotional ──────────────────────────────────────────────────
            Template(
                id = "balaji",
                name = "Lord Balaji Wishes",
                teluguName = "శ్రీ వేంకటేశ్వర స్వామి",
                category = TemplateCategory.DEVOTIONAL,
                thumbnailResId = R.drawable.thumb_balaji,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#1A237E",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "durga",
                name = "Goddess Durga",
                teluguName = "దుర్గా మాత",
                category = TemplateCategory.DEVOTIONAL,
                thumbnailResId = R.drawable.thumb_durga,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#B71C1C",
                accentColor = "#FFCDD2",
                animationStyle = AnimationStyle.ZOOM
            ),
            Template(
                id = "krishna",
                name = "Lord Krishna",
                teluguName = "శ్రీ కృష్ణ భగవానుడు",
                category = TemplateCategory.DEVOTIONAL,
                thumbnailResId = R.drawable.thumb_krishna,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#1B5E20",
                accentColor = "#C8E6C9",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "rama",
                name = "Lord Rama",
                teluguName = "శ్రీ రామ భగవానుడు",
                category = TemplateCategory.DEVOTIONAL,
                thumbnailResId = R.drawable.thumb_rama,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#E65100",
                accentColor = "#FFE0B2",
                animationStyle = AnimationStyle.SLIDE
            ),

            // ── Political ───────────────────────────────────────────────────
            Template(
                id = "political_1",
                name = "Political Wishes",
                teluguName = "రాజకీయ శుభాకాంక్షలు",
                category = TemplateCategory.POLITICAL,
                thumbnailResId = R.drawable.thumb_political_1,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#1565C0",
                accentColor = "#BBDEFB",
                animationStyle = AnimationStyle.SLIDE
            ),
            Template(
                id = "political_2",
                name = "Leader Greetings",
                teluguName = "నేత శుభాకాంక్షలు",
                category = TemplateCategory.POLITICAL,
                thumbnailResId = R.drawable.thumb_political_2,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#B71C1C",
                accentColor = "#FFCDD2",
                animationStyle = AnimationStyle.ZOOM
            ),
            Template(
                id = "political_3",
                name = "Victory Celebration",
                teluguName = "విజయ వేడుక",
                category = TemplateCategory.POLITICAL,
                thumbnailResId = R.drawable.thumb_political_3,
                durationSeconds = 20,
                isPremium = true,
                musicStyle = MusicStyle.FOLK,
                primaryColor = "#1B5E20",
                accentColor = "#C8E6C9",
                animationStyle = AnimationStyle.SPARKLE
            ),

            // ── Baby ────────────────────────────────────────────────────────
            Template(
                id = "baby_boy",
                name = "Baby Boy Welcome",
                teluguName = "మగ బిడ్డ స్వాగతం",
                category = TemplateCategory.BABY,
                thumbnailResId = R.drawable.thumb_baby_boy,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#1565C0",
                accentColor = "#BBDEFB",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "baby_girl",
                name = "Baby Girl Welcome",
                teluguName = "ఆడ బిడ్డ స్వాగతం",
                category = TemplateCategory.BABY,
                thumbnailResId = R.drawable.thumb_baby_girl,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#E91E63",
                accentColor = "#FCE4EC",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "baby_shower",
                name = "Baby Shower",
                teluguName = "బేబీ షవర్",
                category = TemplateCategory.BABY,
                thumbnailResId = R.drawable.thumb_baby_shower,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#9C27B0",
                accentColor = "#E1BEE7",
                animationStyle = AnimationStyle.ZOOM
            ),
            Template(
                id = "baby_naming",
                name = "Naming Ceremony",
                teluguName = "నామకరణం",
                category = TemplateCategory.BABY,
                thumbnailResId = R.drawable.thumb_baby_naming,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#FF9800",
                accentColor = "#FFF3E0",
                animationStyle = AnimationStyle.FADE
            ),

            // ── Wedding ─────────────────────────────────────────────────────
            Template(
                id = "wedding_classic",
                name = "Classic Wedding",
                teluguName = "సాంప్రదాయ వివాహం",
                category = TemplateCategory.WEDDING,
                thumbnailResId = R.drawable.thumb_wedding_classic,
                durationSeconds = 20,
                isPremium = false,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#880E4F",
                accentColor = "#FCE4EC",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "wedding_modern",
                name = "Modern Wedding",
                teluguName = "ఆధునిక వివాహం",
                category = TemplateCategory.WEDDING,
                thumbnailResId = R.drawable.thumb_wedding_modern,
                durationSeconds = 20,
                isPremium = true,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#212121",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.SLIDE
            ),

            // ── Business ────────────────────────────────────────────────────
            Template(
                id = "biz_opening",
                name = "Grand Opening",
                teluguName = "గ్రాండ్ ఓపెనింగ్",
                category = TemplateCategory.BUSINESS,
                thumbnailResId = R.drawable.thumb_biz_opening,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#1A237E",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.ZOOM
            ),
            Template(
                id = "biz_offer",
                name = "Special Offer",
                teluguName = "స్పెషల్ ఆఫర్",
                category = TemplateCategory.BUSINESS,
                thumbnailResId = R.drawable.thumb_biz_offer,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#004D40",
                accentColor = "#B2DFDB",
                animationStyle = AnimationStyle.SLIDE
            )
        )
    }
}
