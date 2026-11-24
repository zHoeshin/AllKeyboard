package nn.zhoeshin.allkeyboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

class ClipboardView(context: Context) : FrameLayout(context), KeyboardContainer {
    var container: DragDropSwipeRecyclerView
    var MAXCLIPBOARDITEMS = 40
    var REMOVEALLDUPLICATES = false
    var history: MutableList<ClipboardItem> = mutableListOf()
    lateinit var adapter: ItemClipboardAdapter


    override var currentLayout: Int = 0
    override var currentState: String = "main"
    override var returnState: String? = null
    override var error: String = ""

    var layoutd: MutableMap<String, List<KeyData>> = mutableMapOf()
    var layout: MutableMap<String, View> = mutableMapOf()

    var layoutContainer: FrameLayout

    init {
        val clipprefs = context.getSharedPreferences("clipboard", MODE_PRIVATE)

        MAXCLIPBOARDITEMS = clipprefs.getInt("max", 40)
        REMOVEALLDUPLICATES = clipprefs.getBoolean("removedupes", false)

        LayoutInflater.from(context).inflate(R.layout.layout_clipboard, this, true)

        container = findViewById(R.id.container)
        container.orientation = DragDropSwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING
        container.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.UP)
        container.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.DOWN)
        container.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.LEFT)
        container.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)

        layoutContainer = findViewById(R.id.keyboardContainer)

        setupHistory()
        setupView()

        buildLayouts()
    }

    fun setupView() {
        adapter = ItemClipboardAdapter(
            history,
            onDelete = { position -> deleteItem(position) },
            onClick = { str -> AllKeyboardService.instance?.textDirect(str) },
        )
        container.adapter = adapter
        container.swipeListener = object : OnItemSwipeListener<ClipboardItem> {
            override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: ClipboardItem): Boolean {
                when (direction) {
                    OnItemSwipeListener.SwipeDirection.LEFT_TO_RIGHT -> {
                        deleteItem(position)
                    }
                    OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT -> {
                        deleteItem(position)
                    }
                    else -> {}
                }
                return true
            }
        }
    }

    fun deleteItem(position: Int) {
        history.removeAt(position)
        adapter.notifyItemRemoved(position)
        saveHistory()
    }

    fun addItem(value: String?) {
        if (value.isNullOrEmpty()) {
            return
        }
        if (REMOVEALLDUPLICATES) {
            if (history.any { it.value == value }) {
                return
            }
        } else {
            if (history.isNotEmpty() && history[0].value == value) {
                return
            }
        }
        history.add(0, ClipboardItem(value))
        adapter.notifyItemInserted(history.size - 1)
        if (MAXCLIPBOARDITEMS > 0) {
            while (history.size > MAXCLIPBOARDITEMS) {
                history.removeAt(history.size - 1)
                adapter.notifyItemRemoved(history.size - 1)
            }
        }
        setupView()
        saveHistory()
    }
    fun setupHistory() {
        try {
            val file = File(context.noBackupFilesDir, "clipboard.json")
            val contents = if (!file.exists()) {
                "[]"
            } else {
                file.readText()
            }
            val type = object : TypeToken<MutableList<ClipboardItem>>() {}.type
            history = Gson().fromJson(contents, type)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val currentlastitem = clipboard.primaryClip?.getItemAt(0)
            val currentlast = currentlastitem?.coerceToText(context)
            if (
                (!currentlast.isNullOrEmpty() && history.isNotEmpty() && history[0].value != currentlast)
                || (history.isEmpty() && !currentlast.isNullOrEmpty())
                ) {
                history.add(0, ClipboardItem(
                    currentlast.toString()
                ))
                saveHistory()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Clipboard not loaded: ${e.message}", Toast.LENGTH_LONG).show()
            history = mutableListOf()
        }
    }

    fun saveHistory() {
        val file = File(context.noBackupFilesDir, "clipboard.json")
        try {
            file.parentFile?.mkdirs()
            val json = Gson().toJson(history)
            file.writeText(json)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun onDestroy() {
        saveHistory()
    }

    fun loadLayouts() {
        val t1 = File(AllKeyboardService.instance!!.filesDir, "special_layouts")
        if (t1.isFile) {
            t1.delete()
        }
        val file = File(AllKeyboardService.instance!!.filesDir, "special_layouts/${SpecialLayout.CLIPBOARD.layoutName}.json")
        val gson = Gson()
        val type = object : TypeToken<Map<String, List<KeyData>>>() {}.type
        try {
            val json = file.readText()
            val layoutMap: MutableMap<String, List<KeyData>> = gson.fromJson(json, type)
            layoutd = layoutMap
        } catch (e: Exception) {
            Log.e("AllKeyboardService", "Failed to parse layout ${file.name}", e)
            error = e.message.toString() + "\n" + e.stackTrace
        }
    }
    fun buildLayouts() {
        layout.values.forEach {view -> (view.parent as? ViewGroup)?.removeView(view)}
        layout.clear()
        loadLayouts()
        if ("main" !in layoutd) {
            return
        }
        for (p in layoutd.entries) {
            val statelayout = FrameLayout(context)
            statelayout.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            statelayout.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            val themedContext = ContextThemeWrapper(context, R.style.KeyContainerStyle)
            for(kd in p.value) {
                    val button = AllKeyboardService.instance!!.makeKey(themedContext, kd)
                    statelayout.addView(button)
            }
            layout[p.key] = statelayout
        }
        layoutContainer.addView(layout["main"])
    }

    override var layouts: MutableList<MutableMap<String, View>> = mutableListOf()

    override fun switchToLayout(newLayout: Int) {
        return
    }

    override fun switchToState(newState: String) {
        layoutContainer.removeAllViews()
        val l = layout[newState]!!
        (l.parent as? ViewGroup)?.removeView(l)
        layoutContainer.addView(l)
        currentState = newState
    }
}