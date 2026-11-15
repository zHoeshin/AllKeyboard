package nn.zhoeshin.allkeyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nn.zhoeshin.allkeyboard.databinding.ActivityEmojiViewSettingsBinding

class EmojiViewSettingsActivity: AppCompatActivity() {
    private lateinit var binding: ActivityEmojiViewSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmojiViewSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EmojiViewSettingsFragment())
                .commit()
        }
    }
}