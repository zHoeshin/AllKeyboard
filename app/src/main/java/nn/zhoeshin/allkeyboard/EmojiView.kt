package nn.zhoeshin.allkeyboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Toast
import androidx.gridlayout.widget.GridLayout
import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.ImageViewCompat
import androidx.core.graphics.toColorInt
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import kotlin.text.isNullOrEmpty

class EmojiView(context: Context): FrameLayout(context) {
    /*
        people, nature, food, infrastructure, abstract, flags
    */
    val CATEGORIES = 6
    val loaded = MutableList(CATEGORIES){false}
    val btns = listOf(
        R.id.btn_history,
        R.id.btn_people,
        R.id.btn_nature,
        R.id.btn_food,
        R.id.btn_infrastructure,
        R.id.btn_abstract,
        R.id.btn_flags,
    )
    var categoryviews: MutableList<GridLayout?> = MutableList(CATEGORIES){null}
    var currentcategory: GridLayout? = null
    val COLUMNS = 9

    var container: FrameLayout
    var scroll: ScrollView
    val btnbg = getResources().getIdentifier("key0", "drawable", context.getPackageName())
    val allEmojiData = compileEmojis()

    var history: MutableMap<String, Int> = mutableMapOf()
    var historyWrapper: MutableList<ItemEmoji> = mutableListOf()
    var currentHistory: Boolean = false
    lateinit var historyContainer: DragDropSwipeRecyclerView
    var MAXHISTORYITEMS: Int = 50
    lateinit var adapter: ItemEmojiAdapter

    init {LayoutInflater.from(context).inflate(R.layout.layout_emojis, this, true)
        container = findViewById(R.id.gridcontainer)
        scroll = findViewById(R.id.scroll)

        ImageViewCompat.setImageTintList(
            findViewById<ImageButton>(R.id.btn_back),
            ColorStateList.valueOf("#908090".toColorInt())
        )
        findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener{ b ->
            scroll.fling(0)
            scroll.scrollTo(0, 0)
            AllKeyboardService.instance?.switchToMainMode()
        }


        val clipprefs = context.getSharedPreferences("emojis", MODE_PRIVATE)
        MAXHISTORYITEMS = clipprefs.getInt("max", 50)
        setupHistory()
        setupHistoryView()
        findViewById<ImageButton>(R.id.btn_history)?.setOnClickListener { b ->
            (currentcategory?.parent as? ViewGroup)?.removeView(currentcategory)
            container.removeAllViews()
            container.addView(historyContainer)
            scroll.fling(0)
            scroll.scrollTo(0, 0)
            updateTints(0)
        }


        for((i, id) in btns.withIndex()) {
            if (i == 0) {
                continue
            }
            val index = i.toLong().toInt() - 1
            findViewById<ImageButton>(id)?.setOnClickListener { b ->
                if (index !in loaded.indices) return@setOnClickListener
                updateTints(index + 1)
                if(!loaded[index]) {
                    buildCategory(index)
                }
                (currentcategory?.parent as? ViewGroup)?.removeView(currentcategory)
                container.removeAllViews()
                container.addView(categoryviews[index])
                scroll.fling(0)
                scroll.scrollTo(0, 0)
            }
        }

        buildCategory(0)
        container.addView(categoryviews[0])
        currentcategory = categoryviews[0]
        updateTints(1)
    }

