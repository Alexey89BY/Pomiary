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
            toleranceMapProfile: List<Int>,
            toleranceMapMoldings: List<Pair<Int, Int>>,
        ) {
            // Distance from base point
            pointsData.forEachIndexed { index, point ->
                val rawValue = PointData.valueFromString(point.rawInput)
                if (index == 0) {
                    pointsData[index].rawValue = rawValue
                    pointsData[index].value = 0.0
                } else {
                    val baseValue = pointsData[0].rawValue
                    pointsData[index].rawValue = pointsDistance(rawValue, baseValue)
                }
            }

            // Profile points
            toleranceMapProfile.forEach { index ->
                if (index != 0) {
                    val pointValue = pointsData[index].rawValue
                    pointsData[index].value = pointValueRound(pointValue)
                }
            }

            // Moldings points
            toleranceMapMoldings.forEach { indexes ->
                val pointValue = pointsData[indexes.first].rawValue
                val baseValue = pointsData[indexes.second].rawValue
                pointsData[indexes.first].value = pointsDistance(pointValue, baseValue, indexes)
            }
        }


        fun pointsDistance(pointValue: Double, pointZero: Double): Double {
            return (pointValue - pointZero).absoluteValue
        }


        fun pointsDistance(pointValue: Double, pointZero: Double, indexes: Pair<Int, Int>): Double {
            return if (indexes.first > indexes.second) pointValue - pointZero else pointZero - pointValue
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
                (pointError < DataStorage.getToleranceInvalid()) -> PointResult.CRITICAL
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
                val pointValue = pointsData[pointIndex].value

                val shiftDown = (tolerance.origin - tolerance.offset) - pointValue
                if ((shiftDown > shiftDownMax) or (shiftDownIndex < 0)) {
                    shiftDownMax = shiftDown
                    shiftDownIndex = pointIndex
                }

                val shiftUp = (tolerance.origin + tolerance.offset) - pointValue
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

            toleranceMap.forEach { index ->
                val shiftedValue = pointsData[index].value + totalShift
                pointsData[index].value =
                    if (index != 0) pointValueRound(shiftedValue)
                    else shiftedValue
            }
        }


        private fun checkPoints(
            pointsData: Array<PointData>,
            tolerancesProfile: Array<DataStorage.PointTolerance>,
            tolerancesMoldings: Array<DataStorage.PointTolerance>,
            toleranceMapProfile: List<Int>,
            toleranceMapMoldings: List<Pair<Int,Int>>,
        ): PointResult {

            tolerancesProfile.forEachIndexed { index, tolerance ->
                val pointIndex = toleranceMapProfile[index]
                val pointResult = testPoint(pointsData[pointIndex].value, tolerance)

                pointsData[pointIndex].result = pointResult
            }

            tolerancesMoldings.forEachIndexed { index, tolerance ->
                val pointIndex = toleranceMapMoldings[index].first
                val pointResult = testPoint(pointsData[pointIndex].value, tolerance)

                pointsData[pointIndex].result =
                    if (pointResult == PointResult.INVALID) PointResult.INVALID
                    else PointResult.UNKNOWN
            }

            pointsData.forEach { point ->
                when (point.result) {
                    PointResult.UNKNOWN,
                    PointResult.OK -> {
                    }
                    PointResult.INVALID -> {
                        return PointResult.INVALID
                    }
                    else -> {
                        return PointResult.NOK
                    }
                }
            }

            return PointResult.OK
        }


        fun alignPoints(
            tolerancesProfile: Array<DataStorage.PointTolerance>,
            tolerancesMoldings: Array<DataStorage.PointTolerance>,
            toleranceMapProfile: List<Int>,
            toleranceMapMoldings: List<Pair<Int,Int>>,
            pointsData: Array<PointData>,
        ): PointResult {
            updatePointsFromText(pointsData, toleranceMapProfile, toleranceMapMoldings)
            shiftPoints(pointsData, tolerancesProfile, toleranceMapProfile)
            return checkPoints(pointsData, tolerancesProfile, tolerancesMoldings, toleranceMapProfile, toleranceMapMoldings)
        }
    }
}