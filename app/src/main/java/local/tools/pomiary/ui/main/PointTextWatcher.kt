package local.tools.pomiary.ui.main


import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointsAligner


class PointTextWatcher(
    private val fragment: StandardFragment,
    private val tolerance: DataStorage.PointTolerance,
    private val textsResult: TextView
) : TextWatcher {

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (fragment.setModified()) {
            val pointValue = PointsAligner.roundPoint(s.toString().toDoubleOrNull() ?: 0.0)
            val pointResult = PointsAligner.testPoint(pointValue, tolerance)
            textsResult.setTextColor(PointsAligner.colorByResult(pointResult))
            textsResult.text = String.format(" %.1f ", pointValue)
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable) {

    }
}