    fun buildCategory(id: Int) {
        loaded[id] = true
        categoryviews[id] = GridLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            columnCount = COLUMNS
        }
        var i = 0
        for(key in allEmojiData[id]) {
            val b = AllKeyboardService.instance?.makeKey(context, key.value)!!.apply {
                layoutParams = GridLayout.LayoutParams()
            }
//                b!!.onStartInput = {
//                    scroll.requestDisallowInterceptTouchEvent(true)
//                }
            b.layoutParams = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(i / COLUMNS, 1f)
                columnSpec = GridLayout.spec(i % COLUMNS, 1f)
                width = (AllKeyboardService.instance?.screenWidth ?: 200) / COLUMNS
                height = (AllKeyboardService.instance?.screenWidth ?: 200) / COLUMNS
            }
            b.onStartLongPress = {
                scroll.requestDisallowInterceptTouchEvent(true)
            }
            b.onEndInput = {
                scroll.requestDisallowInterceptTouchEvent(false)
            }
            b.onTextInput = { s ->
                history[s] = history.getOrElse(s, {0}) + 1
                updateHistoryView()
            }
            b.setBackgroundResource(btnbg)
            categoryviews[id]?.addView(b)
            i++
        }
    }


    fun setupHistory() {
        try {
            val file = File(context.noBackupFilesDir, "emojis.json")
            val contents = if (!file.exists()) {
                "{}"
            } else {
                file.readText()
            }
            val type = object : TypeToken<Map<String, Int>>() {}.type
            history = Gson().fromJson(contents, type)
        } catch (e: Exception) {
            Toast.makeText(context, "Emoji history not loaded: ${e.message}", Toast.LENGTH_LONG).show()
            history = mutableMapOf()
        }
        val min = history.values.minOrNull()
        if (min == null) {
            return
        }
        history = history.mapValues{(k, v) -> v - min}.toMutableMap()
        historyWrapper = history.entries
            .sortedByDescending{(k, v) -> v}
            .take(MAXHISTORYITEMS)
            .map{(k, v) -> ItemEmoji(k)}
            .toMutableList()
    }

    fun setupHistoryView() {
        historyContainer = findViewById(R.id.history_container)
        historyContainer.orientation =
            DragDropSwipeRecyclerView.ListOrientation.GRID_LIST_WITH_HORIZONTAL_SWIPING
        historyContainer.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.UP)
        historyContainer.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.DOWN)
        historyContainer.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.LEFT)
        historyContainer.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)

        adapter = ItemEmojiAdapter(
            historyWrapper,
            onClick = { s ->
                AllKeyboardService.instance?.textDirect(s)
                history[s] = history.getOrElse(s, {0}) + 1
                updateHistoryView()
            },
        )
        historyContainer.adapter = adapter
        historyContainer.swipeListener = object : OnItemSwipeListener<ItemEmoji> {
            override fun onItemSwiped(
                position: Int,
                direction: OnItemSwipeListener.SwipeDirection,
                item: ItemEmoji
            ): Boolean {
                when (direction) {
                    OnItemSwipeListener.SwipeDirection.LEFT_TO_RIGHT -> {
                        deleteHistoryItem(item.value!!)
                    }

                    OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT -> {
                        deleteHistoryItem(item.value!!)
                    }

                    else -> {}
                }
                return true
            }
        }
    }

    fun updateHistoryView() {
        if (currentHistory) {
            return
        }
        val newhistory = history.entries
            .sortedByDescending{(k, v) -> v}
            .take(MAXHISTORYITEMS)
            .map{(k, v) -> ItemEmoji(k)}
        historyWrapper.clear()
        historyWrapper.addAll(newhistory)
        historyContainer.post {
            adapter.notifyDataSetChanged()
        }
        adapter.dataSet = historyWrapper
        saveHistory()
    }

    fun deleteHistoryItem(item: String) {
        history.remove(item)
        saveHistory()
        updateHistoryView()
    }

    fun saveHistory() {
        val file = File(context.noBackupFilesDir, "emojis.json")
        try {
            file.parentFile?.mkdirs()
            val json = Gson().toJson(history)
            file.writeText(json)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun updateTints(active: Int) {
        if (currentHistory && active != 0) {
            currentHistory = false
            updateHistoryView()
        }
        if (active == 0) {
            currentHistory = true
        }
        for((i, id) in btns.withIndex()){
            ImageViewCompat.setImageTintList(
                findViewById(id),
                ColorStateList.valueOf(if (active == i) {
                    "#ffffff".toColorInt()
                } else {
                    "#908090".toColorInt()
                })
            )
        }
    }
    fun compileEmojis() = """grinning face:😀
grinning face with big eyes:😃
grinning face with smiling eyes:😄
beaming face with smiling eyes:😁
grinning squinting face:😆
grinning face with sweat:😅
rolling on the floor laughing:🤣
face with tears of joy:😂
slightly smiling face:🙂
upside down face:🙃
melting face:🫠
winking face:😉
smiling face with smiling eyes:😊
smiling face with halo:😇
smiling face with hearts:🥰
smiling face with heart eyes:😍
star struck:🤩
face blowing a kiss:😘
kissing face:😗
smiling face:☺ ☺️
kissing face with closed eyes:😚
kissing face with smiling eyes:😙
smiling face with tear:🥲
face savoring food:😋
face with tongue:😛
winking face with tongue:😜
zany face:🤪
squinting face with tongue:😝
money mouth face:🤑
smiling face with open hands:🤗
face with hand over mouth:🤭
face with open eyes and hand over mouth:🫢
face with peeking eye:🫣
shushing face:🤫
thinking face:🤔
saluting face:🫡
zipper mouth face:🤐
face with raised eyebrow:🤨
neutral face:😐
expressionless face:😑
face without mouth:😶
dotted line face:🫥
face in clouds:😶‍🌫 😶‍🌫️
smirking face:😏
unamused face:😒
face with rolling eyes:🙄
grimacing face:😬
face exhaling:😮‍💨
lying face:🤥
shaking face:🫨
head shaking horizontally:🙂‍↔ 🙂‍↔️
head shaking vertically:🙂‍↕ 🙂‍↕️
relieved face:😌
pensive face:😔
sleepy face:😪
drooling face:🤤
sleeping face:😴
face with bags under eyes:🫩
face with medical mask:😷
face with thermometer:🤒
face with head bandage:🤕
nauseated face:🤢
face vomiting:🤮
sneezing face:🤧
hot face:🥵
cold face:🥶
woozy face:🥴
face with crossed out eyes:😵
face with spiral eyes:😵‍💫
exploding head:🤯
cowboy hat face:🤠
partying face:🥳
disguised face:🥸
smiling face with sunglasses:😎
nerd face:🤓
face with monocle:🧐
confused face:😕
face with diagonal mouth:🫤
worried face:😟
slightly frowning face:🙁
frowning face:☹ ☹️
face with open mouth:😮
hushed face:😯
astonished face:😲
flushed face:😳
distorted face:🫪
pleading face:🥺
face holding back tears:🥹
frowning face with open mouth:😦
anguished face:😧
fearful face:😨
anxious face with sweat:😰
sad but relieved face:😥
crying face:😢
loudly crying face:😭
face screaming in fear:😱
confounded face:😖
persevering face:😣
disappointed face:😞
downcast face with sweat:😓
weary face:😩
tired face:😫
yawning face:🥱
face with steam from nose:😤
enraged face:😡
angry face:😠
face with symbols on mouth:🤬
smiling face with horns:😈
angry face with horns:👿
skull:💀
skull and crossbones:☠ ☠️
pile of poo:💩
clown face:🤡
ogre:👹
goblin:👺
ghost:👻
alien:👽
alien monster:👾
robot:🤖
grinning cat:😺
grinning cat with smiling eyes:😸
cat with tears of joy:😹
smiling cat with heart eyes:😻
cat with wry smile:😼
kissing cat:😽
weary cat:🙀
crying cat:😿
pouting cat:😾
see no evil monkey:🙈
hear no evil monkey:🙉
speak no evil monkey:🙊
love letter:💌
heart with arrow:💘
heart with ribbon:💝
sparkling heart:💖
growing heart:💗
beating heart:💓
revolving hearts:💞
two hearts:💕
heart decoration:💟
heart exclamation:❣ ❣️
broken heart:💔
heart on fire:❤‍🔥 ❤️‍🔥
mending heart:❤‍🩹 ❤️‍🩹
red heart:❤ ❤️
pink heart:🩷
orange heart:🧡
yellow heart:💛
green heart:💚
blue heart:💙
light blue heart:🩵
purple heart:💜
brown heart:🤎
black heart:🖤
grey heart:🩶
white heart:🤍
kiss mark:💋
hundred points:💯
anger symbol:💢
fight cloud:🫯
collision:💥
dizzy:💫
sweat droplets:💦
dashing away:💨
hole:🕳 🕳️
speech balloon:💬
eye in speech bubble:👁‍🗨 👁‍🗨️ 👁️‍🗨 👁️‍🗨️
left speech bubble:🗨 🗨️
right anger bubble:🗯 🗯️
thought balloon:💭
ZZZ:💤
waving hand:👋
waving hand:👋🏻 👋🏼 👋🏽 👋🏾 👋🏿
raised back of hand:🤚 🤚🏻 🤚🏼 🤚🏽 🤚🏾 🤚🏿
hand with fingers splayed:🖐 🖐️
hand with fingers splayed:🖐🏻 🖐🏼 🖐🏽 🖐🏾 🖐🏿
raised hand:✋
raised hand:✋🏻 ✋🏼 ✋🏽 ✋🏾 ✋🏿
vulcan salute:🖖 🖖🏻 🖖🏼 🖖🏽 🖖🏾 🖖🏿
rightwards hand:🫱 🫱🏻 🫱🏼 🫱🏽 🫱🏾 🫱🏿
leftwards hand:🫲 🫲🏻 🫲🏼 🫲🏽 🫲🏾 🫲🏿
palm down hand:🫳 🫳🏻 🫳🏼 🫳🏽 🫳🏾 🫳🏿
palm up hand:🫴 🫴🏻 🫴🏼 🫴🏽 🫴🏾 🫴🏿
leftwards pushing hand:🫷 🫷🏻 🫷🏼 🫷🏽 🫷🏾 🫷🏿
rightwards pushing hand:🫸 🫸🏻 🫸🏼 🫸🏽 🫸🏾 🫸🏿
OK hand:👌
OK hand:👌🏻 👌🏼 👌🏽 👌🏾 👌🏿
pinched fingers:🤌 🤌🏻 🤌🏼 🤌🏽 🤌🏾 🤌🏿
pinching hand:🤏 🤏🏻 🤏🏼 🤏🏽 🤏🏾 🤏🏿
victory hand:✌ ✌️
victory hand:✌🏻 ✌🏼 ✌🏽 ✌🏾 ✌🏿
crossed fingers:🤞 🤞🏻 🤞🏼 🤞🏽 🤞🏾 🤞🏿
hand with index finger and thumb crossed:🫰 🫰🏻 🫰🏼 🫰🏽 🫰🏾 🫰🏿
love you gesture:🤟 🤟🏻 🤟🏼 🤟🏽 🤟🏾 🤟🏿
sign of the horns:🤘 🤘🏻 🤘🏼 🤘🏽 🤘🏾 🤘🏿
call me hand:🤙 🤙🏻 🤙🏼 🤙🏽 🤙🏾 🤙🏿
backhand index pointing left:👈
backhand index pointing left:👈🏻 👈🏼 👈🏽 👈🏾 👈🏿
backhand index pointing right:👉
backhand index pointing right:👉🏻 👉🏼 👉🏽 👉🏾 👉🏿
backhand index pointing up:👆
backhand index pointing up:👆🏻 👆🏼 👆🏽 👆🏾 👆🏿
middle finger:🖕 🖕🏻 🖕🏼 🖕🏽 🖕🏾 🖕🏿
backhand index pointing down:👇
backhand index pointing down:👇🏻 👇🏼 👇🏽 👇🏾 👇🏿
index pointing up:☝ ☝️
index pointing up:☝🏻 ☝🏼 ☝🏽 ☝🏾 ☝🏿
index pointing at the viewer:🫵 🫵🏻 🫵🏼 🫵🏽 🫵🏾 🫵🏿
thumbs up:👍
thumbs up:👍🏻 👍🏼 👍🏽 👍🏾 👍🏿
thumbs down:👎
thumbs down:👎🏻 👎🏼 👎🏽 👎🏾 👎🏿
raised fist:✊
raised fist:✊🏻 ✊🏼 ✊🏽 ✊🏾 ✊🏿
oncoming fist:👊
oncoming fist:👊🏻 👊🏼 👊🏽 👊🏾 👊🏿
left facing fist:🤛 🤛🏻 🤛🏼 🤛🏽 🤛🏾 🤛🏿
right facing fist:🤜 🤜🏻 🤜🏼 🤜🏽 🤜🏾 🤜🏿
clapping hands:👏
clapping hands:👏🏻 👏🏼 👏🏽 👏🏾 👏🏿
raising hands:🙌
raising hands:🙌🏻 🙌🏼 🙌🏽 🙌🏾 🙌🏿
heart hands:🫶 🫶🏻 🫶🏼 🫶🏽 🫶🏾 🫶🏿
open hands:👐
open hands:👐🏻 👐🏼 👐🏽 👐🏾 👐🏿
palms up together:🤲 🤲🏻 🤲🏼 🤲🏽 🤲🏾 🤲🏿
handshake:🤝
handshake:🤝🏻 🤝🏼 🤝🏽 🤝🏾 🤝🏿 🫱🏻‍🫲🏼 🫱🏻‍🫲🏽 🫱🏻‍🫲🏾 🫱🏻‍🫲🏿 🫱🏼‍🫲🏻 🫱🏼‍🫲🏽 🫱🏼‍🫲🏾 🫱🏼‍🫲🏿 🫱🏽‍🫲🏻 🫱🏽‍🫲🏼 🫱🏽‍🫲🏾 🫱🏽‍🫲🏿 🫱🏾‍🫲🏻 🫱🏾‍🫲🏼 🫱🏾‍🫲🏽 🫱🏾‍🫲🏿 🫱🏿‍🫲🏻 🫱🏿‍🫲🏼 🫱🏿‍🫲🏽 🫱🏿‍🫲🏾
folded hands:🙏
folded hands:🙏🏻 🙏🏼 🙏🏽 🙏🏾 🙏🏿
writing hand:✍ ✍️
writing hand:✍🏻 ✍🏼 ✍🏽 ✍🏾 ✍🏿
nail polish:💅
nail polish:💅🏻 💅🏼 💅🏽 💅🏾 💅🏿
selfie:🤳 🤳🏻 🤳🏼 🤳🏽 🤳🏾 🤳🏿
flexed biceps:💪
flexed biceps:💪🏻 💪🏼 💪🏽 💪🏾 💪🏿
mechanical arm:🦾
mechanical leg:🦿
leg:🦵 🦵🏻 🦵🏼 🦵🏽 🦵🏾 🦵🏿
foot:🦶 🦶🏻 🦶🏼 🦶🏽 🦶🏾 🦶🏿
ear:👂
ear:👂🏻 👂🏼 👂🏽 👂🏾 👂🏿
ear with hearing aid:🦻 🦻🏻 🦻🏼 🦻🏽 🦻🏾 🦻🏿
nose:👃
nose:👃🏻 👃🏼 👃🏽 👃🏾 👃🏿
brain:🧠
anatomical heart:🫀
lungs:🫁
tooth:🦷
bone:🦴
eyes:👀
eye:👁 👁️
tongue:👅
mouth:👄
biting lip:🫦
baby:👶
baby:👶🏻 👶🏼 👶🏽 👶🏾 👶🏿
child:🧒 🧒🏻 🧒🏼 🧒🏽 🧒🏾 🧒🏿
6:👦 👧 👨 👩 👱
0:👦🏻 👦🏼 👦🏽 👦🏾 👦🏿 👧🏻 👧🏼 👧🏽 👧🏾 👧🏿 👨🏻 👨🏼 👨🏽 👨🏾 👨🏿 👩🏻 👩🏼 👩🏽 👩🏾 👩🏿 👱🏻 👱🏼 👱🏽 👱🏾 👱🏿
0:🧑 🧑🏻 🧑🏼 🧑🏽 🧑🏾 🧑🏿 🧔 🧔🏻 🧔🏼 🧔🏽 🧔🏾 🧔🏿
1:🧔‍♀ 🧔‍♀️ 🧔‍♂ 🧔‍♂️ 🧔🏻‍♀ 🧔🏻‍♀️ 🧔🏻‍♂ 🧔🏻‍♂️ 🧔🏼‍♀ 🧔🏼‍♀️ 🧔🏼‍♂ 🧔🏼‍♂️ 🧔🏽‍♀ 🧔🏽‍♀️ 🧔🏽‍♂ 🧔🏽‍♂️ 🧔🏾‍♀ 🧔🏾‍♀️ 🧔🏾‍♂ 🧔🏾‍♂️ 🧔🏿‍♀ 🧔🏿‍♀️ 🧔🏿‍♂ 🧔🏿‍♂️
0:👨‍🦰 👨‍🦱 👨‍🦲 👨‍🦳 👨🏻‍🦰 👨🏻‍🦱 👨🏻‍🦲 👨🏻‍🦳 👨🏼‍🦰 👨🏼‍🦱 👨🏼‍🦲 👨🏼‍🦳 👨🏽‍🦰 👨🏽‍🦱 👨🏽‍🦲 👨🏽‍🦳 👨🏾‍🦰 👨🏾‍🦱 👨🏾‍🦲 👨🏾‍🦳 👨🏿‍🦰 👨🏿‍🦱 👨🏿‍🦲 👨🏿‍🦳 👩‍🦰 👩‍🦱 👩‍🦲 👩‍🦳 👩🏻‍🦰 👩🏻‍🦱 👩🏻‍🦲 👩🏻‍🦳 👩🏼‍🦰 👩🏼‍🦱 👩🏼‍🦲 👩🏼‍🦳 👩🏽‍🦰 👩🏽‍🦱 👩🏽‍🦲 👩🏽‍🦳 👩🏾‍🦰 👩🏾‍🦱 👩🏾‍🦲 👩🏾‍🦳 👩🏿‍🦰 👩🏿‍🦱 👩🏿‍🦲 👩🏿‍🦳
1:🧑‍🦰 🧑‍🦱 🧑‍🦲 🧑‍🦳 🧑🏻‍🦰 🧑🏻‍🦱 🧑🏻‍🦲 🧑🏻‍🦳 🧑🏼‍🦰 🧑🏼‍🦱 🧑🏼‍🦲 🧑🏼‍🦳 🧑🏽‍🦰 🧑🏽‍🦱 🧑🏽‍🦲 🧑🏽‍🦳 🧑🏾‍🦰 🧑🏾‍🦱 🧑🏾‍🦲 🧑🏾‍🦳 🧑🏿‍🦰 🧑🏿‍🦱 🧑🏿‍🦲 🧑🏿‍🦳
0:👱‍♀ 👱‍♀️ 👱‍♂ 👱‍♂️ 👱🏻‍♀ 👱🏻‍♀️ 👱🏻‍♂ 👱🏻‍♂️ 👱🏼‍♀ 👱🏼‍♀️ 👱🏼‍♂ 👱🏼‍♂️ 👱🏽‍♀ 👱🏽‍♀️ 👱🏽‍♂ 👱🏽‍♂️ 👱🏾‍♀ 👱🏾‍♀️ 👱🏾‍♂ 👱🏾‍♂️ 👱🏿‍♀ 👱🏿‍♀️ 👱🏿‍♂ 👱🏿‍♂️
older:🧓 🧓🏻 🧓🏼 🧓🏽 🧓🏾 🧓🏿
old:👴 👵
old:👴🏻 👴🏼 👴🏽 👴🏾 👴🏿 👵🏻 👵🏼 👵🏽 👵🏾 👵🏿
frowning:🙍
frowning:🙍🏻 🙍🏼 🙍🏽 🙍🏾 🙍🏿
frowning:🙍‍♀ 🙍‍♀️ 🙍‍♂ 🙍‍♂️ 🙍🏻‍♀ 🙍🏻‍♀️ 🙍🏻‍♂ 🙍🏻‍♂️ 🙍🏼‍♀ 🙍🏼‍♀️ 🙍🏼‍♂ 🙍🏼‍♂️ 🙍🏽‍♀ 🙍🏽‍♀️ 🙍🏽‍♂ 🙍🏽‍♂️ 🙍🏾‍♀ 🙍🏾‍♀️ 🙍🏾‍♂ 🙍🏾‍♂️ 🙍🏿‍♀ 🙍🏿‍♀️ 🙍🏿‍♂ 🙍🏿‍♂️
pouting:🙎
pouting:🙎🏻 🙎🏼 🙎🏽 🙎🏾 🙎🏿
pouting:🙎‍♀ 🙎‍♀️ 🙎‍♂ 🙎‍♂️ 🙎🏻‍♀ 🙎🏻‍♀️ 🙎🏻‍♂ 🙎🏻‍♂️ 🙎🏼‍♀ 🙎🏼‍♀️ 🙎🏼‍♂ 🙎🏼‍♂️ 🙎🏽‍♀ 🙎🏽‍♀️ 🙎🏽‍♂ 🙎🏽‍♂️ 🙎🏾‍♀ 🙎🏾‍♀️ 🙎🏾‍♂ 🙎🏾‍♂️ 🙎🏿‍♀ 🙎🏿‍♀️ 🙎🏿‍♂ 🙎🏿‍♂️
gesturing NO:🙅
gesturing NO:🙅🏻 🙅🏼 🙅🏽 🙅🏾 🙅🏿
gesturing NO:🙅‍♀ 🙅‍♀️ 🙅‍♂ 🙅‍♂️ 🙅🏻‍♀ 🙅🏻‍♀️ 🙅🏻‍♂ 🙅🏻‍♂️ 🙅🏼‍♀ 🙅🏼‍♀️ 🙅🏼‍♂ 🙅🏼‍♂️ 🙅🏽‍♀ 🙅🏽‍♀️ 🙅🏽‍♂ 🙅🏽‍♂️ 🙅🏾‍♀ 🙅🏾‍♀️ 🙅🏾‍♂ 🙅🏾‍♂️ 🙅🏿‍♀ 🙅🏿‍♀️ 🙅🏿‍♂ 🙅🏿‍♂️
gesturing OK:🙆
gesturing OK:🙆🏻 🙆🏼 🙆🏽 🙆🏾 🙆🏿
gesturing OK:🙆‍♀ 🙆‍♀️ 🙆‍♂ 🙆‍♂️ 🙆🏻‍♀ 🙆🏻‍♀️ 🙆🏻‍♂ 🙆🏻‍♂️ 🙆🏼‍♀ 🙆🏼‍♀️ 🙆🏼‍♂ 🙆🏼‍♂️ 🙆🏽‍♀ 🙆🏽‍♀️ 🙆🏽‍♂ 🙆🏽‍♂️ 🙆🏾‍♀ 🙆🏾‍♀️ 🙆🏾‍♂ 🙆🏾‍♂️ 🙆🏿‍♀ 🙆🏿‍♀️ 🙆🏿‍♂ 🙆🏿‍♂️
tipping hand:💁
tipping hand:💁🏻 💁🏼 💁🏽 💁🏾 💁🏿
tipping hand:💁‍♀ 💁‍♀️ 💁‍♂ 💁‍♂️ 💁🏻‍♀ 💁🏻‍♀️ 💁🏻‍♂ 💁🏻‍♂️ 💁🏼‍♀ 💁🏼‍♀️ 💁🏼‍♂ 💁🏼‍♂️ 💁🏽‍♀ 💁🏽‍♀️ 💁🏽‍♂ 💁🏽‍♂️ 💁🏾‍♀ 💁🏾‍♀️ 💁🏾‍♂ 💁🏾‍♂️ 💁🏿‍♀ 💁🏿‍♀️ 💁🏿‍♂ 💁🏿‍♂️
raising hand:🙋
raising hand:🙋🏻 🙋🏼 🙋🏽 🙋🏾 🙋🏿
raising hand:🙋‍♀ 🙋‍♀️ 🙋‍♂ 🙋‍♂️ 🙋🏻‍♀ 🙋🏻‍♀️ 🙋🏻‍♂ 🙋🏻‍♂️ 🙋🏼‍♀ 🙋🏼‍♀️ 🙋🏼‍♂ 🙋🏼‍♂️ 🙋🏽‍♀ 🙋🏽‍♀️ 🙋🏽‍♂ 🙋🏽‍♂️ 🙋🏾‍♀ 🙋🏾‍♀️ 🙋🏾‍♂ 🙋🏾‍♂️ 🙋🏿‍♀ 🙋🏿‍♀️ 🙋🏿‍♂ 🙋🏿‍♂️
deaf:🧏 🧏‍♀ 🧏‍♀️ 🧏‍♂ 🧏‍♂️ 🧏🏻 🧏🏻‍♀ 🧏🏻‍♀️ 🧏🏻‍♂ 🧏🏻‍♂️ 🧏🏼 🧏🏼‍♀ 🧏🏼‍♀️ 🧏🏼‍♂ 🧏🏼‍♂️ 🧏🏽 🧏🏽‍♀ 🧏🏽‍♀️ 🧏🏽‍♂ 🧏🏽‍♂️ 🧏🏾 🧏🏾‍♀ 🧏🏾‍♀️ 🧏🏾‍♂ 🧏🏾‍♂️ 🧏🏿 🧏🏿‍♀ 🧏🏿‍♀️ 🧏🏿‍♂ 🧏🏿‍♂️
bowing:🙇
bowing:🙇🏻 🙇🏼 🙇🏽 🙇🏾 🙇🏿
bowing:🙇‍♀ 🙇‍♀️ 🙇‍♂ 🙇‍♂️ 🙇🏻‍♀ 🙇🏻‍♀️ 🙇🏻‍♂ 🙇🏻‍♂️ 🙇🏼‍♀ 🙇🏼‍♀️ 🙇🏼‍♂ 🙇🏼‍♂️ 🙇🏽‍♀ 🙇🏽‍♀️ 🙇🏽‍♂ 🙇🏽‍♂️ 🙇🏾‍♀ 🙇🏾‍♀️ 🙇🏾‍♂ 🙇🏾‍♂️ 🙇🏿‍♀ 🙇🏿‍♀️ 🙇🏿‍♂ 🙇🏿‍♂️
facepalming:🤦 🤦🏻 🤦🏼 🤦🏽 🤦🏾 🤦🏿
facepalming:🤦‍♀ 🤦‍♀️ 🤦‍♂ 🤦‍♂️ 🤦🏻‍♀ 🤦🏻‍♀️ 🤦🏻‍♂ 🤦🏻‍♂️ 🤦🏼‍♀ 🤦🏼‍♀️ 🤦🏼‍♂ 🤦🏼‍♂️ 🤦🏽‍♀ 🤦🏽‍♀️ 🤦🏽‍♂ 🤦🏽‍♂️ 🤦🏾‍♀ 🤦🏾‍♀️ 🤦🏾‍♂ 🤦🏾‍♂️ 🤦🏿‍♀ 🤦🏿‍♀️ 🤦🏿‍♂ 🤦🏿‍♂️
shrugging:🤷 🤷🏻 🤷🏼 🤷🏽 🤷🏾 🤷🏿
shrugging:🤷‍♀ 🤷‍♀️ 🤷‍♂ 🤷‍♂️ 🤷🏻‍♀ 🤷🏻‍♀️ 🤷🏻‍♂ 🤷🏻‍♂️ 🤷🏼‍♀ 🤷🏼‍♀️ 🤷🏼‍♂ 🤷🏼‍♂️ 🤷🏽‍♀ 🤷🏽‍♀️ 🤷🏽‍♂ 🤷🏽‍♂️ 🤷🏾‍♀ 🤷🏾‍♀️ 🤷🏾‍♂ 🤷🏾‍♂️ 🤷🏿‍♀ 🤷🏿‍♀️ 🤷🏿‍♂ 🤷🏿‍♂️
health worker:🧑‍⚕ 🧑‍⚕️ 🧑🏻‍⚕ 🧑🏻‍⚕️ 🧑🏼‍⚕ 🧑🏼‍⚕️ 🧑🏽‍⚕ 🧑🏽‍⚕️ 🧑🏾‍⚕ 🧑🏾‍⚕️ 🧑🏿‍⚕ 🧑🏿‍⚕️
health worker:👨‍⚕ 👨‍⚕️ 👨🏻‍⚕ 👨🏻‍⚕️ 👨🏼‍⚕ 👨🏼‍⚕️ 👨🏽‍⚕ 👨🏽‍⚕️ 👨🏾‍⚕ 👨🏾‍⚕️ 👨🏿‍⚕ 👨🏿‍⚕️ 👩‍⚕ 👩‍⚕️ 👩🏻‍⚕ 👩🏻‍⚕️ 👩🏼‍⚕ 👩🏼‍⚕️ 👩🏽‍⚕ 👩🏽‍⚕️ 👩🏾‍⚕ 👩🏾‍⚕️ 👩🏿‍⚕ 👩🏿‍⚕️
student:🧑‍🎓 🧑🏻‍🎓 🧑🏼‍🎓 🧑🏽‍🎓 🧑🏾‍🎓 🧑🏿‍🎓
student:👨‍🎓 👨🏻‍🎓 👨🏼‍🎓 👨🏽‍🎓 👨🏾‍🎓 👨🏿‍🎓 👩‍🎓 👩🏻‍🎓 👩🏼‍🎓 👩🏽‍🎓 👩🏾‍🎓 👩🏿‍🎓
teacher:🧑‍🏫 🧑🏻‍🏫 🧑🏼‍🏫 🧑🏽‍🏫 🧑🏾‍🏫 🧑🏿‍🏫
teacher:👨‍🏫 👨🏻‍🏫 👨🏼‍🏫 👨🏽‍🏫 👨🏾‍🏫 👨🏿‍🏫 👩‍🏫 👩🏻‍🏫 👩🏼‍🏫 👩🏽‍🏫 👩🏾‍🏫 👩🏿‍🏫
judge:🧑‍⚖ 🧑‍⚖️ 🧑🏻‍⚖ 🧑🏻‍⚖️ 🧑🏼‍⚖ 🧑🏼‍⚖️ 🧑🏽‍⚖ 🧑🏽‍⚖️ 🧑🏾‍⚖ 🧑🏾‍⚖️ 🧑🏿‍⚖ 🧑🏿‍⚖️
judge:👨‍⚖ 👨‍⚖️ 👨🏻‍⚖ 👨🏻‍⚖️ 👨🏼‍⚖ 👨🏼‍⚖️ 👨🏽‍⚖ 👨🏽‍⚖️ 👨🏾‍⚖ 👨🏾‍⚖️ 👨🏿‍⚖ 👨🏿‍⚖️ 👩‍⚖ 👩‍⚖️ 👩🏻‍⚖ 👩🏻‍⚖️ 👩🏼‍⚖ 👩🏼‍⚖️ 👩🏽‍⚖ 👩🏽‍⚖️ 👩🏾‍⚖ 👩🏾‍⚖️ 👩🏿‍⚖ 👩🏿‍⚖️
farmer:🧑‍🌾 🧑🏻‍🌾 🧑🏼‍🌾 🧑🏽‍🌾 🧑🏾‍🌾 🧑🏿‍🌾
farmer:👨‍🌾 👨🏻‍🌾 👨🏼‍🌾 👨🏽‍🌾 👨🏾‍🌾 👨🏿‍🌾 👩‍🌾 👩🏻‍🌾 👩🏼‍🌾 👩🏽‍🌾 👩🏾‍🌾 👩🏿‍🌾
cook:🧑‍🍳 🧑🏻‍🍳 🧑🏼‍🍳 🧑🏽‍🍳 🧑🏾‍🍳 🧑🏿‍🍳
cook:👨‍🍳 👨🏻‍🍳 👨🏼‍🍳 👨🏽‍🍳 👨🏾‍🍳 👨🏿‍🍳 👩‍🍳 👩🏻‍🍳 👩🏼‍🍳 👩🏽‍🍳 👩🏾‍🍳 👩🏿‍🍳
mechanic:🧑‍🔧 🧑🏻‍🔧 🧑🏼‍🔧 🧑🏽‍🔧 🧑🏾‍🔧 🧑🏿‍🔧
mechanic:👨‍🔧 👨🏻‍🔧 👨🏼‍🔧 👨🏽‍🔧 👨🏾‍🔧 👨🏿‍🔧 👩‍🔧 👩🏻‍🔧 👩🏼‍🔧 👩🏽‍🔧 👩🏾‍🔧 👩🏿‍🔧
factory worker:🧑‍🏭 🧑🏻‍🏭 🧑🏼‍🏭 🧑🏽‍🏭 🧑🏾‍🏭 🧑🏿‍🏭
factory worker:👨‍🏭 👨🏻‍🏭 👨🏼‍🏭 👨🏽‍🏭 👨🏾‍🏭 👨🏿‍🏭 👩‍🏭 👩🏻‍🏭 👩🏼‍🏭 👩🏽‍🏭 👩🏾‍🏭 👩🏿‍🏭
office worker:🧑‍💼 🧑🏻‍💼 🧑🏼‍💼 🧑🏽‍💼 🧑🏾‍💼 🧑🏿‍💼
office worker:👨‍💼 👨🏻‍💼 👨🏼‍💼 👨🏽‍💼 👨🏾‍💼 👨🏿‍💼 👩‍💼 👩🏻‍💼 👩🏼‍💼 👩🏽‍💼 👩🏾‍💼 👩🏿‍💼
scientist:🧑‍🔬 🧑🏻‍🔬 🧑🏼‍🔬 🧑🏽‍🔬 🧑🏾‍🔬 🧑🏿‍🔬
scientist:👨‍🔬 👨🏻‍🔬 👨🏼‍🔬 👨🏽‍🔬 👨🏾‍🔬 👨🏿‍🔬 👩‍🔬 👩🏻‍🔬 👩🏼‍🔬 👩🏽‍🔬 👩🏾‍🔬 👩🏿‍🔬
technologist:🧑‍💻 🧑🏻‍💻 🧑🏼‍💻 🧑🏽‍💻 🧑🏾‍💻 🧑🏿‍💻
technologist:👨‍💻 👨🏻‍💻 👨🏼‍💻 👨🏽‍💻 👨🏾‍💻 👨🏿‍💻 👩‍💻 👩🏻‍💻 👩🏼‍💻 👩🏽‍💻 👩🏾‍💻 👩🏿‍💻
singer:🧑‍🎤 🧑🏻‍🎤 🧑🏼‍🎤 🧑🏽‍🎤 🧑🏾‍🎤 🧑🏿‍🎤
singer:👨‍🎤 👨🏻‍🎤 👨🏼‍🎤 👨🏽‍🎤 👨🏾‍🎤 👨🏿‍🎤 👩‍🎤 👩🏻‍🎤 👩🏼‍🎤 👩🏽‍🎤 👩🏾‍🎤 👩🏿‍🎤
artist:🧑‍🎨 🧑🏻‍🎨 🧑🏼‍🎨 🧑🏽‍🎨 🧑🏾‍🎨 🧑🏿‍🎨
artist:👨‍🎨 👨🏻‍🎨 👨🏼‍🎨 👨🏽‍🎨 👨🏾‍🎨 👨🏿‍🎨 👩‍🎨 👩🏻‍🎨 👩🏼‍🎨 👩🏽‍🎨 👩🏾‍🎨 👩🏿‍🎨
pilot:🧑‍✈ 🧑‍✈️ 🧑🏻‍✈ 🧑🏻‍✈️ 🧑🏼‍✈ 🧑🏼‍✈️ 🧑🏽‍✈ 🧑🏽‍✈️ 🧑🏾‍✈ 🧑🏾‍✈️ 🧑🏿‍✈ 🧑🏿‍✈️
pilot:👨‍✈ 👨‍✈️ 👨🏻‍✈ 👨🏻‍✈️ 👨🏼‍✈ 👨🏼‍✈️ 👨🏽‍✈ 👨🏽‍✈️ 👨🏾‍✈ 👨🏾‍✈️ 👨🏿‍✈ 👨🏿‍✈️ 👩‍✈ 👩‍✈️ 👩🏻‍✈ 👩🏻‍✈️ 👩🏼‍✈ 👩🏼‍✈️ 👩🏽‍✈ 👩🏽‍✈️ 👩🏾‍✈ 👩🏾‍✈️ 👩🏿‍✈ 👩🏿‍✈️
astronaut:🧑‍🚀 🧑🏻‍🚀 🧑🏼‍🚀 🧑🏽‍🚀 🧑🏾‍🚀 🧑🏿‍🚀
astronaut:👨‍🚀 👨🏻‍🚀 👨🏼‍🚀 👨🏽‍🚀 👨🏾‍🚀 👨🏿‍🚀 👩‍🚀 👩🏻‍🚀 👩🏼‍🚀 👩🏽‍🚀 👩🏾‍🚀 👩🏿‍🚀
firefighter:🧑‍🚒 🧑🏻‍🚒 🧑🏼‍🚒 🧑🏽‍🚒 🧑🏾‍🚒 🧑🏿‍🚒
firefighter:👨‍🚒 👨🏻‍🚒 👨🏼‍🚒 👨🏽‍🚒 👨🏾‍🚒 👨🏿‍🚒 👩‍🚒 👩🏻‍🚒 👩🏼‍🚒 👩🏽‍🚒 👩🏾‍🚒 👩🏿‍🚒
police officer:👮
police officer:👮🏻 👮🏼 👮🏽 👮🏾 👮🏿
police officer:👮‍♀ 👮‍♀️ 👮‍♂ 👮‍♂️ 👮🏻‍♀ 👮🏻‍♀️ 👮🏻‍♂ 👮🏻‍♂️ 👮🏼‍♀ 👮🏼‍♀️ 👮🏼‍♂ 👮🏼‍♂️ 👮🏽‍♀ 👮🏽‍♀️ 👮🏽‍♂ 👮🏽‍♂️ 👮🏾‍♀ 👮🏾‍♀️ 👮🏾‍♂ 👮🏾‍♂️ 👮🏿‍♀ 👮🏿‍♀️ 👮🏿‍♂ 👮🏿‍♂️
detective:🕵 🕵️
detective:🕵🏻 🕵🏼 🕵🏽 🕵🏾 🕵🏿
detective:🕵‍♀ 🕵‍♀️ 🕵‍♂ 🕵‍♂️ 🕵️‍♀ 🕵️‍♀️ 🕵️‍♂ 🕵️‍♂️ 🕵🏻‍♀ 🕵🏻‍♀️ 🕵🏻‍♂ 🕵🏻‍♂️ 🕵🏼‍♀ 🕵🏼‍♀️ 🕵🏼‍♂ 🕵🏼‍♂️ 🕵🏽‍♀ 🕵🏽‍♀️ 🕵🏽‍♂ 🕵🏽‍♂️ 🕵🏾‍♀ 🕵🏾‍♀️ 🕵🏾‍♂ 🕵🏾‍♂️ 🕵🏿‍♀ 🕵🏿‍♀️ 🕵🏿‍♂ 🕵🏿‍♂️
guard:💂
guard:💂🏻 💂🏼 💂🏽 💂🏾 💂🏿
guard:💂‍♀ 💂‍♀️ 💂‍♂ 💂‍♂️ 💂🏻‍♀ 💂🏻‍♀️ 💂🏻‍♂ 💂🏻‍♂️ 💂🏼‍♀ 💂🏼‍♀️ 💂🏼‍♂ 💂🏼‍♂️ 💂🏽‍♀ 💂🏽‍♀️ 💂🏽‍♂ 💂🏽‍♂️ 💂🏾‍♀ 💂🏾‍♀️ 💂🏾‍♂ 💂🏾‍♂️ 💂🏿‍♀ 💂🏿‍♀️ 💂🏿‍♂ 💂🏿‍♂️
ninja:🥷 🥷🏻 🥷🏼 🥷🏽 🥷🏾 🥷🏿
construction worker:👷
construction worker:👷🏻 👷🏼 👷🏽 👷🏾 👷🏿
construction worker:👷‍♀ 👷‍♀️ 👷‍♂ 👷‍♂️ 👷🏻‍♀ 👷🏻‍♀️ 👷🏻‍♂ 👷🏻‍♂️ 👷🏼‍♀ 👷🏼‍♀️ 👷🏼‍♂ 👷🏼‍♂️ 👷🏽‍♀ 👷🏽‍♀️ 👷🏽‍♂ 👷🏽‍♂️ 👷🏾‍♀ 👷🏾‍♀️ 👷🏾‍♂ 👷🏾‍♂️ 👷🏿‍♀ 👷🏿‍♀️ 👷🏿‍♂ 👷🏿‍♂️
with crown:🫅 🫅🏻 🫅🏼 🫅🏽 🫅🏾 🫅🏿
prince:🤴 🤴🏻 🤴🏼 🤴🏽 🤴🏾 🤴🏿
princess:👸
princess:👸🏻 👸🏼 👸🏽 👸🏾 👸🏿
wearing turban:👳
wearing turban:👳🏻 👳🏼 👳🏽 👳🏾 👳🏿
wearing turban:👳‍♀ 👳‍♀️ 👳‍♂ 👳‍♂️ 👳🏻‍♀ 👳🏻‍♀️ 👳🏻‍♂ 👳🏻‍♂️ 👳🏼‍♀ 👳🏼‍♀️ 👳🏼‍♂ 👳🏼‍♂️ 👳🏽‍♀ 👳🏽‍♀️ 👳🏽‍♂ 👳🏽‍♂️ 👳🏾‍♀ 👳🏾‍♀️ 👳🏾‍♂ 👳🏾‍♂️ 👳🏿‍♀ 👳🏿‍♀️ 👳🏿‍♂ 👳🏿‍♂️
with skullcap:👲
with skullcap:👲🏻 👲🏼 👲🏽 👲🏾 👲🏿
with headscarf:🧕 🧕🏻 🧕🏼 🧕🏽 🧕🏾 🧕🏿
in tuxedo:🤵 🤵🏻 🤵🏼 🤵🏽 🤵🏾 🤵🏿
in tuxedo:🤵‍♀ 🤵‍♀️ 🤵‍♂ 🤵‍♂️ 🤵🏻‍♀ 🤵🏻‍♀️ 🤵🏻‍♂ 🤵🏻‍♂️ 🤵🏼‍♀ 🤵🏼‍♀️ 🤵🏼‍♂ 🤵🏼‍♂️ 🤵🏽‍♀ 🤵🏽‍♀️ 🤵🏽‍♂ 🤵🏽‍♂️ 🤵🏾‍♀ 🤵🏾‍♀️ 🤵🏾‍♂ 🤵🏾‍♂️ 🤵🏿‍♀ 🤵🏿‍♀️ 🤵🏿‍♂ 🤵🏿‍♂️
with veil:👰
with veil:👰🏻 👰🏼 👰🏽 👰🏾 👰🏿
with veil:👰‍♀ 👰‍♀️ 👰‍♂ 👰‍♂️ 👰🏻‍♀ 👰🏻‍♀️ 👰🏻‍♂ 👰🏻‍♂️ 👰🏼‍♀ 👰🏼‍♀️ 👰🏼‍♂ 👰🏼‍♂️ 👰🏽‍♀ 👰🏽‍♀️ 👰🏽‍♂ 👰🏽‍♂️ 👰🏾‍♀ 👰🏾‍♀️ 👰🏾‍♂ 👰🏾‍♂️ 👰🏿‍♀ 👰🏿‍♀️ 👰🏿‍♂ 👰🏿‍♂️
pregnant:🤰 🤰🏻 🤰🏼 🤰🏽 🤰🏾 🤰🏿
pregnant:🫃 🫃🏻 🫃🏼 🫃🏽 🫃🏾 🫃🏿 🫄 🫄🏻 🫄🏼 🫄🏽 🫄🏾 🫄🏿
breast feeding:🤱 🤱🏻 🤱🏼 🤱🏽 🤱🏾 🤱🏿
feeding baby:👨‍🍼 👨🏻‍🍼 👨🏼‍🍼 👨🏽‍🍼 👨🏾‍🍼 👨🏿‍🍼 👩‍🍼 👩🏻‍🍼 👩🏼‍🍼 👩🏽‍🍼 👩🏾‍🍼 👩🏿‍🍼 🧑‍🍼 🧑🏻‍🍼 🧑🏼‍🍼 🧑🏽‍🍼 🧑🏾‍🍼 🧑🏿‍🍼
baby angel:👼
baby angel:👼🏻 👼🏼 👼🏽 👼🏾 👼🏿
Santa Claus:🎅
Santa Claus:🎅🏻 🎅🏼 🎅🏽 🎅🏾 🎅🏿
Mrs Claus:🤶 🤶🏻 🤶🏼 🤶🏽 🤶🏾 🤶🏿
Mx Claus:🧑‍🎄 🧑🏻‍🎄 🧑🏼‍🎄 🧑🏽‍🎄 🧑🏾‍🎄 🧑🏿‍🎄
superhero:🦸 🦸‍♀ 🦸‍♀️ 🦸‍♂ 🦸‍♂️ 🦸🏻 🦸🏻‍♀ 🦸🏻‍♀️ 🦸🏻‍♂ 🦸🏻‍♂️ 🦸🏼 🦸🏼‍♀ 🦸🏼‍♀️ 🦸🏼‍♂ 🦸🏼‍♂️ 🦸🏽 🦸🏽‍♀ 🦸🏽‍♀️ 🦸🏽‍♂ 🦸🏽‍♂️ 🦸🏾 🦸🏾‍♀ 🦸🏾‍♀️ 🦸🏾‍♂ 🦸🏾‍♂️ 🦸🏿 🦸🏿‍♀ 🦸🏿‍♀️ 🦸🏿‍♂ 🦸🏿‍♂️
supervillain:🦹 🦹‍♀ 🦹‍♀️ 🦹‍♂ 🦹‍♂️ 🦹🏻 🦹🏻‍♀ 🦹🏻‍♀️ 🦹🏻‍♂ 🦹🏻‍♂️ 🦹🏼 🦹🏼‍♀ 🦹🏼‍♀️ 🦹🏼‍♂ 🦹🏼‍♂️ 🦹🏽 🦹🏽‍♀ 🦹🏽‍♀️ 🦹🏽‍♂ 🦹🏽‍♂️ 🦹🏾 🦹🏾‍♀ 🦹🏾‍♀️ 🦹🏾‍♂ 🦹🏾‍♂️ 🦹🏿 🦹🏿‍♀ 🦹🏿‍♀️ 🦹🏿‍♂ 🦹🏿‍♂️
mage:🧙 🧙‍♀ 🧙‍♀️ 🧙‍♂ 🧙‍♂️ 🧙🏻 🧙🏻‍♀ 🧙🏻‍♀️ 🧙🏻‍♂ 🧙🏻‍♂️ 🧙🏼 🧙🏼‍♀ 🧙🏼‍♀️ 🧙🏼‍♂ 🧙🏼‍♂️ 🧙🏽 🧙🏽‍♀ 🧙🏽‍♀️ 🧙🏽‍♂ 🧙🏽‍♂️ 🧙🏾 🧙🏾‍♀ 🧙🏾‍♀️ 🧙🏾‍♂ 🧙🏾‍♂️ 🧙🏿 🧙🏿‍♀ 🧙🏿‍♀️ 🧙🏿‍♂ 🧙🏿‍♂️
fairy:🧚 🧚‍♀ 🧚‍♀️ 🧚‍♂ 🧚‍♂️ 🧚🏻 🧚🏻‍♀ 🧚🏻‍♀️ 🧚🏻‍♂ 🧚🏻‍♂️ 🧚🏼 🧚🏼‍♀ 🧚🏼‍♀️ 🧚🏼‍♂ 🧚🏼‍♂️ 🧚🏽 🧚🏽‍♀ 🧚🏽‍♀️ 🧚🏽‍♂ 🧚🏽‍♂️ 🧚🏾 🧚🏾‍♀ 🧚🏾‍♀️ 🧚🏾‍♂ 🧚🏾‍♂️ 🧚🏿 🧚🏿‍♀ 🧚🏿‍♀️ 🧚🏿‍♂ 🧚🏿‍♂️
vampire:🧛 🧛‍♀ 🧛‍♀️ 🧛‍♂ 🧛‍♂️ 🧛🏻 🧛🏻‍♀ 🧛🏻‍♀️ 🧛🏻‍♂ 🧛🏻‍♂️ 🧛🏼 🧛🏼‍♀ 🧛🏼‍♀️ 🧛🏼‍♂ 🧛🏼‍♂️ 🧛🏽 🧛🏽‍♀ 🧛🏽‍♀️ 🧛🏽‍♂ 🧛🏽‍♂️ 🧛🏾 🧛🏾‍♀ 🧛🏾‍♀️ 🧛🏾‍♂ 🧛🏾‍♂️ 🧛🏿 🧛🏿‍♀ 🧛🏿‍♀️ 🧛🏿‍♂ 🧛🏿‍♂️
merperson:🧜 🧜🏻 🧜🏼 🧜🏽 🧜🏾 🧜🏿
merman:🧜‍♂ 🧜‍♂️ 🧜🏻‍♂ 🧜🏻‍♂️ 🧜🏼‍♂ 🧜🏼‍♂️ 🧜🏽‍♂ 🧜🏽‍♂️ 🧜🏾‍♂ 🧜🏾‍♂️ 🧜🏿‍♂ 🧜🏿‍♂️
mermaid:🧜‍♀ 🧜‍♀️ 🧜🏻‍♀ 🧜🏻‍♀️ 🧜🏼‍♀ 🧜🏼‍♀️ 🧜🏽‍♀ 🧜🏽‍♀️ 🧜🏾‍♀ 🧜🏾‍♀️ 🧜🏿‍♀ 🧜🏿‍♀️
elf:🧝 🧝‍♀ 🧝‍♀️ 🧝‍♂ 🧝‍♂️ 🧝🏻 🧝🏻‍♀ 🧝🏻‍♀️ 🧝🏻‍♂ 🧝🏻‍♂️ 🧝🏼 🧝🏼‍♀ 🧝🏼‍♀️ 🧝🏼‍♂ 🧝🏼‍♂️ 🧝🏽 🧝🏽‍♀ 🧝🏽‍♀️ 🧝🏽‍♂ 🧝🏽‍♂️ 🧝🏾 🧝🏾‍♀ 🧝🏾‍♀️ 🧝🏾‍♂ 🧝🏾‍♂️ 🧝🏿 🧝🏿‍♀ 🧝🏿‍♀️ 🧝🏿‍♂ 🧝🏿‍♂️
genie:🧞 🧞‍♀ 🧞‍♀️ 🧞‍♂ 🧞‍♂️
zombie:🧟 🧟‍♀ 🧟‍♀️ 🧟‍♂ 🧟‍♂️
troll:🧌
hairy creature:🫈
getting massage:💆
getting massage:💆🏻 💆🏼 💆🏽 💆🏾 💆🏿
getting massage:💆‍♀ 💆‍♀️ 💆‍♂ 💆‍♂️ 💆🏻‍♀ 💆🏻‍♀️ 💆🏻‍♂ 💆🏻‍♂️ 💆🏼‍♀ 💆🏼‍♀️ 💆🏼‍♂ 💆🏼‍♂️ 💆🏽‍♀ 💆🏽‍♀️ 💆🏽‍♂ 💆🏽‍♂️ 💆🏾‍♀ 💆🏾‍♀️ 💆🏾‍♂ 💆🏾‍♂️ 💆🏿‍♀ 💆🏿‍♀️ 💆🏿‍♂ 💆🏿‍♂️
getting haircut:💇
getting haircut:💇🏻 💇🏼 💇🏽 💇🏾 💇🏿
getting haircut:💇‍♀ 💇‍♀️ 💇‍♂ 💇‍♂️ 💇🏻‍♀ 💇🏻‍♀️ 💇🏻‍♂ 💇🏻‍♂️ 💇🏼‍♀ 💇🏼‍♀️ 💇🏼‍♂ 💇🏼‍♂️ 💇🏽‍♀ 💇🏽‍♀️ 💇🏽‍♂ 💇🏽‍♂️ 💇🏾‍♀ 💇🏾‍♀️ 💇🏾‍♂ 💇🏾‍♂️ 💇🏿‍♀ 💇🏿‍♀️ 💇🏿‍♂ 💇🏿‍♂️
walking:🚶
walking:🚶🏻 🚶🏼 🚶🏽 🚶🏾 🚶🏿
walking:🚶‍♀ 🚶‍♀️ 🚶‍♂ 🚶‍♂️ 🚶🏻‍♀ 🚶🏻‍♀️ 🚶🏻‍♂ 🚶🏻‍♂️ 🚶🏼‍♀ 🚶🏼‍♀️ 🚶🏼‍♂ 🚶🏼‍♂️ 🚶🏽‍♀ 🚶🏽‍♀️ 🚶🏽‍♂ 🚶🏽‍♂️ 🚶🏾‍♀ 🚶🏾‍♀️ 🚶🏾‍♂ 🚶🏾‍♂️ 🚶🏿‍♀ 🚶🏿‍♀️ 🚶🏿‍♂ 🚶🏿‍♂️
walking facing right:🚶‍♀‍➡ 🚶‍♀‍➡️ 🚶‍♀️‍➡ 🚶‍♀️‍➡️ 🚶‍♂‍➡ 🚶‍♂‍➡️ 🚶‍♂️‍➡ 🚶‍♂️‍➡️ 🚶‍➡ 🚶‍➡️ 🚶🏻‍♀‍➡ 🚶🏻‍♀‍➡️ 🚶🏻‍♀️‍➡ 🚶🏻‍♀️‍➡️ 🚶🏻‍♂‍➡ 🚶🏻‍♂‍➡️ 🚶🏻‍♂️‍➡ 🚶🏻‍♂️‍➡️ 🚶🏻‍➡ 🚶🏻‍➡️ 🚶🏼‍♀‍➡ 🚶🏼‍♀‍➡️ 🚶🏼‍♀️‍➡ 🚶🏼‍♀️‍➡️ 🚶🏼‍♂‍➡ 🚶🏼‍♂‍➡️ 🚶🏼‍♂️‍➡ 🚶🏼‍♂️‍➡️ 🚶🏼‍➡ 🚶🏼‍➡️ 🚶🏽‍♀‍➡ 🚶🏽‍♀‍➡️ 🚶🏽‍♀️‍➡ 🚶🏽‍♀️‍➡️ 🚶🏽‍♂‍➡ 🚶🏽‍♂‍➡️ 🚶🏽‍♂️‍➡ 🚶🏽‍♂️‍➡️ 🚶🏽‍➡ 🚶🏽‍➡️ 🚶🏾‍♀‍➡ 🚶🏾‍♀‍➡️ 🚶🏾‍♀️‍➡ 🚶🏾‍♀️‍➡️ 🚶🏾‍♂‍➡ 🚶🏾‍♂‍➡️ 🚶🏾‍♂️‍➡ 🚶🏾‍♂️‍➡️ 🚶🏾‍➡ 🚶🏾‍➡️ 🚶🏿‍♀‍➡ 🚶🏿‍♀‍➡️ 🚶🏿‍♀️‍➡ 🚶🏿‍♀️‍➡️ 🚶🏿‍♂‍➡ 🚶🏿‍♂‍➡️ 🚶🏿‍♂️‍➡ 🚶🏿‍♂️‍➡️ 🚶🏿‍➡ 🚶🏿‍➡️
standing:🧍 🧍‍♀ 🧍‍♀️ 🧍‍♂ 🧍‍♂️ 🧍🏻 🧍🏻‍♀ 🧍🏻‍♀️ 🧍🏻‍♂ 🧍🏻‍♂️ 🧍🏼 🧍🏼‍♀ 🧍🏼‍♀️ 🧍🏼‍♂ 🧍🏼‍♂️ 🧍🏽 🧍🏽‍♀ 🧍🏽‍♀️ 🧍🏽‍♂ 🧍🏽‍♂️ 🧍🏾 🧍🏾‍♀ 🧍🏾‍♀️ 🧍🏾‍♂ 🧍🏾‍♂️ 🧍🏿 🧍🏿‍♀ 🧍🏿‍♀️ 🧍🏿‍♂ 🧍🏿‍♂️
kneeling:🧎 🧎‍♀ 🧎‍♀️ 🧎‍♂ 🧎‍♂️ 🧎🏻 🧎🏻‍♀ 🧎🏻‍♀️ 🧎🏻‍♂ 🧎🏻‍♂️ 🧎🏼 🧎🏼‍♀ 🧎🏼‍♀️ 🧎🏼‍♂ 🧎🏼‍♂️ 🧎🏽 🧎🏽‍♀ 🧎🏽‍♀️ 🧎🏽‍♂ 🧎🏽‍♂️ 🧎🏾 🧎🏾‍♀ 🧎🏾‍♀️ 🧎🏾‍♂ 🧎🏾‍♂️ 🧎🏿 🧎🏿‍♀ 🧎🏿‍♀️ 🧎🏿‍♂ 🧎🏿‍♂️
kneeling facing right:🧎‍♀‍➡ 🧎‍♀‍➡️ 🧎‍♀️‍➡ 🧎‍♀️‍➡️ 🧎‍♂‍➡ 🧎‍♂‍➡️ 🧎‍♂️‍➡ 🧎‍♂️‍➡️ 🧎‍➡ 🧎‍➡️ 🧎🏻‍♀‍➡ 🧎🏻‍♀‍➡️ 🧎🏻‍♀️‍➡ 🧎🏻‍♀️‍➡️ 🧎🏻‍♂‍➡ 🧎🏻‍♂‍➡️ 🧎🏻‍♂️‍➡ 🧎🏻‍♂️‍➡️ 🧎🏻‍➡ 🧎🏻‍➡️ 🧎🏼‍♀‍➡ 🧎🏼‍♀‍➡️ 🧎🏼‍♀️‍➡ 🧎🏼‍♀️‍➡️ 🧎🏼‍♂‍➡ 🧎🏼‍♂‍➡️ 🧎🏼‍♂️‍➡ 🧎🏼‍♂️‍➡️ 🧎🏼‍➡ 🧎🏼‍➡️ 🧎🏽‍♀‍➡ 🧎🏽‍♀‍➡️ 🧎🏽‍♀️‍➡ 🧎🏽‍♀️‍➡️ 🧎🏽‍♂‍➡ 🧎🏽‍♂‍➡️ 🧎🏽‍♂️‍➡ 🧎🏽‍♂️‍➡️ 🧎🏽‍➡ 🧎🏽‍➡️ 🧎🏾‍♀‍➡ 🧎🏾‍♀‍➡️ 🧎🏾‍♀️‍➡ 🧎🏾‍♀️‍➡️ 🧎🏾‍♂‍➡ 🧎🏾‍♂‍➡️ 🧎🏾‍♂️‍➡ 🧎🏾‍♂️‍➡️ 🧎🏾‍➡ 🧎🏾‍➡️ 🧎🏿‍♀‍➡ 🧎🏿‍♀‍➡️ 🧎🏿‍♀️‍➡ 🧎🏿‍♀️‍➡️ 🧎🏿‍♂‍➡ 🧎🏿‍♂‍➡️ 🧎🏿‍♂️‍➡ 🧎🏿‍♂️‍➡️ 🧎🏿‍➡ 🧎🏿‍➡️
with white cane:🧑‍🦯 🧑🏻‍🦯 🧑🏼‍🦯 🧑🏽‍🦯 🧑🏾‍🦯 🧑🏿‍🦯
with white cane facing right:👨‍🦯‍➡ 👨‍🦯‍➡️ 👨🏻‍🦯‍➡ 👨🏻‍🦯‍➡️ 👨🏼‍🦯‍➡ 👨🏼‍🦯‍➡️ 👨🏽‍🦯‍➡ 👨🏽‍🦯‍➡️ 👨🏾‍🦯‍➡ 👨🏾‍🦯‍➡️ 👨🏿‍🦯‍➡ 👨🏿‍🦯‍➡️ 👩‍🦯‍➡ 👩‍🦯‍➡️ 👩🏻‍🦯‍➡ 👩🏻‍🦯‍➡️ 👩🏼‍🦯‍➡ 👩🏼‍🦯‍➡️ 👩🏽‍🦯‍➡ 👩🏽‍🦯‍➡️ 👩🏾‍🦯‍➡ 👩🏾‍🦯‍➡️ 👩🏿‍🦯‍➡ 👩🏿‍🦯‍➡️ 🧑‍🦯‍➡ 🧑‍🦯‍➡️ 🧑🏻‍🦯‍➡ 🧑🏻‍🦯‍➡️ 🧑🏼‍🦯‍➡ 🧑🏼‍🦯‍➡️ 🧑🏽‍🦯‍➡ 🧑🏽‍🦯‍➡️ 🧑🏾‍🦯‍➡ 🧑🏾‍🦯‍➡️ 🧑🏿‍🦯‍➡ 🧑🏿‍🦯‍➡️
with white cane:👨‍🦯 👨🏻‍🦯 👨🏼‍🦯 👨🏽‍🦯 👨🏾‍🦯 👨🏿‍🦯 👩‍🦯 👩🏻‍🦯 👩🏼‍🦯 👩🏽‍🦯 👩🏾‍🦯 👩🏿‍🦯
in motorized wheelchair:🧑‍🦼 🧑🏻‍🦼 🧑🏼‍🦼 🧑🏽‍🦼 🧑🏾‍🦼 🧑🏿‍🦼
in motorized wheelchair facing right:👨‍🦼‍➡ 👨‍🦼‍➡️ 👨🏻‍🦼‍➡ 👨🏻‍🦼‍➡️ 👨🏼‍🦼‍➡ 👨🏼‍🦼‍➡️ 👨🏽‍🦼‍➡ 👨🏽‍🦼‍➡️ 👨🏾‍🦼‍➡ 👨🏾‍🦼‍➡️ 👨🏿‍🦼‍➡ 👨🏿‍🦼‍➡️ 👩‍🦼‍➡ 👩‍🦼‍➡️ 👩🏻‍🦼‍➡ 👩🏻‍🦼‍➡️ 👩🏼‍🦼‍➡ 👩🏼‍🦼‍➡️ 👩🏽‍🦼‍➡ 👩🏽‍🦼‍➡️ 👩🏾‍🦼‍➡ 👩🏾‍🦼‍➡️ 👩🏿‍🦼‍➡ 👩🏿‍🦼‍➡️ 🧑‍🦼‍➡ 🧑‍🦼‍➡️ 🧑🏻‍🦼‍➡ 🧑🏻‍🦼‍➡️ 🧑🏼‍🦼‍➡ 🧑🏼‍🦼‍➡️ 🧑🏽‍🦼‍➡ 🧑🏽‍🦼‍➡️ 🧑🏾‍🦼‍➡ 🧑🏾‍🦼‍➡️ 🧑🏿‍🦼‍➡ 🧑🏿‍🦼‍➡️
in motorized wheelchair:👨‍🦼 👨🏻‍🦼 👨🏼‍🦼 👨🏽‍🦼 👨🏾‍🦼 👨🏿‍🦼 👩‍🦼 👩🏻‍🦼 👩🏼‍🦼 👩🏽‍🦼 👩🏾‍🦼 👩🏿‍🦼
in manual wheelchair:🧑‍🦽 🧑🏻‍🦽 🧑🏼‍🦽 🧑🏽‍🦽 🧑🏾‍🦽 🧑🏿‍🦽
in manual wheelchair facing right:👨‍🦽‍➡ 👨‍🦽‍➡️ 👨🏻‍🦽‍➡ 👨🏻‍🦽‍➡️ 👨🏼‍🦽‍➡ 👨🏼‍🦽‍➡️ 👨🏽‍🦽‍➡ 👨🏽‍🦽‍➡️ 👨🏾‍🦽‍➡ 👨🏾‍🦽‍➡️ 👨🏿‍🦽‍➡ 👨🏿‍🦽‍➡️ 👩‍🦽‍➡ 👩‍🦽‍➡️ 👩🏻‍🦽‍➡ 👩🏻‍🦽‍➡️ 👩🏼‍🦽‍➡ 👩🏼‍🦽‍➡️ 👩🏽‍🦽‍➡ 👩🏽‍🦽‍➡️ 👩🏾‍🦽‍➡ 👩🏾‍🦽‍➡️ 👩🏿‍🦽‍➡ 👩🏿‍🦽‍➡️ 🧑‍🦽‍➡ 🧑‍🦽‍➡️ 🧑🏻‍🦽‍➡ 🧑🏻‍🦽‍➡️ 🧑🏼‍🦽‍➡ 🧑🏼‍🦽‍➡️ 🧑🏽‍🦽‍➡ 🧑🏽‍🦽‍➡️ 🧑🏾‍🦽‍➡ 🧑🏾‍🦽‍➡️ 🧑🏿‍🦽‍➡ 🧑🏿‍🦽‍➡️
in manual wheelchair:👨‍🦽 👨🏻‍🦽 👨🏼‍🦽 👨🏽‍🦽 👨🏾‍🦽 👨🏿‍🦽 👩‍🦽 👩🏻‍🦽 👩🏼‍🦽 👩🏽‍🦽 👩🏾‍🦽 👩🏿‍🦽
running:🏃
running:🏃🏻 🏃🏼 🏃🏽 🏃🏾 🏃🏿
running:🏃‍♀ 🏃‍♀️ 🏃‍♂ 🏃‍♂️ 🏃🏻‍♀ 🏃🏻‍♀️ 🏃🏻‍♂ 🏃🏻‍♂️ 🏃🏼‍♀ 🏃🏼‍♀️ 🏃🏼‍♂ 🏃🏼‍♂️ 🏃🏽‍♀ 🏃🏽‍♀️ 🏃🏽‍♂ 🏃🏽‍♂️ 🏃🏾‍♀ 🏃🏾‍♀️ 🏃🏾‍♂ 🏃🏾‍♂️ 🏃🏿‍♀ 🏃🏿‍♀️ 🏃🏿‍♂ 🏃🏿‍♂️
running facing right:🏃‍♀‍➡ 🏃‍♀‍➡️ 🏃‍♀️‍➡ 🏃‍♀️‍➡️ 🏃‍♂‍➡ 🏃‍♂‍➡️ 🏃‍♂️‍➡ 🏃‍♂️‍➡️ 🏃‍➡ 🏃‍➡️ 🏃🏻‍♀‍➡ 🏃🏻‍♀‍➡️ 🏃🏻‍♀️‍➡ 🏃🏻‍♀️‍➡️ 🏃🏻‍♂‍➡ 🏃🏻‍♂‍➡️ 🏃🏻‍♂️‍➡ 🏃🏻‍♂️‍➡️ 🏃🏻‍➡ 🏃🏻‍➡️ 🏃🏼‍♀‍➡ 🏃🏼‍♀‍➡️ 🏃🏼‍♀️‍➡ 🏃🏼‍♀️‍➡️ 🏃🏼‍♂‍➡ 🏃🏼‍♂‍➡️ 🏃🏼‍♂️‍➡ 🏃🏼‍♂️‍➡️ 🏃🏼‍➡ 🏃🏼‍➡️ 🏃🏽‍♀‍➡ 🏃🏽‍♀‍➡️ 🏃🏽‍♀️‍➡ 🏃🏽‍♀️‍➡️ 🏃🏽‍♂‍➡ 🏃🏽‍♂‍➡️ 🏃🏽‍♂️‍➡ 🏃🏽‍♂️‍➡️ 🏃🏽‍➡ 🏃🏽‍➡️ 🏃🏾‍♀‍➡ 🏃🏾‍♀‍➡️ 🏃🏾‍♀️‍➡ 🏃🏾‍♀️‍➡️ 🏃🏾‍♂‍➡ 🏃🏾‍♂‍➡️ 🏃🏾‍♂️‍➡ 🏃🏾‍♂️‍➡️ 🏃🏾‍➡ 🏃🏾‍➡️ 🏃🏿‍♀‍➡ 🏃🏿‍♀‍➡️ 🏃🏿‍♀️‍➡ 🏃🏿‍♀️‍➡️ 🏃🏿‍♂‍➡ 🏃🏿‍♂‍➡️ 🏃🏿‍♂️‍➡ 🏃🏿‍♂️‍➡️ 🏃🏿‍➡ 🏃🏿‍➡️
ballet dancer:🧑‍🩰 🧑🏻‍🩰 🧑🏼‍🩰 🧑🏽‍🩰 🧑🏾‍🩰 🧑🏿‍🩰
dancing:💃
dancing:💃🏻 💃🏼 💃🏽 💃🏾 💃🏿
dancing:🕺 🕺🏻 🕺🏼 🕺🏽 🕺🏾 🕺🏿
in suit levitating:🕴 🕴️
in suit levitating:🕴🏻 🕴🏼 🕴🏽 🕴🏾 🕴🏿
people with bunny ears:👯
people with bunny ears:👯🏻 👯🏼 👯🏽 👯🏾 👯🏿 🧑🏻‍🐰‍🧑🏼 🧑🏻‍🐰‍🧑🏽 🧑🏻‍🐰‍🧑🏾 🧑🏻‍🐰‍🧑🏿 🧑🏼‍🐰‍🧑🏻 🧑🏼‍🐰‍🧑🏽 🧑🏼‍🐰‍🧑🏾 🧑🏼‍🐰‍🧑🏿 🧑🏽‍🐰‍🧑🏻 🧑🏽‍🐰‍🧑🏼 🧑🏽‍🐰‍🧑🏾 🧑🏽‍🐰‍🧑🏿 🧑🏾‍🐰‍🧑🏻 🧑🏾‍🐰‍🧑🏼 🧑🏾‍🐰‍🧑🏽 🧑🏾‍🐰‍🧑🏿 🧑🏿‍🐰‍🧑🏻 🧑🏿‍🐰‍🧑🏼 🧑🏿‍🐰‍🧑🏽 🧑🏿‍🐰‍🧑🏾
men with bunny ears:👯‍♂ 👯‍♂️
men with bunny ears:👨🏻‍🐰‍👨🏼 👨🏻‍🐰‍👨🏽 👨🏻‍🐰‍👨🏾 👨🏻‍🐰‍👨🏿 👨🏼‍🐰‍👨🏻 👨🏼‍🐰‍👨🏽 👨🏼‍🐰‍👨🏾 👨🏼‍🐰‍👨🏿 👨🏽‍🐰‍👨🏻 👨🏽‍🐰‍👨🏼 👨🏽‍🐰‍👨🏾 👨🏽‍🐰‍👨🏿 👨🏾‍🐰‍👨🏻 👨🏾‍🐰‍👨🏼 👨🏾‍🐰‍👨🏽 👨🏾‍🐰‍👨🏿 👨🏿‍🐰‍👨🏻 👨🏿‍🐰‍👨🏼 👨🏿‍🐰‍👨🏽 👨🏿‍🐰‍👨🏾 👯🏻‍♂ 👯🏻‍♂️ 👯🏼‍♂ 👯🏼‍♂️ 👯🏽‍♂ 👯🏽‍♂️ 👯🏾‍♂ 👯🏾‍♂️ 👯🏿‍♂ 👯🏿‍♂️
women with bunny ears:👯‍♀ 👯‍♀️
women with bunny ears:👩🏻‍🐰‍👩🏼 👩🏻‍🐰‍👩🏽 👩🏻‍🐰‍👩🏾 👩🏻‍🐰‍👩🏿 👩🏼‍🐰‍👩🏻 👩🏼‍🐰‍👩🏽 👩🏼‍🐰‍👩🏾 👩🏼‍🐰‍👩🏿 👩🏽‍🐰‍👩🏻 👩🏽‍🐰‍👩🏼 👩🏽‍🐰‍👩🏾 👩🏽‍🐰‍👩🏿 👩🏾‍🐰‍👩🏻 👩🏾‍🐰‍👩🏼 👩🏾‍🐰‍👩🏽 👩🏾‍🐰‍👩🏿 👩🏿‍🐰‍👩🏻 👩🏿‍🐰‍👩🏼 👩🏿‍🐰‍👩🏽 👩🏿‍🐰‍👩🏾 👯🏻‍♀ 👯🏻‍♀️ 👯🏼‍♀ 👯🏼‍♀️ 👯🏽‍♀ 👯🏽‍♀️ 👯🏾‍♀ 👯🏾‍♀️ 👯🏿‍♀ 👯🏿‍♀️
in steamy room:🧖 🧖‍♀ 🧖‍♀️ 🧖‍♂ 🧖‍♂️ 🧖🏻 🧖🏻‍♀ 🧖🏻‍♀️ 🧖🏻‍♂ 🧖🏻‍♂️ 🧖🏼 🧖🏼‍♀ 🧖🏼‍♀️ 🧖🏼‍♂ 🧖🏼‍♂️ 🧖🏽 🧖🏽‍♀ 🧖🏽‍♀️ 🧖🏽‍♂ 🧖🏽‍♂️ 🧖🏾 🧖🏾‍♀ 🧖🏾‍♀️ 🧖🏾‍♂ 🧖🏾‍♂️ 🧖🏿 🧖🏿‍♀ 🧖🏿‍♀️ 🧖🏿‍♂ 🧖🏿‍♂️
climbing:🧗 🧗‍♀ 🧗‍♀️ 🧗‍♂ 🧗‍♂️ 🧗🏻 🧗🏻‍♀ 🧗🏻‍♀️ 🧗🏻‍♂ 🧗🏻‍♂️ 🧗🏼 🧗🏼‍♀ 🧗🏼‍♀️ 🧗🏼‍♂ 🧗🏼‍♂️ 🧗🏽 🧗🏽‍♀ 🧗🏽‍♀️ 🧗🏽‍♂ 🧗🏽‍♂️ 🧗🏾 🧗🏾‍♀ 🧗🏾‍♀️ 🧗🏾‍♂ 🧗🏾‍♂️ 🧗🏿 🧗🏿‍♀ 🧗🏿‍♀️ 🧗🏿‍♂ 🧗🏿‍♂️
fencing:🤺
horse racing:🏇 🏇🏻 🏇🏼 🏇🏽 🏇🏾 🏇🏿
skier:⛷ ⛷️
snowboarder:🏂
snowboarder:🏂🏻 🏂🏼 🏂🏽 🏂🏾 🏂🏿
golfing:🏌 🏌️
golfing:🏌‍♀ 🏌‍♀️ 🏌‍♂ 🏌‍♂️ 🏌️‍♀ 🏌️‍♀️ 🏌️‍♂ 🏌️‍♂️ 🏌🏻 🏌🏻‍♀ 🏌🏻‍♀️ 🏌🏻‍♂ 🏌🏻‍♂️ 🏌🏼 🏌🏼‍♀ 🏌🏼‍♀️ 🏌🏼‍♂ 🏌🏼‍♂️ 🏌🏽 🏌🏽‍♀ 🏌🏽‍♀️ 🏌🏽‍♂ 🏌🏽‍♂️ 🏌🏾 🏌🏾‍♀ 🏌🏾‍♀️ 🏌🏾‍♂ 🏌🏾‍♂️ 🏌🏿 🏌🏿‍♀ 🏌🏿‍♀️ 🏌🏿‍♂ 🏌🏿‍♂️
surfing:🏄
surfing:🏄🏻 🏄🏼 🏄🏽 🏄🏾 🏄🏿
surfing:🏄‍♀ 🏄‍♀️ 🏄‍♂ 🏄‍♂️ 🏄🏻‍♀ 🏄🏻‍♀️ 🏄🏻‍♂ 🏄🏻‍♂️ 🏄🏼‍♀ 🏄🏼‍♀️ 🏄🏼‍♂ 🏄🏼‍♂️ 🏄🏽‍♀ 🏄🏽‍♀️ 🏄🏽‍♂ 🏄🏽‍♂️ 🏄🏾‍♀ 🏄🏾‍♀️ 🏄🏾‍♂ 🏄🏾‍♂️ 🏄🏿‍♀ 🏄🏿‍♀️ 🏄🏿‍♂ 🏄🏿‍♂️
rowing boat:🚣 🚣🏻 🚣🏼 🚣🏽 🚣🏾 🚣🏿
rowing boat:🚣‍♀ 🚣‍♀️ 🚣‍♂ 🚣‍♂️ 🚣🏻‍♀ 🚣🏻‍♀️ 🚣🏻‍♂ 🚣🏻‍♂️ 🚣🏼‍♀ 🚣🏼‍♀️ 🚣🏼‍♂ 🚣🏼‍♂️ 🚣🏽‍♀ 🚣🏽‍♀️ 🚣🏽‍♂ 🚣🏽‍♂️ 🚣🏾‍♀ 🚣🏾‍♀️ 🚣🏾‍♂ 🚣🏾‍♂️ 🚣🏿‍♀ 🚣🏿‍♀️ 🚣🏿‍♂ 🚣🏿‍♂️
swimming:🏊
swimming:🏊🏻 🏊🏼 🏊🏽 🏊🏾 🏊🏿
swimming:🏊‍♀ 🏊‍♀️ 🏊‍♂ 🏊‍♂️ 🏊🏻‍♀ 🏊🏻‍♀️ 🏊🏻‍♂ 🏊🏻‍♂️ 🏊🏼‍♀ 🏊🏼‍♀️ 🏊🏼‍♂ 🏊🏼‍♂️ 🏊🏽‍♀ 🏊🏽‍♀️ 🏊🏽‍♂ 🏊🏽‍♂️ 🏊🏾‍♀ 🏊🏾‍♀️ 🏊🏾‍♂ 🏊🏾‍♂️ 🏊🏿‍♀ 🏊🏿‍♀️ 🏊🏿‍♂ 🏊🏿‍♂️
bouncing ball:⛹ ⛹️
bouncing ball:⛹🏻 ⛹🏼 ⛹🏽 ⛹🏾 ⛹🏿
bouncing ball:⛹‍♀ ⛹‍♀️ ⛹‍♂ ⛹‍♂️ ⛹️‍♀ ⛹️‍♀️ ⛹️‍♂ ⛹️‍♂️ ⛹🏻‍♀ ⛹🏻‍♀️ ⛹🏻‍♂ ⛹🏻‍♂️ ⛹🏼‍♀ ⛹🏼‍♀️ ⛹🏼‍♂ ⛹🏼‍♂️ ⛹🏽‍♀ ⛹🏽‍♀️ ⛹🏽‍♂ ⛹🏽‍♂️ ⛹🏾‍♀ ⛹🏾‍♀️ ⛹🏾‍♂ ⛹🏾‍♂️ ⛹🏿‍♀ ⛹🏿‍♀️ ⛹🏿‍♂ ⛹🏿‍♂️
lifting weights:🏋 🏋️
lifting weights:🏋🏻 🏋🏼 🏋🏽 🏋🏾 🏋🏿
lifting weights:🏋‍♀ 🏋‍♀️ 🏋‍♂ 🏋‍♂️ 🏋️‍♀ 🏋️‍♀️ 🏋️‍♂ 🏋️‍♂️ 🏋🏻‍♀ 🏋🏻‍♀️ 🏋🏻‍♂ 🏋🏻‍♂️ 🏋🏼‍♀ 🏋🏼‍♀️ 🏋🏼‍♂ 🏋🏼‍♂️ 🏋🏽‍♀ 🏋🏽‍♀️ 🏋🏽‍♂ 🏋🏽‍♂️ 🏋🏾‍♀ 🏋🏾‍♀️ 🏋🏾‍♂ 🏋🏾‍♂️ 🏋🏿‍♀ 🏋🏿‍♀️ 🏋🏿‍♂ 🏋🏿‍♂️
biking:🚴 🚴🏻 🚴🏼 🚴🏽 🚴🏾 🚴🏿
biking:🚴‍♀ 🚴‍♀️ 🚴‍♂ 🚴‍♂️ 🚴🏻‍♀ 🚴🏻‍♀️ 🚴🏻‍♂ 🚴🏻‍♂️ 🚴🏼‍♀ 🚴🏼‍♀️ 🚴🏼‍♂ 🚴🏼‍♂️ 🚴🏽‍♀ 🚴🏽‍♀️ 🚴🏽‍♂ 🚴🏽‍♂️ 🚴🏾‍♀ 🚴🏾‍♀️ 🚴🏾‍♂ 🚴🏾‍♂️ 🚴🏿‍♀ 🚴🏿‍♀️ 🚴🏿‍♂ 🚴🏿‍♂️
mountain biking:🚵 🚵🏻 🚵🏼 🚵🏽 🚵🏾 🚵🏿
mountain biking:🚵‍♀ 🚵‍♀️ 🚵‍♂ 🚵‍♂️ 🚵🏻‍♀ 🚵🏻‍♀️ 🚵🏻‍♂ 🚵🏻‍♂️ 🚵🏼‍♀ 🚵🏼‍♀️ 🚵🏼‍♂ 🚵🏼‍♂️ 🚵🏽‍♀ 🚵🏽‍♀️ 🚵🏽‍♂ 🚵🏽‍♂️ 🚵🏾‍♀ 🚵🏾‍♀️ 🚵🏾‍♂ 🚵🏾‍♂️ 🚵🏿‍♀ 🚵🏿‍♀️ 🚵🏿‍♂ 🚵🏿‍♂️
cartwheeling:🤸 🤸🏻 🤸🏼 🤸🏽 🤸🏾 🤸🏿
cartwheeling:🤸‍♀ 🤸‍♀️ 🤸‍♂ 🤸‍♂️ 🤸🏻‍♀ 🤸🏻‍♀️ 🤸🏻‍♂ 🤸🏻‍♂️ 🤸🏼‍♀ 🤸🏼‍♀️ 🤸🏼‍♂ 🤸🏼‍♂️ 🤸🏽‍♀ 🤸🏽‍♀️ 🤸🏽‍♂ 🤸🏽‍♂️ 🤸🏾‍♀ 🤸🏾‍♀️ 🤸🏾‍♂ 🤸🏾‍♂️ 🤸🏿‍♀ 🤸🏿‍♀️ 🤸🏿‍♂ 🤸🏿‍♂️
people wrestling:🤼
people wrestling:🤼🏻 🤼🏼 🤼🏽 🤼🏾 🤼🏿 🧑🏻‍🫯‍🧑🏼 🧑🏻‍🫯‍🧑🏽 🧑🏻‍🫯‍🧑🏾 🧑🏻‍🫯‍🧑🏿 🧑🏼‍🫯‍🧑🏻 🧑🏼‍🫯‍🧑🏽 🧑🏼‍🫯‍🧑🏾 🧑🏼‍🫯‍🧑🏿 🧑🏽‍🫯‍🧑🏻 🧑🏽‍🫯‍🧑🏼 🧑🏽‍🫯‍🧑🏾 🧑🏽‍🫯‍🧑🏿 🧑🏾‍🫯‍🧑🏻 🧑🏾‍🫯‍🧑🏼 🧑🏾‍🫯‍🧑🏽 🧑🏾‍🫯‍🧑🏿 🧑🏿‍🫯‍🧑🏻 🧑🏿‍🫯‍🧑🏼 🧑🏿‍🫯‍🧑🏽 🧑🏿‍🫯‍🧑🏾
men wrestling:🤼‍♂ 🤼‍♂️
men wrestling:👨🏻‍🫯‍👨🏼 👨🏻‍🫯‍👨🏽 👨🏻‍🫯‍👨🏾 👨🏻‍🫯‍👨🏿 👨🏼‍🫯‍👨🏻 👨🏼‍🫯‍👨🏽 👨🏼‍🫯‍👨🏾 👨🏼‍🫯‍👨🏿 👨🏽‍🫯‍👨🏻 👨🏽‍🫯‍👨🏼 👨🏽‍🫯‍👨🏾 👨🏽‍🫯‍👨🏿 👨🏾‍🫯‍👨🏻 👨🏾‍🫯‍👨🏼 👨🏾‍🫯‍👨🏽 👨🏾‍🫯‍👨🏿 👨🏿‍🫯‍👨🏻 👨🏿‍🫯‍👨🏼 👨🏿‍🫯‍👨🏽 👨🏿‍🫯‍👨🏾 🤼🏻‍♂ 🤼🏻‍♂️ 🤼🏼‍♂ 🤼🏼‍♂️ 🤼🏽‍♂ 🤼🏽‍♂️ 🤼🏾‍♂ 🤼🏾‍♂️ 🤼🏿‍♂ 🤼🏿‍♂️
women wrestling:🤼‍♀ 🤼‍♀️
women wrestling:👩🏻‍🫯‍👩🏼 👩🏻‍🫯‍👩🏽 👩🏻‍🫯‍👩🏾 👩🏻‍🫯‍👩🏿 👩🏼‍🫯‍👩🏻 👩🏼‍🫯‍👩🏽 👩🏼‍🫯‍👩🏾 👩🏼‍🫯‍👩🏿 👩🏽‍🫯‍👩🏻 👩🏽‍🫯‍👩🏼 👩🏽‍🫯‍👩🏾 👩🏽‍🫯‍👩🏿 👩🏾‍🫯‍👩🏻 👩🏾‍🫯‍👩🏼 👩🏾‍🫯‍👩🏽 👩🏾‍🫯‍👩🏿 👩🏿‍🫯‍👩🏻 👩🏿‍🫯‍👩🏼 👩🏿‍🫯‍👩🏽 👩🏿‍🫯‍👩🏾 🤼🏻‍♀ 🤼🏻‍♀️ 🤼🏼‍♀ 🤼🏼‍♀️ 🤼🏽‍♀ 🤼🏽‍♀️ 🤼🏾‍♀ 🤼🏾‍♀️ 🤼🏿‍♀ 🤼🏿‍♀️
playing water polo:🤽 🤽🏻 🤽🏼 🤽🏽 🤽🏾 🤽🏿
playing water polo:🤽‍♀ 🤽‍♀️ 🤽‍♂ 🤽‍♂️ 🤽🏻‍♀ 🤽🏻‍♀️ 🤽🏻‍♂ 🤽🏻‍♂️ 🤽🏼‍♀ 🤽🏼‍♀️ 🤽🏼‍♂ 🤽🏼‍♂️ 🤽🏽‍♀ 🤽🏽‍♀️ 🤽🏽‍♂ 🤽🏽‍♂️ 🤽🏾‍♀ 🤽🏾‍♀️ 🤽🏾‍♂ 🤽🏾‍♂️ 🤽🏿‍♀ 🤽🏿‍♀️ 🤽🏿‍♂ 🤽🏿‍♂️
playing handball:🤾 🤾🏻 🤾🏼 🤾🏽 🤾🏾 🤾🏿
playing handball:🤾‍♀ 🤾‍♀️ 🤾‍♂ 🤾‍♂️ 🤾🏻‍♀ 🤾🏻‍♀️ 🤾🏻‍♂ 🤾🏻‍♂️ 🤾🏼‍♀ 🤾🏼‍♀️ 🤾🏼‍♂ 🤾🏼‍♂️ 🤾🏽‍♀ 🤾🏽‍♀️ 🤾🏽‍♂ 🤾🏽‍♂️ 🤾🏾‍♀ 🤾🏾‍♀️ 🤾🏾‍♂ 🤾🏾‍♂️ 🤾🏿‍♀ 🤾🏿‍♀️ 🤾🏿‍♂ 🤾🏿‍♂️
juggling:🤹 🤹🏻 🤹🏼 🤹🏽 🤹🏾 🤹🏿
juggling:🤹‍♀ 🤹‍♀️ 🤹‍♂ 🤹‍♂️ 🤹🏻‍♀ 🤹🏻‍♀️ 🤹🏻‍♂ 🤹🏻‍♂️ 🤹🏼‍♀ 🤹🏼‍♀️ 🤹🏼‍♂ 🤹🏼‍♂️ 🤹🏽‍♀ 🤹🏽‍♀️ 🤹🏽‍♂ 🤹🏽‍♂️ 🤹🏾‍♀ 🤹🏾‍♀️ 🤹🏾‍♂ 🤹🏾‍♂️ 🤹🏿‍♀ 🤹🏿‍♀️ 🤹🏿‍♂ 🤹🏿‍♂️
in lotus position:🧘 🧘‍♀ 🧘‍♀️ 🧘‍♂ 🧘‍♂️ 🧘🏻 🧘🏻‍♀ 🧘🏻‍♀️ 🧘🏻‍♂ 🧘🏻‍♂️ 🧘🏼 🧘🏼‍♀ 🧘🏼‍♀️ 🧘🏼‍♂ 🧘🏼‍♂️ 🧘🏽 🧘🏽‍♀ 🧘🏽‍♀️ 🧘🏽‍♂ 🧘🏽‍♂️ 🧘🏾 🧘🏾‍♀ 🧘🏾‍♀️ 🧘🏾‍♂ 🧘🏾‍♂️ 🧘🏿 🧘🏿‍♀ 🧘🏿‍♀️ 🧘🏿‍♂ 🧘🏿‍♂️
taking bath:🛀
taking bath:🛀🏻 🛀🏼 🛀🏽 🛀🏾 🛀🏿
in bed:🛌
in bed:🛌🏻 🛌🏼 🛌🏽 🛌🏾 🛌🏿
people holding hands:🧑‍🤝‍🧑 🧑🏻‍🤝‍🧑🏻 🧑🏼‍🤝‍🧑🏻 🧑🏼‍🤝‍🧑🏼 🧑🏽‍🤝‍🧑🏻 🧑🏽‍🤝‍🧑🏼 🧑🏽‍🤝‍🧑🏽 🧑🏾‍🤝‍🧑🏻 🧑🏾‍🤝‍🧑🏼 🧑🏾‍🤝‍🧑🏽 🧑🏾‍🤝‍🧑🏾 🧑🏿‍🤝‍🧑🏻 🧑🏿‍🤝‍🧑🏼 🧑🏿‍🤝‍🧑🏽 🧑🏿‍🤝‍🧑🏾 🧑🏿‍🤝‍🧑🏿
people holding hands:🧑🏻‍🤝‍🧑🏼 🧑🏻‍🤝‍🧑🏽 🧑🏻‍🤝‍🧑🏾 🧑🏻‍🤝‍🧑🏿 🧑🏼‍🤝‍🧑🏽 🧑🏼‍🤝‍🧑🏾 🧑🏼‍🤝‍🧑🏿 🧑🏽‍🤝‍🧑🏾 🧑🏽‍🤝‍🧑🏿 🧑🏾‍🤝‍🧑🏿
women holding hands:👭
women holding hands:👩🏼‍🤝‍👩🏻 👩🏽‍🤝‍👩🏻 👩🏽‍🤝‍👩🏼 👩🏾‍🤝‍👩🏻 👩🏾‍🤝‍👩🏼 👩🏾‍🤝‍👩🏽 👩🏿‍🤝‍👩🏻 👩🏿‍🤝‍👩🏼 👩🏿‍🤝‍👩🏽 👩🏿‍🤝‍👩🏾 👭🏻 👭🏼 👭🏽 👭🏾 👭🏿
women holding hands:👩🏻‍🤝‍👩🏼 👩🏻‍🤝‍👩🏽 👩🏻‍🤝‍👩🏾 👩🏻‍🤝‍👩🏿 👩🏼‍🤝‍👩🏽 👩🏼‍🤝‍👩🏾 👩🏼‍🤝‍👩🏿 👩🏽‍🤝‍👩🏾 👩🏽‍🤝‍👩🏿 👩🏾‍🤝‍👩🏿
and holding hands:👫
and holding hands:👩🏻‍🤝‍👨🏼 👩🏻‍🤝‍👨🏽 👩🏻‍🤝‍👨🏾 👩🏻‍🤝‍👨🏿 👩🏼‍🤝‍👨🏻 👩🏼‍🤝‍👨🏽 👩🏼‍🤝‍👨🏾 👩🏼‍🤝‍👨🏿 👩🏽‍🤝‍👨🏻 👩🏽‍🤝‍👨🏼 👩🏽‍🤝‍👨🏾 👩🏽‍🤝‍👨🏿 👩🏾‍🤝‍👨🏻 👩🏾‍🤝‍👨🏼 👩🏾‍🤝‍👨🏽 👩🏾‍🤝‍👨🏿 👩🏿‍🤝‍👨🏻 👩🏿‍🤝‍👨🏼 👩🏿‍🤝‍👨🏽 👩🏿‍🤝‍👨🏾 👫🏻 👫🏼 👫🏽 👫🏾 👫🏿
men holding hands:👬
men holding hands:👨🏼‍🤝‍👨🏻 👨🏽‍🤝‍👨🏻 👨🏽‍🤝‍👨🏼 👨🏾‍🤝‍👨🏻 👨🏾‍🤝‍👨🏼 👨🏾‍🤝‍👨🏽 👨🏿‍🤝‍👨🏻 👨🏿‍🤝‍👨🏼 👨🏿‍🤝‍👨🏽 👨🏿‍🤝‍👨🏾 👬🏻 👬🏼 👬🏽 👬🏾 👬🏿
men holding hands:👨🏻‍🤝‍👨🏼 👨🏻‍🤝‍👨🏽 👨🏻‍🤝‍👨🏾 👨🏻‍🤝‍👨🏿 👨🏼‍🤝‍👨🏽 👨🏼‍🤝‍👨🏾 👨🏼‍🤝‍👨🏿 👨🏽‍🤝‍👨🏾 👨🏽‍🤝‍👨🏿 👨🏾‍🤝‍👨🏿
kiss:💏
kiss:👨🏻‍❤‍💋‍👨🏻 👨🏻‍❤‍💋‍👨🏼 👨🏻‍❤‍💋‍👨🏽 👨🏻‍❤‍💋‍👨🏾 👨🏻‍❤‍💋‍👨🏿 👨🏻‍❤️‍💋‍👨🏻 👨🏻‍❤️‍💋‍👨🏼 👨🏻‍❤️‍💋‍👨🏽 👨🏻‍❤️‍💋‍👨🏾 👨🏻‍❤️‍💋‍👨🏿 👨🏼‍❤‍💋‍👨🏻 👨🏼‍❤‍💋‍👨🏼 👨🏼‍❤‍💋‍👨🏽 👨🏼‍❤‍💋‍👨🏾 👨🏼‍❤‍💋‍👨🏿 👨🏼‍❤️‍💋‍👨🏻 👨🏼‍❤️‍💋‍👨🏼 👨🏼‍❤️‍💋‍👨🏽 👨🏼‍❤️‍💋‍👨🏾 👨🏼‍❤️‍💋‍👨🏿 👨🏽‍❤‍💋‍👨🏻 👨🏽‍❤‍💋‍👨🏼 👨🏽‍❤‍💋‍👨🏽 👨🏽‍❤‍💋‍👨🏾 👨🏽‍❤‍💋‍👨🏿 👨🏽‍❤️‍💋‍👨🏻 👨🏽‍❤️‍💋‍👨🏼 👨🏽‍❤️‍💋‍👨🏽 👨🏽‍❤️‍💋‍👨🏾 👨🏽‍❤️‍💋‍👨🏿 👨🏾‍❤‍💋‍👨🏻 👨🏾‍❤‍💋‍👨🏼 👨🏾‍❤‍💋‍👨🏽 👨🏾‍❤‍💋‍👨🏾 👨🏾‍❤‍💋‍👨🏿 👨🏾‍❤️‍💋‍👨🏻 👨🏾‍❤️‍💋‍👨🏼 👨🏾‍❤️‍💋‍👨🏽 👨🏾‍❤️‍💋‍👨🏾 👨🏾‍❤️‍💋‍👨🏿 👨🏿‍❤‍💋‍👨🏻 👨🏿‍❤‍💋‍👨🏼 👨🏿‍❤‍💋‍👨🏽 👨🏿‍❤‍💋‍👨🏾 👨🏿‍❤‍💋‍👨🏿 👨🏿‍❤️‍💋‍👨🏻 👨🏿‍❤️‍💋‍👨🏼 👨🏿‍❤️‍💋‍👨🏽 👨🏿‍❤️‍💋‍👨🏾 👨🏿‍❤️‍💋‍👨🏿 👩🏻‍❤‍💋‍👨🏻 👩🏻‍❤‍💋‍👨🏼 👩🏻‍❤‍💋‍👨🏽 👩🏻‍❤‍💋‍👨🏾 👩🏻‍❤‍💋‍👨🏿 👩🏻‍❤‍💋‍👩🏻 👩🏻‍❤‍💋‍👩🏼 👩🏻‍❤‍💋‍👩🏽 👩🏻‍❤‍💋‍👩🏾 👩🏻‍❤‍💋‍👩🏿 👩🏻‍❤️‍💋‍👨🏻 👩🏻‍❤️‍💋‍👨🏼 👩🏻‍❤️‍💋‍👨🏽 👩🏻‍❤️‍💋‍👨🏾 👩🏻‍❤️‍💋‍👨🏿 👩🏻‍❤️‍💋‍👩🏻 👩🏻‍❤️‍💋‍👩🏼 👩🏻‍❤️‍💋‍👩🏽 👩🏻‍❤️‍💋‍👩🏾 👩🏻‍❤️‍💋‍👩🏿 👩🏼‍❤‍💋‍👨🏻 👩🏼‍❤‍💋‍👨🏼 👩🏼‍❤‍💋‍👨🏽 👩🏼‍❤‍💋‍👨🏾 👩🏼‍❤‍💋‍👨🏿 👩🏼‍❤‍💋‍👩🏻 👩🏼‍❤‍💋‍👩🏼 👩🏼‍❤‍💋‍👩🏽 👩🏼‍❤‍💋‍👩🏾 👩🏼‍❤‍💋‍👩🏿 👩🏼‍❤️‍💋‍👨🏻 👩🏼‍❤️‍💋‍👨🏼 👩🏼‍❤️‍💋‍👨🏽 👩🏼‍❤️‍💋‍👨🏾 👩🏼‍❤️‍💋‍👨🏿 👩🏼‍❤️‍💋‍👩🏻 👩🏼‍❤️‍💋‍👩🏼 👩🏼‍❤️‍💋‍👩🏽 👩🏼‍❤️‍💋‍👩🏾 👩🏼‍❤️‍💋‍👩🏿 👩🏽‍❤‍💋‍👨🏻 👩🏽‍❤‍💋‍👨🏼 👩🏽‍❤‍💋‍👨🏽 👩🏽‍❤‍💋‍👨🏾 👩🏽‍❤‍💋‍👨🏿 👩🏽‍❤‍💋‍👩🏻 👩🏽‍❤‍💋‍👩🏼 👩🏽‍❤‍💋‍👩🏽 👩🏽‍❤‍💋‍👩🏾 👩🏽‍❤‍💋‍👩🏿 👩🏽‍❤️‍💋‍👨🏻 👩🏽‍❤️‍💋‍👨🏼 👩🏽‍❤️‍💋‍👨🏽 👩🏽‍❤️‍💋‍👨🏾 👩🏽‍❤️‍💋‍👨🏿 👩🏽‍❤️‍💋‍👩🏻 👩🏽‍❤️‍💋‍👩🏼 👩🏽‍❤️‍💋‍👩🏽 👩🏽‍❤️‍💋‍👩🏾 👩🏽‍❤️‍💋‍👩🏿 👩🏾‍❤‍💋‍👨🏻 👩🏾‍❤‍💋‍👨🏼 👩🏾‍❤‍💋‍👨🏽 👩🏾‍❤‍💋‍👨🏾 👩🏾‍❤‍💋‍👨🏿 👩🏾‍❤‍💋‍👩🏻 👩🏾‍❤‍💋‍👩🏼 👩🏾‍❤‍💋‍👩🏽 👩🏾‍❤‍💋‍👩🏾 👩🏾‍❤‍💋‍👩🏿 👩🏾‍❤️‍💋‍👨🏻 👩🏾‍❤️‍💋‍👨🏼 👩🏾‍❤️‍💋‍👨🏽 👩🏾‍❤️‍💋‍👨🏾 👩🏾‍❤️‍💋‍👨🏿 👩🏾‍❤️‍💋‍👩🏻 👩🏾‍❤️‍💋‍👩🏼 👩🏾‍❤️‍💋‍👩🏽 👩🏾‍❤️‍💋‍👩🏾 👩🏾‍❤️‍💋‍👩🏿 👩🏿‍❤‍💋‍👨🏻 👩🏿‍❤‍💋‍👨🏼 👩🏿‍❤‍💋‍👨🏽 👩🏿‍❤‍💋‍👨🏾 👩🏿‍❤‍💋‍👨🏿 👩🏿‍❤‍💋‍👩🏻 👩🏿‍❤‍💋‍👩🏼 👩🏿‍❤‍💋‍👩🏽 👩🏿‍❤‍💋‍👩🏾 👩🏿‍❤‍💋‍👩🏿 👩🏿‍❤️‍💋‍👨🏻 👩🏿‍❤️‍💋‍👨🏼 👩🏿‍❤️‍💋‍👨🏽 👩🏿‍❤️‍💋‍👨🏾 👩🏿‍❤️‍💋‍👨🏿 👩🏿‍❤️‍💋‍👩🏻 👩🏿‍❤️‍💋‍👩🏼 👩🏿‍❤️‍💋‍👩🏽 👩🏿‍❤️‍💋‍👩🏾 👩🏿‍❤️‍💋‍👩🏿 💏🏻 💏🏼 💏🏽 💏🏾 💏🏿 🧑🏻‍❤‍💋‍🧑🏼 🧑🏻‍❤‍💋‍🧑🏽 🧑🏻‍❤‍💋‍🧑🏾 🧑🏻‍❤‍💋‍🧑🏿 🧑🏻‍❤️‍💋‍🧑🏼 🧑🏻‍❤️‍💋‍🧑🏽 🧑🏻‍❤️‍💋‍🧑🏾 🧑🏻‍❤️‍💋‍🧑🏿 🧑🏼‍❤‍💋‍🧑🏻 🧑🏼‍❤‍💋‍🧑🏽 🧑🏼‍❤‍💋‍🧑🏾 🧑🏼‍❤‍💋‍🧑🏿 🧑🏼‍❤️‍💋‍🧑🏻 🧑🏼‍❤️‍💋‍🧑🏽 🧑🏼‍❤️‍💋‍🧑🏾 🧑🏼‍❤️‍💋‍🧑🏿 🧑🏽‍❤‍💋‍🧑🏻 🧑🏽‍❤‍💋‍🧑🏼 🧑🏽‍❤‍💋‍🧑🏾 🧑🏽‍❤‍💋‍🧑🏿 🧑🏽‍❤️‍💋‍🧑🏻 🧑🏽‍❤️‍💋‍🧑🏼 🧑🏽‍❤️‍💋‍🧑🏾 🧑🏽‍❤️‍💋‍🧑🏿 🧑🏾‍❤‍💋‍🧑🏻 🧑🏾‍❤‍💋‍🧑🏼 🧑🏾‍❤‍💋‍🧑🏽 🧑🏾‍❤‍💋‍🧑🏿 🧑🏾‍❤️‍💋‍🧑🏻 🧑🏾‍❤️‍💋‍🧑🏼 🧑🏾‍❤️‍💋‍🧑🏽 🧑🏾‍❤️‍💋‍🧑🏿 🧑🏿‍❤‍💋‍🧑🏻 🧑🏿‍❤‍💋‍🧑🏼 🧑🏿‍❤‍💋‍🧑🏽 🧑🏿‍❤‍💋‍🧑🏾 🧑🏿‍❤️‍💋‍🧑🏻 🧑🏿‍❤️‍💋‍🧑🏼 🧑🏿‍❤️‍💋‍🧑🏽 🧑🏿‍❤️‍💋‍🧑🏾
kiss:👨‍❤‍💋‍👨 👨‍❤️‍💋‍👨 👩‍❤‍💋‍👨 👩‍❤‍💋‍👩 👩‍❤️‍💋‍👨 👩‍❤️‍💋‍👩
couple with heart:💑
couple with heart:👨🏻‍❤‍👨🏻 👨🏻‍❤‍👨🏼 👨🏻‍❤‍👨🏽 👨🏻‍❤‍👨🏾 👨🏻‍❤‍👨🏿 👨🏻‍❤️‍👨🏻 👨🏻‍❤️‍👨🏼 👨🏻‍❤️‍👨🏽 👨🏻‍❤️‍👨🏾 👨🏻‍❤️‍👨🏿 👨🏼‍❤‍👨🏻 👨🏼‍❤‍👨🏼 👨🏼‍❤‍👨🏽 👨🏼‍❤‍👨🏾 👨🏼‍❤‍👨🏿 👨🏼‍❤️‍👨🏻 👨🏼‍❤️‍👨🏼 👨🏼‍❤️‍👨🏽 👨🏼‍❤️‍👨🏾 👨🏼‍❤️‍👨🏿 👨🏽‍❤‍👨🏻 👨🏽‍❤‍👨🏼 👨🏽‍❤‍👨🏽 👨🏽‍❤‍👨🏾 👨🏽‍❤‍👨🏿 👨🏽‍❤️‍👨🏻 👨🏽‍❤️‍👨🏼 👨🏽‍❤️‍👨🏽 👨🏽‍❤️‍👨🏾 👨🏽‍❤️‍👨🏿 👨🏾‍❤‍👨🏻 👨🏾‍❤‍👨🏼 👨🏾‍❤‍👨🏽 👨🏾‍❤‍👨🏾 👨🏾‍❤‍👨🏿 👨🏾‍❤️‍👨🏻 👨🏾‍❤️‍👨🏼 👨🏾‍❤️‍👨🏽 👨🏾‍❤️‍👨🏾 👨🏾‍❤️‍👨🏿 👨🏿‍❤‍👨🏻 👨🏿‍❤‍👨🏼 👨🏿‍❤‍👨🏽 👨🏿‍❤‍👨🏾 👨🏿‍❤‍👨🏿 👨🏿‍❤️‍👨🏻 👨🏿‍❤️‍👨🏼 👨🏿‍❤️‍👨🏽 👨🏿‍❤️‍👨🏾 👨🏿‍❤️‍👨🏿 👩🏻‍❤‍👨🏻 👩🏻‍❤‍👨🏼 👩🏻‍❤‍👨🏽 👩🏻‍❤‍👨🏾 👩🏻‍❤‍👨🏿 👩🏻‍❤‍👩🏻 👩🏻‍❤‍👩🏼 👩🏻‍❤‍👩🏽 👩🏻‍❤‍👩🏾 👩🏻‍❤‍👩🏿 👩🏻‍❤️‍👨🏻 👩🏻‍❤️‍👨🏼 👩🏻‍❤️‍👨🏽 👩🏻‍❤️‍👨🏾 👩🏻‍❤️‍👨🏿 👩🏻‍❤️‍👩🏻 👩🏻‍❤️‍👩🏼 👩🏻‍❤️‍👩🏽 👩🏻‍❤️‍👩🏾 👩🏻‍❤️‍👩🏿 👩🏼‍❤‍👨🏻 👩🏼‍❤‍👨🏼 👩🏼‍❤‍👨🏽 👩🏼‍❤‍👨🏾 👩🏼‍❤‍👨🏿 👩🏼‍❤‍👩🏻 👩🏼‍❤‍👩🏼 👩🏼‍❤‍👩🏽 👩🏼‍❤‍👩🏾 👩🏼‍❤‍👩🏿 👩🏼‍❤️‍👨🏻 👩🏼‍❤️‍👨🏼 👩🏼‍❤️‍👨🏽 👩🏼‍❤️‍👨🏾 👩🏼‍❤️‍👨🏿 👩🏼‍❤️‍👩🏻 👩🏼‍❤️‍👩🏼 👩🏼‍❤️‍👩🏽 👩🏼‍❤️‍👩🏾 👩🏼‍❤️‍👩🏿 👩🏽‍❤‍👨🏻 👩🏽‍❤‍👨🏼 👩🏽‍❤‍👨🏽 👩🏽‍❤‍👨🏾 👩🏽‍❤‍👨🏿 👩🏽‍❤‍👩🏻 👩🏽‍❤‍👩🏼 👩🏽‍❤‍👩🏽 👩🏽‍❤‍👩🏾 👩🏽‍❤‍👩🏿 👩🏽‍❤️‍👨🏻 👩🏽‍❤️‍👨🏼 👩🏽‍❤️‍👨🏽 👩🏽‍❤️‍👨🏾 👩🏽‍❤️‍👨🏿 👩🏽‍❤️‍👩🏻 👩🏽‍❤️‍👩🏼 👩🏽‍❤️‍👩🏽 👩🏽‍❤️‍👩🏾 👩🏽‍❤️‍👩🏿 👩🏾‍❤‍👨🏻 👩🏾‍❤‍👨🏼 👩🏾‍❤‍👨🏽 👩🏾‍❤‍👨🏾 👩🏾‍❤‍👨🏿 👩🏾‍❤‍👩🏻 👩🏾‍❤‍👩🏼 👩🏾‍❤‍👩🏽 👩🏾‍❤‍👩🏾 👩🏾‍❤‍👩🏿 👩🏾‍❤️‍👨🏻 👩🏾‍❤️‍👨🏼 👩🏾‍❤️‍👨🏽 👩🏾‍❤️‍👨🏾 👩🏾‍❤️‍👨🏿 👩🏾‍❤️‍👩🏻 👩🏾‍❤️‍👩🏼 👩🏾‍❤️‍👩🏽 👩🏾‍❤️‍👩🏾 👩🏾‍❤️‍👩🏿 👩🏿‍❤‍👨🏻 👩🏿‍❤‍👨🏼 👩🏿‍❤‍👨🏽 👩🏿‍❤‍👨🏾 👩🏿‍❤‍👨🏿 👩🏿‍❤‍👩🏻 👩🏿‍❤‍👩🏼 👩🏿‍❤‍👩🏽 👩🏿‍❤‍👩🏾 👩🏿‍❤‍👩🏿 👩🏿‍❤️‍👨🏻 👩🏿‍❤️‍👨🏼 👩🏿‍❤️‍👨🏽 👩🏿‍❤️‍👨🏾 👩🏿‍❤️‍👨🏿 👩🏿‍❤️‍👩🏻 👩🏿‍❤️‍👩🏼 👩🏿‍❤️‍👩🏽 👩🏿‍❤️‍👩🏾 👩🏿‍❤️‍👩🏿 💑🏻 💑🏼 💑🏽 💑🏾 💑🏿 🧑🏻‍❤‍🧑🏼 🧑🏻‍❤‍🧑🏽 🧑🏻‍❤‍🧑🏾 🧑🏻‍❤‍🧑🏿 🧑🏻‍❤️‍🧑🏼 🧑🏻‍❤️‍🧑🏽 🧑🏻‍❤️‍🧑🏾 🧑🏻‍❤️‍🧑🏿 🧑🏼‍❤‍🧑🏻 🧑🏼‍❤‍🧑🏽 🧑🏼‍❤‍🧑🏾 🧑🏼‍❤‍🧑🏿 🧑🏼‍❤️‍🧑🏻 🧑🏼‍❤️‍🧑🏽 🧑🏼‍❤️‍🧑🏾 🧑🏼‍❤️‍🧑🏿 🧑🏽‍❤‍🧑🏻 🧑🏽‍❤‍🧑🏼 🧑🏽‍❤‍🧑🏾 🧑🏽‍❤‍🧑🏿 🧑🏽‍❤️‍🧑🏻 🧑🏽‍❤️‍🧑🏼 🧑🏽‍❤️‍🧑🏾 🧑🏽‍❤️‍🧑🏿 🧑🏾‍❤‍🧑🏻 🧑🏾‍❤‍🧑🏼 🧑🏾‍❤‍🧑🏽 🧑🏾‍❤‍🧑🏿 🧑🏾‍❤️‍🧑🏻 🧑🏾‍❤️‍🧑🏼 🧑🏾‍❤️‍🧑🏽 🧑🏾‍❤️‍🧑🏿 🧑🏿‍❤‍🧑🏻 🧑🏿‍❤‍🧑🏼 🧑🏿‍❤‍🧑🏽 🧑🏿‍❤‍🧑🏾 🧑🏿‍❤️‍🧑🏻 🧑🏿‍❤️‍🧑🏼 🧑🏿‍❤️‍🧑🏽 🧑🏿‍❤️‍🧑🏾
couple with heart:👨‍❤‍👨 👨‍❤️‍👨 👩‍❤‍👨 👩‍❤‍👩 👩‍❤️‍👨 👩‍❤️‍👩
family:👨‍👨‍👦 👨‍👨‍👦‍👦 👨‍👨‍👧 👨‍👨‍👧‍👦 👨‍👨‍👧‍👧 👨‍👩‍👦 👨‍👩‍👦‍👦 👨‍👩‍👧 👨‍👩‍👧‍👦 👨‍👩‍👧‍👧 👩‍👩‍👦 👩‍👩‍👦‍👦 👩‍👩‍👧 👩‍👩‍👧‍👦 👩‍👩‍👧‍👧
family:👨‍👦 👨‍👦‍👦 👨‍👧 👨‍👧‍👦 👨‍👧‍👧 👩‍👦 👩‍👦‍👦 👩‍👧 👩‍👧‍👦 👩‍👧‍👧
speaking head:🗣 🗣️
bust in silhouette:👤
busts in silhouette:👥
people hugging:🫂
family:👪
family:🧑‍🧑‍🧒 🧑‍🧑‍🧒‍🧒 🧑‍🧒 🧑‍🧒‍🧒
footprints:👣
fingerprint:🫆

monkey face:🐵
monkey:🐒
gorilla:🦍
orangutan:🦧
dog face:🐶
dog:🐕
guide dog:🦮
service dog:🐕‍🦺
poodle:🐩
wolf:🐺
fox:🦊
raccoon:🦝
cat face:🐱
cat:🐈
black cat:🐈‍⬛
lion:🦁
tiger face:🐯
tiger:🐅
leopard:🐆
horse face:🐴
moose:🫎
donkey:🫏
horse:🐎
unicorn:🦄
zebra:🦓
deer:🦌
bison:🦬
cow face:🐮
ox:🐂
water buffalo:🐃
cow:🐄
pig face:🐷
pig:🐖
boar:🐗
pig nose:🐽
ram:🐏
ewe:🐑
goat:🐐
camel:🐪
two hump camel:🐫
llama:🦙
giraffe:🦒
elephant:🐘
mammoth:🦣
rhinoceros:🦏
hippopotamus:🦛
mouse face:🐭
mouse:🐁
rat:🐀
hamster:🐹
rabbit face:🐰
rabbit:🐇
chipmunk:🐿 🐿️
beaver:🦫
hedgehog:🦔
bat:🦇
bear:🐻
polar bear:🐻‍❄ 🐻‍❄️
koala:🐨
panda:🐼
sloth:🦥
otter:🦦
skunk:🦨
kangaroo:🦘
badger:🦡
paw prints:🐾
turkey:🦃
chicken:🐔
rooster:🐓
hatching chick:🐣
baby chick:🐤
front facing baby chick:🐥
bird:🐦
penguin:🐧
dove:🕊 🕊️
eagle:🦅
duck:🦆
swan:🦢
owl:🦉
dodo:🦤
feather:🪶
flamingo:🦩
peacock:🦚
parrot:🦜
wing:🪽
black bird:🐦‍⬛
goose:🪿
phoenix:🐦‍🔥
frog:🐸
crocodile:🐊
turtle:🐢
lizard:🦎
snake:🐍
dragon face:🐲
dragon:🐉
sauropod:🦕
T Rex:🦖
spouting whale:🐳
whale:🐋
dolphin:🐬
orca:🫍
seal:🦭
fish:🐟
tropical fish:🐠
blowfish:🐡
shark:🦈
octopus:🐙
spiral shell:🐚
coral:🪸
jellyfish:🪼
crab:🦀
lobster:🦞
shrimp:🦐
squid:🦑
oyster:🦪
snail:🐌
butterfly:🦋
bug:🐛
ant:🐜
honeybee:🐝
beetle:🪲
lady beetle:🐞
cricket:🦗
cockroach:🪳
spider:🕷 🕷️
spider web:🕸 🕸️
scorpion:🦂
mosquito:🦟
fly:🪰
worm:🪱
microbe:🦠
bouquet:💐
cherry blossom:🌸
white flower:💮
lotus:🪷
rosette:🏵 🏵️
rose:🌹
wilted flower:🥀
hibiscus:🌺
sunflower:🌻
blossom:🌼
tulip:🌷
hyacinth:🪻
seedling:🌱
potted plant:🪴
evergreen tree:🌲
deciduous tree:🌳
palm tree:🌴
cactus:🌵
sheaf of rice:🌾
herb:🌿
shamrock:☘ ☘️
four leaf clover:🍀
maple leaf:🍁
fallen leaf:🍂
leaf fluttering in wind:🍃
empty nest:🪹
nest with eggs:🪺
mushroom:🍄
leafless tree:🪾

grapes:🍇
melon:🍈
watermelon:🍉
tangerine:🍊
lemon:🍋
lime:🍋‍🟩
banana:🍌
pineapple:🍍
mango:🥭
red apple:🍎
green apple:🍏
pear:🍐
peach:🍑
cherries:🍒
strawberry:🍓
blueberries:🫐
kiwi fruit:🥝
tomato:🍅
olive:🫒
coconut:🥥
avocado:🥑
eggplant:🍆
potato:🥔
carrot:🥕
ear of corn:🌽
hot pepper:🌶 🌶️
bell pepper:🫑
cucumber:🥒
leafy green:🥬
broccoli:🥦
garlic:🧄
onion:🧅
peanuts:🥜
beans:🫘
chestnut:🌰
ginger root:🫚
pea pod:🫛
brown mushroom:🍄‍🟫
root vegetable:🫜
bread:🍞
croissant:🥐
baguette bread:🥖
flatbread:🫓
pretzel:🥨
bagel:🥯
pancakes:🥞
waffle:🧇
cheese wedge:🧀
meat on bone:🍖
poultry leg:🍗
cut of meat:🥩
bacon:🥓
hamburger:🍔
french fries:🍟
pizza:🍕
hot dog:🌭
sandwich:🥪
taco:🌮
burrito:🌯
tamale:🫔
stuffed flatbread:🥙
falafel:🧆
egg:🥚
cooking:🍳
shallow pan of food:🥘
pot of food:🍲
fondue:🫕
bowl with spoon:🥣
green salad:🥗
popcorn:🍿
butter:🧈
salt:🧂
canned food:🥫
bento box:🍱
rice cracker:🍘
rice ball:🍙
cooked rice:🍚
curry rice:🍛
steaming bowl:🍜
spaghetti:🍝
roasted sweet potato:🍠
oden:🍢
sushi:🍣
fried shrimp:🍤
fish cake with swirl:🍥
moon cake:🥮
dango:🍡
dumpling:🥟
fortune cookie:🥠
takeout box:🥡
soft ice cream:🍦
shaved ice:🍧
ice cream:🍨
doughnut:🍩
cookie:🍪
birthday cake:🎂
shortcake:🍰
cupcake:🧁
pie:🥧
chocolate bar:🍫
candy:🍬
lollipop:🍭
custard:🍮
honey pot:🍯
baby bottle:🍼
glass of milk:🥛
hot beverage:☕
teapot:🫖
teacup without handle:🍵
sake:🍶
bottle with popping cork:🍾
wine glass:🍷
cocktail glass:🍸
tropical drink:🍹
beer mug:🍺
clinking beer mugs:🍻
clinking glasses:🥂
tumbler glass:🥃
pouring liquid:🫗
cup with straw:🥤
bubble tea:🧋
beverage box:🧃
mate:🧉
ice:🧊
chopsticks:🥢
fork and knife with plate:🍽 🍽️
fork and knife:🍴
spoon:🥄
kitchen knife:🔪
jar:🫙
amphora:🏺

globe showing Europe Africa:🌍
globe showing Americas:🌎
globe showing Asia Australia:🌏
globe with meridians:🌐
world map:🗺 🗺️
map of Japan:🗾
compass:🧭
snow capped mountain:🏔 🏔️
mountain:⛰ ⛰️
landslide:🛘
volcano:🌋
mount fuji:🗻
camping:🏕 🏕️
beach with umbrella:🏖 🏖️
desert:🏜 🏜️
desert island:🏝 🏝️
national park:🏞 🏞️
stadium:🏟 🏟️
classical building:🏛 🏛️
building construction:🏗 🏗️
brick:🧱
rock:🪨
wood:🪵
hut:🛖
houses:🏘 🏘️
derelict house:🏚 🏚️
house:🏠
house with garden:🏡
office building:🏢
Japanese post office:🏣
post office:🏤
hospital:🏥
bank:🏦
hotel:🏨
love hotel:🏩
convenience store:🏪
school:🏫
department store:🏬
factory:🏭
Japanese castle:🏯
castle:🏰
wedding:💒
Tokyo tower:🗼
Statue of Liberty:🗽
church:⛪
mosque:🕌
hindu temple:🛕
synagogue:🕍
shinto shrine:⛩ ⛩️
kaaba:🕋
fountain:⛲
tent:⛺
foggy:🌁
night with stars:🌃
cityscape:🏙 🏙️
sunrise over mountains:🌄
sunrise:🌅
cityscape at dusk:🌆
sunset:🌇
bridge at night:🌉
hot springs:♨ ♨️
carousel horse:🎠
playground slide:🛝
ferris wheel:🎡
roller coaster:🎢
barber pole:💈
circus tent:🎪
locomotive:🚂
railway car:🚃
high speed train:🚄
bullet train:🚅
train:🚆
metro:🚇
light rail:🚈
station:🚉
tram:🚊
monorail:🚝
mountain railway:🚞
tram car:🚋
bus:🚌
oncoming bus:🚍
trolleybus:🚎
minibus:🚐
ambulance:🚑
fire engine:🚒
police car:🚓
oncoming police car:🚔
taxi:🚕
oncoming taxi:🚖
automobile:🚗
oncoming automobile:🚘
sport utility vehicle:🚙
pickup truck:🛻
delivery truck:🚚
articulated lorry:🚛
tractor:🚜
racing car:🏎 🏎️
motorcycle:🏍 🏍️
motor scooter:🛵
manual wheelchair:🦽
motorized wheelchair:🦼
auto rickshaw:🛺
bicycle:🚲
kick scooter:🛴
skateboard:🛹
roller skate:🛼
bus stop:🚏
motorway:🛣 🛣️
railway track:🛤 🛤️
oil drum:🛢 🛢️
fuel pump:⛽
wheel:🛞
police car light:🚨
horizontal traffic light:🚥
vertical traffic light:🚦
stop sign:🛑
construction:🚧
anchor:⚓
ring buoy:🛟
sailboat:⛵
canoe:🛶
speedboat:🚤
passenger ship:🛳 🛳️
ferry:⛴ ⛴️
motor boat:🛥 🛥️
ship:🚢
airplane:✈ ✈️
small airplane:🛩 🛩️
airplane departure:🛫
airplane arrival:🛬
parachute:🪂
seat:💺
helicopter:🚁
suspension railway:🚟
mountain cableway:🚠
aerial tramway:🚡
satellite:🛰 🛰️
rocket:🚀
flying saucer:🛸
bellhop bell:🛎 🛎️
luggage:🧳
hourglass done:⌛
hourglass not done:⏳
watch:⌚
alarm clock:⏰
stopwatch:⏱ ⏱️
timer clock:⏲ ⏲️
mantelpiece clock:🕰 🕰️
twelve o clock:🕛
twelve thirty:🕧
one o clock:🕐
one thirty:🕜
two o clock:🕑
two thirty:🕝
three o clock:🕒
three thirty:🕞
four o clock:🕓
four thirty:🕟
five o clock:🕔
five thirty:🕠
six o clock:🕕
six thirty:🕡
seven o clock:🕖
seven thirty:🕢
eight o clock:🕗
eight thirty:🕣
nine o clock:🕘
nine thirty:🕤
ten o clock:🕙
ten thirty:🕥
eleven o clock:🕚
eleven thirty:🕦
new moon:🌑
waxing crescent moon:🌒
first quarter moon:🌓
waxing gibbous moon:🌔
full moon:🌕
waning gibbous moon:🌖
last quarter moon:🌗
waning crescent moon:🌘
crescent moon:🌙
new moon face:🌚
first quarter moon face:🌛
last quarter moon face:🌜
thermometer:🌡 🌡️
sun:☀ ☀️
full moon face:🌝
sun with face:🌞
ringed planet:🪐
star:⭐
glowing star:🌟
shooting star:🌠
milky way:🌌
cloud:☁ ☁️
sun behind cloud:⛅
cloud with lightning and rain:⛈ ⛈️
sun behind small cloud:🌤 🌤️
sun behind large cloud:🌥 🌥️
sun behind rain cloud:🌦 🌦️
cloud with rain:🌧 🌧️
cloud with snow:🌨 🌨️
cloud with lightning:🌩 🌩️
tornado:🌪 🌪️
fog:🌫 🌫️
wind face:🌬 🌬️
cyclone:🌀
rainbow:🌈
closed umbrella:🌂
umbrella:☂ ☂️
umbrella with rain drops:☔
umbrella on ground:⛱ ⛱️
high voltage:⚡
snowflake:❄ ❄️
snowman:☃ ☃️
snowman without snow:⛄
comet:☄ ☄️
fire:🔥
droplet:💧
water wave:🌊
jack o lantern:🎃
Christmas tree:🎄
fireworks:🎆
sparkler:🎇
firecracker:🧨
sparkles:✨
balloon:🎈
party popper:🎉
confetti ball:🎊
tanabata tree:🎋
pine decoration:🎍
Japanese dolls:🎎
carp streamer:🎏
wind chime:🎐
moon viewing ceremony:🎑
red envelope:🧧
ribbon:🎀
wrapped gift:🎁
reminder ribbon:🎗 🎗️
admission tickets:🎟 🎟️
ticket:🎫
military medal:🎖 🎖️
trophy:🏆
sports medal:🏅
1st place medal:🥇
2nd place medal:🥈
3rd place medal:🥉
soccer ball:⚽
baseball:⚾
softball:🥎
basketball:🏀
volleyball:🏐
american football:🏈
rugby football:🏉
tennis:🎾
flying disc:🥏
bowling:🎳
cricket game:🏏
field hockey:🏑
ice hockey:🏒
lacrosse:🥍
ping pong:🏓
badminton:🏸
boxing glove:🥊
martial arts uniform:🥋
goal net:🥅
flag in hole:⛳
ice skate:⛸ ⛸️
fishing pole:🎣
diving mask:🤿
running shirt:🎽
skis:🎿
sled:🛷
curling stone:🥌
bullseye:🎯
yo yo:🪀
kite:🪁
water pistol:🔫
pool 8 ball:🎱
crystal ball:🔮
magic wand:🪄
video game:🎮
joystick:🕹 🕹️
slot machine:🎰
game die:🎲
puzzle piece:🧩
teddy bear:🧸
piñata:🪅
mirror ball:🪩
nesting dolls:🪆
spade suit:♠ ♠️
heart suit:♥ ♥️
diamond suit:♦ ♦️
club suit:♣ ♣️
chess pawn:♟ ♟️
joker:🃏
mahjong red dragon:🀄
flower playing cards:🎴
performing arts:🎭
framed picture:🖼 🖼️
artist palette:🎨
thread:🧵
sewing needle:🪡
yarn:🧶
knot:🪢
glasses:👓
sunglasses:🕶 🕶️
goggles:🥽
lab coat:🥼
safety vest:🦺
necktie:👔
t shirt:👕
jeans:👖
scarf:🧣
gloves:🧤
coat:🧥
socks:🧦
dress:👗
kimono:👘
sari:🥻
one piece swimsuit:🩱
briefs:🩲
shorts:🩳
bikini:👙
woman s clothes:👚
folding hand fan:🪭
purse:👛
handbag:👜
clutch bag:👝
shopping bags:🛍 🛍️
backpack:🎒
thong sandal:🩴
man s shoe:👞
running shoe:👟
hiking boot:🥾
flat shoe:🥿
high heeled shoe:👠
woman s sandal:👡
ballet shoes:🩰
woman s boot:👢
hair pick:🪮
crown:👑
woman s hat:👒
top hat:🎩
graduation cap:🎓
billed cap:🧢
military helmet:🪖
rescue worker s helmet:⛑ ⛑️
prayer beads:📿
lipstick:💄
ring:💍
gem stone:💎
muted speaker:🔇
speaker low volume:🔈
speaker medium volume:🔉
speaker high volume:🔊
loudspeaker:📢
megaphone:📣
postal horn:📯
bell:🔔
bell with slash:🔕
musical score:🎼
musical note:🎵
musical notes:🎶
studio microphone:🎙 🎙️
level slider:🎚 🎚️
control knobs:🎛 🎛️
microphone:🎤
headphone:🎧
radio:📻
saxophone:🎷
trumpet:🎺
trombone:🪊
accordion:🪗
guitar:🎸
musical keyboard:🎹
violin:🎻
banjo:🪕
drum:🥁
long drum:🪘
maracas:🪇
flute:🪈
harp:🪉
mobile phone:📱
mobile phone with arrow:📲
telephone:☎ ☎️
telephone receiver:📞
pager:📟
fax machine:📠
battery:🔋
low battery:🪫
electric plug:🔌
laptop:💻
desktop computer:🖥 🖥️
printer:🖨 🖨️
keyboard:⌨ ⌨️
computer mouse:🖱 🖱️
trackball:🖲 🖲️
computer disk:💽
floppy disk:💾
optical disk:💿
dvd:📀
abacus:🧮
movie camera:🎥
film frames:🎞 🎞️
film projector:📽 📽️
clapper board:🎬
television:📺
camera:📷
camera with flash:📸
video camera:📹
videocassette:📼
magnifying glass tilted left:🔍
magnifying glass tilted right:🔎
candle:🕯 🕯️
light bulb:💡
flashlight:🔦
red paper lantern:🏮
diya lamp:🪔
notebook with decorative cover:📔
closed book:📕
open book:📖
green book:📗
blue book:📘
orange book:📙
books:📚
notebook:📓
ledger:📒
page with curl:📃
scroll:📜
page facing up:📄
newspaper:📰
rolled up newspaper:🗞 🗞️
bookmark tabs:📑
bookmark:🔖
label:🏷 🏷️
coin:🪙
money bag:💰
treasure chest:🪎
yen banknote:💴
dollar banknote:💵
euro banknote:💶
pound banknote:💷
money with wings:💸
credit card:💳
receipt:🧾
chart increasing with yen:💹
envelope:✉ ✉️
e mail:📧
incoming envelope:📨
envelope with arrow:📩
outbox tray:📤
inbox tray:📥
package:📦
closed mailbox with raised flag:📫
closed mailbox with lowered flag:📪
open mailbox with raised flag:📬
open mailbox with lowered flag:📭
postbox:📮
ballot box with ballot:🗳 🗳️
pencil:✏ ✏️
black nib:✒ ✒️
fountain pen:🖋 🖋️
pen:🖊 🖊️
paintbrush:🖌 🖌️
crayon:🖍 🖍️
memo:📝
briefcase:💼
file folder:📁
open file folder:📂
card index dividers:🗂 🗂️
calendar:📅
tear off calendar:📆
spiral notepad:🗒 🗒️
spiral calendar:🗓 🗓️
card index:📇
chart increasing:📈
chart decreasing:📉
bar chart:📊
clipboard:📋
pushpin:📌
round pushpin:📍
paperclip:📎
linked paperclips:🖇 🖇️
straight ruler:📏
triangular ruler:📐
scissors:✂ ✂️
card file box:🗃 🗃️
file cabinet:🗄 🗄️
wastebasket:🗑 🗑️
locked:🔒
unlocked:🔓
locked with pen:🔏
locked with key:🔐
key:🔑
old key:🗝 🗝️
hammer:🔨
axe:🪓
pick:⛏ ⛏️
hammer and pick:⚒ ⚒️
hammer and wrench:🛠 🛠️
dagger:🗡 🗡️
crossed swords:⚔ ⚔️
bomb:💣
boomerang:🪃
bow and arrow:🏹
shield:🛡 🛡️
carpentry saw:🪚
wrench:🔧
screwdriver:🪛
nut and bolt:🔩
gear:⚙ ⚙️
clamp:🗜 🗜️
balance scale:⚖ ⚖️
white cane:🦯
link:🔗
broken chain:⛓‍💥 ⛓️‍💥
chains:⛓ ⛓️
hook:🪝
toolbox:🧰
magnet:🧲
ladder:🪜
shovel:🪏
alembic:⚗ ⚗️
test tube:🧪
petri dish:🧫
dna:🧬
microscope:🔬
telescope:🔭
satellite antenna:📡
syringe:💉
drop of blood:🩸
pill:💊
adhesive bandage:🩹
crutch:🩼
stethoscope:🩺
x ray:🩻
door:🚪
elevator:🛗
mirror:🪞
window:🪟
bed:🛏 🛏️
couch and lamp:🛋 🛋️
chair:🪑
toilet:🚽
plunger:🪠
shower:🚿
bathtub:🛁
mouse trap:🪤
razor:🪒
lotion bottle:🧴
safety pin:🧷
broom:🧹
basket:🧺
roll of paper:🧻
bucket:🪣
soap:🧼
bubbles:🫧
toothbrush:🪥
sponge:🧽
fire extinguisher:🧯
shopping cart:🛒
cigarette:🚬
coffin:⚰ ⚰️
headstone:🪦
funeral urn:⚱ ⚱️
nazar amulet:🧿
hamsa:🪬
moai:🗿
placard:🪧
identification card:🪪

ATM sign:🏧
litter in bin sign:🚮
potable water:🚰
wheelchair symbol:♿
men s room:🚹
women s room:🚺
restroom:🚻
baby symbol:🚼
water closet:🚾
passport control:🛂
customs:🛃
baggage claim:🛄
left luggage:🛅
warning:⚠ ⚠️
children crossing:🚸
no entry:⛔
prohibited:🚫
no bicycles:🚳
no smoking:🚭
no littering:🚯
non potable water:🚱
no pedestrians:🚷
no mobile phones:📵
no one under eighteen:🔞
radioactive:☢ ☢️
biohazard:☣ ☣️
up arrow:⬆ ⬆️
up right arrow:↗ ↗️
right arrow:➡ ➡️
down right arrow:↘ ↘️
down arrow:⬇ ⬇️
down left arrow:↙ ↙️
left arrow:⬅ ⬅️
up left arrow:↖ ↖️
up down arrow:↕ ↕️
left right arrow:↔ ↔️
right arrow curving left:↩ ↩️
left arrow curving right:↪ ↪️
right arrow curving up:⤴ ⤴️
right arrow curving down:⤵ ⤵️
clockwise vertical arrows:🔃
counterclockwise arrows button:🔄
BACK arrow:🔙
END arrow:🔚
ON arrow:🔛
SOON arrow:🔜
TOP arrow:🔝
place of worship:🛐
atom symbol:⚛ ⚛️
om:🕉 🕉️
star of David:✡ ✡️
wheel of dharma:☸ ☸️
yin yang:☯ ☯️
latin cross:✝ ✝️
orthodox cross:☦ ☦️
star and crescent:☪ ☪️
peace symbol:☮ ☮️
menorah:🕎
dotted six pointed star:🔯
khanda:🪯
Aries:♈
Taurus:♉
Gemini:♊
Cancer:♋
Leo:♌
Virgo:♍
Libra:♎
Scorpio:♏
Sagittarius:♐
Capricorn:♑
Aquarius:♒
Pisces:♓
Ophiuchus:⛎
shuffle tracks button:🔀
repeat button:🔁
repeat single button:🔂
play button:▶ ▶️
fast forward button:⏩
next track button:⏭ ⏭️
play or pause button:⏯ ⏯️
reverse button:◀ ◀️
fast reverse button:⏪
last track button:⏮ ⏮️
upwards button:🔼
fast up button:⏫
downwards button:🔽
fast down button:⏬
pause button:⏸ ⏸️
stop button:⏹ ⏹️
record button:⏺ ⏺️
eject button:⏏ ⏏️
cinema:🎦
dim button:🔅
bright button:🔆
antenna bars:📶
wireless:🛜
vibration mode:📳
mobile phone off:📴
female sign:♀ ♀️
male sign:♂ ♂️
transgender symbol:⚧ ⚧️
multiply:✖ ✖️
plus:➕
minus:➖
divide:➗
heavy equals sign:🟰
infinity:♾ ♾️
double exclamation mark:‼ ‼️
exclamation question mark:⁉ ⁉️
red question mark:❓
white question mark:❔
white exclamation mark:❕
red exclamation mark:❗
wavy dash:〰 〰️
currency exchange:💱
heavy dollar sign:💲
medical symbol:⚕ ⚕️
recycling symbol:♻ ♻️
fleur de lis:⚜ ⚜️
trident emblem:🔱
name badge:📛
Japanese symbol for beginner:🔰
hollow red circle:⭕
check mark button:✅
check box with check:☑ ☑️
check mark:✔ ✔️
cross mark:❌
cross mark button:❎
curly loop:➰
double curly loop:➿
part alternation mark:〽 〽️
eight spoked asterisk:✳ ✳️
eight pointed star:✴ ✴️
sparkle:❇ ❇️
copyright:© ©️
registered:® ®️
trade mark:™ ™️
splatter:🫟
keycap::#⃣ #️⃣
keycap::*⃣ *️⃣
0 keycap: 0:0⃣ 0️⃣
1 keycap: 1:1⃣ 1️⃣
2 keycap: 2:2⃣ 2️⃣
3 keycap: 3:3⃣ 3️⃣
4 keycap: 4:4⃣ 4️⃣
5 keycap: 5:5⃣ 5️⃣
6 keycap: 6:6⃣ 6️⃣
7 keycap: 7:7⃣ 7️⃣
8 keycap: 8:8⃣ 8️⃣
9 keycap: 9:9⃣ 9️⃣
keycap: 10:🔟
input latin uppercase:🔠
input latin lowercase:🔡
input numbers:🔢
input symbols:🔣
input latin letters:🔤
A button blood type:🅰 🅰️
AB button blood type:🆎
B button blood type:🅱 🅱️
CL button:🆑
COOL button:🆒
FREE button:🆓
ℹ information:ℹ ℹ️
ID button:🆔
circled M:Ⓜ Ⓜ️
NEW button:🆕
NG button:🆖
O button blood type:🅾 🅾️
OK button:🆗
P button:🅿 🅿️
SOS button:🆘
UP button:🆙
VS button:🆚
Japanese here button:🈁
Japanese service charge button:🈂 🈂️
Japanese monthly amount button:🈷 🈷️
Japanese not free of charge button:🈶
Japanese reserved button:🈯
Japanese bargain button:🉐
Japanese discount button:🈹
Japanese free of charge button:🈚
Japanese prohibited button:🈲
Japanese acceptable button:🉑
Japanese application button:🈸
Japanese passing grade button:🈴
Japanese vacancy button:🈳
Japanese congratulations button:㊗ ㊗️
Japanese secret button:㊙ ㊙️
Japanese open for business button:🈺
Japanese no vacancy button:🈵
red circle:🔴
orange circle:🟠
yellow circle:🟡
green circle:🟢
blue circle:🔵
purple circle:🟣
brown circle:🟤
black circle:⚫
white circle:⚪
red square:🟥
orange square:🟧
yellow square:🟨
green square:🟩
blue square:🟦
purple square:🟪
brown square:🟫
black large square:⬛
white large square:⬜
black medium square:◼ ◼️
white medium square:◻ ◻️
black medium small square:◾
white medium small square:◽
black small square:▪ ▪️
white small square:▫ ▫️
large orange diamond:🔶
large blue diamond:🔷
small orange diamond:🔸
small blue diamond:🔹
red triangle pointed up:🔺
red triangle pointed down:🔻
diamond with a dot:💠
radio button:🔘
white square button:🔳
black square button:🔲

chequered flag:🏁
triangular flag:🚩
crossed flags:🎌
black flag:🏴
white flag:🏳🏳️
rainbow flag:🏳‍🌈🏳️‍🌈
transgender flag:🏳‍⚧️ 🏳️‍⚧
pirate flag:🏴‍☠ 🏴‍☠️
ascension island flag:🇦🇨
andorra flag:🇦🇩
united arab emirates flag:🇦🇪
afghanistan flag:🇦🇫
antigua and barbuda flag:🇦🇬
anguilla flag:🇦🇮
albania flag:🇦🇱
armenia flag:🇦🇲
angola flag:🇦🇴
antarctica flag:🇦🇶
argentina flag:🇦🇷
american samoa flag:🇦🇸
austria flag:🇦🇹
australia flag:🇦🇺
aruba flag:🇦🇼
åland islands flag:🇦🇽
azerbaijan flag:🇦🇿
bosnia and herzegovina flag:🇧🇦
barbados flag:🇧🇧
bangladesh flag:🇧🇩
belgium flag:🇧🇪
burkina faso flag:🇧🇫
bulgaria flag:🇧🇬
bahrain flag:🇧🇭
burundi flag:🇧🇮
benin flag:🇧🇯
saint barthelemy flag:🇧🇱
bermuda flag:🇧🇲
brunei flag:🇧🇳
bolivia flag:🇧🇴
caribbean netherlands flag:🇧🇶
brazil flag:🇧🇷
bahamas flag:🇧🇸
bhutan flag:🇧🇹
bouvet island flag:🇧🇻
botswana flag:🇧🇼
belarus flag:🇧🇾
belize flag:🇧🇿
canada flag:🇨🇦
cocos islands flag:🇨🇨
congo kinshasa flag:🇨🇩
central african republic flag:🇨🇫
congo brazzaville flag:🇨🇬
switzerland flag:🇨🇭
cote d ivoire flag:🇨🇮
cook islands flag:🇨🇰
chile flag:🇨🇱
cameroon flag:🇨🇲
china flag:🇨🇳
colombia flag:🇨🇴
clipperton island flag:🇨🇵
sark flag:🇨🇶
costa rica flag:🇨🇷
cuba flag:🇨🇺
cape verde flag:🇨🇻
curacao flag:🇨🇼
christmas island flag:🇨🇽
cyprus flag:🇨🇾
czechia flag:🇨🇿
germany flag:🇩🇪
diego garcia flag:🇩🇬
djibouti flag:🇩🇯
denmark flag:🇩🇰
dominica flag:🇩🇲
dominican republic flag:🇩🇴
algeria flag:🇩🇿
ceuta and melilla flag:🇪🇦
ecuador flag:🇪🇨
estonia flag:🇪🇪
egypt flag:🇪🇬
western sahara flag:🇪🇭
eritrea flag:🇪🇷
spain flag:🇪🇸
ethiopia flag:🇪🇹
european union flag:🇪🇺
finland flag:🇫🇮
fiji flag:🇫🇯
falkland islands flag:🇫🇰
micronesia flag:🇫🇲
faroe islands flag:🇫🇴
france flag:🇫🇷
gabon flag:🇬🇦
united kingdom flag:🇬🇧
grenada flag:🇬🇩
georgia flag:🇬🇪
french guiana flag:🇬🇫
guernsey flag:🇬🇬
ghana flag:🇬🇭
gibraltar flag:🇬🇮
greenland flag:🇬🇱
gambia flag:🇬🇲
guinea flag:🇬🇳
guadeloupe flag:🇬🇵
equatorial guinea flag:🇬🇶
greece flag:🇬🇷
south georgia flag:🇬🇸
guatemala flag:🇬🇹
guam flag:🇬🇺
guinea bissau flag:🇬🇼
guyana flag:🇬🇾
hong kong flag:🇭🇰
heard and mcdonald islands flag:🇭🇲
honduras flag:🇭🇳
croatia flag:🇭🇷
haiti flag:🇭🇹
hungary flag:🇭🇺
canary islands flag:🇮🇨
indonesia flag:🇮🇩
ireland flag:🇮🇪
israel flag:🇮🇱
isle of man flag:🇮🇲
india flag:🇮🇳
british indian ocean territory flag:🇮🇴
iraq flag:🇮🇶
iran flag:🇮🇷
iceland flag:🇮🇸
italy flag:🇮🇹
jersey flag:🇯🇪
jamaica flag:🇯🇲
jordan flag:🇯🇴
japan flag:🇯🇵
kenya flag:🇰🇪
kyrgyzstan flag:🇰🇬
cambodia flag:🇰🇭
kiribati flag:🇰🇮
comoros flag:🇰🇲
saint kitts and nevis flag:🇰🇳
north korea flag:🇰🇵
south korea flag:🇰🇷
kuwait flag:🇰🇼
cayman islands flag:🇰🇾
kazakhstan flag:🇰🇿
laos flag:🇱🇦
lebanon flag:🇱🇧
saint lucia flag:🇱🇨
liechtenstein flag:🇱🇮
sri lanka flag:🇱🇰
liberia flag:🇱🇷
lesotho flag:🇱🇸
lithuania flag:🇱🇹
luxembourg flag:🇱🇺
latvia flag:🇱🇻
libya flag:🇱🇾
morocco flag:🇲🇦
monaco flag:🇲🇨
moldova flag:🇲🇩
montenegro flag:🇲🇪
saint martin flag:🇲🇫
madagascar flag:🇲🇬
marshall islands flag:🇲🇭
north macedonia flag:🇲🇰
mali flag:🇲🇱
myanmar flag:🇲🇲
mongolia flag:🇲🇳
macao flag:🇲🇴
northern mariana islands flag:🇲🇵
martinique flag:🇲🇶
mauritania flag:🇲🇷
montserrat flag:🇲🇸
malta flag:🇲🇹
mauritius flag:🇲🇺
maldives flag:🇲🇻
malawi flag:🇲🇼
mexico flag:🇲🇽
malaysia flag:🇲🇾
mozambique flag:🇲🇿
namibia flag:🇳🇦
new caledonia flag:🇳🇨
niger flag:🇳🇪
norfolk island flag:🇳🇫
nigeria flag:🇳🇬
nicaragua flag:🇳🇮
netherlands flag:🇳🇱
norway flag:🇳🇴
nepal flag:🇳🇵
nauru flag:🇳🇷
niue flag:🇳🇺
new zealand flag:🇳🇿
oman flag:🇴🇲
panama flag:🇵🇦
peru flag:🇵🇪
french polynesia flag:🇵🇫
papua new guinea flag:🇵🇬
philippines flag:🇵🇭
pakistan flag:🇵🇰
poland flag:🇵🇱
saint pierre and miquelon flag:🇵🇲
pitcairn islands flag:🇵🇳
puerto rico flag:🇵🇷
palestinian territories flag:🇵🇸
portugal flag:🇵🇹
palau flag:🇵🇼
paraguay flag:🇵🇾
qatar flag:🇶🇦
reunion flag:🇷🇪
romania flag:🇷🇴
serbia flag:🇷🇸
rwanda flag:🇷🇼
saudi arabia flag:🇸🇦
solomon islands flag:🇸🇧
seychelles flag:🇸🇨
sudan flag:🇸🇩
sweden flag:🇸🇪
singapore flag:🇸🇬
saint helena flag:🇸🇭
slovenia flag:🇸🇮
svalbard and jan mayen flag:🇸🇯
slovakia flag:🇸🇰
sierra leone flag:🇸🇱
san marino flag:🇸🇲
senegal flag:🇸🇳
somalia flag:🇸🇴
suriname flag:🇸🇷
south sudan flag:🇸🇸
sao tome and principe flag:🇸🇹
el salvador flag:🇸🇻
sint maarten flag:🇸🇽
syria flag:🇸🇾
eswatini flag:🇸🇿
tristan da cunha flag:🇹🇦
turks and caicos islands flag:🇹🇨
chad flag:🇹🇩
french southern territories flag:🇹🇫
togo flag:🇹🇬
thailand flag:🇹🇭
tajikistan flag:🇹🇯
tokelau flag:🇹🇰
timor leste flag:🇹🇱
turkmenistan flag:🇹🇲
tunisia flag:🇹🇳
tonga flag:🇹🇴
turkey flag:🇹🇷
trinidad and tobago flag:🇹🇹
tuvalu flag:🇹🇻
taiwan flag:🇹🇼
tanzania flag:🇹🇿
ukraine flag:🇺🇦
uganda flag:🇺🇬
u s outlying islands flag:🇺🇲
united nations flag:🇺🇳
united states flag:🇺🇸
uruguay flag:🇺🇾
uzbekistan flag:🇺🇿
vatican city flag:🇻🇦
saint vincent and grenadines flag:🇻🇨
venezuela flag:🇻🇪
british virgin islands flag:🇻🇬
u s virgin islands flag:🇻🇮
vietnam flag:🇻🇳
vanuatu flag:🇻🇺
wallis and futuna flag:🇼🇫
samoa flag:🇼🇸
kosovo flag:🇽🇰
yemen flag:🇾🇪
mayotte flag:🇾🇹
south africa flag:🇿🇦
zambia flag:🇿🇲
zimbabwe flag:🇿🇼
england flag:🏴󠁧󠁢󠁥󠁮󠁧󠁿
scotland flag:🏴󠁧󠁢󠁳󠁣󠁴󠁿
wales flag:🏴󠁧󠁢󠁷󠁬󠁳󠁿""".split("\n\n").map { section ->
        section.lines().groupBy {
            it.substringBefore(":")
        }.mapValues { (_, lines) ->
            lines.map { line ->
                val v = line.substringAfter(":")
                if (!v.contains(" ")) {
                    KeyData(
                        KeyLabel(v),
                        KeyActions(listOf(KeyAction(KeyActionType.STRING, v))),
                        0f, 0f, 1/9f, 1/9f
                    )
                } else {
                    var e = v.split(" ")
                    if (e.size == 2) {
                        e = e.reversed()
                    }
                    KeyData(
                        KeyLabel(e[0], "* "),
                        KeyActions(listOf(KeyAction(KeyActionType.STRING, e[0]))),
                        0f, 0f, 1/9f, 1/9f,
                        longPress = e.drop(1)
                            .map { KeyActions(
                                listOf(KeyAction(KeyActionType.STRING, it)),
                                KeyLabel(it)
                            ) }
                    )
                }
            }.reduce { acc: KeyData, curr: KeyData ->
                KeyData(
                    acc.label,
                    KeyActions(acc.action!!.actions + curr.action!!.actions),
                    0f, 0f, 1/9f, 1/9f,
                    longPress = acc.longPress + curr.longPress
                )
            }
        }
    }
}