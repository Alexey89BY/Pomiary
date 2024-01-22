package local.tools.pomiary.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private var historyData = emptyArray<List<String>>()
    private var currentData: List<String>? = null

    @Suppress("ArrayInDataClass")
    data class GraphData (
        //var title: String = String(),
        //var timeStamp: String = String(),
        var pointsP6: Array<PointData> = emptyArray(),
        var tolerancesP6: Array<DataStorage.PointTolerance> = emptyArray(),
        var pointsP7: Array<PointData> = emptyArray(),
        var tolerancesP7: Array<DataStorage.PointTolerance> = emptyArray(),
    )

    private lateinit var viewGraph: ViewCanvas

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

        val refreshButton = viewOfLayout.findViewById<FloatingActionButton>(R.id.buttonRefreshHistory)
        refreshButton.setOnClickListener { refreshHistory() }

        val copyButton = viewOfLayout.findViewById<ImageButton>(R.id.buttonCopy)
        copyButton.setOnClickListener { copyToClipboard() }

        val switchRaw = viewOfLayout.findViewById<SwitchMaterial>(R.id.switchShowRaw)
        switchRaw.setOnCheckedChangeListener { _, checked ->
            viewGraph.setDrawRaw(checked)
            viewGraph.invalidate()
        }

        viewGraph = ViewCanvas(activity)
        viewGraph.setDrawRaw(switchRaw.isChecked)
        viewOfLayout.findViewById<LinearLayout>(R.id.containerGraph).addView(viewGraph)

        val spinner = viewOfLayout.findViewById<Spinner>(R.id.spinnerGraph)
        spinner.onItemSelectedListener = spinnerListener

        return viewOfLayout
    }


    private val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?, view: View?, position: Int, id: Long
        ) {
            val itemData = historyData[position]

            val graphData = GraphData()
            when (itemData[1]) {
                DataStorage.getStorageStandard().title -> {
                    graphData.tolerancesP6 = DataStorage.getToleranceStandardP6()
                    graphData.tolerancesP7 = DataStorage.getToleranceStandardP7()

                    val mapAlignedP6 = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11)
                    val mapRawP6 = listOf(17, 18, 19, 20, 21, 22, 23, 24, 26)
                    graphData.pointsP6 = decodePoints(itemData, mapAlignedP6, mapRawP6,  graphData.tolerancesP6)

                    val mapAlignedP7 = listOf(12, 13, 14, 15)
                    val mapRawP7 = listOf(27, 29, 30, 32)
                    graphData.pointsP7 = decodePoints(itemData, mapAlignedP7, mapRawP7, graphData.tolerancesP7)
                }
                DataStorage.getStorageMaxi().title -> {
                    graphData.tolerancesP6 = DataStorage.getToleranceMaxiP6()
                    graphData.tolerancesP7 = DataStorage.getToleranceMaxiP7()

                    val mapAlignedP6 = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)
                    val mapRawP6 = listOf(19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 30)
                    graphData.pointsP6 = decodePoints(itemData, mapAlignedP6, mapRawP6,  graphData.tolerancesP6)

                    val mapAlignedP7 = listOf(14, 15, 16, 17)
                    val mapRawP7 = listOf(31, 33, 34, 36)
                    graphData.pointsP7 = decodePoints(itemData, mapAlignedP7, mapRawP7, graphData.tolerancesP7)
                }
            }

            viewGraph.setData(graphData)
            currentData = itemData
            viewGraph.invalidate()
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {
            viewGraph.setData(GraphData())
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

        historyData = Array(historyLines.size) {
            historyLines[it].split(historyDelimiter)
        }

        val prefixSize = 3
        val separator = " "
        val historyTitles = Array(historyData.size) {
            val number = it+1
            "$number) " + historyData[it].take(prefixSize).joinToString(separator)
        }

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, historyTitles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = viewOfLayout.findViewById<Spinner>(R.id.spinnerGraph)
        spinner.adapter = spinnerAdapter
    }


    private fun decodePoints(
        pointsText: List<String>,
        indexesAligned: List<Int>,
        indexesRaw: List<Int>,
        tolerances: Array<DataStorage.PointTolerance>
    ): Array<PointData> {
        return Array(tolerances.size) { index ->
            val valueRaw = decodeValue(index, indexesRaw, pointsText)
            val valueAligned = decodeValue(index, indexesAligned, pointsText)
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
        pointsText: List<String>
    ): Double {
        val textIndex = indexesRemap[index]
        return if (textIndex < pointsText.size) {
            pointsText[textIndex].toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }
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
                builder.append(PointData.valueToString(point.value))
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
                builder.append(PointData.valueToString(point.rawValue))
            }
        }
    }


    private class ViewCanvas(context: Context?) : View(context) {
        var paint = Paint()

        val titlesP6 = listOf(
            resources.getString(R.string.text_P6_0),
            resources.getString(R.string.text_P6_1),
            resources.getString(R.string.text_P6_2),
            resources.getString(R.string.text_P6_3),
            resources.getString(R.string.text_P6_4),
            resources.getString(R.string.text_P6_5),
            resources.getString(R.string.text_P6_6),
            resources.getString(R.string.text_P6_7),
            resources.getString(R.string.text_P6_8),
            resources.getString(R.string.text_P6_9),
            resources.getString(R.string.text_P6_10),
        )

        val titlesP7 = listOf(
            resources.getString(R.string.text_P7_0),
            resources.getString(R.string.text_P7_1),
            resources.getString(R.string.text_P7_2),
            resources.getString(R.string.text_P7_3),
        )

        var pointInRow = 1
        var pointWidth = 0
        val pointHeight = 210
        var isPointsRaw = false
        var graphData: GraphData = GraphData()

        fun setData(data: GraphData)
        {
            graphData = data
        }


        fun setDrawRaw(isRaw: Boolean) {
            isPointsRaw = isRaw
        }


        init {
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    pointInRow = 5
                    pointWidth = 450
                    minimumWidth = 60 + pointInRow * pointWidth
                    minimumHeight = 4 * pointHeight
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    pointInRow = 2
                    pointWidth = 500
                    minimumWidth = 60 + pointInRow * pointWidth
                    minimumHeight = 8 * pointHeight
                }
                else -> {

                }
            }
        }

        public override fun onDraw(canvas: Canvas) {

            paint.color = Color.WHITE
            paint.textSize = 38F

            //canvas.drawText(graphData.title, 40F, 40F, paint)
            //canvas.drawText(graphData.timeStamp, 350F, 40F, paint)

            paint.strokeWidth = 3F
            paint.textSize = 32F

            val offsetX = 30F
            val offsetYP6 = 0F

            graphData.pointsP6.forEachIndexed { index, point ->
                val x = offsetX + pointWidth * index.mod(pointInRow)
                val y = offsetYP6 + pointHeight * index.div(pointInRow)
                drawDataPoint(canvas, x, y, titlesP6[index], point, graphData.tolerancesP6[index], index == 0)
            }

            val offsetYP7 = offsetYP6 + pointHeight * (graphData.pointsP6.size.div(pointInRow) + if (graphData.pointsP6.size.mod(pointInRow) != 0) 1 else 0)

            graphData.pointsP7.forEachIndexed {index, point ->
                val x = offsetX + pointWidth * index.mod(pointInRow)
                val y = offsetYP7 + pointHeight * index.div(pointInRow)
                drawDataPoint(canvas, x, y, titlesP7[index], point, graphData.tolerancesP7[index], index == 0)
            }
        }


        private fun drawDataPoint(
            canvas: Canvas,
            offsetX: Float,
            offsetY: Float,
            title: String,
            point: PointData,
            tolerance: DataStorage.PointTolerance,
            isBasePoint: Boolean
        ) {
            val dyt = paint.textSize * 1.2F
            paint.color = Color.WHITE
            canvas.drawText(title, offsetX, offsetY + dyt, paint)

            val hw = pointWidth * 0.5F
            val scale = pointWidth / (10F) // in point +-5 mm
            val dxw = hw - 15F
            val x0 = offsetX + hw
            val y0 = offsetY + 3.5F * dyt

            // draw units
            val dy0 = 20F
            val dyu = 12.5F
            paint.color = Color.GRAY
            canvas.drawLine(x0 - dxw, y0, x0 + dxw, y0, paint)
            canvas.drawLine(x0, y0, x0, y0 + dy0, paint)
            var xu = scale
            while (xu < dxw) {
                canvas.drawLine(x0 + xu, y0, x0 + xu, y0 + dyu, paint)
                canvas.drawLine(x0 - xu, y0, x0 - xu, y0 + dyu, paint)
                xu += scale
            }

            // draw tolerance
            val pointZero = if (isBasePoint) 0.0 else tolerance.origin
            val ptl = tolerance.origin - tolerance.offset
            val ptr = tolerance.origin + tolerance.offset
            val dxl = x0 + (ptl - pointZero).toFloat() * scale
            val dxr = x0 + (ptr - pointZero).toFloat() * scale
            val dyb = 35F

            canvas.drawLine(dxl, y0, dxl, y0 + dyb, paint)
            canvas.drawLine(dxr, y0, dxr, y0 + dyb, paint)

            val toleranceString1  = String.format("%.1f\u2026%.1f",
                ptl,
                ptr
            )
            canvas.drawText(toleranceString1, offsetX, offsetY + 2F * dyt, paint)

            if (! isBasePoint) {
                val toleranceString2 = String.format("%.1f\u00B1%.1f",
                    tolerance.origin,
                    tolerance.offset,
                )
                canvas.drawText(toleranceString2, offsetX + hw, offsetY + 2F * dyt, paint)
            }

            // draw point
            val yp = y0 - paint.strokeWidth
            val pointValue =
                if (isPointsRaw) point.rawValue
                else point.value
            val xp = x0 + (pointValue - pointZero).toFloat() * scale
            val dpy = 35F
            val dpx = 15F
            paint.color =
                if (isBasePoint) Color.LTGRAY
                else if (isPointsRaw) Color.WHITE
                else point.result.toColor()
            val xpl = x0 - dxw
            val xpr = x0 + dxw

            val path = Path()
            when {
                xp < xpl -> {
                    path.moveTo(xpl, yp)
                    path.lineTo(xpl, yp - dpy)
                    path.lineTo(xpl + dpx, yp - dpy)
                }
                xp > xpr -> {
                    path.moveTo(xpr, yp)
                    path.lineTo(xpr - dpx, yp - dpy)
                    path.lineTo(xpr, yp - dpy)
                }
                else -> {
                    path.moveTo(xp, yp)
                    path.lineTo(xp - dpx, yp - dpy)
                    path.lineTo(xp + dpx, yp - dpy)
                }
            }
            canvas.drawPath(path, paint)

            val valueString  =
                if (isPointsRaw) String.format("%.2f", pointValue)
                else String.format("%.1f", pointValue)
            canvas.drawText(valueString, offsetX + hw, offsetY + 1F * dyt, paint)
        }
    }
}