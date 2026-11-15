package nn.zhoeshin.allkeyboard

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter

class ClipboardItem(val value: String?)

class ItemClipboardAdapter(
    dataSet: MutableList<ClipboardItem>,
    val onDelete: (Int) -> Unit,
    val onClick: (String) -> Unit,
) : DragDropSwipeAdapter<ClipboardItem, ItemClipboardAdapter.ViewHolder>(dataSet) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val value: TextView = itemView.findViewById(R.id.value)
        val delete: ImageButton = itemView.findViewById(R.id.delete)
    }

    override fun getViewHolder(itemView: View): ViewHolder = ViewHolder(itemView)

    override fun onBindViewHolder(item: ClipboardItem, viewHolder: ViewHolder, position: Int) {
        viewHolder.value.text = item.value//?.replace("\n", "  ")
        viewHolder.delete.setOnClickListener {
            onDelete(position)
        }
        viewHolder.value.setOnClickListener {
            onClick(item.value!!)
        }
    }

    override fun getViewToTouchToStartDraggingItem(
        item: ClipboardItem,
        viewHolder: ViewHolder,
        position: Int
    ): View? = null
}