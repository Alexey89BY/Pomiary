package local.tools.pomiary.ui.main

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointResult


class HistoryViewCanvas(context: Context?) : View(context) {

    data class GraphPoint (
        val title: String,
        val tolerance: DataStorage.PointTolerance,
        val value: Double,
        val result: PointResult,
    )

    @Suppress("ArrayInDataClass")
    data class GraphData (
        var title: String = String(),
        var timeStamp: String = String(),
        var sideLR: String = String(),
        var points: Array<GraphPoint> = emptyArray(),
        var isPrecise: Boolean = false,
        var isZeroBase: Boolean = false,
    )


    private val paint = Paint()
    private val path = Path()

    private var pointInRow = 1
    private var pointWidth = 0
    private val pointHeight = 200
    private var isPointsGraph = false
    private var graphData = GraphData()
    private var pointScale = 1.0F


    init {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                pointInRow = 5
                pointWidth = 450
            }
            else -> {
                pointInRow = 2
                pointWidth = 500
            }
        }

        minimumWidth = 60 + pointInRow * pointWidth

        pointScale = pointWidth / (2.0F * DataStorage.getToleranceInvalid().toFloat()) // in point +-
    }


    fun setData(data: GraphData)
    {
        graphData = data

        minimumHeight = ((data.points.size + (pointInRow - 1)) / pointInRow) * pointHeight

        invalidate()
    }


    fun setValuesOnly(isRaw: Boolean) {
        isPointsGraph = isRaw
        invalidate()
    }


    public override fun onDraw(canvas: Canvas) {
        val offsetX = 30F
        val offsetY = 0F

        // for test only
/*
        val titleString = String.format(
            "%s %s %s",
            graphData.timeStamp,
            graphData.title,
            graphData.sideLR
        )
        paint.color = Color.DKGRAY
        paint.textSize = 72F
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(titleString, offsetX, paint.textSize, paint)
*/

        paint.strokeWidth = 3F

        graphData.points.forEachIndexed { index, point ->
            val x = offsetX + pointWidth * index.mod(pointInRow)
            val y = offsetY + pointHeight * index.div(pointInRow)
            val isBasePoint = graphData.isZeroBase && (index == 0)
            drawDataPoint(canvas, x, y, point, isBasePoint)
        }
    }


    private fun drawDataPoint(
        canvas: Canvas,
        offsetX: Float,
        offsetY: Float,
        point: GraphPoint,
        isBasePoint: Boolean
    ) {
        val ptl = point.tolerance.origin - point.tolerance.offset
        val ptr = point.tolerance.origin + point.tolerance.offset
        val pointValue = point.value

        val pointColor =
            if (isBasePoint) Color.LTGRAY
            else point.result.toColor()

        val toleranceString1 = String.format(
            "%.1f\u2026%.1f",
            ptl,
            ptr
        )

        val toleranceString2 = String.format(
            "%.1f\u00B1%.1f",
            point.tolerance.origin,
            point.tolerance.offset,
        )

        val valueString =
            if (graphData.isPrecise) String.format("%.2f", pointValue)
            else String.format("%.1f", pointValue)

        paint.textSize = 34F
        paint.textAlign = Paint.Align.LEFT

        val dyt = paint.textSize * 1.2F
        val hw = pointWidth * 0.5F

        // draw title
        paint.color = Color.WHITE
        canvas.drawText(point.title, offsetX, offsetY + dyt, paint)

        if (isPointsGraph) {

            // draw tolerance
            paint.color = Color.GRAY
            canvas.drawText(toleranceString1, offsetX, offsetY + 2F * dyt, paint)
            if (!isBasePoint) {
                canvas.drawText(toleranceString2, offsetX, offsetY + 3F * dyt, paint)
            }

            // draw result
            val xp = offsetX + pointWidth - 30F

            paint.color = pointColor
            paint.textAlign = Paint.Align.RIGHT
            paint.textSize = 54F

            val pointMessage = point.result.toMessage()
            canvas.drawText(pointMessage, xp, offsetY + 1F * dyt, paint)

            // draw point
            paint.textSize = 72F

            canvas.drawText(valueString, xp, offsetY + 3F * dyt, paint)

        } else {

            val scale = pointScale
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
            else point.tolerance.origin

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