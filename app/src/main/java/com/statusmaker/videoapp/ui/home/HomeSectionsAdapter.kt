package com.statusmaker.videoapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.model.CategorySection
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.ui.template.TemplateAdapter

/**
 * Outer vertical adapter for the Home discovery feed. Each row is one
 * category section containing its own horizontally-scrolling RecyclerView —
 * the standard "rows of rows" pattern (Play Store / Pinterest style).
 * Each section ViewHolder owns exactly one inner RecyclerView + adapter,
 * which the outer RecyclerView recycles together — the correct way to do
 * nested scrolling lists without recycling bugs.
 */
class HomeSectionsAdapter(
    private val onTemplateClick: (Template) -> Unit,
    private val onFavoriteToggle: (Template, Boolean) -> Unit,
    private val onSeeAllClick: (CategorySection) -> Unit
) : RecyclerView.Adapter<HomeSectionsAdapter.SectionViewHolder>() {

    private var sections: List<CategorySection> = emptyList()
    private var favoriteIds: Set<String> = emptySet()

    fun submitSections(newSections: List<CategorySection>) {
        sections = newSections
        notifyDataSetChanged()
    }

    fun updateFavorites(ids: Set<String>) {
        favoriteIds = ids
        notifyDataSetChanged()
    }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]
        holder.title.text = "${section.category.emoji} ${section.category.displayName}"
        holder.innerAdapter.submitList(section.templates)
        holder.innerAdapter.updateFavorites(favoriteIds)
        holder.seeAll.setOnClickListener { onSeeAllClick(section) }
    }

    override fun getItemCount() = sections.size
}
