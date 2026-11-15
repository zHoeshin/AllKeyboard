package nn.zhoeshin.allkeyboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

enum class SpecialLayout(val layoutName: String) {
    CLIPBOARD("clipboard")
}

class SpecialLayoutsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_special_layouts, container, false)

        view?.findViewById<Button>(R.id.btn_clipboardOverlay)?.setOnClickListener {
            val intent = Intent(requireContext(), CodeActivity::class.java).apply{
                putExtra("LABEL", "Clipboard Overlay")
                putExtra("FILE", SpecialLayout.CLIPBOARD.layoutName)
            }
            startActivity(intent)
        }

        return view
    }

}