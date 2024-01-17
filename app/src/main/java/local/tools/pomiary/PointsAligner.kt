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
            pointsData.forEachIndexed { index, point ->
                pointsData[index].rawValue = PointData.valueFromString(point.rawInput)
            }
            val pointZero = pointsData[0].rawValue
            val points = Array(pointsData.size) { index ->
                pointDistance(pointsData[index].rawValue, pointZero)
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
            pointsRaw: Array<Double>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
            toleranceMap: List<Int>,
        ): PointResult {
            var shiftUpMin = 0.0
            var shiftUpIndex = -1
            var shiftDownMax = 0.0
            var shiftDownIndex = -1

            tolerancesRaw.forEachIndexed { index, tolerance ->
                val pointIndex = toleranceMap[index]

                val shiftDown = (tolerance.origin - tolerance.offset) - pointsRaw[pointIndex]
                if ((shiftDown > shiftDownMax) or (shiftDownIndex < 0)) {
                    shiftDownMax = shiftDown
                    shiftDownIndex = pointIndex
                }

                val shiftUp = (tolerance.origin + tolerance.offset) - pointsRaw[pointIndex]
                if ((shiftUp < shiftUpMin) or (shiftUpIndex < 0)) {
                    shiftUpMin = shiftUp
                    shiftUpIndex = pointIndex
                }
            }

            val totalShift = when {
                (shiftUpMin > 0) and (shiftDownMax > 0) -> min(shiftUpMin, shiftDownMax)
                (shiftUpMin < 0) and (shiftDownMax < 0) -> max(shiftUpMin, shiftDownMax)
                //(shiftUpMin < 0) and (shiftDownMax > 0) -> (shiftUpMin + shiftDownMax) / 2
                else -> 0.0
            }

            pointsData.forEachIndexed { index, _ ->
                val shiftedValue = pointsRaw[index] + totalShift
                val pointValue =
                    if (index != 0) pointDistance(shiftedValue, 0.0)
                    else shiftedValue
                pointsData[index].value = pointValue
                pointsData[index].result = PointResult.UNKNOWN
            }

            var result = PointResult.OK
            tolerancesRaw.forEachIndexed { index, tolerance ->
                val pointIndex = toleranceMap[index]
                val pointResult = testPoint(pointsData[pointIndex].value, tolerance)

                //if ((pointResult == PointResult.CRITICAL) and ((index == shiftDownIndex) or (index == shiftUpIndex))) {
                //    pointResult = PointResult.CRITICAL_LIMITED
                //}
                if (pointResult != PointResult.OK)
                {
                    result = PointResult.NOK
                }

                pointsData[pointIndex].result = pointResult
            }

            return result
        }


        fun alignPoints(
            tolerances: Array<DataStorage.PointTolerance>,
            toleranceMap: List<Int>,
            pointsData: Array<PointData>,
        ): PointResult {
            val pointsRaw = getPointsFromText(pointsData)
            return shiftAndCheckPoints(pointsData, pointsRaw, tolerances, toleranceMap)
        }
    }
}