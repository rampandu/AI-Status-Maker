package com.statusmaker.videoapp.data.repository

import android.content.Context
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.db.AppDatabase
import com.statusmaker.videoapp.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TemplateRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.projectDao()
    private val favoriteDao = db.favoriteDao()

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

    /** Matches the English name/category plus every language's localized name. */
    fun searchTemplates(query: String): List<Template> {
        if (query.isBlank()) return TEMPLATES
        val q = query.trim()
        val qLower = q.lowercase()
        return TEMPLATES.filter {
            it.name.lowercase().contains(qLower) ||
            it.localizedNames.values.any { localized -> localized.contains(q) } ||
            it.category.displayName.lowercase().contains(qLower)
        }
    }

    /** One section per category that has at least one template, in enum declaration order. */
    fun getCategorySections(): List<CategorySection> =
        TemplateCategory.values()
            .map { cat -> CategorySection(cat, getTemplatesByCategory(cat)) }
            .filter { it.templates.isNotEmpty() }

    // ── Favorites ──────────────────────────────────────────────────────────────

    fun isFavorite(templateId: String): Flow<Boolean> = favoriteDao.isFavorite(templateId)

    fun getFavoriteIdsFlow(): Flow<Set<String>> =
        favoriteDao.getAllFavoriteIds().map { it.toSet() }

    fun getFavoriteTemplates(): Flow<List<Template>> =
        favoriteDao.getAllFavoriteIds().map { ids -> TEMPLATES.filter { it.id in ids } }

    suspend fun toggleFavorite(templateId: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) favoriteDao.remove(templateId)
        else favoriteDao.add(FavoriteTemplate(templateId))
    }

    companion object {
        private val TE = AppLanguage.TELUGU
        private val HI = AppLanguage.HINDI

        val TEMPLATES = listOf(

            // ── Birthday ────────────────────────────────────────────────────
            Template(
                id = "bday_gold",
                name = "Golden Birthday",
                localizedNames = mapOf(TE to "బంగారు పుట్టినరోజు", HI to "सुनहरा जन्मदिन"),
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
                localizedNames = mapOf(TE to "రోజ్ పుట్టినరోజు", HI to "गुलाबी जन्मदिन"),
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
                localizedNames = mapOf(TE to "నీలి మెరుపు పుట్టినరోజు", HI to "नीला चमकीला जन्मदिन"),
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
                localizedNames = mapOf(TE to "పిల్లల పుట్టినరోజు", HI to "बच्चों का मजेदार जन्मदिन"),
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
                localizedNames = mapOf(TE to "సంక్రాంతి శుభాకాంక్షలు", HI to "मकर संक्रांति की शुभकामनाएं"),
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
                localizedNames = mapOf(TE to "ఉగాది శుభాకాంక్షలు", HI to "उगादी की शुभकामनाएं"),
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
                localizedNames = mapOf(TE to "దసరా శుభాకాంక్షలు", HI to "दशहरा की शुभकामनाएं"),
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
                localizedNames = mapOf(TE to "దీపావళి శుభాకాంక్షలు", HI to "दीपावली की शुभकामनाएं"),
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
                localizedNames = mapOf(TE to "బతుకమ్మ శుభాకాంక్షలు", HI to "बतुकम्मा की शुभकामनाएं"),
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
                localizedNames = mapOf(TE to "వినాయక చవితి శుభాకాంక్షలు", HI to "गणेश चतुर्थी की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_vinayaka,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#FF9800",
                accentColor = "#FFF9C4",
                animationStyle = AnimationStyle.FADE
            ),

            // ── Festival (North-Indian / pan-India) ────────────────────────
            Template(
                id = "holi",
                name = "Holi Wishes",
                localizedNames = mapOf(TE to "హోలీ శుభాకాంక్షలు", HI to "होली की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_holi,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#E91E63",
                accentColor = "#FFC107",
                animationStyle = AnimationStyle.SPARKLE
            ),
            Template(
                id = "raksha_bandhan",
                name = "Raksha Bandhan Wishes",
                localizedNames = mapOf(TE to "రక్షా బంధన్ శుభాకాంక్షలు", HI to "रक्षा बंधन की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_raksha_bandhan,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#FF6F91",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "karva_chauth",
                name = "Karva Chauth Wishes",
                localizedNames = mapOf(TE to "కర్వా చౌత్ శుభాకాంక్షలు", HI to "करवा चौथ की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_karva_chauth,
                durationSeconds = 20,
                isPremium = true,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#4A148C",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "chhath_puja",
                name = "Chhath Puja Wishes",
                localizedNames = mapOf(TE to "ఛఠ్ పూజ శుభాకాంక్షలు", HI to "छठ पूजा की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_chhath_puja,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#FF5722",
                accentColor = "#FFC107",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "navratri",
                name = "Navratri Wishes",
                localizedNames = mapOf(TE to "నవరాత్రి శుభాకాంక్షలు", HI to "नवरात्रि की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_navratri,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FOLK,
                primaryColor = "#9C27B0",
                accentColor = "#FF9800",
                animationStyle = AnimationStyle.SPARKLE
            ),

            // ── Festival (pan-India / national / multi-faith) ──────────────
            Template(
                id = "eid_mubarak",
                name = "Eid Mubarak Wishes",
                localizedNames = mapOf(TE to "ఈద్ ముబారక్ శుభాకాంక్షలు", HI to "ईद मुबारक की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_eid_mubarak,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#00796B",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "christmas",
                name = "Christmas Wishes",
                localizedNames = mapOf(TE to "క్రిస్మస్ శుభాకాంక్షలు", HI to "क्रिसमस की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_christmas,
                durationSeconds = 20,
                isPremium = true,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#B71C1C",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.SPARKLE
            ),
            Template(
                id = "new_year",
                name = "New Year Wishes",
                localizedNames = mapOf(TE to "నూతన సంవత్సర శుభాకాంక్షలు", HI to "नए साल की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_new_year,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#1A237E",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.SPARKLE
            ),
            Template(
                id = "independence_day",
                name = "Independence Day Wishes",
                localizedNames = mapOf(TE to "స్వాతంత్ర్య దినోత్సవ శుభాకాంక్షలు", HI to "स्वतंत्रता दिवस की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_independence_day,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#FF9800",
                accentColor = "#1B5E20",
                animationStyle = AnimationStyle.SLIDE
            ),
            Template(
                id = "republic_day",
                name = "Republic Day Wishes",
                localizedNames = mapOf(TE to "గణతంత్ర దినోత్సవ శుభాకాంక్షలు", HI to "गणतंत्र दिवस की शुभकामनाएं"),
                category = TemplateCategory.FESTIVAL,
                thumbnailResId = R.drawable.thumb_republic_day,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#1565C0",
                accentColor = "#FF9800",
                animationStyle = AnimationStyle.SLIDE
            ),

            // ── Devotional ──────────────────────────────────────────────────
            Template(
                id = "balaji",
                name = "Lord Balaji Wishes",
                localizedNames = mapOf(TE to "శ్రీ వేంకటేశ్వర స్వామి", HI to "श्री वेंकटेश्वर स्वामी"),
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
                localizedNames = mapOf(TE to "దుర్గా మాత", HI to "दुर्गा माता"),
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
                localizedNames = mapOf(TE to "శ్రీ కృష్ణ భగవానుడు", HI to "श्री कृष्ण भगवान"),
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
                localizedNames = mapOf(TE to "శ్రీ రామ భగవానుడు", HI to "श्री राम भगवान"),
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
                localizedNames = mapOf(TE to "రాజకీయ శుభాకాంక్షలు", HI to "राजनीतिक शुभकामनाएं"),
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
                localizedNames = mapOf(TE to "నేత శుభాకాంక్షలు", HI to "नेता जी की शुभकामनाएं"),
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
                localizedNames = mapOf(TE to "విజయ వేడుక", HI to "विजय समारोह"),
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
                localizedNames = mapOf(TE to "మగ బిడ్డ స్వాగతం", HI to "बेटे का स्वागत"),
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
                localizedNames = mapOf(TE to "ఆడ బిడ్డ స్వాగతం", HI to "बेटी का स्वागत"),
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
                localizedNames = mapOf(TE to "బేబీ షవర్", HI to "बेबी शावर"),
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
                localizedNames = mapOf(TE to "నామకరణం", HI to "नामकरण समारोह"),
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
                localizedNames = mapOf(TE to "సాంప్రదాయ వివాహం", HI to "पारंपरिक विवाह"),
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
                localizedNames = mapOf(TE to "ఆధునిక వివాహం", HI to "आधुनिक विवाह"),
                category = TemplateCategory.WEDDING,
                thumbnailResId = R.drawable.thumb_wedding_modern,
                durationSeconds = 20,
                isPremium = true,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#212121",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.SLIDE
            ),
            Template(
                id = "wedding_anniversary",
                name = "Wedding Anniversary",
                localizedNames = mapOf(TE to "వివాహ వార్షికోత్సవం", HI to "विवाह वर्षगांठ"),
                category = TemplateCategory.WEDDING,
                thumbnailResId = R.drawable.thumb_wedding_anniversary,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#B76E79",
                accentColor = "#FFF3E0",
                animationStyle = AnimationStyle.SPARKLE
            ),

            // ── Business ────────────────────────────────────────────────────
            Template(
                id = "biz_opening",
                name = "Grand Opening",
                localizedNames = mapOf(TE to "గ్రాండ్ ఓపెనింగ్", HI to "भव्य उद्घाटन"),
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
                localizedNames = mapOf(TE to "స్పెషల్ ఆఫర్", HI to "विशेष ऑफर"),
                category = TemplateCategory.BUSINESS,
                thumbnailResId = R.drawable.thumb_biz_offer,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#004D40",
                accentColor = "#B2DFDB",
                animationStyle = AnimationStyle.SLIDE
            ),

            // ── Housewarming ────────────────────────────────────────────────
            Template(
                id = "griha_pravesh",
                name = "Griha Pravesh Wishes",
                localizedNames = mapOf(TE to "గృహప్రవేశ శుభాకాంక్షలు", HI to "गृह प्रवेश की शुभकामनाएं"),
                category = TemplateCategory.HOUSEWARMING,
                thumbnailResId = R.drawable.thumb_griha_pravesh,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#C97B4A",
                accentColor = "#FFD700",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "new_home",
                name = "New Home Wishes",
                localizedNames = mapOf(TE to "కొత్త ఇల్లు శుభాకాంక్షలు", HI to "नए घर की शुभकामनाएं"),
                category = TemplateCategory.HOUSEWARMING,
                thumbnailResId = R.drawable.thumb_new_home,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#8D6E63",
                accentColor = "#FFE0B2",
                animationStyle = AnimationStyle.ZOOM
            ),

            // ── Good Morning ────────────────────────────────────────────────
            Template(
                id = "morning_sunrise",
                name = "Rise with the sun, shine with your soul.",
                localizedNames = mapOf(
                    TE to "సూర్యునితో మేల్కో, మీ ఆత్మతో వెలుగు.",
                    HI to "सूरज के साथ जागो, अपनी आत्मा से चमको।"
                ),
                category = TemplateCategory.GOOD_MORNING,
                thumbnailResId = R.drawable.thumb_morning_sunrise,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#FFA94D",
                accentColor = "#FFE08A",
                animationStyle = AnimationStyle.SLIDE
            ),
            Template(
                id = "morning_chai",
                name = "A fresh morning, a fresh chance to smile.",
                localizedNames = mapOf(
                    TE to "కొత్త ఉదయం, నవ్వడానికి కొత్త అవకాశం.",
                    HI to "नई सुबह, मुस्कुराने का नया मौका।"
                ),
                category = TemplateCategory.GOOD_MORNING,
                thumbnailResId = R.drawable.thumb_morning_chai,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#FF9F45",
                accentColor = "#FFD9A0",
                animationStyle = AnimationStyle.FADE
            ),

            // ── Good Night ──────────────────────────────────────────────────
            Template(
                id = "night_stars",
                name = "Let go of today, the stars will hold your dreams.",
                localizedNames = mapOf(
                    TE to "ఈ రోజును వదిలేయండి, నక్షత్రాలు మీ కలలను పట్టుకుంటాయి.",
                    HI to "आज को जाने दो, तारे तुम्हारे सपनों को संभालेंगे।"
                ),
                category = TemplateCategory.GOOD_NIGHT,
                thumbnailResId = R.drawable.thumb_night_stars,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#6B5FD9",
                accentColor = "#8A7FF0",
                animationStyle = AnimationStyle.FADE
            ),
            Template(
                id = "night_moon",
                name = "Rest well, tomorrow is already smiling at you.",
                localizedNames = mapOf(
                    TE to "బాగా విశ్రాంతి తీసుకోండి, రేపు మీవైపు నవ్వుతోంది.",
                    HI to "अच्छी नींद लो, कल पहले से ही तुम्हारी ओर मुस्कुरा रहा है।"
                ),
                category = TemplateCategory.GOOD_NIGHT,
                thumbnailResId = R.drawable.thumb_night_moon,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.DEVOTIONAL,
                primaryColor = "#3D3590",
                accentColor = "#5A6FD9",
                animationStyle = AnimationStyle.FADE
            ),

            // ── Love ────────────────────────────────────────────────────────
            Template(
                id = "love_heart",
                name = "Some hearts just know how to find each other.",
                localizedNames = mapOf(
                    TE to "కొన్ని హృదయాలు ఒకదానికొకటి కనుగొనడం తెలుసు.",
                    HI to "कुछ दिल एक-दूसरे को ढूंढना जानते हैं।"
                ),
                category = TemplateCategory.LOVE,
                thumbnailResId = R.drawable.thumb_love_heart,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#FF4D6D",
                accentColor = "#FF8FA8",
                animationStyle = AnimationStyle.ZOOM
            ),
            Template(
                id = "love_roses",
                name = "Every little moment with you feels like home.",
                localizedNames = mapOf(
                    TE to "నీతో గడిపే ప్రతి క్షణం ఇంటిలా అనిపిస్తుంది.",
                    HI to "तुम्हारे साथ हर पल घर जैसा लगता है।"
                ),
                category = TemplateCategory.LOVE,
                thumbnailResId = R.drawable.thumb_love_roses,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#E0577E",
                accentColor = "#FFB3C6",
                animationStyle = AnimationStyle.FADE
            ),

            // ── Friendship ──────────────────────────────────────────────────
            Template(
                id = "friend_squad",
                name = "Real friends don't need everyday calls to stay close.",
                localizedNames = mapOf(
                    TE to "నిజమైన మిత్రులకు దగ్గరగా ఉండటానికి ప్రతిరోజు కాల్స్ అవసరం లేదు.",
                    HI to "सच्चे दोस्तों को करीब रहने के लिए रोज़ बात करने की ज़रूरत नहीं होती।"
                ),
                category = TemplateCategory.FRIENDSHIP,
                thumbnailResId = R.drawable.thumb_friend_squad,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FOLK,
                primaryColor = "#22D3EE",
                accentColor = "#9EF0FF",
                animationStyle = AnimationStyle.SPARKLE
            ),
            Template(
                id = "friend_bond",
                name = "Good friends are the family we choose for ourselves.",
                localizedNames = mapOf(
                    TE to "మంచి మిత్రులు మనం ఎంచుకున్న కుటుంబం.",
                    HI to "अच्छे दोस्त वह परिवार हैं जिन्हें हम खुद चुनते हैं।"
                ),
                category = TemplateCategory.FRIENDSHIP,
                thumbnailResId = R.drawable.thumb_friend_bond,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#22B8CE",
                accentColor = "#7FE0E8",
                animationStyle = AnimationStyle.FADE
            ),

            // ── Attitude ────────────────────────────────────────────────────
            Template(
                id = "attitude_fire",
                name = "I don't chase, I attract. What's meant to be will be.",
                localizedNames = mapOf(
                    TE to "నేను వెంబడించను, ఆకర్షిస్తాను. జరగాల్సింది జరుగుతుంది.",
                    HI to "मैं पीछा नहीं करता, आकर्षित करता हूं। जो होना है वो होकर रहेगा।"
                ),
                category = TemplateCategory.ATTITUDE,
                thumbnailResId = R.drawable.thumb_attitude_fire,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#C026D3",
                accentColor = "#F08AF0",
                animationStyle = AnimationStyle.ZOOM
            ),
            Template(
                id = "attitude_bold",
                name = "Silence is my reply when words are not worth it.",
                localizedNames = mapOf(
                    TE to "మాటలు అవసరం లేనప్పుడు మౌనమే నా సమాధానం.",
                    HI to "जब शब्दों की कीमत नहीं होती, तो मौन ही मेरा जवाब है।"
                ),
                category = TemplateCategory.ATTITUDE,
                thumbnailResId = R.drawable.thumb_attitude_bold,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.FILMY,
                primaryColor = "#1F1428",
                accentColor = "#C026D3",
                animationStyle = AnimationStyle.SLIDE
            ),

            // ── Motivational ────────────────────────────────────────────────
            Template(
                id = "motivation_rise",
                name = "Every sunrise is a new page — write something good.",
                localizedNames = mapOf(
                    TE to "ప్రతి సూర్యోదయం ఒక కొత్త పేజీ — మంచిది రాయండి.",
                    HI to "हर सूर्योदय एक नया पन्ना है — कुछ अच्छा लिखो।"
                ),
                category = TemplateCategory.MOTIVATIONAL,
                thumbnailResId = R.drawable.thumb_motivation_rise,
                durationSeconds = 15,
                isPremium = false,
                musicStyle = MusicStyle.INSTRUMENTAL,
                primaryColor = "#FFD23F",
                accentColor = "#FFEB9C",
                animationStyle = AnimationStyle.SLIDE
            ),
            Template(
                id = "motivation_grind",
                name = "Small steps every day still take you the whole way.",
                localizedNames = mapOf(
                    TE to "ప్రతిరోజు చిన్న అడుగులు అయినా మిమ్మల్ని పూర్తి దూరం తీసుకెళ్తాయి.",
                    HI to "हर दिन के छोटे कदम भी तुम्हें पूरी मंज़िल तक ले जाते हैं।"
                ),
                category = TemplateCategory.MOTIVATIONAL,
                thumbnailResId = R.drawable.thumb_motivation_grind,
                durationSeconds = 15,
                isPremium = true,
                musicStyle = MusicStyle.CLASSICAL,
                primaryColor = "#F2B705",
                accentColor = "#FFE066",
                animationStyle = AnimationStyle.ZOOM
            )
        )
    }
}
