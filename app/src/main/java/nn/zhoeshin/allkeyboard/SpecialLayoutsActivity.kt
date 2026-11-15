package nn.zhoeshin.allkeyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nn.zhoeshin.allkeyboard.databinding.ActivitySpecialLayoutsBinding

class SpecialLayoutsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpecialLayoutsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySpecialLayoutsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SpecialLayoutsFragment())
                .commit()
        }
    }
}