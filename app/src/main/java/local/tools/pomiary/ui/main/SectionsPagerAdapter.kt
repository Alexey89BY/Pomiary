package local.tools.pomiary.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import local.tools.pomiary.R

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    companion object {
        private val TAB_TITLES = arrayOf(
            R.string.tab_std_lh,
            R.string.tab_std_rh,
            R.string.tab_max_lh,
            R.string.tab_max_rh,
            R.string.tab_settings
        )
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }

    override fun getItem(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0, 1 -> StandardFragment.newInstance()
            2, 3 -> MaxiFragment.newInstance()
            else -> SettingsFragment.newInstance("")
        }
        return fragment
    }
}