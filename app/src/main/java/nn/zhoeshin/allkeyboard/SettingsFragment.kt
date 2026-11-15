package nn.zhoeshin.allkeyboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class SettingsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        view?.findViewById<Button>(R.id.btn_layouts)?.setOnClickListener {
            val intent = Intent(requireContext(), LayoutsListActivity::class.java)
            startActivity(intent)
        }
        view?.findViewById<Button>(R.id.btn_clipboard)?.setOnClickListener {
            val intent = Intent(requireContext(), ClipboardSettingsActivity::class.java)
            startActivity(intent)
        }
        view?.findViewById<Button>(R.id.btn_emoji)?.setOnClickListener {
            val intent = Intent(requireContext(), EmojiSettingsActivity::class.java)
            startActivity(intent)
        }
        view?.findViewById<Button>(R.id.btn_timers)?.setOnClickListener {
            val intent = Intent(requireContext(), TimersSettingsActivity::class.java)
            startActivity(intent)
        }
        view?.findViewById<Button>(R.id.btn_special_layouts)?.setOnClickListener {
            val intent = Intent(requireContext(), SpecialLayoutsActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}