package local.tools.pomiary

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class PointsAligner {
    companion object {
        private const val roundFactor = 0.1
        private const val errorEpsilon = roundFactor / 2


        private fun updatePointsFromText(
            pointsData: Array<PointData>,
        ) {
            pointsData.forEachIndexed{ index, point ->
                val rawValue = PointData.valueFromString(point.rawInput)
                if (index == 0) {
                    pointsData[index].rawValue = rawValue
                    pointsData[index].value = 0.0
                } else {
                    val pointValue = pointsDistance(rawValue, pointsData[0].rawValue)
                    pointsData[index].rawValue = pointValue
                    pointsData[index].value = pointValueRound(pointValue)
                }
                pointsData[index].result = PointResult.UNKNOWN
            }
        }


        fun pointsDistance(point: Double, pointZero: Double): Double {
            return (point - pointZero).absoluteValue
        }


        private fun pointValueRound(value: Double): Double {
            return kotlin.math.round(value / roundFactor) * roundFactor
        }


        fun testPoint(value: Double, tolerance: DataStorage.PointTolerance): PointResult {
            val pointOffset = value - tolerance.origin
            val pointError = pointOffset.absoluteValue - tolerance.offset
            return when {
                (pointError < errorEpsilon) -> PointResult.OK
                (pointError < DataStorage.getToleranceNok()) -> if (pointOffset < 0) PointResult.WARNING_DOWN else PointResult.WARNING_UP
                (pointError < DataStorage.getToleranceInvalid()) -> if (pointOffset < 0) PointResult.CRITICAL_DOWN else PointResult.CRITICAL_UP
                else -> PointResult.INVALID
            }
        }


        private fun shiftAndCheckPoints(
            pointsData: Array<PointData>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
            toleranceMap: List<Int>,
        ): PointResult {
            var shiftUpMin = 0.0
            var shiftUpIndex = -1
            var shiftDownMax = 0.0
            var shiftDownIndex = -1

            tolerancesRaw.forEachIndexed { index, tolerance ->
                val pointIndex = toleranceMap[index]

                val shiftDown = (tolerance.origin - tolerance.offset) - pointsData[pointIndex].value
                if ((shiftDown > shiftDownMax) or (shiftDownIndex < 0)) {
                    shiftDownMax = shiftDown
                    shiftDownIndex = pointIndex
                }

                val shiftUp = (tolerance.origin + tolerance.offset) - pointsData[pointIndex].value
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

            pointsData.forEachIndexed { index, point ->
                val shiftedValue = point.value + totalShift
                pointsData[index].value =
                    if (index != 0) pointValueRound(shiftedValue)
                    else shiftedValue
            }

            var result = PointResult.OK
            tolerancesRaw.forEachIndexed { index, tolerance ->
                val pointIndex = toleranceMap[index]
                val pointResult = testPoint(pointsData[pointIndex].value, tolerance)

                //if ((pointResult == PointResult.CRITICAL) and ((index == shiftDownIndex) or (index == shiftUpIndex))) {
                //    pointResult = PointResult.CRITICAL_LIMITED
                //}

                if (result != PointResult.INVALID) {
                    if (pointResult == PointResult.INVALID) {
                        result = PointResult.INVALID
                    } else
                    if (pointResult != PointResult.OK) {
                        result = PointResult.NOK
                    }
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
            updatePointsFromText(pointsData)
            return shiftAndCheckPoints(pointsData, tolerances, toleranceMap)
        }
    }
}