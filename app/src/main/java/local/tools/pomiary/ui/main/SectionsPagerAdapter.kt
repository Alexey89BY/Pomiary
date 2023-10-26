package local.tools.pomiary.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import local.tools.pomiary.DataStorage
import local.tools.pomiary.R

/**
 * A [FragmentStateAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val fa: FragmentActivity) :
    FragmentStateAdapter(fa) {

    private var pageStandard: StandardFragment? = null
    private var pageMaxi: StandardFragment? = null

    private val pagesList = listOf(
        R.string.tab_std,
        R.string.tab_max,
        R.string.tab_graph,
        R.string.tab_history,
        R.string.tab_settings
    )

    fun getTabTitle(position: Int): CharSequence {
        return fa.resources.getString(pagesList[position])
    }

    override fun getItemCount(): Int {
        return pagesList.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                val fragment = StandardFragment.newInstance(DataStorage.getStorageStandard().title)
                pageStandard = fragment
                return fragment
            }
            1 -> {
                val fragment = StandardFragment.newInstance(DataStorage.getStorageMaxi().title)
                pageMaxi = fragment
                return fragment
            }
            2 -> return BlankFragment.newInstance()
            3 -> return HistoryFragment.newInstance()
            else -> return SettingsFragment.newInstance()
        }
    }

    fun broadcastSettingsChange() {
        pageStandard?.onSettingsChange()
        pageMaxi?.onSettingsChange()
    }
}