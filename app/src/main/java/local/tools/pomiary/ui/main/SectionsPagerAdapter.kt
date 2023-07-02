package local.tools.pomiary.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import local.tools.pomiary.R

/**
 * A [FragmentStateAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val fa: FragmentActivity) :
    FragmentStateAdapter(fa) {

    private val sectionsList: MutableList<Pair<Fragment,String>> = ArrayList()

    fun createTabs() {
        val tabTitles = arrayOf(
            R.string.tab_std_lh,
            R.string.tab_std_rh,
            R.string.tab_max_lh,
            R.string.tab_max_rh,
            R.string.tab_history,
            R.string.tab_settings
        )

        tabTitles.forEachIndexed() {index, id ->
            val title = fa.resources.getString(id)
            val fragment: Fragment = when (index) {
                0, 1 -> StandardFragment.newInstance(title)
                2, 3 -> MaxiFragment.newInstance(title)
                4 -> HistoryFragment.newInstance()
                else -> SettingsFragment.newInstance()
            }
            sectionsList.add(Pair(fragment,title))
        }
    }

    fun getTabTitle(position: Int): CharSequence {
        return sectionsList[position].second
    }

    override fun getItemCount(): Int {
        return sectionsList.size
    }

    override fun createFragment(position: Int): Fragment {
        return sectionsList[position].first
    }
}