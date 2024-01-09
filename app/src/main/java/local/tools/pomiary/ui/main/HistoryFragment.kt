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
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointsAligner
import local.tools.pomiary.R
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class HistoryFragment : Fragment() {
    private lateinit var viewOfLayout: View
    private var historyData = emptyArray<List<String>>()
    private var currentData: List<String>? = null

    @Suppress("ArrayInDataClass")
    data class GraphData (
        //var title: String = String(),
        //var timeStamp: String = String(),
        var pointsP6: Array<DataStorage.PointData> = emptyArray(),
        var tolerancesP6: Array<DataStorage.PointTolerance> = emptyArray(),
        var pointsP7: Array<DataStorage.PointData> = emptyArray(),
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

        viewGraph = ViewCanvas(activity)
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
            when (itemData.size) {
                16 -> {
                    graphData.tolerancesP6 = DataStorage.getToleranceStandardP6()
                    graphData.tolerancesP7 = DataStorage.getToleranceStandardP7()
                    graphData.pointsP6 = decodePoints(itemData.slice(3..11), graphData.tolerancesP6)
                    graphData.pointsP7 = decodePoints(itemData.slice(12..15), graphData.tolerancesP7)
                }
                18 -> {
                    graphData.tolerancesP6 = DataStorage.getToleranceMaxiP6()
                    graphData.tolerancesP7 = DataStorage.getToleranceMaxiP7()
                    graphData.pointsP6 = decodePoints(itemData.slice(3..13), graphData.tolerancesP6)
                    graphData.pointsP7 = decodePoints(itemData.slice(14..17), graphData.tolerancesP7)
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
        tolerances: Array<DataStorage.PointTolerance>
    ): Array<DataStorage.PointData> {
        return Array(tolerances.size) { index ->
            val rawInput = pointsText[index]
            val value = rawInput.toDoubleOrNull() ?: 0.0
            val result = PointsAligner.testPoint(value, tolerances[index])
            DataStorage.PointData(rawInput, value, result)
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
            return DateTimeFormatter.ofPattern("dd.MM.yy HH:mm").format(LocalDateTime.now())
        }


        fun savePoints(
            fragment: Fragment,
            subsetTitle: String,
            dataTitle: String,
            timeStamp: String,
            points1: Array<DataStorage.PointData>,
            points2: Array<DataStorage.PointData>,
        ) {
            val context = fragment.requireContext()
            val file = getHistoryFile(context)

            if (file.exists()) {
                val line = buildString {
                    append(timeStamp)
                    append(historyDelimiter)
                    append(subsetTitle)
                    append(historyDelimiter)
                    append(dataTitle)
                    writePointsToFile(this, points1)
                    writePointsToFile(this, points2)
                    appendLine()
                }

                file.appendText(line)
            }
        }

        private fun writePointsToFile(builder: StringBuilder, pointsArray: Array<DataStorage.PointData>) {
            pointsArray.forEachIndexed {_, it ->
                //val nokText = "*"
                //val baseText = "!"
                builder.append(historyDelimiter)
                builder.append(String.format(Locale.US, "%.2f", it.value))
                //if (index == 0) {
                //    builder.append(baseText)
                //} else
                //if (it.result != DataStorage.PointResult.OK) {
                //    builder.append(nokText)
                // }
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

        var graphData: GraphData = GraphData()

        fun setData(data: GraphData)
        {
            graphData = data
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
            point: DataStorage.PointData,
            tolerance: DataStorage.PointTolerance,
            isBasePoint: Boolean
        ) {
            val dyt = paint.textSize * 1.2F
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
            canvas.drawText(title, offsetX, offsetY + dyt, paint)

            // draw tolerance
            val pointZero = if (isBasePoint) 0.0 else tolerance.origin
            val ptl = tolerance.origin - tolerance.offset
            val ptr = tolerance.origin + tolerance.offset
            val dxl = x0 + (ptl - pointZero).toFloat() * scale
            val dxr = x0 + (ptr - pointZero).toFloat() * scale
            val dyb = 35F
            paint.color = Color.WHITE

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
            val xp = x0 + (point.value - pointZero).toFloat() * scale
            val dpy = 35F
            val dpx = 15F
            paint.color = if (isBasePoint) Color.WHITE else PointsAligner.colorByResult(point.result)
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
            val valueString  = String.format("%.1f",
                point.value
            )
            canvas.drawText(valueString, offsetX + hw, offsetY + 1F * dyt, paint)
        }
    }
}