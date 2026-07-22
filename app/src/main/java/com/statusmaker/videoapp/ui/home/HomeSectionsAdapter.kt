package com.statusmaker.videoapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.model.CategorySection
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.ui.template.TemplateAdapter

/**
 * Outer vertical adapter for the Home discovery feed. Each row is one
 * category section containing its own horizontally-scrolling RecyclerView —
 * the standard "rows of rows" pattern (Play Store / Pinterest style).
 *
 * When a native ad is supplied (free users only), one in-feed ad card is
 * inserted after the second section — native in-feed ads typically earn a
 * multiple of banner eCPM because they render like content.
 */
class HomeSectionsAdapter(
    private val onTemplateClick: (Template) -> Unit,
    private val onFavoriteToggle: (Template, Boolean) -> Unit,
    private val onSeeAllClick: (CategorySection) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SECTION = 0
        private const val TYPE_AD = 1
        private const val AD_ROW_INDEX = 2   // after the 2nd section
    }

    private var sections: List<CategorySection> = emptyList()
    private var favoriteIds: Set<String> = emptySet()
    private var nativeAd: NativeAd? = null

    fun submitSections(newSections: List<CategorySection>) {
        sections = newSections
        notifyDataSetChanged()
    }

    fun updateFavorites(ids: Set<String>) {
        favoriteIds = ids
        notifyDataSetChanged()
    }

    /** Pass null to remove the ad row (premium users / ad destroyed). */
    fun setNativeAd(ad: NativeAd?) {
        nativeAd = ad
        notifyDataSetChanged()
    }

    private val hasAdRow: Boolean
        get() = nativeAd != null && sections.size >= AD_ROW_INDEX

    /** Maps an adapter position to an index into [sections], skipping the ad row. */
    private fun sectionIndex(position: Int): Int =
        if (hasAdRow && position > AD_ROW_INDEX) position - 1 else position

    inner class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvSectionTitle)
        val seeAll: TextView = view.findViewById(R.id.tvSeeAll)
        val rv: RecyclerView = view.findViewById(R.id.rvSectionTemplates)
        val innerAdapter = TemplateAdapter(
            onTemplateClick = onTemplateClick,
            onFavoriteToggle = onFavoriteToggle,
            layoutRes = R.layout.item_template_compact
        )
        init {
            rv.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            rv.adapter = innerAdapter
        }
    }

    inner class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val adView: NativeAdView = view.findViewById(R.id.nativeAdView)
        val icon: ImageView = view.findViewById(R.id.ivAdIcon)
        val headline: TextView = view.findViewById(R.id.tvAdHeadline)
        val body: TextView = view.findViewById(R.id.tvAdBody)
        val media: MediaView = view.findViewById(R.id.mvAdMedia)
        val cta: MaterialButton = view.findViewById(R.id.btnAdCta)
    }

    override fun getItemViewType(position: Int): Int =
        if (hasAdRow && position == AD_ROW_INDEX) TYPE_AD else TYPE_SECTION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_AD) {
            AdViewHolder(inflater.inflate(R.layout.item_native_ad, parent, false))
        } else {
            SectionViewHolder(inflater.inflate(R.layout.item_home_section, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AdViewHolder -> bindAd(holder)
            is SectionViewHolder -> {
                val section = sections[sectionIndex(position)]
                holder.title.text = "${section.category.emoji} ${section.category.displayName}"
                holder.innerAdapter.submitList(section.templates)
                holder.innerAdapter.updateFavorites(favoriteIds)
                holder.seeAll.setOnClickListener { onSeeAllClick(section) }
            }
        }
    }

    private fun bindAd(holder: AdViewHolder) {
        val ad = nativeAd ?: return
        holder.headline.text = ad.headline
        holder.body.text = ad.body ?: ""
        holder.body.visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.cta.text = ad.callToAction ?: "Learn more"
        val iconDrawable = ad.icon?.drawable
        holder.icon.setImageDrawable(iconDrawable)
        holder.icon.visibility = if (iconDrawable == null) View.GONE else View.VISIBLE

        // Register views with the NativeAdView so AdMob tracks impressions
        // and routes clicks (mandatory — manual click listeners are not allowed).
        holder.adView.headlineView = holder.headline
        holder.adView.bodyView = holder.body
        holder.adView.iconView = holder.icon
        holder.adView.callToActionView = holder.cta
        holder.adView.mediaView = holder.media
        holder.adView.setNativeAd(ad)
    }

    override fun getItemCount() = sections.size + if (hasAdRow) 1 else 0
}
