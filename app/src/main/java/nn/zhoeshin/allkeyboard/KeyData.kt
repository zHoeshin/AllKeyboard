package nn.zhoeshin.allkeyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NONE
import android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED
import android.view.inputmethod.ExtractedTextRequest
import kotlin.text.isNullOrEmpty

interface KeyboardContainer {
    var layouts: MutableList<MutableMap<String, View>>
    fun switchToLayout(newLayout: Int)
    fun switchToState(newState: String)
    var currentLayout: Int
    var currentState: String
    var returnState: String?
    var error: String
}

enum class KeyActionType {
    STRING, MAGIC, TRANSITIONTEMP, TRANSITIONPERM,
}

data class KeyActions (
    var actions: List<KeyAction>,
    var label: KeyLabel = KeyLabel(""),
    var flags: KeyActionFlags = KeyActionFlags(),
) {
    fun firstString(): String {
        var s = ""
        for (a in actions) {
            if (a.type == KeyActionType.STRING) {
                s = a.value
                break
            }
        }
        return s
    }
    fun isEmpty(): Boolean = actions.isEmpty()

    fun doActions(container: KeyboardContainer, button: View? = null, canSwitch: Boolean = true): String? {
        val ic = AllKeyboardService.instance?.currentInputConnection ?: return null

        var s: String? = null

        var switching = false
        for (action in actions) {
            when (action.type) {
                KeyActionType.STRING -> {
                    ic.commitText(action.value, 1)
                    s = action.value
                }

                KeyActionType.MAGIC -> {
                    when (action.value) {
                        "return" -> {
                            val editorInfo = AllKeyboardService.instance?.currentInputEditorInfo ?: return null
                            val actionId = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
                            val noEnterAction =
                                (editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
                            val isMultiLine =
                                (editorInfo.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0

                            if (noEnterAction || (isMultiLine && (actionId == IME_ACTION_UNSPECIFIED || actionId == IME_ACTION_NONE))) {
                                ic.commitText("\n", 1)
                            } else {
                                ic.performEditorAction(actionId)
                            }
                        }

                        "delete1" -> {
                            if (!ic.getSelectedText(0).isNullOrEmpty()) {
                                ic.commitText("", 1)
                            } else {
                                ic.deleteSurroundingText(1, 0)
                            }
                        }

                        "delete" -> {
                            if (!ic.getSelectedText(0).isNullOrEmpty()) {
                                ic.commitText("", 1)
                            } else {
                                val before = ic.getTextBeforeCursor(64, 0)?.toString() ?: ""
                                val bi = java.text.BreakIterator.getCharacterInstance()
                                bi.setText(before)
                                bi.last()
                                ic.deleteSurroundingText(before.length - bi.previous(), 0)
                            }
                        }

                        "start" -> {
                            ic.setSelection(0, 0)
                        }

                        "end" -> {
                            val extracted = ic.getExtractedText(ExtractedTextRequest(), 0)
                            if (extracted != null && extracted.text != null) {
                                val endIndex = extracted.startOffset + extracted.text.length
                                ic.setSelection(endIndex, endIndex)
                            }
                        }

                        "lineend" -> {
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END))
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP,   KeyEvent.KEYCODE_MOVE_END))
                        }

                        "linestart" -> {
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME))
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP,   KeyEvent.KEYCODE_MOVE_HOME))
                        }

                        "layoutprev" -> {
                            container.switchToLayout((container.currentLayout - 1).mod(container.layouts.size))
                        }

                        "layoutnext" -> {
                            container.switchToLayout((container.currentLayout + 1).mod(container.layouts.size))
                        }

                        "error" -> {
                            ic.commitText(container.error, 1)
                        }

                        "quit" -> {
                            AllKeyboardService.instance?.requestHideSelf(0)
                        }

                        "left" -> {
                            ic.sendKeyEvent(
                                KeyEvent(
                                    KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_DPAD_LEFT
                                )
                            )
                            ic.sendKeyEvent(
                                KeyEvent(
                                    KeyEvent.ACTION_UP,
                                    KeyEvent.KEYCODE_DPAD_LEFT
                                )
                            )
                        }

                        "right" -> {
                            ic.sendKeyEvent(
                                KeyEvent(
                                    KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_DPAD_RIGHT
                                )
                            )
                            ic.sendKeyEvent(
                                KeyEvent(
                                    KeyEvent.ACTION_UP,
                                    KeyEvent.KEYCODE_DPAD_RIGHT
                                )
                            )
                        }

                        "up" -> {
                            ic.sendKeyEvent(
                                KeyEvent(
                                    KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_DPAD_UP
                                )
                            )
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP))
                        }

                        "down" -> {
                            ic.sendKeyEvent(
                                KeyEvent(
                                    KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_DPAD_DOWN
                                )
                            )
                            ic.sendKeyEvent(
                                KeyEvent(
                                    KeyEvent.ACTION_UP,
                                    KeyEvent.KEYCODE_DPAD_DOWN
                                )
                            )
                        }

                        "cut" -> {
                            val selected = ic.getSelectedText(0) ?: return null
                            val clip = ClipData.newPlainText("text", selected)
                            AllKeyboardService.instance?.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(clip)
                            ic.commitText("", 1)
                        }

                        "copy" -> {
                            val selected = ic.getSelectedText(0) ?: return null
                            val clip = ClipData.newPlainText("text", selected)
                            AllKeyboardService.instance?.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(clip)
                        }

                        "paste" -> {
                            val clipboard = AllKeyboardService.instance?.getSystemService(ClipboardManager::class.java)
                            val clip = clipboard?.primaryClip ?: return null
                            val text = clip.getItemAt(0).text ?: return null
                            AllKeyboardService.instance?.currentInputConnection?.commitText(text, 1)
                        }

                        "undo" -> {
                            AllKeyboardService.instance?.currentInputConnection?.performContextMenuAction(android.R.id.undo)
                        }

                        "redo" -> {
                            AllKeyboardService.instance?.currentInputConnection?.performContextMenuAction(android.R.id.redo)
                        }

                        "selectall" -> {
                            val ic = AllKeyboardService.instance?.currentInputConnection ?: return null
                            val before = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)?.length ?: 0
                            val after = ic.getTextAfterCursor(Int.MAX_VALUE, 0)?.length ?: 0
                            ic.setSelection(0, before + after)
                        }
                    }
                }

                KeyActionType.TRANSITIONPERM -> {
                    container.returnState = null
                    switching = true
                    container.switchToState(action.value)
                }
                KeyActionType.TRANSITIONTEMP -> {
                    switching = true
                    if (container.returnState == null) {
                        container.returnState = container.currentState
                    }
                    container.switchToState(action.value)
                }
            }
        }

        if (canSwitch && !switching && (container.returnState != null)) {
            container.switchToState(container.returnState!!)
            container.returnState = null
        }
        return s
    }
}

fun KeyActions?.isNullOrEmpty(): Boolean = this == null || this.isEmpty()

data class KeyActionFlags (
    var immediate: Boolean = false,
    var repeated: Boolean = false,
    var cancelling: Boolean = false,
    var start: Boolean = false,
    var blockedSwitch: Boolean = false,
    var delayedSwitch: Boolean = false,
)
data class KeyAction(
    var type: KeyActionType,
    var value: String,
)
data class KeyLabel (
    var center: String,
    var corner: String = "",
    var left: String = "",
    var right: String = "",
    var up: String = "",
    var down: String = "",
)

data class KeyData(
    val label: KeyLabel,
    var action: KeyActions?,
    var x: Float, var y: Float,
    var width: Float, var height: Float,
    var longPress: List<KeyActions> = emptyList(),
    var swipeUp: KeyActions? = null,
    var swipeDown: KeyActions? = null,
    var swipeLeft: KeyActions? = null,
    var swipeRight: KeyActions? = null,

    var style: String? = null
) {
    operator fun get(d: String?): KeyActions? {
        return when (d) {
            "left" ->  swipeLeft
            "right" -> swipeRight
            "up" ->    swipeUp
            "down" ->  swipeDown
            "main" ->  action
            else ->    null
        }
    }
}