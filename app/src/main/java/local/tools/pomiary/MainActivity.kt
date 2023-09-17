package local.tools.pomiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        pageAdapter = SectionsPagerAdapter(this)
        val viewPager = binding.viewPager
        viewPager.adapter = pageAdapter
        viewPager.offscreenPageLimit = pageAdapter.itemCount
        val tabs: TabLayout = binding.tabs
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = pageAdapter.getTabTitle(position)
        }.attach()
    }


    override fun onStart() {
        super.onStart()
        SettingsFragment.loadSettings(this)
        HistoryFragment.checkHistory(this)
        DataStorage.loadData(this)
    }

    //override fun onResume() {
    //    super.onResume()
    //}

    //override fun onPause() {
    //    super.onPause()
    //}

    override fun onStop() {
        super.onStop()
        DataStorage.saveData(this)
    }

    companion object {

        private lateinit var pageAdapter: SectionsPagerAdapter

        fun broadcastSettingsChange() {
            pageAdapter.broadcastSettingsChange()
            DataStorage.broadcastSettingsChange()
        }
    }
}