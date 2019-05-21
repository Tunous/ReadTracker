package me.thanel.readtracker.ui.readinglist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.thanel.readtracker.R
import me.thanel.readtracker.SelectWithBookInformation

class BookAdapter(
    private val onUpdateProgressCallback: (SelectWithBookInformation, Int) -> Unit
) : RecyclerView.Adapter<BookViewHolder>() {

    val items = mutableListOf<SelectWithBookInformation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_currently_reading_book, parent, false)
        return BookViewHolder(view, onUpdateProgressCallback)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val progressItem = items[position]
        holder.bind(progressItem)
    }
}
