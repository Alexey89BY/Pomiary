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
    private lateinit var viewGraph: HistoryViewCanvas
    private lateinit var historySpinner: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<String>
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

        val switchRaw = viewOfLayout.findViewById<SwitchMaterial>(R.id.switchShowRaw)
        switchRaw.setOnCheckedChangeListener { _, checked ->
            viewGraph.setDrawRaw(checked)
            viewGraph.invalidate()
        }

        val switchGraphs = viewOfLayout.findViewById<SwitchMaterial>(R.id.switchValuesOnly)
        switchGraphs.setOnCheckedChangeListener { _, checked ->
            viewGraph.setValuesOnly(checked)
            viewGraph.invalidate()
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
            if (newIndex < spinnerAdapter.count)
                historySpinner.setSelection(newIndex)
        }

        viewGraph = HistoryViewCanvas(activity)
        viewGraph.setDrawRaw(switchRaw.isChecked)
        viewOfLayout.findViewById<LinearLayout>(R.id.containerGraph).addView(viewGraph)

        historySpinner = viewOfLayout.findViewById(R.id.spinnerGraph)
        historySpinner.onItemSelectedListener = spinnerListener

        spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, historyTitles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        historySpinner.adapter = spinnerAdapter

        return viewOfLayout
    }


    private val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?, view: View?, position: Int, id: Long
        ) {
            val graphData = HistoryViewCanvas.GraphData()

            val pointsData = historyData[position]
            graphData.timeStamp = getSafe(pointsData, 0)
            graphData.title = getSafe(pointsData, 1)
            graphData.sideLR = getSafe(pointsData, 2)

            when (graphData.title) {
                DataStorage.getStorageStandard().title -> {
                    graphData.tolerancesP6 = DataStorage.getToleranceStandardP6()
                    graphData.tolerancesP7 = DataStorage.getToleranceStandardP7()

                    val mapAlignedP6 = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11)
                    val mapRawP6 = listOf(17, 18, 19, 20, 21, 22, 23, 24, 26)
                    graphData.pointsP6 = decodePoints(pointsData, mapAlignedP6, mapRawP6,  graphData.tolerancesP6)

                    val mapAlignedP7 = listOf(12, 13, 14, 15)
                    val mapRawP7 = listOf(27, 29, 30, 32)
                    graphData.pointsP7 = decodePoints(pointsData, mapAlignedP7, mapRawP7, graphData.tolerancesP7)
                }
                DataStorage.getStorageMaxi().title -> {
                    graphData.tolerancesP6 = DataStorage.getToleranceMaxiP6()
                    graphData.tolerancesP7 = DataStorage.getToleranceMaxiP7()

                    val mapAlignedP6 = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)
                    val mapRawP6 = listOf(19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 30)
                    graphData.pointsP6 = decodePoints(pointsData, mapAlignedP6, mapRawP6,  graphData.tolerancesP6)

                    val mapAlignedP7 = listOf(14, 15, 16, 17)
                    val mapRawP7 = listOf(31, 33, 34, 36)
                    graphData.pointsP7 = decodePoints(pointsData, mapAlignedP7, mapRawP7, graphData.tolerancesP7)
                }
            }

            viewGraph.setData(graphData)
            currentData = pointsData
            viewGraph.invalidate()
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {
            viewGraph.setData(HistoryViewCanvas.GraphData())
            currentData = null
            viewGraph.invalidate()
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

        spinnerAdapter.notifyDataSetChanged()
    }


    private fun decodePoints(
        pointsData: List<String>,
        indexesAligned: List<Int>,
        indexesRaw: List<Int>,
        tolerances: Array<DataStorage.PointTolerance>
    ): Array<PointData> {
        return Array(tolerances.size) { index ->
            val valueRaw = decodeValue(index, indexesRaw, pointsData)
            val valueAligned = decodeValue(index, indexesAligned, pointsData)
            val pointResult = PointsAligner.testPoint(valueAligned, tolerances[index])
            PointData(
                rawValue = valueRaw,
                value = valueAligned,
                result = pointResult
            )
        }
    }

    private fun decodeValue(
        index: Int,
        indexesRemap: List<Int>,
        pointsData: List<String>
    ): Double {
        val textIndex = indexesRemap[index]
        val textValue = getSafe(pointsData, textIndex)
        return PointData.valueFromIntString(textValue)
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