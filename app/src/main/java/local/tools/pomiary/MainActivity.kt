package local.tools.pomiary

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import local.tools.pomiary.ui.main.SectionsPagerAdapter
import local.tools.pomiary.databinding.ActivityMainBinding
import local.tools.pomiary.ui.main.SettingsFragment
import local.tools.pomiary.ui.main.HistoryFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = SectionsPagerAdapter(this, supportFragmentManager)
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        SettingsFragment.loadSettings(this)
        HistoryFragment.checkHistory(this)
    }
}