package nn.zhoeshin.allkeyboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import kotlin.math.abs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nn.zhoeshin.allkeyboard.BuiltinLayouts.QWERTY

class AllKeyboardService : InputMethodService(), KeyboardContainer {
    lateinit var clipboardView: ClipboardView
    lateinit var emojiView: EmojiView
    lateinit var clipboardManager: ClipboardManager
    var currentMode = "keyboard"

    lateinit var rootView: LinearLayout
    var keyboardContainer: FrameLayout? = null
    var currentPopup: PopupWindow? = null
    var screenWidth = 0
    val keyHeight: Int by lazy { dpToPx(48) }
    val rowHeight: Int by lazy { keyHeight + dpToPx(2) }

    val screenHeight: Int by lazy { rowHeight * 5 }
    val mainHandler = Handler(Looper.getMainLooper())

    var activePopupView: LongPressOptionsPopupView? = null
    var activePopupAnchor: View? = null

    val swipeThreshold: Int by lazy { dpToPx(24) }

    override var currentLayout = 0
    override var currentState = "main"
    override var returnState: String? = null

    var navBarHeight: Int = 0

    companion object {
        @Volatile
        var instance: AllKeyboardService? = null
    }


    var delayLongPress = 150
    var delayRepeat = 75
    var delayPopupDecay = 75
    override var error = ""

    override var layouts: MutableList<MutableMap<String, View>> = mutableListOf()
    var layoutData: MutableList<MutableMap<String, List<KeyData>>> = mutableListOf()
    fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()

    fun pxToDp(px: Int): Float = px / resources.displayMetrics.density

    fun loadLayouts() {
        currentLayout = 0
        currentState = "main"
        returnState = null

        layouts = mutableListOf()
        layoutData = mutableListOf()

        val dir = File(filesDir, "layouts")
        dir.mkdirs()

        val files = dir.listFiles()?.filter { it.isFile } ?: emptyList()
        val prefs = getSharedPreferences("layout_metadata", MODE_PRIVATE)

        val enabled_ = prefs.getString("enabled", "") ?: ""
        val enabled = if (enabled_.isNotEmpty()) enabled_.split("\n").toSet() else emptySet()

        val order_ = prefs.getString("order", "") ?: ""
        val order = if (order_.isNotEmpty()) order_.split("\n") else emptyList()

        val enabledFiles = files.filter { it.name in enabled }

        val sortedFiles = enabledFiles.sortedBy { file ->
            val index = order.indexOf(file.name)
            if (index >= 0) index else Int.MAX_VALUE
        }

        val gson = Gson()
        val type = object : TypeToken<Map<String, List<KeyData>>>() {}.type

        for (file in sortedFiles) {
            try {
                val json = file.readText()
                val layoutMap: MutableMap<String, List<KeyData>> = gson.fromJson(json, type)
                layoutData.add(layoutMap)
            } catch (e: Exception) {
                Log.e("AllKeyboardService", "Failed to parse layout ${file.name}", e)
                error = e.message.toString() + "\n" + e.stackTrace
            }
        }
    }



