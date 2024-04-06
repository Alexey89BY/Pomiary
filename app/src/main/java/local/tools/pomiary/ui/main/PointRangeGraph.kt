package local.tools.pomiary.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import local.tools.pomiary.DataStorage

class PointRangeGraph(context: Context?) : View(context) {
    private val paint = Paint()
    private val path = Path()
    private var pointTolerance: DataStorage.PointTolerance? = null
    private var pointValue = 0.0
    private var pointColor = Color.WHITE

    init {
        minimumWidth = 280
        minimumHeight = 90
    }

    fun setPointWatcher(pointWatcher: PointTextWatcher) {

    }

    fun setTolerance(tolerance: DataStorage.PointTolerance) {
        pointTolerance = tolerance
        invalidate()
    }

    fun getTolerance(): DataStorage.PointTolerance? {
        return pointTolerance
    }

    fun setPoint(value: Double, color: Int) {
        pointValue = value
        pointColor = color
        invalidate()
    }

    public override fun onDraw(canvas: Canvas) {
        paint.strokeWidth = 3F
        paint.textSize = 40F

        val dyt = paint.textSize
        val xpl = 0F
        val xpr = width.toFloat()
        val x0 = (xpr + xpl) * 0.5F
        val y0 = 1.5F * dyt

        paint.color = Color.GRAY
        canvas.drawLine(xpl, y0, xpr, y0, paint)

        if (pointTolerance == null)
            return

        val tolerance = pointTolerance!!

        if (tolerance.offset < 0)
            return

        val scale = (xpr - xpl) / (1.2F * 2.0F * (tolerance.offset + DataStorage.getToleranceNok()).toFloat()) // zoom * offset+-

        // draw zero
        val dy0 = 20F

        canvas.drawLine(x0, y0, x0, y0 + dy0, paint)

        // draw tolerance
        paint.color = Color.LTGRAY
        val pointZero = tolerance.origin
        val ptl = pointZero - tolerance.offset
        val ptr = pointZero + tolerance.offset
        val dxl = x0 + (ptl - pointZero).toFloat() * scale
        val dxr = x0 + (ptr - pointZero).toFloat() * scale
        val dyb = 25F

        canvas.drawLine(dxl, y0, dxl, y0 + dyb, paint)
        canvas.drawLine(dxr, y0, dxr, y0 + dyb, paint)

        val toleranceString1 = String.format(
            "%.1f",
            ptl,
        )
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(toleranceString1, xpl, dyt, paint)

        val toleranceString2 = String.format(
            "%.1f",
            ptr,
        )
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(toleranceString2, xpr, dyt, paint)

        // draw point
        val yp = y0 + paint.strokeWidth
        val xp = x0 + (pointValue - pointZero).toFloat() * scale
        val dpy = -30F
        val dpx = 15F
        paint.color = pointColor

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

        canvas.drawPath(path, paint)
    }
}