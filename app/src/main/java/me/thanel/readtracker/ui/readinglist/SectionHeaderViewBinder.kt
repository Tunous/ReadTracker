package me.thanel.readtracker.ui.readinglist

import kotlinx.android.synthetic.main.item_section_header.*
import me.thanel.readtracker.R
import me.thanel.recyclerviewutils.viewholder.ContainerViewHolder
import me.thanel.recyclerviewutils.viewholder.SimpleItemViewBinder

class SectionHeaderViewBinder : SimpleItemViewBinder<String>(R.layout.item_section_header) {

    override fun onBindViewHolder(holder: ContainerViewHolder, item: String) {
        super.onBindViewHolder(holder, item)
        holder.sectionHeaderTextView.text = item
    }
}
