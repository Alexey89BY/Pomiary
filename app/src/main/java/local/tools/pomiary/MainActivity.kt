package local.tools.pomiary

import android.graphics.Color
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import local.tools.pomiary.ui.main.SectionsPagerAdapter
import local.tools.pomiary.databinding.ActivityMainBinding
import local.tools.pomiary.ui.main.SettingsFragment
import kotlin.math.absoluteValue

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
    }

    companion object {
        @JvmStatic
        fun alignPoints(view: View, tolerancesRaw: Array<Pair<Float,Float>>, editsPointIds: Array<Int>, textsResultIds: Array<Int>, textsNokIds: Array<Int>) {

            val pointsRaw = FloatArray(editsPointIds.size)
            editsPointIds.forEachIndexed { index, editId ->
                val editText = view.findViewById<EditText>(editId)
                val textFloat = editText.text.toString()
                val floatValue = textFloat.toFloatOrNull()
                pointsRaw[index] = floatValue ?: tolerancesRaw[index].first
            }

            val points = FloatArray(pointsRaw.size)
            val pointZero = pointsRaw[0]
            pointsRaw.forEachIndexed { index, pointRaw ->
                points[index] = (pointRaw - pointZero).absoluteValue
            }

            var shiftsSum = 0.0F
            for (index in 1 until points.size-1)
            {
                val pointShift = tolerancesRaw[index].first - points[index]
                shiftsSum += pointShift
            }

            val totalShift = shiftsSum / (points.size - 2)
            points.forEachIndexed { index, point ->
                points[index] = point + totalShift
            }

            points.forEachIndexed { index, pointValue ->
                val toleranceMin = tolerancesRaw[index].first-tolerancesRaw[index].second
                val toleranceMax = tolerancesRaw[index].first+tolerancesRaw[index].second

                val textNok = view.findViewById<TextView>(textsNokIds[index])
                if ((pointValue < toleranceMin) or (toleranceMax < pointValue)) {
                    textNok.text = view.resources.getString(R.string.result_Nok)
                    textNok.setTextColor(Color.RED)
                } else {
                    textNok.text = view.resources.getString(R.string.result_Ok)
                    textNok.setTextColor(Color.GREEN)
                }

                val textView = view.findViewById<TextView>(textsResultIds[index])
                textView.text = buildString { append(" %.1f mm (%.1f - %.1f)") }.format(
                    pointValue,
                    toleranceMin,
                    toleranceMax,
                )
            }
        }

    }
}