package local.tools.pomiary.ui.main

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointsAligner
import local.tools.pomiary.R


/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlankFragment : Fragment() {

    @Suppress("ArrayInDataClass")
    data class GraphData (
        var title: String = String(),
        var timeStamp: String = String(),
        var pointsP6: Array<DataStorage.PointData> = emptyArray(),
        var tolerancesP6: Array<DataStorage.PointTolerance> = emptyArray(),
        var pointsP7: Array<DataStorage.PointData> = emptyArray(),
        var tolerancesP7: Array<DataStorage.PointTolerance> = emptyArray(),
    )


    private val spinnerItems: MutableList<String> = emptyList<String>().toMutableList()
    private val graphData1Items: MutableList<DataStorage.SillSealData> = emptyList<DataStorage.SillSealData>().toMutableList()
    private val graphData2Items: MutableList<DataStorage.DataSubSet> = emptyList<DataStorage.DataSubSet>().toMutableList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_blank, container, false)
        viewGraph = ViewCanvas(activity)
        rootView.findViewById<LinearLayout>(R.id.containerGraph).addView(viewGraph)

        addToSpinner(DataStorage.getStorageStandardLeft())
        addToSpinner(DataStorage.getStorageStandardRight())
        addToSpinner(DataStorage.getStorageMaxiLeft())
        addToSpinner(DataStorage.getStorageMaxiRight())

        val spinner = rootView.findViewById<Spinner>(R.id.spinnerGraph)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerItems)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                //switchGraph(position)

                graphData.title = graphData2Items[position].title
                graphData.timeStamp = graphData1Items[position].timeStamp
                graphData.pointsP6 = graphData1Items[position].sectionP6.points
                graphData.tolerancesP6 = graphData2Items[position].tolerancesP6
                graphData.pointsP7 = graphData1Items[position].sectionP7.points
                graphData.tolerancesP7 = graphData2Items[position].tolerancesP7

                viewGraph.invalidate()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                //switchGraph(-1)
                graphData = GraphData()
                viewGraph.invalidate()
            }
        }



        return rootView
    }


    private fun addToSpinner(dataSubset: DataStorage.DataSubSet) {
        dataSubset.data.forEachIndexed { _, sillSealData ->
            spinnerItems.add(DataStorage.getStorageDataTitle(dataSubset, sillSealData))
            graphData1Items.add(sillSealData)
            graphData2Items.add(dataSubset)
        }
    }


    companion object {
        private var graphData: GraphData = GraphData()
        private lateinit var viewGraph: ViewCanvas
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BlankFragment.
         */
        @JvmStatic
        fun newInstance() =
            BlankFragment().apply {
                arguments = Bundle().apply {
                }
            }


        fun setData(
            title: String,
            timeStamp: String,
            pointsP6: Array<DataStorage.PointData>,
            tolerancesP6: Array<DataStorage.PointTolerance>,
            pointsP7: Array<DataStorage.PointData>,
            tolerancesP7: Array<DataStorage.PointTolerance>
        ) {
            graphData.title = title
            graphData.timeStamp = timeStamp
            graphData.pointsP6 = pointsP6
            graphData.tolerancesP6 = tolerancesP6
            graphData.pointsP7 = pointsP7
            graphData.tolerancesP7 = tolerancesP7
            viewGraph.invalidate()
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


        init {
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    pointInRow = 5
                    pointWidth = 440
                    minimumWidth = 80 + pointInRow * pointWidth
                    minimumHeight = 60 + 4 * pointHeight
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    pointInRow = 2
                    pointWidth = 500
                    minimumWidth = 80 + pointInRow * pointWidth
                    minimumHeight = 60 + 8 * pointHeight
                }
                else -> {

                }
            }
        }

        public override fun onDraw(canvas: Canvas) {

            paint.color = Color.WHITE
            paint.textSize = 38F

            canvas.drawText(graphData.title, 40F, 40F, paint)
            canvas.drawText(graphData.timeStamp, 300F, 40F, paint)

            paint.strokeWidth = 3F
            paint.textSize = 32F

            val offsetX = 40F
            val offsetYP6 = 80F

            graphData.pointsP6.forEachIndexed { index, point ->
                val x = offsetX + pointWidth * index.mod(pointInRow)
                val y = offsetYP6 + pointHeight * index.div(pointInRow)
                drawDataPoint(canvas, x, y, titlesP6[index], point, graphData.tolerancesP6[index])
            }

            val offsetYP7 = offsetYP6 + pointHeight * (graphData.pointsP6.size.div(pointInRow) + if (graphData.pointsP6.size.mod(pointInRow) != 0) 1 else 0)

            graphData.pointsP7.forEachIndexed {index, point ->
                val x = offsetX + pointWidth * index.mod(pointInRow)
                val y = offsetYP7 + pointHeight * index.div(pointInRow)
                drawDataPoint(canvas, x, y, titlesP7[index], point, graphData.tolerancesP7[index])
            }
        }


        private fun drawDataPoint(
            canvas: Canvas,
            offsetX: Float,
            offsetY: Float,
            title: String,
            point: DataStorage.PointData,
            tolerance: DataStorage.PointTolerance
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
            val dxb = tolerance.offset * scale
            val dyb = 35F
            paint.color = Color.WHITE
            canvas.drawLine(x0 - dxb, y0, x0 - dxb, y0 + dyb, paint)
            canvas.drawLine(x0 + dxb, y0, x0 + dxb, y0 + dyb, paint)
            val toleranceString1  = buildString { append("%.1f\u2026%.1f") }.format(
                tolerance.origin - tolerance.offset,
                tolerance.origin + tolerance.offset
            )
            canvas.drawText(toleranceString1, offsetX, offsetY + 2F * dyt, paint)
            val toleranceString2  = buildString { append("%.1f\u00B1%.1f") }.format(
                tolerance.origin,
                tolerance.offset,
            )
            canvas.drawText(toleranceString2, offsetX + hw, offsetY + 2F * dyt, paint)

            // draw point
            val yp = y0 - paint.strokeWidth
            val xp = x0 + (point.value - tolerance.origin) * scale
            val dpy = 35F
            val dpx = 15F
            paint.color = PointsAligner.colorByResult(point.result)
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
            val valueString  = buildString { append("%.1f") }.format(
                point.value
            )
            canvas.drawText(valueString, offsetX + hw, offsetY + 1F * dyt, paint)
        }
    }
}


