package nn.zhoeshin.allkeyboard

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter

class ItemEmoji(val value: String?)

class ItemEmojiAdapter(
    dataSet: MutableList<ItemEmoji>,
    val onClick: (String) -> Unit,
) : DragDropSwipeAdapter<ItemEmoji, ItemEmojiAdapter.ViewHolder>(dataSet) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val value: TextView = itemView.findViewById(R.id.value)
    }

    override fun getViewHolder(itemView: View): ViewHolder = ViewHolder(itemView)

    override fun onBindViewHolder(item: ItemEmoji, viewHolder: ViewHolder, position: Int) {
        viewHolder.value.text = item.value//?.replace("\n", "  ")
        viewHolder.value.setOnClickListener {
            onClick(item.value!!)
        }
    }

    override fun getViewToTouchToStartDraggingItem(
        item: ItemEmoji,
        viewHolder: ViewHolder,
        position: Int
    ): View? = null

}