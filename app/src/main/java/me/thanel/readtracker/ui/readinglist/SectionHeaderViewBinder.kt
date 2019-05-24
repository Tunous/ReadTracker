package me.thanel.readtracker.ui.readinglist

import android.view.View
import kotlinx.android.synthetic.main.item_section_header.*
import me.thanel.readtracker.R
import me.thanel.recyclerviewutils.viewholder.BaseItemViewBinder
import me.thanel.recyclerviewutils.viewholder.ContainerViewHolder

class SectionHeaderViewBinder :
    BaseItemViewBinder<String, ContainerViewHolder>(R.layout.item_section_header) {

    override fun onCreateViewHolder(itemView: View): ContainerViewHolder {
        return ContainerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, item: String) {
        super.onBindViewHolder(holder, item)
        holder.sectionHeaderTextView.text = item
    }
}
