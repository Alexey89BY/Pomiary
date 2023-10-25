package local.tools.pomiary

import android.graphics.Color
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class PointsAligner {
    companion object {
        private const val roundFactor = 0.1
        private const val errorEpsilon = roundFactor / 2


        private fun getPointsFromText(
            pointsData: Array<DataStorage.PointData>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
        ): Array<Double> {
            val pointsRaw = Array(pointsData.size) { index ->
                val floatValue = pointsData[index].rawInput.toDoubleOrNull()
                floatValue ?: if (index != 0) tolerancesRaw[index].origin else 0.0
            }
            return pointsRaw
        }


        fun roundPoint(value: Double): Double {
            return kotlin.math.round(value / roundFactor) * roundFactor
        }


        fun testPoint(value: Double, tolerance: DataStorage.PointTolerance): DataStorage.PointResult {
            val pointOffset = value - tolerance.origin
            val pointError = pointOffset.absoluteValue - tolerance.offset
            val toleranceNok = DataStorage.getToleranceNok()
            return when {
                (pointError < errorEpsilon) -> DataStorage.PointResult.OK
                (pointError < toleranceNok) -> if (pointOffset < 0) DataStorage.PointResult.WARNING_DOWN else DataStorage.PointResult.WARNING_UP
                else -> if (pointOffset < 0) DataStorage.PointResult.CRITICAL_DOWN else DataStorage.PointResult.CRITICAL_UP
            }
        }


        fun colorByResult(result: DataStorage.PointResult): Int {
            return when (result) {
                DataStorage.PointResult.OK -> Color.GREEN
                DataStorage.PointResult.WARNING_DOWN -> Color.YELLOW
                DataStorage.PointResult.WARNING_UP -> Color.YELLOW
                else -> Color.RED
            }
        }


        //val wrongText: String = view.resources.getString(R.string.result_Nok)
        //val goodText: String = view.resources.getString(R.string.result_Ok)
        fun messageByResult(result: DataStorage.PointResult): String {
            return when (result) {
                DataStorage.PointResult.OK -> "OK"
                DataStorage.PointResult.NOK -> "NOK"
                DataStorage.PointResult.WARNING_DOWN,
                DataStorage.PointResult.CRITICAL_DOWN -> "NOK\u2193"
                DataStorage.PointResult.WARNING_UP,
                DataStorage.PointResult.CRITICAL_UP -> "NOK\u2191"
                else -> ""
            }
        }


        private fun shiftAndCheckPoints(
            pointsData: Array<DataStorage.PointData>,
            pointsRaw: Array<Double>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
        ): DataStorage.PointResult {
            val pointZero = pointsRaw[0]
            val points = List(pointsRaw.size) { index ->
                (pointsRaw[index] - pointZero).absoluteValue
            }

            var shiftUpMin = 0.0
            var shiftUpIndex = -1
            var shiftDownMax = 0.0
            var shiftDownIndex = -1

            points.forEachIndexed { index, pointValue ->
                val shiftDown =
                    (tolerancesRaw[index].origin - tolerancesRaw[index].offset) - pointValue
                if ((shiftDown > shiftDownMax) or (shiftDownIndex < 0)) {
                    shiftDownMax = shiftDown
                    shiftDownIndex = index
                }

                val shiftUp =
                    (tolerancesRaw[index].origin + tolerancesRaw[index].offset) - pointValue
                if ((shiftUp < shiftUpMin) or (shiftUpIndex < 0)) {
                    shiftUpMin = shiftUp
                    shiftUpIndex = index
                }
            }

            val totalShift = when {
                (shiftUpMin > 0) and (shiftDownMax > 0) -> min(shiftUpMin, shiftDownMax)
                (shiftUpMin < 0) and (shiftDownMax < 0) -> max(shiftUpMin, shiftDownMax)
                //(shiftUpMin < 0) and (shiftDownMax > 0) -> (shiftUpMin + shiftDownMax) / 2
                else -> 0.0
            }

            var result = DataStorage.PointResult.OK

            points.forEachIndexed { index, _ ->
                val shiftedValue = points[index] + totalShift
                val pointValue =  if (index != 0) roundPoint(shiftedValue) else shiftedValue
                val pointResult = testPoint(pointValue, tolerancesRaw[index])

                //if ((pointResult == PointResult.CRITICAL) and ((index == shiftDownIndex) or (index == shiftUpIndex))) {
                //    pointResult = PointResult.CRITICAL_LIMITED
                //}
                if (pointResult != DataStorage.PointResult.OK)
                {
                    result = DataStorage.PointResult.NOK
                }

                pointsData[index].value = pointValue
                pointsData[index].result = pointResult
            }

            return result
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