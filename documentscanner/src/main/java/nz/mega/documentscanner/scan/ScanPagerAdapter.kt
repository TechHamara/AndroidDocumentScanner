package nz.mega.documentscanner.scan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nz.mega.documentscanner.data.Page
import nz.mega.documentscanner.databinding.ItemScanBinding

class ScanPagerAdapter : RecyclerView.Adapter<ScanPagerViewHolder>() {

    private var items = emptyList<Page>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanPagerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemScanBinding.inflate(layoutInflater, parent, false)
        return ScanPagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanPagerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int =
        items.size

    override fun getItemId(position: Int): Long =
        items[position].id

    fun submitList(items: List<Page>) {
        this.items = items
        notifyDataSetChanged()
    }
}
