package nn.zhoeshin.allkeyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nn.zhoeshin.allkeyboard.databinding.ActivityLayoutsListBinding

class LayoutsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLayoutsListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLayoutsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LayoutListFragment())
                .commit()
        }
    }
}