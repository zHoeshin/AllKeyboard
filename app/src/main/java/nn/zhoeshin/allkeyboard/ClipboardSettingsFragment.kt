package nn.zhoeshin.allkeyboard

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment

class ClipboardSettingsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clipboard_settings, container, false);

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }
        
        val prefs = requireContext().getSharedPreferences("clipboard", MODE_PRIVATE)

//        if (!prefs.contains("max")) {
//            prefs.edit().putInt("max", 40)
//        }
        val maxHistoryEdit = view?.findViewById<EditText>(R.id.maxItems)
        maxHistoryEdit?.setText(prefs.getInt("max", 40).toString())
        maxHistoryEdit?.doAfterTextChanged{ editable ->
            val text = editable.toString()
            if (text.isNotEmpty()) {
                val number = text.toIntOrNull()
                if (number != null) {
                    prefs.edit{ putInt("max", number) }
                    AllKeyboardService.instance?.clipboardView?.MAXCLIPBOARDITEMS = number
                } else {
                    maxHistoryEdit.setText(prefs.getInt("max", 40).toString())
                }
            } else {
                //maxHistoryEdit.setText(prefs.getInt("max", 40).toString())
            }
        }

//        if (!prefs.contains("removedupes")) {
//            prefs.edit().putBoolean("removedupes", false)
//        }
        val removeDupesEdit = view?.findViewById<Switch>(R.id.removeDupes)
        removeDupesEdit?.isChecked = prefs.getBoolean("removedupes", false)
        removeDupesEdit?.setOnCheckedChangeListener { buttonView, isChecked ->
            prefs.edit{ putBoolean("removedupes", isChecked) }
            AllKeyboardService.instance?.clipboardView?.REMOVEALLDUPLICATES = isChecked
        }

        return view
    }
}