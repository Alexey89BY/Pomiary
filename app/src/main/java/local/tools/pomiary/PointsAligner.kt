package local.tools.pomiary

import android.graphics.Color
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class PointsAligner {
    companion object {
        private const val roundFactor = 0.1F


        private fun getPointsFromText(
            pointsData: Array<DataStorage.PointData>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
        ): Array<Float> {
            val pointsRaw = Array(pointsData.size) { index ->
                val floatValue = pointsData[index].rawInput.toFloatOrNull()
                floatValue ?: tolerancesRaw[index].origin
            }
            return pointsRaw
        }


        fun roundPoint(value: Float): Float {
            return (value / roundFactor).roundToInt() * roundFactor
        }


        fun testPoint(value: Float, tolerance: DataStorage.PointTolerance): DataStorage.PointResult {
            val pointError = (value - tolerance.origin).absoluteValue - tolerance.offset
            val toleranceNok = DataStorage.getToleranceNok()[0].offset
            return when {
                (pointError < roundFactor) -> DataStorage.PointResult.OK
                (pointError < toleranceNok) -> DataStorage.PointResult.TOLERANCE
                else -> DataStorage.PointResult.CRITICAL
            }
        }


        fun colorByResult(result: DataStorage.PointResult): Int {
            return when (result) {
                DataStorage.PointResult.OK -> Color.GREEN
                DataStorage.PointResult.TOLERANCE -> Color.YELLOW
                else -> Color.RED
            }
        }


        private fun shiftAndCheckPoints(
            pointsData: Array<DataStorage.PointData>,
            pointsRaw: Array<Float>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
        ): DataStorage.PointResult {
            val pointZero = pointsRaw[0]
            val points = List(pointsRaw.size) { index ->
                roundPoint((pointsRaw[index] - pointZero).absoluteValue)
            }

            var shiftUpMin = 0.0F
            var shiftUpIndex = -1
            var shiftDownMax = 0.0F
            var shiftDownIndex = -1
            points.forEachIndexed { index, pointValue ->
                val shiftDown =
                    tolerancesRaw[index].origin - tolerancesRaw[index].offset - pointValue
                if ((shiftDown > shiftDownMax) or (shiftDownIndex < 0)) {
                    shiftDownMax = shiftDown
                    shiftDownIndex = index
                }

                val shiftUp = tolerancesRaw[index].origin + tolerancesRaw[index].offset - pointValue
                if ((shiftUp < shiftUpMin) or (shiftUpIndex < 0)) {
                    shiftUpMin = shiftUp
                    shiftUpIndex = index
                }
            }

            var totalShift = 0.0F
            if ((shiftDownMax > 0) and (shiftUpMin < 0)) {
                totalShift = (shiftUpMin + shiftDownMax) / 2
            } else if (shiftDownMax > 0) {
                totalShift = min(shiftDownMax, shiftUpMin)
            } else if (shiftUpMin < 0) {
                totalShift = max(shiftUpMin, shiftDownMax)
            }

            points.forEachIndexed { index, _ ->
                val pointValue = points[index] + totalShift
                val pointResult = testPoint(pointValue, tolerancesRaw[index])

                //if ((pointResult == PointResult.CRITICAL) and ((index == shiftDownIndex) or (index == shiftUpIndex))) {
                //    pointResult = PointResult.CRITICAL_LIMITED
                //}

                pointsData[index].value = pointValue
                pointsData[index].result = pointResult
            }

            return DataStorage.PointResult.OK//pointsShifted
        }


        fun alignPoints(
            tolerancesRaw: Array<DataStorage.PointTolerance>,
            pointsData: Array<DataStorage.PointData>,
        ): DataStorage.PointResult {
            val pointsRaw = getPointsFromText(pointsData, tolerancesRaw)
            return shiftAndCheckPoints(pointsData, pointsRaw, tolerancesRaw)
        }
    }
}