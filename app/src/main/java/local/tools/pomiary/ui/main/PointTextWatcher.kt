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
    private var graphRange: PointRangeGraph? = null
    private val pointData = PointData()

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

    override fun afterTextChanged(s: Editable) {
        if (isSelfModify)
            return

        pointData.rawInput = s.toString()
        updateData()

        childrenPoints?.forEach {
            it.updateData()
        }

        fragment.setModified()
    }

    private fun getTolerance(): DataStorage.PointTolerance? {
        return graphRange?.getTolerance()
    }

    private fun updateData() {
        if (! viewEdit.isEnabled)
            return

        pointData.rawValue = PointData.valueFromString(pointData.rawInput)
        pointData.value = 0.0
        pointData.result = PointResult.UNKNOWN

        parentPoint?.let { parent ->
            pointData.value = PointsAligner.pointsDistance(pointData.rawValue, parent.pointData.rawValue)

            getTolerance()?.let { tolerance ->
                pointData.result = PointsAligner.testPoint(pointData.value, tolerance)
            }
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
        } else if (getTolerance() == null) {
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

        graphRange?.setPoint(alignedValue, viewResult.currentTextColor)
    }

    fun clear() {
        viewResult.text = String()
        setRawInput(String(), 0.0)
    }

    fun getRawInput(): String {
        return pointData.rawInput
    }

    fun setRawInput(input: String, value: Double) {
        if (viewEdit.isEnabled) {
            pointData.rawInput = input
            pointData.rawValue = value
        } else {
            pointData.rawInput = String()
            pointData.rawValue = 0.0
        }

        isSelfModify = true
        viewEdit.setText(pointData.rawInput)
        isSelfModify = false
    }

    fun setRangeGraph(graph: PointRangeGraph?) {
        graphRange = graph
    }

    fun setChildrenPoints(children: Array<PointTextWatcher>) {
        childrenPoints = children
    }

    fun setParent(parent: PointTextWatcher) {
        parentPoint = parent
    }
}