    val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clip = clipboardManager.primaryClip
        val text = clip?.getItemAt(0)
        clipboardView?.post {
            clipboardView.addItem(text?.coerceToText(this)?.toString() ?: "")
        }
    }

    override fun onCreate() {
        screenWidth = resources.displayMetrics.widthPixels
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        super.onCreate()
        try {
            loadLayouts()
        } catch (e: Throwable) {
            Toast.makeText(applicationContext, "Load error: ${e.message}", Toast.LENGTH_LONG).show()
            error = e.message.toString() + "\n" + e.stackTrace
        }
        instance = this
        val themedContext = ContextThemeWrapper(this, R.style.KeyboardRootStyle)
        clipboardView = ClipboardView(themedContext).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)


        emojiView = EmojiView(themedContext).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val timerprefs = getSharedPreferences("timers", MODE_PRIVATE)
        delayLongPress = timerprefs.getInt("longPress", 150)
        delayRepeat = timerprefs.getInt("repeat", 75)
        delayPopupDecay = timerprefs.getInt("popupDecay", 75)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        clipboardView.onDestroy()
    }

    fun buildLayouts() {
        try {
            if (layoutData.isEmpty()) {
                error = "No user layouts loaded"
                createLayout(QWERTY)
            } else {
                for (layout_ in layoutData) {
                    createLayout(layout_)
                }
            }

            switchToLayout(currentLayout)
        } catch (e: Throwable) {
            Toast.makeText(applicationContext, "Init error: ${e.message}", Toast.LENGTH_LONG).show()
            error = e.message.toString() + "\n" + e.stackTrace
            createLayout(QWERTY)
            switchToLayout(currentLayout)
        }
    }

    override fun onCreateInputView(): View? {
        screenWidth = resources.displayMetrics.widthPixels

        val themedContext = ContextThemeWrapper(this, R.style.KeyboardRootStyle)

        rootView = LinearLayout(themedContext).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        rootView.addView(createTopBar())
        keyboardContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(50 * 5)//ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        rootView.addView(keyboardContainer)

        buildLayouts()

//        navBarHeight = resources.getIdentifier("navigation_bar_height", "dimen", "android")
//            .let { id -> if (id > 0) resources.getDimensionPixelSize(id) else 0 }
//
//        rootView.setPadding(0, 0, 0, navBarHeight)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                view.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
            } else {
                view.setPadding(0, 0, 0, 0)
            }
            insets
        }

        //window.window!!.findViewById<ViewGroup>(android.R.id.content).addView(rootView)
        //return null

        return rootView
    }

