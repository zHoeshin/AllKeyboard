package nn.zhoeshin.allkeyboard

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import com.google.gson.Gson
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.MismatchedToken
import com.github.h0tk3y.betterParse.parser.NoMatchingToken
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.UnexpectedEof
import com.github.h0tk3y.betterParse.parser.UnparsedRemainder
import androidx.core.content.edit
import com.github.h0tk3y.betterParse.parser.Parsed

class LayoutListFragment : Fragment() {

    lateinit var recyclerView: DragDropSwipeRecyclerView
    lateinit var adapter: ItemLayoutAdapter
    val items = mutableListOf<LayoutFile>()
    val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { copyFileToLayouts(it) }
    }

    fun setupAdapter() {
        adapter = ItemLayoutAdapter(
            items,
            onEnabledChanged = { position, enabled ->
                items[position].enabled = enabled
                saveEnabled()
                update()
            },
            onDelete = { position -> deleteItem(position) }
        )
        recyclerView.adapter = adapter
    }

    fun getFileName(uri: android.net.Uri): String? {
        var name: String? = null
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    fun copyFileToLayouts(uri: android.net.Uri) {
        val fileName = getFileName(uri) ?: "layout_${System.currentTimeMillis()}"
        val destFile = File(requireContext().filesDir, "layouts/$fileName")

        try {
            val content = requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: throw IllegalStateException("Could not read input stream")


//            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("Tokens", KeyboardMarkdownParser.tokens(content))
//            clipboard.setPrimaryClip(clip)

            fun failure(e: ErrorResult): TokenMatch? {
                when (e) {
                    is UnparsedRemainder -> e.startsWith
                    is MismatchedToken -> e.found
                    is NoMatchingToken -> e.tokenMismatch
                    is UnexpectedEof -> null
                    is AlternativesFailure -> e.errors.mapNotNull { failure(it) }.maxByOrNull { it.offset }
                }
                return null
            }

            val layout = try {
                KeyboardMarkdownParser.parseToEnd(content.trimStart('\uFEFF'))
            } catch (e: ParseException) {
                val t = failure(e.errorResult)
                if (t == null) {
                    throw IllegalArgumentException(
                        "Failed to parse layout: ${e.message}",
                        e
                    )
                } else {
                    throw IllegalArgumentException(
                        "Failed to parse layout: ${e.message}\nat ${t.row}:${t.column} at $t",
                        e
                    )
                }
            }
            destFile.parentFile?.mkdirs()
            val json = Gson().toJson(layout)
            destFile.writeText(json)

//            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
//                destFile.outputStream().use { outputStream ->
//                    inputStream.copyTo(outputStream)
//                }
//            }

            val prefs = getPrefs()
            val enabledString = prefs.getString("enabled", "") ?: ""
            val enabledSet = if (enabledString.isNotEmpty()) enabledString.split("\n").toMutableSet() else mutableSetOf()
            if (enabledSet.add(fileName)) {
                prefs.edit { putString("enabled", enabledSet.joinToString("\n")) }
            }

            val orderString = prefs.getString("order", "") ?: ""
            val orderList = if (orderString.isNotEmpty()) orderString.split("\n").toMutableList() else mutableListOf()
            if (!orderList.contains(fileName)) {
                orderList.add(fileName)
                prefs.edit { putString("order", orderList.joinToString("\n")) }
                adapter.notifyItemInserted(items.size - 1)
            }
            update()


        } catch (e: Exception) {
            val errorMessage = when (e) {
                is IllegalArgumentException -> e.message ?: "Parsing error"
                else -> "Failed to save layout: ${e.message}"
            }
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Layout import error", errorMessage)
            clipboard.setPrimaryClip(clip)
            e.printStackTrace()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_layout_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        recyclerView = view.findViewById(R.id.layout_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING

        update()

        recyclerView.dragListener = object : OnItemDragListener<LayoutFile> {
            override fun onItemDragged(previousPosition: Int, newPosition: Int, item: LayoutFile) {}
            override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: LayoutFile) {
                val item = items.removeAt(initialPosition)
                items.add(finalPosition, item)
                //adapter.notifyItemMoved(initialPosition, finalPosition)
                AllKeyboardService.instance?.loadLayouts()
                AllKeyboardService.instance?.buildLayouts()
                saveOrder()
            }
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener {
            pickFileLauncher.launch("*/*")
        }
    }

    fun update() {
        loadFiles()
        setupAdapter()
        AllKeyboardService.instance?.loadLayouts()
        AllKeyboardService.instance?.buildLayouts()
    }
    fun deleteItem(position: Int) {
        val item = items[position]
        val fileName = item.name

        val file = File(requireContext().filesDir, "layouts/$fileName")
        if (file.exists()) {
            file.delete()
        }

        val prefs = getPrefs()
        val orderStr = prefs.getString("order", "") ?: ""
        val orderList = if (orderStr.isNotEmpty()) orderStr.split("\n").toMutableList() else mutableListOf()
        orderList.remove(fileName)
        prefs.edit {putString("order", orderList.joinToString("\n"))}

        val enabledStr = prefs.getString("enabled", "") ?: ""
        val enabledList = if (enabledStr.isNotEmpty()) enabledStr.split("\n").toMutableList() else mutableListOf()
        enabledList.remove(fileName)
        prefs.edit { putString("enabled", enabledList.joinToString("\n")) }

        update()
        //adapter.notifyDataSetChanged()
        adapter.notifyItemRemoved(position)
    }

    fun getPrefs() = requireContext().getSharedPreferences("layout_metadata", MODE_PRIVATE)

    fun saveOrder() {
        val lines = mutableListOf<String>()
        for (item in items) {
            lines.add(item.name)
        }
        getPrefs().edit { putString("order", lines.joinToString("\n")) }
    }

    fun loadOrder(): List<String> {
        val str = getPrefs().getString("order", "") ?: ""
        return if (str.isNotEmpty()) str.split("\n") else emptyList()
    }

    fun saveEnabled() {
        val lines = mutableListOf<String>()
        for (item in items) {
            if (item.enabled) {
                lines.add(item.name)
            }
        }
        getPrefs().edit { putString("enabled", lines.joinToString("\n")) }
    }

    fun loadEnabledSet(): Set<String> {
        val str = getPrefs().getString("enabled", "") ?: ""
        return if (str.isNotEmpty()) str.split("\n").toSet() else emptySet()
    }

    fun loadFiles() {
        val dir = File(requireContext().filesDir, "layouts")
        dir.mkdirs()

        val files = dir.listFiles()?.filter { it.isFile } ?: emptyList()
        val order = loadOrder()

        val enabled = loadEnabledSet()

        val unsorted = mutableListOf<LayoutFile>()
        for (file in files) {
            unsorted.add(LayoutFile(file, file.name, file.name in enabled))
        }

        items.clear()
        val orderMap = mutableMapOf<String, Int>()
        for ((index, name) in order.withIndex()) {
            orderMap[name] = index
        }
        val sorted = unsorted.sortedBy { item ->
            orderMap[item.name] ?: Int.MAX_VALUE
        }
        for (item in sorted) {
            items.add(item)
        }
    }
}