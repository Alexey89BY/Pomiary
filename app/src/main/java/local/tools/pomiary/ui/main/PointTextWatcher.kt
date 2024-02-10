package local.tools.pomiary.ui.main


import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointData
import local.tools.pomiary.PointResult
import local.tools.pomiary.PointsAligner


class PointTextWatcher(
    private val fragment: StandardFragment,
    private val viewEdit: EditText,
    private val viewResult: TextView,
) : TextWatcher {

    private var isSelfModify = false
    private var parentPoint: PointTextWatcher? = null
    private var childrenPoints: Array<PointTextWatcher>? = null
    private var pointTolerance: DataStorage.PointTolerance? = null
    private val pointData = PointData()

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

    override fun afterTextChanged(s: Editable) {
        if (isSelfModify)
            return

        pointData.rawInput = s.toString()
        updateFromParent()
        childrenPoints?.forEach {
            it.updateFromParent()
        }

        fragment.setModified()
    }

    private fun updateFromParent() {
        pointData.rawValue = PointData.valueFromString(pointData.rawInput)

        if (parentPoint == null) {
            pointData.value = 0.0
            pointData.result = PointResult.UNKNOWN
        } else {
            val parentValue = parentPoint!!.pointData.rawValue
            pointData.value = PointsAligner.pointsDistance(pointData.rawValue, parentValue)
            pointData.result =
                if (pointTolerance == null) PointResult.UNKNOWN
                else PointsAligner.testPoint(pointData.value, pointTolerance!!)
        }

        val resultForMessage = if (pointData.result == PointResult.INVALID) PointResult.INVALID else PointResult.UNKNOWN
        updateResult(pointData.rawValue, pointData.value, pointData.result, resultForMessage)
    }

    fun updateResult(
        rawValue: Double,
        alignedValue: Double,
        resultForColor: PointResult,
        resultForMessage: PointResult
    ) {
        if (parentPoint == null) {
            viewResult.setTextColor(PointResult.UNKNOWN.toColor())
            viewResult.text = String.format(" %.2f %+.2f ",
                rawValue,
                alignedValue
            )
        } else if (pointTolerance == null) {
            viewResult.setTextColor(PointResult.UNKNOWN.toColor())
            viewResult.text = String.format(" %.1f ",
                alignedValue
            )
        } else {
            viewResult.setTextColor(resultForColor.toColor())
            viewResult.text = String.format(" %s %.1f ",
                resultForMessage.toMessage(),
                alignedValue
            )
        }
    }

    fun clear() {
        viewResult.text = String()
        setRawInput(String(), 0.0)
    }

    fun getRawInput(): String {
        return pointData.rawInput
    }

    fun setRawInput(input: String, value: Double) {
        isSelfModify = true
        pointData.rawInput = input
        pointData.rawValue = value
        viewEdit.setText(input)
        isSelfModify = false
    }

    fun setTolerance(tolerance: DataStorage.PointTolerance?) {
        pointTolerance = tolerance
    }

    fun setChildrenPoints(children: Array<PointTextWatcher>) {
        childrenPoints = children
    }

    fun setParent(parent: PointTextWatcher) {
        parentPoint = parent
    }
}