package nn.zhoeshin.allkeyboard

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import java.io.File

class LayoutFile(val file: File, val name: String, var enabled: Boolean = false)

class ItemLayoutAdapter(
    dataSet: MutableList<LayoutFile>,
    val onEnabledChanged: (Int, Boolean) -> Unit,
    val onDelete: (Int) -> Unit
) : DragDropSwipeAdapter<LayoutFile, ItemLayoutAdapter.ViewHolder>(dataSet) {

    class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.name)
        val enabledButton: CheckBox = itemView.findViewById(R.id.enabled)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete)
        val dragHandle: ImageView = itemView.findViewById(R.id.drag)
    }

    override fun getViewHolder(itemLayout: View): ViewHolder = ViewHolder(itemLayout)

    override fun onBindViewHolder(item: LayoutFile, holder: ViewHolder, position: Int) {
        holder.fileName.text = item.name
        holder.enabledButton.isChecked = item.enabled
        holder.enabledButton.setOnCheckedChangeListener { _, isChecked ->
            item.enabled = isChecked
            onEnabledChanged(position, isChecked)
        }
        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getViewToTouchToStartDraggingItem(
        item: LayoutFile,
        holder: ViewHolder,
        position: Int
    ): View? = holder.dragHandle
}