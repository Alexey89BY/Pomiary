package local.tools.pomiary

import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.TextView
import local.tools.pomiary.ui.main.SettingsFragment
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class PointsAligner {
    enum class PointResult {
        OK,
        TOLERANCE,
        CRITICAL,
        CRITICAL_LIMITED,
    }

    data class Point (
        val value: Float,
        val result: PointResult
    )

    companion object {
        private fun getPointsFromEditText(
            view: View,
            editsPointIds: Array<Int>,
            tolerancesRaw: Array<Pair<Float, Float>>,
        ): Array<Float> {
            val pointsRaw = Array(editsPointIds.size) { index ->
                val editText = view.findViewById<EditText>(editsPointIds[index])
                val textFloat = editText.text.toString()
                val floatValue = textFloat.toFloatOrNull()
                floatValue ?: tolerancesRaw[index].first
            }
            return pointsRaw
        }


        private fun shiftAndCheckPoints(
            pointsRaw: Array<Float>,
            toleranceNok: Float,
            tolerancesRaw: Array<Pair<Float, Float>>,
        ): Array<Point> {
            val pointZero = pointsRaw[0]
            val pointFactor = 10.0
            val points = List(pointsRaw.size) { index ->
                ((pointsRaw[index] - pointZero).absoluteValue * pointFactor).roundToInt() / pointFactor
            }

            var shiftUpMin = 0.0
            var shiftUpIndex = -1
            var shiftDownMax = 0.0
            var shiftDownIndex = -1
            points.forEachIndexed { index, pointValue ->
                val shiftDown =
                    tolerancesRaw[index].first - tolerancesRaw[index].second - pointValue
                if ((shiftDown > shiftDownMax) or (shiftDownIndex < 0)) {
                    shiftDownMax = shiftDown
                    shiftDownIndex = index
                }

                val shiftUp = tolerancesRaw[index].first + tolerancesRaw[index].second - pointValue
                if ((shiftUp < shiftUpMin) or (shiftUpIndex < 0)) {
                    shiftUpMin = shiftUp
                    shiftUpIndex = index
                }
            }

            var totalShift = 0.0
            if ((shiftDownMax > 0) and (shiftUpMin < 0)) {
                totalShift = (shiftUpMin + shiftDownMax) / 2
            } else if (shiftDownMax > 0) {
                totalShift = min(shiftDownMax, shiftUpMin)
            } else if (shiftUpMin < 0) {
                totalShift = max(shiftUpMin, shiftDownMax)
            }

            val pointsShifted = Array(points.size) { index ->
                val pointValue = points[index] + totalShift
                val pointError =
                    (pointValue - tolerancesRaw[index].first).absoluteValue - tolerancesRaw[index].second
                var pointResult = when {
                    (pointError > toleranceNok) -> PointResult.CRITICAL
                    (pointError > 0.0F) -> PointResult.TOLERANCE
                    else -> PointResult.OK
                }

                if ((pointResult == PointResult.CRITICAL) and ((index == shiftDownIndex) or (index == shiftUpIndex))) {
                    pointResult = PointResult.CRITICAL_LIMITED
                }

                Point(pointValue.toFloat(), pointResult)
            }

            return pointsShifted
        }


        private fun setPointResultsToView (
            view: View,
            points: Array<Point>,
            tolerancesRaw: Array<Pair<Float, Float>>,
            textsResultIds: Array<Int>,
            textsNokIds: Array<Int>
        ) {
            val pointsColors = List(points.size) {
                index ->
                when (points[index].result) {
                    PointResult.OK -> Color.GREEN
                    PointResult.TOLERANCE -> Color.YELLOW
                    else -> Color.RED
                }
            }

            points.forEachIndexed { index, point ->
                val textView = view.findViewById<TextView>(textsResultIds[index])
                textView.setTextColor(pointsColors[index])
                //textView.setBackgroundColor(Color.BLACK)
                textView.text = buildString { append(" %.1f ") }.format(
                    point.value
                )
            }

            val wrongText: String = view.resources.getString(R.string.result_Nok)
            val goodText: String = view.resources.getString(R.string.result_Ok)
            points.forEachIndexed { index, point ->
                val textView = view.findViewById<TextView>(textsNokIds[index])
                textView.setTextColor(pointsColors[index])
                //textView.setBackgroundColor(Color.BLACK)
                textView.text = buildString { append(" %s (%.1f - %.1f)") }.format(
                    when (point.result) {
                        PointResult.OK -> goodText
                        PointResult.CRITICAL_LIMITED -> "$wrongText*"
                        else -> wrongText
                    },
                    tolerancesRaw[index].first - tolerancesRaw[index].second,
                    tolerancesRaw[index].first + tolerancesRaw[index].second
                )
            }
        }


        fun alignPoints(
            view: View,
            tolerancesRaw: Array<Pair<Float, Float>>,
            editsPointIds: Array<Int>,
            textsResultIds: Array<Int>,
            textsNokIds: Array<Int>
        ): Array<Point> {
            val pointsRaw = getPointsFromEditText(view, editsPointIds, tolerancesRaw)
            val toleranceNok = SettingsFragment.getToleranceNok()
            val points = shiftAndCheckPoints(pointsRaw, toleranceNok, tolerancesRaw)
            setPointResultsToView(view, points, tolerancesRaw, textsResultIds, textsNokIds)
            return points
        }
    }
}