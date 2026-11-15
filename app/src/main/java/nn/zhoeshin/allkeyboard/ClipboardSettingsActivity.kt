package nn.zhoeshin.allkeyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nn.zhoeshin.allkeyboard.databinding.ActivityClipboardSettingsBinding

class ClipboardSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClipboardSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClipboardSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ClipboardSettingsFragment())
                .commit()
        }
    }
}