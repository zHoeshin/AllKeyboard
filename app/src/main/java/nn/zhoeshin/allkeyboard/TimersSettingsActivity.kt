package nn.zhoeshin.allkeyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nn.zhoeshin.allkeyboard.databinding.ActivityTimersSettingsBinding

class TimersSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimersSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTimersSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TimersSettingsFragment())
                .commit()
        }
    }
}