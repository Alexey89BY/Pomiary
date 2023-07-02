package local.tools.pomiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import local.tools.pomiary.databinding.ActivityMainBinding
import local.tools.pomiary.ui.main.HistoryFragment
import local.tools.pomiary.ui.main.SectionsPagerAdapter
import local.tools.pomiary.ui.main.SettingsFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = SectionsPagerAdapter(this)
        adapter.createTabs()
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = adapter
        val tabs: TabLayout = binding.tabs
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()

        SettingsFragment.loadSettings(this)
        HistoryFragment.checkHistory(this)
    }
}