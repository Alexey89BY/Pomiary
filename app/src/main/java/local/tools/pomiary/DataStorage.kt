package local.tools.pomiary

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DataStorage {
    enum class PointResult {
        OK,
        TOLERANCE,
        CRITICAL, ;

        fun toInt(): Int {
            return when (this) {
                OK -> 1
                CRITICAL -> 2
                else -> 0
            }
        }

        companion object {
            fun fromInt(result: Int): PointResult {
                return when (result) {
                    1 -> OK
                    2 -> CRITICAL
                    else -> TOLERANCE
                }
            }
        }
    }

    data class PointData (
        var rawInput: String = String(),
        var value: Float = 0.0F,
        var result: PointResult = PointResult.TOLERANCE
    )

    @Suppress("ArrayInDataClass")
    data class SectionData(
        var points: Array<PointData>,
        var result: PointResult
    )

    data class SillSealData(
        var title: String,
        var timeStamp: String = String(),
        var sectionP6: SectionData,
        var sectionP7: SectionData,
    )

    @Suppress("ArrayInDataClass")
    data class DataSubSet(
        var title: String = String(),
        var data: Array<SillSealData>,
        var tolerancesP6: Array<PointTolerance>,
        var tolerancesP7: Array<PointTolerance>,
    )

    data class PointTolerance(
        var origin: Float,
        var offset: Float
    )


    companion object {

        private const val storageDataSize = 12
        private const val standardSectionP6Size = 9
        private const val standardSectionP7Size = 4
        private const val maxiSectionP6Size = 11
        private const val maxiSectionP7Size = 4

        private val storageDataSteps = listOf(
            "LH 1.1",
            "LH 1.2",
            "LH 1.3",
            "LH 2.1",
            "LH 2.2",
            "LH 2.3",
            "RH 1.1",
            "RH 1.2",
            "RH 1.3",
            "RH 2.1",
            "RH 2.2",
            "RH 2.3",
        )

        private var toleranceStandardP6 = arrayOf(
            PointTolerance(0.0F, 3.0F),
            PointTolerance(21.0F, 1.5F),
            PointTolerance(123.0F, 2.5F),
            PointTolerance(225.0F, 2.5F),
            PointTolerance(327.0F, 2.5F),
            PointTolerance(429.0F, 2.5F),
            PointTolerance(531.0F, 2.5F),
            PointTolerance(633.0F, 2.5F),
            PointTolerance(655.0F, 3.0F)
        )

        private var toleranceStandardP7 = arrayOf(
            PointTolerance(0.0F, 2.5F),
            PointTolerance(29.0F, 2.5F),
            PointTolerance(114.0F, 2.5F),
            PointTolerance(149.0F, 2.5F)
        )

        private var toleranceMaxiP6 = arrayOf(
            PointTolerance(0.0F, 3.0F),
            PointTolerance(14.0F, 1.5F),
            PointTolerance(116.0F, 2.5F),
            PointTolerance(218.0F, 2.5F),
            PointTolerance(320.0F, 2.5F),
            PointTolerance(422.0F, 2.5F),
            PointTolerance(524.0F, 2.5F),
            PointTolerance(626.0F, 2.5F),
            PointTolerance(728.0F, 2.5F),
            PointTolerance(830.0F, 2.5F),
            PointTolerance(839.5F, 3.0F)
        )

        private var toleranceMaxiP7 = arrayOf(
            PointTolerance(0.0F, 2.5F),
            PointTolerance(22.5F, 1.5F),
            PointTolerance(107.5F, 2.5F),
            PointTolerance(130.5F, 2.5F)
        )

        private var toleranceNok = arrayOf(
            PointTolerance(0.0F, 0.5F),
        )

        private val storageStandard = Array(storageDataSize) { index ->
            SillSealData(
                title = storageDataSteps[index],
                sectionP6 = SectionData(
                    points = Array(standardSectionP6Size) { PointData() },
                    result = PointResult.OK
                ),
                sectionP7 = SectionData(
                    points = Array(standardSectionP7Size) { PointData() },
                    result = PointResult.OK
                )
            )
        }

        private val storageMaxi = Array(storageDataSize) { index ->
            SillSealData(
                title = storageDataSteps[index],
                sectionP6 = SectionData(
                    points = Array(maxiSectionP6Size) { PointData() },
                    result = PointResult.OK
                ),
                sectionP7 = SectionData(
                    points = Array(maxiSectionP7Size) { PointData() },
                    result = PointResult.OK
                )
            )
        }

        private val subsetStandard = DataSubSet(
            title = "Standard",
            data = storageStandard,
            tolerancesP6 = toleranceStandardP6,
            tolerancesP7 = toleranceStandardP7,
        )

        private val subsetMaxi = DataSubSet(
            title = "Maxi",
            data = storageMaxi,
            tolerancesP6 = toleranceMaxiP6,
            tolerancesP7 = toleranceMaxiP7,
        )


        fun getStorageStandard(): DataSubSet {
            return subsetStandard
        }


        fun getStorageMaxi(): DataSubSet {
            return subsetMaxi
        }


        fun getToleranceStandardP6(): Array<PointTolerance> {
            return toleranceStandardP6
        }


        fun getToleranceStandardP7(): Array<PointTolerance> {
            return toleranceStandardP7
        }


        fun getToleranceMaxiP6(): Array<PointTolerance> {
            return toleranceMaxiP6
        }


        fun getToleranceMaxiP7(): Array<PointTolerance> {
            return toleranceMaxiP7
        }


        fun getToleranceNok(): Array<PointTolerance> {
            return toleranceNok
        }


        fun getStorageDataTitle(
            subset: DataSubSet,
            data: SillSealData
        ): String {
            return subset.title +
                    " " + data.title +
                    " - " + data.timeStamp +
                    " - " + PointsAligner.messageByResult(data.sectionP6.result) +
                    " / " + PointsAligner.messageByResult(data.sectionP7.result)
        }

        fun broadcastSettingsChange() {
            subsetStandard.tolerancesP6 = getToleranceStandardP6()
            subsetStandard.tolerancesP7 = getToleranceStandardP7()
            subsetMaxi.tolerancesP6 = getToleranceMaxiP6()
            subsetMaxi.tolerancesP7 = getToleranceMaxiP7()
        }


        private const val jsonFileName = "data.json"

        fun loadData(context: Context) {
            val filesDir = context.getExternalFilesDir(null)
            val file = File(filesDir, jsonFileName)

            try {
                val json = JSONObject(file.readText())
                storageSubsetFromJson(json.getJSONArray(subsetStandard.title), subsetStandard)
                storageSubsetFromJson(json.getJSONArray(subsetMaxi.title), subsetMaxi)
            }
            catch (_: Exception) {

            }

        }

        fun saveData(context: Context) {
            val json = JSONObject()
            json.put(subsetStandard.title, storageSubsetToJson(subsetStandard))
            json.put(subsetMaxi.title, storageSubsetToJson(subsetMaxi))

            val filesDir = context.getExternalFilesDir(null)
            val file = File(filesDir, jsonFileName)
            file.writeText(json.toString())
        }


        private const val jsonSubsetTimeStampName = "ts"
        private const val jsonSubsetSectionP6Name = "p6"
        private const val jsonSubsetSectionP7Name = "p7"

        private fun storageSubsetToJson(subset: DataSubSet): JSONArray {
            val jsonArray = JSONArray()
            subset.data.forEach { sillSealData ->
                val jsonObject = JSONObject()
                jsonObject.put(jsonSubsetTimeStampName, sillSealData.timeStamp)
                jsonObject.put(jsonSubsetSectionP6Name, storageSectionToJson(sillSealData.sectionP6))
                jsonObject.put(jsonSubsetSectionP7Name, storageSectionToJson(sillSealData.sectionP7))
                jsonArray.put(jsonObject)
            }

            return jsonArray
        }

        private fun storageSubsetFromJson(jsonArray: JSONArray, subset: DataSubSet) {
            subset.data.forEachIndexed { index, data ->
                val jsonObject = jsonArray.getJSONObject(index)
                data.timeStamp = jsonObject.getString(jsonSubsetTimeStampName)
                storageSectionFromJson(jsonObject.getJSONObject(jsonSubsetSectionP6Name), data.sectionP6)
                storageSectionFromJson(jsonObject.getJSONObject(jsonSubsetSectionP7Name), data.sectionP7)
            }
        }


        private const val jsonSectionPointsRawName = "ri"
        private const val jsonSectionPointsAlignedName = "pa"
        private const val jsonSectionPointsResultName = "pr"
        private const val jsonSectionResultName = "sr"

        private fun storageSectionToJson(section: SectionData): JSONObject {
            val jsonPointsRaw = JSONArray()
            val jsonPointsAligned = JSONArray()
            val jsonPointsResult = JSONArray()

            section.points.forEach {
                jsonPointsRaw.put(it.rawInput)
                jsonPointsAligned.put(it.value.toDouble())
                jsonPointsResult.put(it.result.toInt())
            }

            val jsonSection = JSONObject()
            jsonSection.put(jsonSectionResultName, section.result.toInt())
            jsonSection.put(jsonSectionPointsRawName, jsonPointsRaw)
            jsonSection.put(jsonSectionPointsAlignedName, jsonPointsAligned)
            jsonSection.put(jsonSectionPointsResultName, jsonPointsResult)

            return jsonSection
        }

        private fun storageSectionFromJson(jsonObject: JSONObject, section: SectionData) {
            val jsonPointsRaw = jsonObject.getJSONArray(jsonSectionPointsRawName)
            val jsonPointsAligned = jsonObject.getJSONArray(jsonSectionPointsAlignedName)
            val jsonPointsResult = jsonObject.getJSONArray(jsonSectionPointsResultName)

            val sectionResult = PointResult.fromInt(jsonObject.getInt(jsonSectionResultName))
            section.result = sectionResult

            section.points.forEachIndexed { index, _ ->
                val pointRawInput = jsonPointsRaw.getString(index)
                val pointValue = jsonPointsAligned.getDouble(index).toFloat()
                val pointResult = PointResult.fromInt(jsonPointsResult.getInt(index))

                section.points[index] = PointData(
                    rawInput = pointRawInput,
                    value = pointValue,
                    result = pointResult
                )
            }
        }
    }
}