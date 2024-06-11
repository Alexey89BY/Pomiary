package local.tools.pomiary.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointData
import local.tools.pomiary.PointsAligner
import local.tools.pomiary.R
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class HistoryFragment : Fragment() {
    private lateinit var viewOfLayout: View
    private lateinit var viewGraphP6: HistoryViewCanvas
    private lateinit var viewGraphP7: HistoryViewCanvas
    private lateinit var historySpinner: Spinner
    private lateinit var historySpinnerAdapter: ArrayAdapter<String>
    private val historyTitles = emptyList<String>().toMutableList()
    private var historyData = emptyList<List<String>>()
    private var currentData: List<String>? = null
    private var currentDataPosition = -1
    private var currentAnalysisType = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewOfLayout = inflater.inflate(R.layout.fragment_history, container, false)

        val refreshButton = viewOfLayout.findViewById<ImageButton>(R.id.buttonRefreshHistory)
        refreshButton.setOnClickListener { refreshHistory() }

        val copyButton = viewOfLayout.findViewById<ImageButton>(R.id.buttonCopy)
        copyButton.setOnClickListener { copyToClipboard() }

        val switchGraphs = viewOfLayout.findViewById<SwitchMaterial>(R.id.switchValuesOnly)
        switchGraphs.setOnCheckedChangeListener { _, checked ->
            viewGraphP6.setValuesOnly(checked)
            viewGraphP7.setValuesOnly(checked)
        }

        val prevButton = viewOfLayout.findViewById<ImageButton>(R.id.buttonPrev)
        prevButton.setOnClickListener {
            val newIndex = historySpinner.selectedItemPosition - 1
            if (newIndex >= 0)
                historySpinner.setSelection(newIndex)
        }

        val nextButton = viewOfLayout.findViewById<ImageButton>(R.id.buttonNext)
        nextButton.setOnClickListener {
            val newIndex = historySpinner.selectedItemPosition + 1
            if (newIndex < historySpinnerAdapter.count)
                historySpinner.setSelection(newIndex)
        }

        viewGraphP6 = HistoryViewCanvas(activity)
        viewGraphP7 = HistoryViewCanvas(activity)
        val graphsLayout = viewOfLayout.findViewById<LinearLayout>(R.id.containerGraph)
        graphsLayout.addView(viewGraphP6)
        graphsLayout.addView(viewGraphP7)

        historySpinner = viewOfLayout.findViewById(R.id.spinnerHistory)
        historySpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, historyTitles)
        historySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        historySpinner.adapter = historySpinnerAdapter
        historySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                currentDataPosition = position
                refreshGraph()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                currentDataPosition = -1
                refreshGraph()
            }
        }

        val analysisSpinner = viewOfLayout.findViewById<Spinner>(R.id.spinnerAnalysis)
        val analysisTitles = resources.getStringArray(R.array.analysis)
        val analysisSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, analysisTitles)
        analysisSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        analysisSpinner.adapter = analysisSpinnerAdapter
        analysisSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                currentAnalysisType = position
                refreshGraph()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                currentAnalysisType = -1
                refreshGraph()
            }
        }

        return viewOfLayout
    }


    // Sill Seal

    private val titlesSillSealP6 = listOf(
        "P6 (*), mm",
        "P6 (1), mm",
        "P6 (2), mm",
        "P6 (3), mm",
        "P6 (4), mm",
        "P6 (5), mm",
        "P6 (6), mm",
        "P6 (7), mm",
        "P6 (8), mm",
        "P6 (9), mm",
        "P6 (10), mm",
    )

    private val titlesSillSealP7 = listOf(
        "P7 (*), mm",
        "P7 (1), mm",
        "P7 (2), mm",
        "P7 (3), mm",
    )

    // Cutting

    private val titlesCuttingP6 = listOf(
        "P6, mm",
        "P6 (0-1), mm",
        "P6 (1-2), mm",
        "P6 (2-3), mm",
        "P6 (3-4), mm",
        "P6 (4-5), mm",
        "P6 (5-6), mm",
        "P6 (6-7), mm",
        "P6 (7-8), mm",
        "P6 (8-9), mm",
        "P6 (9-10), mm",
    )

    private val titlesCuttingP7 = listOf(
        "P7, mm",
        "P7 (0-1), mm",
        "P7 (1-2), mm",
        "P7 (2-3), mm",
    )

    private val toleranceMaxiCuttingP6 = arrayOf(
        DataStorage.PointTolerance(837.0, 2.0),
        DataStorage.PointTolerance(14.0, 1.3),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(7.0, 1.0)
    )

    private val toleranceMaxiCuttingP7 = arrayOf(
        DataStorage.PointTolerance(123.0, 1.5),
        DataStorage.PointTolerance(19.5, 1.0),
        DataStorage.PointTolerance(84.0, 1.0),
        DataStorage.PointTolerance(19.5, 1.0),
    )

    private val toleranceStandardCuttingP6 = arrayOf(
        DataStorage.PointTolerance(653.0, 2.0),
        DataStorage.PointTolerance(21.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(102.0, 1.0),
        DataStorage.PointTolerance(20.0, 1.3)
    )

    private val toleranceStandardCuttingP7 = arrayOf(
        DataStorage.PointTolerance(142.5, 1.5),
        DataStorage.PointTolerance(26.0, 1.0),
        DataStorage.PointTolerance(84.0, 1.0),
        DataStorage.PointTolerance(32.5, 1.0),
    )

    // Moldings

    private val titlesStandardMoldingsP6 = listOf(
        "P6 - M6, mm",
    )

    private val titlesStandardMoldingsP7 = listOf(
        "P7 - M6, mm",
        "P7 - M7, mm",
    )

    private val titlesMaxiMoldingsP6 = listOf(
        "P6 - M8, mm",
    )

    private val titlesMaxiMoldingsP7 = listOf(
        "P7 - M8, mm",
        "P7 - M9, mm",
    )

    private val toleranceStandardMoldingsP6 = arrayOf(
        DataStorage.PointTolerance(2.0, 1.0),
    )

    private val toleranceStandardMoldingsP7 = arrayOf(
        DataStorage.PointTolerance(3.0, 1.0),
        DataStorage.PointTolerance(3.5, 1.0),
    )

    private val toleranceMaxiMoldingsP6 = arrayOf(
        DataStorage.PointTolerance(2.5, 1.0),
    )

    private val toleranceMaxiMoldingsP7 = arrayOf(
        DataStorage.PointTolerance(3.0, 1.0),
        DataStorage.PointTolerance(4.5, 1.0),
    )

    // Maps

    private val mapStandardAlignedP6 = listOf(
        Pair(3, -1),
        Pair(4, -1),
        Pair(5, -1),
        Pair(6, -1),
        Pair(7, -1),
        Pair(8, -1),
        Pair(9, -1),
        Pair(10, -1),
        Pair(11, -1),
    )

    private val mapStandardAlignedP7 = listOf(
        Pair(12, -1),
        Pair(13, -1),
        Pair(14, -1),
        Pair(15, -1),
    )

    private val mapMaxiAlignedP6 = listOf(
        Pair(3, -1),
        Pair(4, -1),
        Pair(5, -1),
        Pair(6, -1),
        Pair(7, -1),
        Pair(8, -1),
        Pair(9, -1),
        Pair(10, -1),
        Pair(11, -1),
        Pair(12, -1),
        Pair(13, -1),
    )

    private val mapMaxiAlignedP7 = listOf(
        Pair(14, -1),
        Pair(15, -1),
        Pair(16, -1),
        Pair(17, -1),
    )

    // Maps - RAW

    private val mapStandardRawP6 = listOf(
        Pair(17, -1),
        Pair(18, 17),
        Pair(19, 17),
        Pair(20, 17),
        Pair(21, 17),
        Pair(22, 17),
        Pair(23, 17),
        Pair(24, 17),
        Pair(26, 17),
    )

    private val mapStandardRawP7 = listOf(
        Pair(27, -1),
        Pair(29, 27),
        Pair(30, 27),
        Pair(32, 27),
    )

    private val mapMaxiRawP6 = listOf(
        Pair(19, -1),
        Pair(20, 19),
        Pair(21, 19),
        Pair(22, 19),
        Pair(23, 19),
        Pair(24, 19),
        Pair(25, 19),
        Pair(26, 19),
        Pair(27, 19),
        Pair(28, 19),
        Pair(30, 19),
    )

    private val mapMaxiRawP7 = listOf(
        Pair(31, -1),
        Pair(33, 31),
        Pair(34, 31),
        Pair(36, 31),
    )

    // Maps - Cutting

    private val mapStandardCuttingP6 = listOf(
        Pair(25, 17),
        Pair(18, 17),
        Pair(19, 18),
        Pair(20, 19),
        Pair(21, 20),
        Pair(22, 21),
        Pair(23, 22),
        Pair(24, 23),
        Pair(25, 24),
    )

    private val mapStandardCuttingP7 = listOf(
        Pair(31, 28),
        Pair(29, 28),
        Pair(30, 29),
        Pair(31, 30),
    )

    private val mapMaxiCuttingP6 = listOf(
        Pair(29, 19),
        Pair(20, 19),
        Pair(21, 20),
        Pair(22, 21),
        Pair(23, 22),
        Pair(24, 23),
        Pair(25, 24),
        Pair(26, 25),
        Pair(27, 26),
        Pair(28, 27),
        Pair(29, 28),
    )

    private val mapMaxiCuttingP7 = listOf(
        Pair(35, 32),
        Pair(33, 32),
        Pair(34, 33),
        Pair(35, 34),
    )

    // Maps - Moldings

    private  val mapStandardMoldingsP7 = listOf(
        Pair(28, 27),
        Pair(32, 31),
    )

    private val mapStandardMoldingsP6 = listOf(
        Pair(26, 25),
    )

    private val mapMaxiMoldingsP7 = listOf(
        Pair(32, 31),
        Pair(36, 35),
    )

    private val mapMaxiMoldingsP6 = listOf(
        Pair(30, 29),
    )

    //

    private fun refreshGraph() {

        val graphDataP6 = HistoryViewCanvas.GraphData()
        val graphDataP7 = HistoryViewCanvas.GraphData()

        if (currentDataPosition < 0) {
            currentData = null
            viewGraphP6.setData(graphDataP6)
            viewGraphP7.setData(graphDataP7)
            return
        }

        val pointsData = historyData[currentDataPosition]
        currentData = pointsData

        graphDataP6.timeStamp = getSafe(pointsData, 0)
        graphDataP6.title = getSafe(pointsData, 1)
        graphDataP6.sideLR = getSafe(pointsData, 2)

        val titlesStandardP6: List<String>
        val titlesStandardP7: List<String>
        val titlesMaxiP6: List<String>
        val titlesMaxiP7: List<String>
        val tolerancesStandardP6: Array<DataStorage.PointTolerance>
        val tolerancesStandardP7: Array<DataStorage.PointTolerance>
        val tolerancesMaxiP6: Array<DataStorage.PointTolerance>
        val tolerancesMaxiP7: Array<DataStorage.PointTolerance>
        val mapStandardPointsP6: List<Pair<Int,Int>>
        val mapStandardPointsP7: List<Pair<Int,Int>>
        val mapMaxiPointsP6: List<Pair<Int,Int>>
        val mapMaxiPointsP7: List<Pair<Int,Int>>

        when (currentAnalysisType) {
            1 -> { // Sill Seal RAW
                graphDataP6.isPrecise = true
                graphDataP6.isZeroBase = true
                graphDataP7.isPrecise = true
                graphDataP7.isZeroBase = true
                tolerancesStandardP6 = DataStorage.getToleranceStandardP6()
                tolerancesStandardP7 = DataStorage.getToleranceStandardP7()
                tolerancesMaxiP6 = DataStorage.getToleranceMaxiP6()
                tolerancesMaxiP7 = DataStorage.getToleranceMaxiP7()
                titlesStandardP6 = titlesSillSealP6
                titlesStandardP7 = titlesSillSealP7
                titlesMaxiP6 = titlesSillSealP6
                titlesMaxiP7 = titlesSillSealP7
                mapStandardPointsP6 = mapStandardRawP6
                mapStandardPointsP7 = mapStandardRawP7
                mapMaxiPointsP6 = mapMaxiRawP6
                mapMaxiPointsP7 = mapMaxiRawP7
            }
            2 -> { // Cutting
                graphDataP6.isPrecise = true
                graphDataP6.isZeroBase = false
                graphDataP7.isPrecise = true
                graphDataP7.isZeroBase = false
                tolerancesStandardP6 = toleranceStandardCuttingP6
                tolerancesStandardP7 = toleranceStandardCuttingP7
                tolerancesMaxiP6 = toleranceMaxiCuttingP6
                tolerancesMaxiP7 = toleranceMaxiCuttingP7
                titlesStandardP6 = titlesCuttingP6
                titlesStandardP7 = titlesCuttingP7
                titlesMaxiP6 = titlesCuttingP6
                titlesMaxiP7 = titlesCuttingP7
                mapStandardPointsP6 = mapStandardCuttingP6
                mapStandardPointsP7 = mapStandardCuttingP7
                mapMaxiPointsP6 = mapMaxiCuttingP6
                mapMaxiPointsP7 = mapMaxiCuttingP7
            }
            3 -> { // Moldings
                graphDataP6.isPrecise = true
                graphDataP6.isZeroBase = false
                graphDataP7.isPrecise = true
                graphDataP7.isZeroBase = false
                tolerancesStandardP6 = toleranceStandardMoldingsP6
                tolerancesStandardP7 = toleranceStandardMoldingsP7
                tolerancesMaxiP6 = toleranceMaxiMoldingsP6
                tolerancesMaxiP7 = toleranceMaxiMoldingsP7
                titlesStandardP6 = titlesStandardMoldingsP6
                titlesStandardP7 = titlesStandardMoldingsP7
                titlesMaxiP6 = titlesMaxiMoldingsP6
                titlesMaxiP7 = titlesMaxiMoldingsP7
                mapStandardPointsP6 = mapStandardMoldingsP6
                mapStandardPointsP7 = mapStandardMoldingsP7
                mapMaxiPointsP6 = mapMaxiMoldingsP6
                mapMaxiPointsP7 = mapMaxiMoldingsP7
            }
            else -> { // Sill Seal aligned
                graphDataP6.isPrecise = false
                graphDataP6.isZeroBase = true
                graphDataP7.isPrecise = false
                graphDataP7.isZeroBase = true
                tolerancesStandardP6 = DataStorage.getToleranceStandardP6()
                tolerancesStandardP7 = DataStorage.getToleranceStandardP7()
                tolerancesMaxiP6 = DataStorage.getToleranceMaxiP6()
                tolerancesMaxiP7 = DataStorage.getToleranceMaxiP7()
                titlesStandardP6 = titlesSillSealP6
                titlesStandardP7 = titlesSillSealP7
                titlesMaxiP6 = titlesSillSealP6
                titlesMaxiP7 = titlesSillSealP7
                mapStandardPointsP6 = mapStandardAlignedP6
                mapStandardPointsP7 = mapStandardAlignedP7
                mapMaxiPointsP6 = mapMaxiAlignedP6
                mapMaxiPointsP7 = mapMaxiAlignedP7
            }
        }

        when (graphDataP6.title) {
            DataStorage.getStorageStandard().title -> {
                graphDataP6.points = decodePoints(titlesStandardP6, pointsData, mapStandardPointsP6, tolerancesStandardP6)
            }
            DataStorage.getStorageMaxi().title -> {
                graphDataP6.points = decodePoints(titlesMaxiP6, pointsData, mapMaxiPointsP6, tolerancesMaxiP6)
            }
        }

        viewGraphP6.setData(graphDataP6)

        graphDataP7.timeStamp = graphDataP6.timeStamp
        graphDataP7.title = graphDataP6.title
        graphDataP7.sideLR = graphDataP6.sideLR

        when (graphDataP7.title) {
            DataStorage.getStorageStandard().title -> {
                graphDataP7.points = decodePoints(titlesStandardP7, pointsData, mapStandardPointsP7, tolerancesStandardP7)
            }
            DataStorage.getStorageMaxi().title -> {
                graphDataP7.points = decodePoints(titlesMaxiP7, pointsData, mapMaxiPointsP7, tolerancesMaxiP7)
            }
        }

        viewGraphP7.setData(graphDataP7)
    }


    private fun refreshHistory() {
        val context = requireContext()

        val message: String = context.resources.getString(R.string.load_msg)
        Snackbar.make(viewOfLayout, message, 500).show()

        //val historyLimit = 100
        var historyLines: List<String> = emptyList()

        val file = getHistoryFile(context)
        if (file.exists()) {
            val fileLines = file.readLines()
            historyLines = fileLines.asReversed()
            //historyLines = fileLines.asReversed().take(historyLimit)
        }

        historyData = List(historyLines.size) {
            historyLines[it].split(historyDelimiter)
        }

        val prefixSize = 3
        val separator = " "

        historyTitles.clear()
        historyData.forEach {
            historyTitles.add(it.take(prefixSize).joinToString(separator))
        }

        historySpinnerAdapter.notifyDataSetChanged()
    }


    private fun decodePoints(
        pointsTitles: List<String>,
        pointsData: List<String>,
        indexesValues: List<Pair<Int, Int>>,
        tolerances: Array<DataStorage.PointTolerance>
    ): Array<HistoryViewCanvas.GraphPoint> {
        return Array(tolerances.size) { index ->
            val pointValue = decodeValue(index, indexesValues, pointsData)
            HistoryViewCanvas.GraphPoint(
                value = pointValue,
                result = PointsAligner.testPoint(pointValue, tolerances[index]),
                title = pointsTitles[index],
                tolerance = tolerances[index],
            )
        }
    }

    private fun decodeValue(
        index: Int,
        indexesRemap: List<Pair<Int,Int>>,
        pointsData: List<String>
    ): Double {
        val (indexValue, indexZero) = indexesRemap[index]
        val textValue = getSafe(pointsData, indexValue)
        val textZero = getSafe(pointsData, indexZero)
        val value = PointData.valueFromIntString(textValue)
        val zero = PointData.valueFromIntString(textZero)
        return (value - zero)
    }


    private fun copyToClipboard() {
        if (currentData == null)
            return

        val clipboardManager = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("", currentData!!.joinToString("\t"))
        clipboardManager.setPrimaryClip(clipData)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) // Only show a toast for Android 12 and lower.
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }


    fun onSettingsChange() {
        refreshGraph()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment StandardFragment.
         */
        @JvmStatic
        fun newInstance() =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                }
            }


        private const val historyFileName = "history.tsv"
        private const val historyDelimiter = '\t'


        private fun getHistoryFile(context: Context): File {
            val filesDir = context.getExternalFilesDir(null)
            return File(filesDir, historyFileName)
        }


        fun checkHistory(
            context: Context
        )  {
            val file = getHistoryFile(context)
            if (! file.exists()) {
                file.createNewFile()
            }
        }


        fun generateTimeStamp(): String {
            return DateTimeFormatter.ofPattern("dd-MM-yy HH:mm").format(LocalDateTime.now())
        }


        fun savePoints(
            fragment: Fragment,
            dataStorage: DataStorage.DataSubSet,
            currentStorage: DataStorage.SillSealData
        ) {
            val context = fragment.requireContext()
            val file = getHistoryFile(context)

            if (file.exists()) {
                val line = buildString {
                    append(currentStorage.timeStamp)
                    append(historyDelimiter)
                    append(dataStorage.title)
                    append(historyDelimiter)
                    append(currentStorage.title.first)
                    writePointsToFile(this, currentStorage.sectionP6.points, dataStorage.toleranceMapP6)
                    writePointsToFile(this, currentStorage.sectionP7.points, dataStorage.toleranceMapP7)
                    append(historyDelimiter)
                    writePointsRawToFile(this, currentStorage.sectionP6.points)
                    writePointsRawToFile(this, currentStorage.sectionP7.points)
                    appendLine()
                }

                file.appendText(line)
            }
        }

        private fun writePointsToFile(
            builder: StringBuilder,
            pointsData: Array<PointData>,
            toleranceMap: List<Int>,
        ) {
            toleranceMap.forEach { pointIndex ->
                val point = pointsData[pointIndex]
                builder.append(historyDelimiter)
                builder.append(PointData.valueToIntString(point.value))
                //if (it == 0) {
                //    builder.append("!")
                //} else if (point.result != DataStorage.PointResult.OK) {
                //    builder.append("*")
                // }
            }
        }

        private fun writePointsRawToFile(
            builder: StringBuilder,
            pointsData: Array<PointData>
        ) {
            pointsData.forEach { point ->
                builder.append(historyDelimiter)
                builder.append(PointData.valueToIntString(point.rawValue))
            }
        }


        private fun getSafe(
            list: List<String>,
            index: Int
        ): String {
            return list.getOrElse(index) { "" }
        }
    }
}