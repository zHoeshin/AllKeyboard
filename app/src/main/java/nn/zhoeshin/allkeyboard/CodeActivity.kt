package nn.zhoeshin.allkeyboard

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nn.zhoeshin.allkeyboard.databinding.ActivityCodeBinding
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.AlternativesFailure
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.MismatchedToken
import com.github.h0tk3y.betterParse.parser.NoMatchingToken
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.UnexpectedEof
import com.github.h0tk3y.betterParse.parser.UnparsedRemainder
import com.google.gson.Gson
import java.io.File
import androidx.activity.OnBackPressedCallback

class CodeActivity: AppCompatActivity() {
    lateinit var binding: ActivityCodeBinding
    lateinit var prefs: SharedPreferences

    lateinit var code: EditText

    lateinit var layoutName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        layoutName = intent.getStringExtra("FILE") ?: ""
        if (layoutName == "") {
            Toast.makeText(applicationContext, "No special layout file from intent", Toast.LENGTH_LONG).show()
            super.onSupportNavigateUp()
        }

        prefs = this.getSharedPreferences("special_layouts", MODE_PRIVATE)

        code = findViewById(R.id.editor)
        code?.setText(prefs.getString(layoutName, ""))

        findViewById<Toolbar>(R.id.toolbar)?.setTitle(intent.getStringExtra("LABEL") ?: "?!")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                save()
                onSupportNavigateUp()
            }
        })
    }

    fun save() {
        prefs.edit{ putString(layoutName, code.text.toString()) }

        val destFile = File(this.filesDir, "special_layouts/$layoutName.json")
        destFile.parentFile?.mkdirs()
        if (destFile.isDirectory) {
            destFile.deleteRecursively()
        }
        try {
            val content = code.text.toString()

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
            val json = Gson().toJson(layout)
            destFile.writeText(json)
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is IllegalArgumentException -> e.message ?: "Parsing error"
                else -> "Failed to save layout: ${e.message}"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Layout import error", errorMessage)
            clipboard.setPrimaryClip(clip)
            e.printStackTrace()
            if (!destFile.exists()) {
                destFile.writeText("{}")
            }
        }

        if (layoutName == SpecialLayout.CLIPBOARD.layoutName) {
            AllKeyboardService.instance?.clipboardView?.buildLayouts()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        save()
        finish()
        return super.onSupportNavigateUp()
    }
}