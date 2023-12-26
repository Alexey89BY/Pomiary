package local.tools.pomiary

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DataStorage {
    enum class PointResult {
        UNKNOWN,
        OK,
        NOK,
        WARNING_DOWN,
        WARNING_UP,
        CRITICAL_DOWN,
        CRITICAL_UP,
        ;

        fun toInt(): Int {
            return when (this) {
                OK -> 1
                NOK -> 2
                WARNING_DOWN -> 3
                WARNING_UP -> 4
                CRITICAL_DOWN -> 5
                CRITICAL_UP -> 6
                else -> 0
            }
        }

        companion object {
            fun fromInt(result: Int): PointResult {
                return when (result) {
                    1 -> OK
                    2 -> NOK
                    3 -> WARNING_DOWN
                    4 -> WARNING_UP
                    5 -> CRITICAL_DOWN
                    6 -> CRITICAL_UP
                    else -> UNKNOWN
                }
            }
        }
    }

    data class PointData (
        var rawInput: String = String(),
        var value: Double = 0.0,
        var result: PointResult = PointResult.UNKNOWN
    )

    @Suppress("ArrayInDataClass")
    data class SectionData(
        var points: Array<PointData>,
        var result: PointResult
    )

    data class SillSealData(
        var title: Pair<String, String>,
        var timeStamp: String,
        var isModified: Boolean = false,
        var sectionP6: SectionData,
        var sectionP7: SectionData,
    ) {
        fun dataTitle(): String {
            val resultP6 = PointsAligner.messageByResult(sectionP6.result)
            val resultP7 = PointsAligner.messageByResult(sectionP7.result)
            return "${title.first} ${title.second}: $timeStamp, $resultP6/$resultP7"
        }
    }

    @Suppress("ArrayInDataClass")
    data class DataSubSet(
        var title: String = String(),
        var data: Array<SillSealData>,
        var tolerancesP6: Array<PointTolerance>,
        var tolerancesP7: Array<PointTolerance>,
    ) {
        fun storageTitle(data: SillSealData): String {
            return title +
                    " " + data.dataTitle()
        }

        fun getData(index: Int): SillSealData {
            return data[index]
        }
    }

    data class PointTolerance(
        var origin: Double,
        var offset: Double
    )


    companion object {

        private const val storageDataSize = 6
        private val storageDataSteps = listOf(
            Pair("LH","#1"),
            Pair("LH","#2"),
            Pair("LH","#3"),
            Pair("RH","#1"),
            Pair("RH","#2"),
            Pair("RH","#3"),
        )

        private const val standardSectionP6Size = 9
        private var toleranceStandardP6 = arrayOf(
            PointTolerance(0.0, 3.0),
            //PointTolerance(21.0, 1.5),
            PointTolerance(21.0, 1.5),
            PointTolerance(123.0, 2.5),
            PointTolerance(225.0, 2.5),
            PointTolerance(327.0, 2.5),
            PointTolerance(429.0, 2.5),
            PointTolerance(531.0, 2.5),
            PointTolerance(633.0, 2.5),
            //PointTolerance(640.0, 2.5),
            PointTolerance(655.0, 3.0)
        )

        private const val standardSectionP7Size = 4
        private var toleranceStandardP7 = arrayOf(
            PointTolerance(0.0, 2.5),
            //PointTolerance(29.0, 2.5),
            PointTolerance(29.0, 2.5),
            PointTolerance(114.0, 2.5),
            //PointTolerance(114.0, 2.5),
            PointTolerance(149.0, 2.5)
        )

        private const val maxiSectionP6Size = 11
        private var toleranceMaxiP6 = arrayOf(
            PointTolerance(0.0, 3.0),
            PointTolerance(14.0, 1.5),
            //PointTolerance(14.0, 1.5),
            PointTolerance(116.0, 2.5),
            PointTolerance(218.0, 2.5),
            PointTolerance(320.0, 2.5),
            PointTolerance(422.0, 2.5),
            PointTolerance(524.0, 2.5),
            PointTolerance(626.0, 2.5),
            PointTolerance(728.0, 2.5),
            PointTolerance(830.0, 2.5),
            //PointTolerance(830.0, 2.5),
            PointTolerance(839.5, 3.0)
        )

        private const val maxiSectionP7Size = 4
        private var toleranceMaxiP7 = arrayOf(
            PointTolerance(0.0, 2.5),
            //PointTolerance(22.5, 1.5),
            PointTolerance(22.5, 1.5),
            PointTolerance(107.5, 2.5),
            //PointTolerance(130.5, 1.5),
            PointTolerance(130.5, 2.5)
        )

        private var toleranceNok = arrayOf(
            PointTolerance(0.0, 0.5),
        )

        private val storageStandard = Array(storageDataSize) { index ->
            SillSealData(
                title = storageDataSteps[index],
                timeStamp = "",
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
                timeStamp = "",
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


        fun getStorageByName(name: String?): DataSubSet {
            return when (name) {
                subsetMaxi.title -> subsetMaxi
                else -> subsetStandard
            }
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


        fun getTolerancesNok(): Array<PointTolerance> {
            return toleranceNok
        }


        fun getToleranceNok(): Double {
            return toleranceNok[0].offset
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
                jsonPointsAligned.put(it.value)
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
                val pointValue = jsonPointsAligned.getDouble(index)
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