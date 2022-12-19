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
import kotlin.math.max
import kotlin.math.min

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

            var shiftUpMin = 0.0F
            var shiftUpIndex = -1
            var shiftDownMax = 0.0F
            var shiftDownIndex = -1
            points.forEachIndexed { index, pointValue ->
                val shiftDown = tolerancesRaw[index].first - tolerancesRaw[index].second - pointValue
                if ((shiftDown > shiftDownMax) or (shiftDownIndex < 0))
                {
                    shiftDownMax = shiftDown
                    shiftDownIndex = index
                }

                val shiftUp = tolerancesRaw[index].first + tolerancesRaw[index].second - pointValue
                if ((shiftUp < shiftUpMin) or (shiftUpIndex < 0))
                {
                    shiftUpMin = shiftUp
                    shiftUpIndex = index
                }
            }

            var totalShift = 0.0F
            if ((shiftDownMax > 0) and (shiftUpMin < 0)) {
                totalShift = (shiftUpMin + shiftDownMax) / 2
            }
            else if (shiftDownMax > 0) {
                totalShift = min(shiftDownMax, shiftUpMin)
            }
            else if (shiftUpMin < 0) {
                totalShift = max(shiftUpMin, shiftDownMax)
            }

            points.forEachIndexed { index, pointValue ->
                points[index] = pointValue + totalShift
            }

            val pointsColors = IntArray(points.size)
            val toleranceNok = SettingsFragment.getToleranceNok()
            points.forEachIndexed { index, pointValue ->
                val pointError = (pointValue - tolerancesRaw[index].first).absoluteValue - tolerancesRaw[index].second
                pointsColors[index] = if (pointError > toleranceNok) {
                    Color.RED
                } else if (pointError > 0.0F) {
                    Color.YELLOW
                } else {
                    Color.GREEN
                }
            }

            points.forEachIndexed { index, pointValue ->
                val textView = view.findViewById<TextView>(textsResultIds[index])
                textView.setTextColor(pointsColors[index])
                //textView.setBackgroundColor(Color.BLACK)
                textView.text = buildString { append(" %.1f ") }.format(
                    pointValue
                )
            }

            val wrongText: String = view.resources.getString(R.string.result_Nok)
            val goodText: String = view.resources.getString(R.string.result_Ok)
            points.forEachIndexed { index, _ ->
                val textView = view.findViewById<TextView>(textsNokIds[index])
                textView.setTextColor(pointsColors[index])
                //textView.setBackgroundColor(Color.BLACK)
                textView.text = buildString { append(" %s (%.1f - %.1f)") }.format(
                    if (pointsColors[index] == Color.GREEN) goodText else wrongText,
                    tolerancesRaw[index].first - tolerancesRaw[index].second,
                    tolerancesRaw[index].first + tolerancesRaw[index].second
                )
            }
        }
    }
}