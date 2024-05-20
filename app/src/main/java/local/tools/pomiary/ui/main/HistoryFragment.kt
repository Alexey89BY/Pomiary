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
        historySpinner.onItemSelectedListener = spinnerListener



        return viewOfLayout
    }


    private val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?, view: View?, position: Int, id: Long
        ) {
            val graphDataP6 = HistoryViewCanvas.GraphData()
            val graphDataP7 = HistoryViewCanvas.GraphData()

            val pointsData = historyData[position]
            currentData = pointsData

            graphDataP6.timeStamp = getSafe(pointsData, 0)
            graphDataP6.title = getSafe(pointsData, 1)
            graphDataP6.sideLR = getSafe(pointsData, 2)

            val titlesSillSealP6 = listOf(
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

            val titlesSillSealP7 = listOf(
                "P7 (*), mm",
                "P7 (1), mm",
                "P7 (2), mm",
                "P7 (3), mm",
            )

            val titlesStandardP6 = titlesSillSealP6
            val titlesStandardP7 = titlesSillSealP7
            val titlesMaxiP6 = titlesSillSealP6
            val titlesMaxiP7 = titlesSillSealP7

            val tolerancesStandardP6 = DataStorage.getToleranceStandardP6()
            val tolerancesStandardP7 = DataStorage.getToleranceStandardP7()
            val tolerancesMaxiP6 = DataStorage.getToleranceMaxiP6()
            val tolerancesMaxiP7 = DataStorage.getToleranceMaxiP7()

            val mapStandardAlignedP6 = listOf(
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

            val mapStandardAlignedP7 = listOf(
                Pair(12, -1),
                Pair(13, -1),
                Pair(14, -1),
                Pair(15, -1),
            )

            val mapMaxiAlignedP6 = listOf(
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

            val mapMaxiAlignedP7 = listOf(
                Pair(14, -1),
                Pair(15, -1),
                Pair(16, -1),
                Pair(17, -1),
            )

            val mapStandardPointsP6 = mapStandardAlignedP6
            val mapStandardPointsP7 = mapStandardAlignedP7
            val mapMaxiPointsP6 = mapMaxiAlignedP6
            val mapMaxiPointsP7 = mapMaxiAlignedP7

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

        override fun onNothingSelected(parentView: AdapterView<*>?) {
            viewGraphP6.setData(HistoryViewCanvas.GraphData())
            viewGraphP7.setData(HistoryViewCanvas.GraphData())
            currentData = null
        }
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