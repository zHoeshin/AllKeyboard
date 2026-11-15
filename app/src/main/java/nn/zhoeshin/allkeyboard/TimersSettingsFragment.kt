package nn.zhoeshin.allkeyboard

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment

class TimersSettingsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timers_settings, container, false);

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        val prefs = requireContext().getSharedPreferences("timers", MODE_PRIVATE)

        val longPress = view?.findViewById<EditText>(R.id.longPress)
        longPress?.setText(prefs.getInt("longPress", 150).toString())
        longPress?.doAfterTextChanged{ editable ->
            val text = editable.toString()
            if (text.isNotEmpty()) {
                val number = text.toIntOrNull()
                if (number != null) {
                    prefs.edit{ putInt("longPress", number) }
                    AllKeyboardService.instance?.delayLongPress = number
                } else {
                    longPress.setText(prefs.getInt("longPress", 150).toString())
                }
            }
        }
        val repeat = view?.findViewById<EditText>(R.id.repeat)
        repeat?.setText(prefs.getInt("repeat", 75).toString())
        repeat?.doAfterTextChanged{ editable ->
            val text = editable.toString()
            if (text.isNotEmpty()) {
                val number = text.toIntOrNull()
                if (number != null) {
                    prefs.edit{ putInt("repeat", number) }
                    AllKeyboardService.instance?.delayRepeat = number
                } else {
                    repeat.setText(prefs.getInt("repeat", 75).toString())
                }
            }
        }
        val decay = view?.findViewById<EditText>(R.id.decay)
        decay?.setText(prefs.getInt("popupDecay", 75).toString())
        decay?.doAfterTextChanged{ editable ->
            val text = editable.toString()
            if (text.isNotEmpty()) {
                val number = text.toIntOrNull()
                if (number != null) {
                    prefs.edit{ putInt("popupDecay", number) }
                    AllKeyboardService.instance?.delayRepeat = number
                } else {
                    decay.setText(prefs.getInt("popupDecay", 75).toString())
                }
            }
        }

        return view
    }

}