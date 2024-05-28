package local.tools.pomiary

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class PointsAligner {
    companion object {
        private const val roundFactor = 0.1
        private const val errorEpsilon = 0.01 / 2


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
            val pointOffset = (value - tolerance.origin).absoluteValue
            val pointError = pointOffset - tolerance.offset
            return when {
                (tolerance.offset < 0.0) -> PointResult.OK
                (pointError < - DataStorage.getToleranceNok()) -> PointResult.OK
                (pointError < errorEpsilon) -> PointResult.WARNING
                (pointOffset < DataStorage.getToleranceInvalid()) -> PointResult.CRITICAL
                else -> PointResult.INVALID
            }
        }


        private fun shiftPoints(
            pointsData: Array<PointData>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
            toleranceMap: List<Int>,
        ) {
            val baseIndex = toleranceMap[0]
            if (testPoint(0.0, tolerancesRaw[baseIndex]) != PointResult.OK)
                return

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
        }


        private fun checkPoints(
            pointsData: Array<PointData>,
            tolerancesRaw: Array<DataStorage.PointTolerance>,
            toleranceMap: List<Int>,
        ): PointResult {
            var result = PointResult.OK
            tolerancesRaw.forEachIndexed { index, tolerance ->
                val pointIndex = toleranceMap[index]
                val pointResult =
                    if (tolerance.offset < 0.0) PointResult.OK
                    else testPoint(pointsData[pointIndex].value, tolerance)

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
            shiftPoints(pointsData, tolerances, toleranceMap)
            return checkPoints(pointsData, tolerances, toleranceMap)
        }
    }
}