package local.tools.pomiary.ui.main

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointData
import local.tools.pomiary.R


class HistoryViewCanvas(context: Context?) : View(context) {
    @Suppress("ArrayInDataClass")
    data class GraphData (
        var title: String = String(),
        var timeStamp: String = String(),
        var sideLR: String = String(),
        var pointsP6: Array<PointData> = emptyArray(),
        var tolerancesP6: Array<DataStorage.PointTolerance> = emptyArray(),
        var pointsP7: Array<PointData> = emptyArray(),
        var tolerancesP7: Array<DataStorage.PointTolerance> = emptyArray(),
    )

    private val paint = Paint()
    private val path = Path()

    private val titlesP6 = listOf(
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

    private val titlesP7 = listOf(
        resources.getString(R.string.text_P7_0),
        resources.getString(R.string.text_P7_1),
        resources.getString(R.string.text_P7_2),
        resources.getString(R.string.text_P7_3),
    )

    private var pointInRow = 1
    private var pointWidth = 0
    private val pointHeight = 200
    private var isPointsRaw = false
    private var isPointsGraph = false
    private var graphData = GraphData()


    init {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                pointInRow = 5
                pointWidth = 450
                minimumWidth = 60 + pointInRow * pointWidth
                minimumHeight = 4 * pointHeight + 55
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                pointInRow = 2
                pointWidth = 500
                minimumWidth = 60 + pointInRow * pointWidth
                minimumHeight = 8 * pointHeight + 55
            }
            else -> {

            }
        }
    }


    fun setData(data: GraphData)
    {
        graphData = data
    }


    fun setDrawRaw(isRaw: Boolean) {
        isPointsRaw = isRaw
    }


    fun setValuesOnly(isRaw: Boolean) {
        isPointsGraph = isRaw
    }


    public override fun onDraw(canvas: Canvas) {
        val offsetX = 30F

        val titleString = String.format(
            "%s %s %s",
            graphData.timeStamp,
            graphData.title,
            graphData.sideLR
        )
        paint.color = Color.GRAY
        paint.textSize = 38F
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(titleString, offsetX, 40F, paint)

        paint.strokeWidth = 3F

        val offsetYP6 = 55F

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
        val ptl = tolerance.origin - tolerance.offset
        val ptr = tolerance.origin + tolerance.offset
        val pointValue = if (isPointsRaw) point.rawValue
        else point.value

        val pointColor = if (isBasePoint) Color.LTGRAY
        else if (isPointsRaw) Color.WHITE
        else point.result.toColor()

        val toleranceString1 = String.format(
            "%.1f\u2026%.1f",
            ptl,
            ptr
        )

        val toleranceString2 = String.format(
            "%.1f\u00B1%.1f",
            tolerance.origin,
            tolerance.offset,
        )

        val valueString =
            if (isPointsRaw) String.format("%.2f", pointValue)
            else String.format("%.1f", pointValue)

        paint.textSize = 34F
        paint.textAlign = Paint.Align.LEFT

        val dyt = paint.textSize * 1.2F
        val hw = pointWidth * 0.5F

        // draw title
        paint.color = Color.WHITE
        canvas.drawText(title, offsetX, offsetY + dyt, paint)

        if (isPointsGraph) {

            // draw tolerance
            paint.color = Color.GRAY
            canvas.drawText(toleranceString1, offsetX, offsetY + 2F * dyt, paint)
            if (!isBasePoint) {
                canvas.drawText(toleranceString2, offsetX, offsetY + 3F * dyt, paint)
            }

            // draw result
            paint.color = pointColor

            val pointMessage = if (isBasePoint) ""
            else if (isPointsRaw) ""
            else point.result.toMessage()

            canvas.drawText(pointMessage, offsetX + hw, offsetY + 1F * dyt, paint)

            // draw point
            val xp = offsetX + pointWidth - 30F
            val yp = offsetY + 3F * dyt

            paint.textSize = 72F
            paint.textAlign = Paint.Align.RIGHT

            canvas.drawText(valueString, xp, yp, paint)

        } else {

            val scale = pointWidth / (2.0F * DataStorage.getToleranceInvalid().toFloat()) // in point +-
            val dxw = hw - 15F
            val x0 = offsetX + hw
            val y0 = offsetY + 3.5F * dyt
            val xpl = x0 - dxw
            val xpr = x0 + dxw

            // draw units
            val dy0 = 20F
            val dyu = 12.5F

            paint.color = Color.GRAY
            canvas.drawLine(xpl, y0, xpr, y0, paint)
            canvas.drawLine(x0, y0, x0, y0 + dy0, paint)

            var xu = scale
            while (xu < dxw) {
                canvas.drawLine(x0 + xu, y0, x0 + xu, y0 + dyu, paint)
                canvas.drawLine(x0 - xu, y0, x0 - xu, y0 + dyu, paint)
                xu += scale
            }

            // draw tolerance
            val pointZero = if (isBasePoint) 0.0
            else tolerance.origin

            val dxl = x0 + (ptl - pointZero).toFloat() * scale
            val dxr = x0 + (ptr - pointZero).toFloat() * scale
            val dyb = 35F

            canvas.drawLine(dxl, y0, dxl, y0 + dyb, paint)
            canvas.drawLine(dxr, y0, dxr, y0 + dyb, paint)

            canvas.drawText(toleranceString1, offsetX, offsetY + 2F * dyt, paint)

            if (! isBasePoint) {
                canvas.drawText(toleranceString2, offsetX + hw, offsetY + 2F * dyt, paint)
            }

            // draw point
            val yp = y0 - paint.strokeWidth
            val xp = x0 + (pointValue - pointZero).toFloat() * scale
            val dpy = 35F
            val dpx = 15F

            path.reset()
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

            paint.color = pointColor
            canvas.drawPath(path, paint)
            canvas.drawText(valueString, offsetX + hw, offsetY + 1F * dyt, paint)

        }
    }
}