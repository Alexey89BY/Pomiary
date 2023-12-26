package local.tools.pomiary.ui.main


import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointsAligner


class PointTextWatcher(
    private val fragment: StandardFragment,
    private val viewEdit: EditText,
    private val viewResult: TextView,
) : TextWatcher {

    private var isSelfModify = false
    private var pointTolerance = DataStorage.PointTolerance(0.0, 0.0)
    private var parentPoint: PointTextWatcher? = null
    private var childrenPoints: Array<PointTextWatcher>? = null
    private val pointData = DataStorage.PointData()

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
        val rawValue = PointsAligner.pointFromString(pointData.rawInput)

        if (parentPoint == null) {
            pointData.value = rawValue
            pointData.result = DataStorage.PointResult.UNKNOWN
        } else {
            val parentValue = parentPoint!!.pointData.value
            pointData.value = PointsAligner.pointDistance(rawValue, parentValue)
            pointData.result = PointsAligner.testPoint(pointData.value, pointTolerance)
        }

        updateResult(pointData.value, pointData.result, DataStorage.PointResult.UNKNOWN)
    }

    fun updateResult(
        value: Double,
        resultColor: DataStorage.PointResult,
        resultMessage: DataStorage.PointResult
    ) {
        if (parentPoint == null) {
            // viewResult.setTextColor()
            viewResult.text = String.format(" %.2f ",
                value
            )
        } else {
            viewResult.setTextColor(PointsAligner.colorByResult(resultColor))
            viewResult.text = String.format(" %s %.1f ",
                PointsAligner.messageByResult(resultMessage),
                value
            )
        }
    }

    fun clear() {
        viewResult.text = String()
        setRawInput(String())
    }

    fun getRawInput(): String {
        return pointData.rawInput
    }

    fun setRawInput(input: String) {
        isSelfModify = true
        pointData.rawInput = input
        viewEdit.setText(input)
        isSelfModify = false
    }

    fun setTolerance(tolerance: DataStorage.PointTolerance) {
        pointTolerance = tolerance
    }

    fun setChildrenPoints(children: Array<PointTextWatcher>) {
        childrenPoints = children
    }

    fun setParent(parent: PointTextWatcher) {
        parentPoint = parent
    }
}