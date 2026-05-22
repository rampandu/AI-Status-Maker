package com.statusmaker.videoapp.ui.template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.statusmaker.videoapp.R
import com.statusmaker.videoapp.data.model.Template
import com.statusmaker.videoapp.data.model.TemplateCategory

// ─── Category Adapter ─────────────────────────────────────────────────────────

class CategoryAdapter(
    private val categories: List<TemplateCategory>,
    private val onCategoryClick: (TemplateCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.tvCategoryEmoji)
        val name: TextView = view.findViewById(R.id.tvCategoryName)
        val teluguName: TextView = view.findViewById(R.id.tvCategoryTelugu)
        val container: View = view.findViewById(R.id.categoryContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.emoji.text = category.emoji
        holder.name.text = category.displayName
        holder.teluguName.text = category.teluguName
        holder.container.setOnClickListener { onCategoryClick(category) }
    }

    override fun getItemCount() = categories.size
}

// ─── Template Adapter ─────────────────────────────────────────────────────────

class TemplateAdapter(
    private val onTemplateClick: (Template) -> Unit
) : ListAdapter<Template, TemplateAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.ivTemplateThumbnail)
        val name: TextView = view.findViewById(R.id.tvTemplateName)
        val teluguName: TextView = view.findViewById(R.id.tvTemplateTeluguName)
        val duration: TextView = view.findViewById(R.id.tvTemplateDuration)
        val premiumBadge: View = view.findViewById(R.id.ivPremiumBadge)
        val musicIcon: TextView = view.findViewById(R.id.tvMusicStyle)
        val container: View = view.findViewById(R.id.templateContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_template, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val template = currentList[position]

        holder.thumbnail.setImageResource(
            try { template.thumbnailResId }
            catch (e: Exception) { R.drawable.ic_template_placeholder }
        )
        holder.name.text = template.name
        holder.teluguName.text = template.teluguName
        holder.duration.text = "${template.durationSeconds}s"
        holder.premiumBadge.visibility = if (template.isPremium) View.VISIBLE else View.GONE
        holder.musicIcon.text = when (template.musicStyle.displayName) {
            "Folk / Janapada" -> "🎵 Folk"
            "Devotional / Bhakti" -> "🙏 Bhakti"
            "Filmy / Tollywood" -> "🎬 Filmy"
            else -> "🎵 ${template.musicStyle.displayName}"
        }

        holder.container.setOnClickListener { onTemplateClick(template) }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Template>() {
            override fun areItemsTheSame(old: Template, new: Template) = old.id == new.id
            override fun areContentsTheSame(old: Template, new: Template) = old == new
        }
    }
}
