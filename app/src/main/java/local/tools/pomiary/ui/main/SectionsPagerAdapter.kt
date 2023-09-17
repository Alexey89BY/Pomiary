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

    private var pageIdStandardLH = -1
    private var pageIdStandardRH = -1
    private var pageIdMaxiLH = -1
    private var pageIdMaxiRH = -1

    private val pagesList = listOf(
        R.string.tab_std_lh,
        R.string.tab_std_rh,
        R.string.tab_max_lh,
        R.string.tab_max_rh,
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
                val fragment = StandardFragment.newInstance()
                fragment.attachToStorage(DataStorage.getStorageStandardLeft())
                pageIdStandardLH = fragment.id
                return fragment
            }
            1 -> {
                val fragment = StandardFragment.newInstance()
                fragment.attachToStorage(DataStorage.getStorageStandardRight())
                pageIdStandardRH = fragment.id
                return fragment
            }
            2 -> {
                val fragment = MaxiFragment.newInstance()
                fragment.attachToStorage(DataStorage.getStorageMaxiLeft())
                pageIdMaxiLH = fragment.id
                return fragment
            }
            3 -> {
                val fragment = MaxiFragment.newInstance()
                fragment.attachToStorage(DataStorage.getStorageMaxiRight())
                pageIdMaxiRH = fragment.id
                return fragment
            }
            4 -> return BlankFragment.newInstance()
            5 -> return HistoryFragment.newInstance()
            else -> return SettingsFragment.newInstance()
        }
    }

    fun broadcastSettingsChange() {
        val pageStandardLH = fa.supportFragmentManager.findFragmentById(pageIdStandardLH)
        (pageStandardLH as? StandardFragment)?.onSettingsChange()

        val pageStandardRH = fa.supportFragmentManager.findFragmentById(pageIdStandardRH)
        (pageStandardRH as? StandardFragment)?.onSettingsChange()

        val pageMaxiLH = fa.supportFragmentManager.findFragmentById(pageIdMaxiLH)
        (pageMaxiLH as? MaxiFragment)?.onSettingsChange()

        val pageMaxiRH = fa.supportFragmentManager.findFragmentById(pageIdMaxiRH)
        (pageMaxiRH as? MaxiFragment)?.onSettingsChange()
    }
}