//    override fun onComputeInsets(outInsets: Insets) {
//        outInsets.contentTopInsets = screenHeight + navBarHeight + (window.window?.attributes?.y ?: 0)
//    }

    fun createLayout(layout: MutableMap<String, List<KeyData>>) {
        val newlayout = mutableMapOf<String, View>()
        for ((state, keydata) in layout) {
            val statelayout = createLayoutFrame(keydata)

            newlayout[state] = statelayout
        }
        layouts.add(newlayout)
    }
    fun createLayoutFrame(layout: List<KeyData>): FrameLayout {
        val statelayout = FrameLayout(this)
        statelayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val themedContext = ContextThemeWrapper(this, R.style.KeyContainerStyle)

        for(kd in layout) {
            val button = Key(themedContext, kd)
            statelayout.addView(button)
        }

        return statelayout
    }

    fun createTopBar(): View {
        val themedContext = ContextThemeWrapper(this, R.style.KeyboardBarStyle)
        val topBar = LinearLayout(themedContext).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                screenWidth,
                keyHeight
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        val barButtons = ContextThemeWrapper(this, R.style.KeyboardBarButtonStyle)

        val clipboardBtn = ImageButton(barButtons).apply {
            setImageResource(R.drawable.clip)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                screenWidth / 2,
                dpToPx(48),
            )
            setOnClickListener {
                if (currentMode == "clipboard") {
                    switchToState(currentState)
                    currentMode = "keyboard"
                } else {
                    keyboardContainer?.removeAllViews()
                    (clipboardView.parent as? ViewGroup)?.removeView(clipboardView)
                    keyboardContainer?.addView(clipboardView)
                    currentMode = "clipboard"
                }
            }
        }
        topBar.addView(clipboardBtn, LinearLayout.LayoutParams((screenWidth / 10f).toInt(), keyHeight))

        val settingsBtn = ImageButton(barButtons).apply {
            setImageResource(R.drawable.settings)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                screenWidth / 2,
                dpToPx(48),
            )
            setOnClickListener {
                val intent = Intent(this@AllKeyboardService, SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        topBar.addView(settingsBtn, LinearLayout.LayoutParams((screenWidth / 10f).toInt(), keyHeight))

        val emojiBtn = ImageButton(barButtons).apply {
            setImageResource(R.drawable.emoji)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                screenWidth / 2,
                dpToPx(48),
            )
            setOnClickListener {
                if (currentMode == "emoji") {
                    switchToState(currentState)
                    currentMode = "keyboard"
                } else {
                    keyboardContainer?.removeAllViews()
                    (emojiView.parent as? ViewGroup)?.removeView(emojiView)
                    keyboardContainer?.addView(emojiView)
                    currentMode = "emoji"
                }
            }
        }
        topBar.addView(emojiBtn, LinearLayout.LayoutParams((screenWidth / 10f).toInt(), keyHeight))
        val emoji2Btn = ImageButton(barButtons).apply {
            setImageResource(R.drawable.colon3)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                screenWidth / 2,
                dpToPx(48),
            )
            setOnClickListener {
                if (currentMode == "emoji") {
                    switchToState(currentState)
                    currentMode = "keyboard"
                } else {
                    keyboardContainer?.removeAllViews()
                    (emojiView.parent as? ViewGroup)?.removeView(emojiView)
                    keyboardContainer?.addView(emojiView)
                    currentMode = "emoji"
                }
            }
        }
        topBar.addView(emoji2Btn, LinearLayout.LayoutParams((screenWidth / 10f).toInt(), keyHeight))

        val spacer = View(this)
        topBar.addView(spacer, LinearLayout.LayoutParams(0, keyHeight, 1f))

        return topBar
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return super.onEvaluateInputViewShown()
        //return true
    }
    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    fun switchToMainMode() {
        switchToState(currentState)
        currentMode = "keyboard"
    }

    override fun switchToState(newState: String) {
        keyboardContainer?.removeAllViews()
        val l = layouts[currentLayout][newState] ?: return
        (l.parent as? ViewGroup)?.removeView(l)
        keyboardContainer?.addView(l)
        currentState = newState
    }
    override fun switchToLayout(newLayout: Int) {
        currentLayout = newLayout
        switchToState("main")
    }
    fun textDirect(str: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(str, 1)
    }
    /*
    fun doAction(action: KeyAction, button: View? = null, canSwitch: Boolean = true) {
        val ic = currentInputConnection ?: return

        var switching = false
        when (action.type) {
            KeyActionType.STRING -> {
                ic.commitText(action.value, 1)
            }
            KeyActionType.MAGIC -> {
                when (action.value) {
                    "return" -> {
                        val editorInfo = currentInputEditorInfo
                        val actionId = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
                        val noEnterAction = (editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
                        val isMultiLine = (editorInfo.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0

                        if (noEnterAction || (isMultiLine && (actionId == IME_ACTION_UNSPECIFIED || actionId == IME_ACTION_NONE))) {
                            ic.commitText("\n", 1)
                        } else {
                            ic.performEditorAction(actionId)
                        }
                    }
                    "delete" -> {
                        if (!ic.getSelectedText(0).isNullOrEmpty()) {
                            ic.commitText("", 1)
                        } else {
                            ic.deleteSurroundingText(1, 0)
                        }
                    }
                    "layoutprev" -> {
                        switchToLayout((currentLayout - 1).mod(layouts.size))
                    }
                    "layoutnext" -> {
                        switchToLayout((currentLayout + 1).mod(layouts.size))
                    }
                    "error" -> {
                        ic.commitText(error, 1)
                    }
                    "quit" -> {
                        requestHideSelf(0)
                    }
                    "left" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
                    }
                    "right" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
                    }
                    "up" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP))
                    }
                    "down" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN))
                    }
                    "cut" -> {
                        val selected = ic.getSelectedText(0) ?: return
                        val clip = ClipData.newPlainText("text", selected)
                        getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
                        ic.commitText("", 1)
                    }
                    "copy" -> {
                        val selected = ic.getSelectedText(0) ?: return
                        val clip = ClipData.newPlainText("text", selected)
                        getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
                    }
                    "paste" -> {
                        val clipboard = getSystemService(ClipboardManager::class.java)
                        val clip = clipboard.primaryClip ?: return
                        val text = clip.getItemAt(0).text ?: return
                        currentInputConnection?.commitText(text, 1)
                    }
                    "undo" -> {
                        currentInputConnection?.performContextMenuAction(android.R.id.undo)
                    }
                    "redo" -> {
                        currentInputConnection?.performContextMenuAction(android.R.id.redo)
                    }
                    "selectall" -> {
                        val ic = currentInputConnection ?: return
                        val before = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)?.length ?: 0
                        val after = ic.getTextAfterCursor(Int.MAX_VALUE, 0)?.length ?: 0
                        ic.setSelection(0, before + after)
                    }
                }
            }
            KeyActionType.TRANSITIONPERM -> {
                returnState = null
                switching = true
                switchToState(action.value)
            }
            KeyActionType.TRANSITIONTEMP -> {
                switching = true
                if (returnState == null) {
                    returnState = currentState
                }
                switchToState(action.value)
            }
        }
        if (canSwitch && !switching && (returnState != null)) {
            switchToState(returnState!!)
            returnState = null
        }
    }
    */
    fun switchBack() {
        if (returnState != null) {
            switchToState(returnState!!)
            returnState = null
        }
    }



    fun showLongPressOptions(anchor: View, options: List<KeyActions>, touchx: Float?, touchy: Float?) {
        currentPopup?.dismiss()




        val anchorLocation = IntArray(2)
        anchor.getLocationOnScreen(anchorLocation)
        val anchorx = anchorLocation[0]
        val reverse = anchorx > screenWidth / 2


        val popupView = LongPressOptionsPopupView(this, reverse, options) { selected ->
            var s = selected.doActions(this)
            if (s != null) {
                (anchor as Key).onTextInput?.invoke(s)
            }
            currentPopup?.dismiss()
            activePopupView = null
            activePopupAnchor = null
        }

        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )


        val popup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        ).apply {
            isOutsideTouchable = false
            setOnDismissListener {
                currentPopup = null
                activePopupView = null
                activePopupAnchor = null
            }
        }

        if (reverse) {
            popup.showAsDropDown(anchor, anchor.width - screenWidth / 100 - popupView.measuredWidth, - popupView.measuredHeight - anchor.height)
        } else {
            popup.showAsDropDown(anchor, screenWidth / 100, -popupView.measuredHeight - anchor.height)
        }


        if (touchx != null && touchy != null) {
            popupView.post {
                val popupLocation = IntArray(2)
                popupView.getLocationOnScreen(popupLocation)
                val lx = touchx - popupLocation[0]
                val ly = touchy - popupLocation[1]
                val downTime = SystemClock.uptimeMillis()
                val event = MotionEvent.obtain(
                    downTime,
                    downTime,
                    MotionEvent.ACTION_DOWN,
                    lx,
                    ly,
                    0
                )
                popupView.dispatchTouchEvent(event)
                event.recycle()
            }
        }

        currentPopup = popup
        activePopupView = popupView
        activePopupAnchor = anchor
    }


    inner class LongPressOptionsPopupView(context: Context, reversed: Boolean = false, val options: List<KeyActions>, val onChosen: (KeyActions) -> Unit)
    : View(context) {

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = resources.getDimension(R.dimen.popup_text_size)
            color = ContextCompat.getColor(context, R.color.popup_text)
            textAlign = Paint.Align.CENTER
        }
        val chosenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.popup_highlight)
        }
        var selected = 0
        var selectedx = 0
        var selectedy = 0

        var cellWidth = 0
        var cellHeight = 0
        var rows = 0
        var columns = 0

        var indexes: MutableList<MutableList<Int>>

        init {
            setBackgroundResource(R.drawable.popup_background)

            cellWidth = screenWidth / 12
            cellHeight = resources.getDimension(R.dimen.popup_cell_height).toInt()

            indexes = MutableList(1, {_ -> MutableList(0, {_ -> -1})})

            var i = 0
            if (options.size < 7) {
                while (i < 7 && i < options.size) {
                    indexes.last().add(i)
                    i += 1
                }
            } else if (options.size < 15) {
                while (i < 7 && i < options.size / 2f) {
                    indexes.last().add(i)
                    i += 1
                }
                if (options.size > 7) {
                    indexes.add(MutableList(0, {_ -> -1}))
                    while (i < 15 && indexes.last().size < indexes[0].size) {
                        if (i < options.size) {
                            indexes.last().add(i)
                        } else {
                            indexes.last().add(-1)
                        }
                        i += 1
                    }
                }
            } else {
                while (i < 7 && i < options.size) {
                    indexes.last().add(i)
                    i += 1
                }
                if (options.size > 7) {
                    indexes.add(MutableList(0, {_ -> -1}))
                    while (i < 15 && indexes.last().size < indexes[0].size) {
                        if (i < options.size) {
                            indexes.last().add(i)
                        } else {
                            indexes.last().add(-1)
                        }
                        i += 1
                    }
                }
                if (options.size > 14) {
                    while(i < options.size) {
                        for(row in indexes) {
                            if (i < options.size) {
                                row.add(i)
                            } else {
                                row.add(-1)
                            }
                            i += 1
                        }
                        if (i >= options.size) {
                            break
                        }
                        var newrow = MutableList(0, {_ -> -1})
                        for(row in (0 until indexes[0].size)) {
                            if (i < options.size) {
                                newrow.add(i)
                            } else {
                                newrow.add(-1)
                            }
                            i += 1
                        }
                        indexes.add(newrow)
                    }
                }
            }
            if (reversed) {
                for (row in indexes) {
                    row.reverse()
                }
            }
            rows = indexes.size
            columns = if (indexes.isNotEmpty()) {indexes[0].size} else {0}
            indexes.reverse()
            if (rows == 0 || columns == 0) {
                currentPopup?.dismiss()
            }
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            setMeasuredDimension(cellWidth * columns, cellHeight * rows)
        }

        fun updateSelected(x: Float, y: Float) {
            val realx = (x / cellWidth).toInt().coerceIn(0, columns - 1)
            val realy = (y / cellHeight).toInt().coerceIn(0, rows - 1)
            if (indexes[realy][realx] != -1) {
                selected = indexes[realy][realx]
                selectedx = realx
                selectedy = realy
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    updateSelected(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    updateSelected(event.x, event.y)
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (selected >= 0 && selected < options.size) {
                        onChosen(options[selected])
                    } else {
                        currentPopup?.dismiss()
                    }
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    currentPopup?.dismiss()
                    return true
                }
            }
            return true //super.onTouchEvent(event)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (selected != -1) {
                canvas.drawRect(
                    (selectedx * cellWidth).toFloat(),
                    (selectedy * cellHeight).toFloat(),
                    (selectedx * cellWidth + cellWidth).toFloat(),
                    (selectedy * cellHeight + cellHeight).toFloat(),
                    chosenPaint
                )
            }
            for((y, row) in indexes.withIndex()) {
                for((x, i) in row.withIndex()) {
                    if (i != -1) {
                        val realx = x * cellWidth + cellWidth / 2f
                        val realy = y * cellHeight + cellHeight / 2f - (paint.descent() + paint.ascent()) / 2f
                        canvas.drawText(options[i].label.center, realx, realy, paint)
                    }
                }
            }
        }
    }

    inner class Key(context: Context, val keyData: KeyData): FrameLayout(context) {
        fun addLabel(text: String, gravity: Int, styleRes: Int) {
            if (text.isEmpty()) return
            val tv = TextView(instance)
            tv.text = text
            tv.setTextAppearance(styleRes)
            tv.layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                gravity
            )
            addView(tv)
        }


        var onStartInput: (() -> Unit)? = null
        var onEndInput: (() -> Unit)? = null
        var onTextInput: ((String) -> Unit)? = null
        var onStartLongPress: (() -> Unit)? = null
        val themedContext = ContextThemeWrapper(instance, R.style.KeyContainerStyle)

        init {
            addLabel(keyData.label.center, Gravity.CENTER, R.style.KeyLabel_Center)
            addLabel(keyData.label.corner, Gravity.TOP or Gravity.RIGHT, R.style.KeyLabel_Corner)
            addLabel(keyData.label.left,   Gravity.CENTER_VERTICAL or Gravity.LEFT, R.style.KeyLabel_Left)
            addLabel(keyData.label.right,  Gravity.CENTER_VERTICAL or Gravity.RIGHT, R.style.KeyLabel_Right)
            addLabel(keyData.label.up,     Gravity.TOP or Gravity.CENTER_HORIZONTAL, R.style.KeyLabel_Up)
            addLabel(keyData.label.down,   Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, R.style.KeyLabel_Down)


            layoutParams = LayoutParams(
                (keyData.width * screenWidth).toInt(),
                (keyData.height * screenHeight).toInt()
            )
                .also {
                it.leftMargin = (keyData.x * screenWidth).toInt()
                it.topMargin = (keyData.y * screenHeight).toInt()
            }

            val bg = getResources().getIdentifier("key${keyData.style ?: "1"}", "drawable", getPackageName())
            if (bg != 0) {
                this.setBackgroundResource(bg)
            }

//            setX(keyData.x * screenWidth)
//            setY(keyData.y * screenHeight)
        }



        var longPressing = false
        var swiping = false
        var sx = 0f
        var sy = 0f
        var lx = 0f
        var ly = 0f
        var lastDir: String = "main"
        var labelPopup: PopupWindow? = null
        val dismissLabelPopupRunnable = Runnable {
            labelPopup?.dismiss()
        }
        var labelText: TextView? = null
        val longPressRunnable = Runnable {
            if (currentPopup == null && !swiping) {
                mainHandler.removeCallbacks(dismissLabelPopupRunnable)
                labelPopup?.dismiss()
                if (keyData.longPress.isNotEmpty()) {
                    mainHandler.removeCallbacks(repeatedPressRunnable)
                    showLongPressOptions(this, keyData.longPress, lx, ly)
                    longPressing = true
                    onStartLongPress?.invoke()
                }
            }
        }
        var repeatedPress: String = "main"
        val repeatedPressRunnable: Runnable = Runnable {
            if (lastDir == repeatedPress) {
                var s = keyData[repeatedPress]!!.doActions(
                    AllKeyboardService.instance!!,
                    this,
                    !(keyData[repeatedPress]?.flags?.blockedSwitch ?: false) && !(keyData[repeatedPress]?.flags?.delayedSwitch ?: false))
                if (s != null) {
                    onTextInput?.invoke(s)
                }
                mainHandler.postDelayed(repeatedPressRunnable, delayRepeat.toLong())
            }
        }
        val cellWidth = screenWidth / 12
        val cellHeight = resources.getDimension(R.dimen.popup_cell_height).toInt()

        fun doAndRepeat(dir: String) {
            mainHandler.removeCallbacks(repeatedPressRunnable)
            if (keyData[dir]?.flags?.immediate ?: false) {
                var s = keyData[dir]?.doActions(
                    AllKeyboardService.instance!!,
                    this,
                    !(keyData[dir]?.flags?.blockedSwitch ?: false) && !(keyData[dir]?.flags?.delayedSwitch ?: false))
                if (s != null) {
                    onTextInput?.invoke(s)
                }
                if (keyData[dir]?.flags?.repeated ?: false) {
                    repeatedPress = dir
                    mainHandler.postDelayed(repeatedPressRunnable, delayLongPress.toLong())
                }
            }
        }

        fun showLabelPopup(label: String) {
            if (labelPopup == null && delayPopupDecay > 0) {
                labelText = TextView(themedContext).apply {
                    text = label
                    setPadding(16, 8, 16, 8)
                    setTextColor(Color.WHITE)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    setBackgroundResource(R.drawable.popup_background)
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                    )
                }
                labelPopup = PopupWindow(
                    labelText,
                    cellWidth,
                    cellHeight,
                    true
                ).apply {
                    isFocusable = false
                    isTouchable = false
                    animationStyle = 0
                }
            }
            if (labelPopup != null) {
                labelPopup!!.dismiss()
                labelPopup!!.showAsDropDown(this, screenWidth / 100, -this.height - labelPopup!!.height)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onStartInput?.invoke()
                    mainHandler.removeCallbacks(dismissLabelPopupRunnable)
                    sx = event.rawX
                    sy = event.rawY
                    lx = sx
                    ly = sy
                    swiping = false
                    longPressing = false
                    if (keyData["main"]?.flags?.immediate ?: false) {
                        var s = keyData["main"]!!.doActions(
                            AllKeyboardService.instance!!,
                            this,
                            !(keyData["main"]?.flags?.blockedSwitch ?: false) && !(keyData["main"]?.flags?.delayedSwitch ?: false)
                        )
                        if (s != null) {
                            onTextInput?.invoke(s)
                        }
                    }
                    if (keyData["main"]?.flags?.repeated ?: false) {
                        repeatedPress = "main"
                        mainHandler.postDelayed(repeatedPressRunnable, delayLongPress.toLong())
                    } else {
                        mainHandler.postDelayed(longPressRunnable, delayLongPress.toLong())
                    }
                    isPressed = true
                    if (delayPopupDecay > 0) {
                        showLabelPopup(keyData.label.center)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    lx = event.rawX
                    ly = event.rawY
                    if (currentPopup == null && activePopupView == null) {
                        val dx = event.rawX - sx
                        val dy = event.rawY - sy
                        if (abs(dx) > swipeThreshold || abs(dy) > swipeThreshold) {
                            mainHandler.removeCallbacks(longPressRunnable)
                            longPressing = false
                            swiping = true
                            val swipeDirection = if (abs(dx) > abs(dy)) {
                                if (dx > 0) "right" else "left"
                            } else {
                                if (dy > 0) "down" else "up"
                            }
                            if (labelPopup == null) {
                                showLabelPopup("")
                            }
                            labelText?.text = if (swipeDirection == "up" && !keyData.swipeUp.isNullOrEmpty()) {
                                keyData.label.up
                            } else if (swipeDirection == "down" && !keyData.swipeDown.isNullOrEmpty()) {
                                keyData.label.down
                            } else if (swipeDirection == "left" && !keyData.swipeLeft.isNullOrEmpty()) {
                                keyData.label.left
                            } else if (swipeDirection == "right" && !keyData.swipeRight.isNullOrEmpty()) {
                                keyData.label.right
                            } else {
                                keyData.label.center
                            }
                            if (lastDir != swipeDirection && keyData[swipeDirection] != null) {
                                lastDir = swipeDirection
                                if (!keyData[lastDir].isNullOrEmpty()) {
                                    if (keyData[lastDir]!!.flags.immediate) {
                                        var s = keyData[lastDir]!!.doActions(AllKeyboardService.instance!!,
                                            this,
                                            !(keyData[lastDir]?.flags?.blockedSwitch ?: false) && !(keyData[lastDir]?.flags?.delayedSwitch ?: false)
                                        )
                                        if (s != null) {
                                            onTextInput?.invoke(s)
                                        }
                                        if (keyData[lastDir]!!.flags.start) {
                                            sx = lx
                                            sy = ly
                                            lastDir = "none"
                                        }
                                    }
                                }
                                doAndRepeat(lastDir)
                            }
                        } else {
                            if (labelPopup == null) {
                                showLabelPopup("")
                            }
                            labelText?.text = keyData.label.center
                            if (lastDir != "none") {
                                swiping = false
                            }
                            if (lastDir != "main" && lastDir != "none") {
                                lastDir = "main"
                                if (keyData["main"]?.flags?.immediate ?: false) {
                                    var s = keyData["main"]!!.doActions(
                                        AllKeyboardService.instance!!,
                                        this,
                                        !(keyData["main"]?.flags?.blockedSwitch ?: false) && !(keyData["main"]?.flags?.delayedSwitch ?: false)
                                    )
                                    if (s != null) {
                                        onTextInput?.invoke(s)
                                    }
                                }
                                if (keyData["main"]?.flags?.repeated ?: false) {
                                    repeatedPress = "main"
                                    mainHandler.postDelayed(repeatedPressRunnable, delayRepeat.toLong())
                                } else {
                                    mainHandler.removeCallbacks(longPressRunnable)
                                    mainHandler.postDelayed(longPressRunnable, (delayLongPress * 2).toLong())
                                }
                            }
                        }
                    }
                    if (activePopupView != null) {
                        val popup = activePopupView!!
                        val location = IntArray(2)
                        popup.getLocationOnScreen(location)
                        val localX = event.rawX - location[0]
                        val localY = event.rawY - location[1]
                        val translatedEvent = MotionEvent.obtain(
                            event.downTime,
                            event.eventTime,
                            event.action,
                            localX,
                            localY,
                            event.metaState
                        )
                        popup.dispatchTouchEvent(translatedEvent)
                        translatedEvent.recycle()
                        return true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mainHandler.removeCallbacks(longPressRunnable)
                    mainHandler.removeCallbacks(repeatedPressRunnable)
                    if (swiping) {
                        val dx = event.rawX - sx
                        val dy = event.rawY - sy
                        val swipeDirection = if (abs(dx) > abs(dy)) {
                            if (dx > 0) "right" else "left"
                        } else {
                            if (dy > 0) "down" else "up"
                        }
//                        var actions = when (swipeDirection) {
//                            "up" -> keyData.swipeUp
//                            "down" -> keyData.swipeDown
//                            "left" -> keyData.swipeLeft
//                            "right" -> keyData.swipeRight
//                            else -> null
//                        }
//                        if (actions == null) {
//                            actions = keyData.action
//                        }
//                        for (act in actions?.actions ?: emptyList()) {
//                            doAction(act)
//                        }
                        if(keyData[swipeDirection] == null) {
                            if(keyData[lastDir] != null) {
                                var s = keyData[lastDir]!!.doActions(
                                    AllKeyboardService.instance!!,
                                    this,
                                    !(keyData[lastDir]?.flags?.blockedSwitch ?: false) && !(keyData[lastDir]?.flags?.delayedSwitch ?: false)
                                )
                                if (s != null) {
                                    onTextInput?.invoke(s)
                                }
                            }
                        } else if(!(keyData[swipeDirection]?.flags?.immediate ?: false)) {
                            var s = keyData[swipeDirection]!!.doActions(
                                AllKeyboardService.instance!!,
                                this,
                                !(keyData[swipeDirection]?.flags?.blockedSwitch ?: false) && !(keyData[swipeDirection]?.flags?.delayedSwitch ?: false)
                            )
                            if (s != null) {
                                onTextInput?.invoke(s)
                            }
                        }
                        swiping = false
                    } else if (!longPressing && currentPopup == null) {
                        if (keyData.action != null && !(keyData["main"]?.flags?.immediate ?: false)) {
                            var s = keyData.action!!.doActions(
                                AllKeyboardService.instance!!,
                                this,
                                !(keyData["main"]?.flags?.blockedSwitch ?: false) && !(keyData["main"]?.flags?.delayedSwitch ?: false)
                            )
                            if (s != null) {
                                onTextInput?.invoke(s)
                            }
                        }
                        if (keyData["main"]?.flags?.delayedSwitch ?: false) {
                            switchBack()
                        }
                    } else if (activePopupView != null) {
                        val popup = activePopupView!!
                        val location = IntArray(2)
                        popup.getLocationOnScreen(location)
                        val localX = event.rawX - location[0]
                        val localY = event.rawY - location[1]
                        val translatedEvent = MotionEvent.obtain(
                            event.downTime,
                            event.eventTime,
                            event.action,
                            localX,
                            localY,
                            event.metaState
                        )
                        popup.dispatchTouchEvent(translatedEvent)
                        currentPopup?.dismiss()
                        translatedEvent.recycle()
                        activePopupView = null
                        activePopupAnchor = null
                        currentPopup = null
                    }
                    isPressed = false
                    if (delayPopupDecay <= 0) {
                        dismissLabelPopupRunnable.run()
                    } else {
                        mainHandler.postDelayed(dismissLabelPopupRunnable, delayPopupDecay.toLong())
                    }
                    onEndInput?.invoke()
                }
                MotionEvent.ACTION_CANCEL -> {
                    mainHandler.removeCallbacks(longPressRunnable)
                    mainHandler.removeCallbacks(repeatedPressRunnable)
                    if (activePopupView != null) {
                        activePopupView = null
                        activePopupAnchor = null
                        currentPopup?.dismiss()
                        currentPopup = null
                    }
                    isPressed = false
                    if (delayPopupDecay <= 0) {
                        dismissLabelPopupRunnable.run()
                    } else {
                        mainHandler.postDelayed(dismissLabelPopupRunnable, delayPopupDecay.toLong())
                    }
                    onEndInput?.invoke()
                }
            }
            return true
        }
    }

    fun makeKey(context: Context, keyData: KeyData): Key = Key(context, keyData)
}
