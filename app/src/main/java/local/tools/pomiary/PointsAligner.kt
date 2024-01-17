package local.tools.pomiary

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class PointsAligner {
    companion object {
        private const val roundFactor = 0.1
        private const val errorEpsilon = roundFactor / 2


        private fun getPointsFromText(
            pointsData: Array<PointData>,
        ): Array<Double> {
            val pointsRaw = Array(pointsData.size) { index ->
                PointData.valueFromString(pointsData[index].rawInput)
            }
            val pointZero = pointsRaw[0]
            val points = Array(pointsRaw.size) { index ->
                pointDistance(pointsRaw[index], pointZero)
            }
            return points
        }


        fun pointDistance(pointRaw: Double, pointZero: Double): Double {
            val value = (pointRaw - pointZero).absoluteValue
            return kotlin.math.round(value / roundFactor) * roundFactor
        }


        fun testPoint(value: Double, tolerance: DataStorage.PointTolerance): PointResult {
            val pointOffset = value - tolerance.origin
            val pointError = pointOffset.absoluteValue - tolerance.offset
            val toleranceNok = DataStorage.getToleranceNok()
            return when {
                (pointError < errorEpsilon) -> PointResult.OK
                (pointError < toleranceNok) -> if (pointOffset < 0) PointResult.WARNING_DOWN else PointResult.WARNING_UP
                else -> if (pointOffset < 0) PointResult.CRITICAL_DOWN else PointResult.CRITICAL_UP
            }
        }


        private fun shiftAndCheckPoints(
            pointsData: Array<PointData>,
            points: Array<Double>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
        ): PointResult {
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

            var result = PointResult.OK

            points.forEachIndexed { index, _ ->
                val shiftedValue = points[index] + totalShift
                val pointValue = if (index != 0) pointDistance(shiftedValue, 0.0) else shiftedValue
                val pointResult = testPoint(pointValue, tolerancesRaw[index])

                //if ((pointResult == PointResult.CRITICAL) and ((index == shiftDownIndex) or (index == shiftUpIndex))) {
                //    pointResult = PointResult.CRITICAL_LIMITED
                //}
                if (pointResult != PointResult.OK)
                {
                    result = PointResult.NOK
                }

                pointsData[index].value = pointValue
                pointsData[index].result = pointResult
            }

            return result
        }


        fun alignPoints(
            tolerancesRaw: Array<DataStorage.PointTolerance>,
            pointsData: Array<PointData>,
        ): PointResult {
            val pointsRaw = getPointsFromText(pointsData)
            return shiftAndCheckPoints(pointsData, pointsRaw, tolerancesRaw)
        }
    }
}