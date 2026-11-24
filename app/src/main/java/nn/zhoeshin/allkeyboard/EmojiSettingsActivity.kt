package nn.zhoeshin.allkeyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nn.zhoeshin.allkeyboard.databinding.ActivityClipboardSettingsBinding
import nn.zhoeshin.allkeyboard.databinding.ActivityEmojiSettingsBinding

class EmojiSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmojiSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmojiSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EmojiSettingsFragment())
                .commit()
        }
    }
}