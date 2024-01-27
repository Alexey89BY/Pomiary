package local.tools.pomiary.ui.main


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointData
import local.tools.pomiary.PointResult
import local.tools.pomiary.PointsAligner
import local.tools.pomiary.R


/**
 * A simple [Fragment] subclass.
 * Use the [StandardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val ARG_STORAGE_NAME = "storage_name"


class StandardFragment : Fragment() {
    private var storageName: String? = null
    private lateinit var viewOfLayout: View
    private lateinit var dataStorage: DataStorage.DataSubSet
    private lateinit var spinnerItems: Array<String>
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var watchersPointsP6: Array<PointTextWatcher>
    private lateinit var watchersPointsP7: Array<PointTextWatcher>
    private lateinit var editsP6: List<Int>
    private lateinit var textsResultP6: List<Int>
    private lateinit var textsRangeP6: List<Int>
    private lateinit var editsP7: List<Int>
    private lateinit var textsResultP7: List<Int>
    private lateinit var textsRangeP7: List<Int>
    private var storageCurrentIndex = -1
    private var isModified = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storageName = it.getString(ARG_STORAGE_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_standard, container, false)

        val refreshButton = viewOfLayout.findViewById<ImageButton>(R.id.buttonRefreshStandard)
        refreshButton.setOnClickListener { recalculateValues() }

        val saveButton = viewOfLayout.findViewById<ImageButton>(R.id.buttonSaveStandard)
        saveButton.setOnClickListener { saveValues() }

        dataStorage = DataStorage.getStorageByName(storageName)
        spinnerItems = buildStoragesSpinnerArray()

        val spinner = viewOfLayout.findViewById<Spinner>(R.id.spinnerStandard)
        spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            spinnerItems
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = spinnerListener

        val isMaxi = dataStorage.title == DataStorage.getStorageMaxi().title

        if (isMaxi) {
            editsP6 = editsMaxiP6
            textsResultP6 = textsResultMaxiP6
            textsRangeP6 = textsRangeMaxiP6

            removeRow(R.id.tableStandard, R.id.rowStandardP6_P1)
        } else {
            editsP6 = editsStandardP6
            textsResultP6 = textsResultStandardP6
            textsRangeP6 = textsRangeStandardP6

            removeRow(R.id.tableStandard, R.id.rowStandardP6_9)
            removeRow(R.id.tableStandard, R.id.rowStandardP6_P2)
            removeRow(R.id.tableStandard, R.id.rowStandardP6_10)
        }

        editsP7 = editsStandardP7
        textsResultP7 = textsResultStandardP7
        textsRangeP7 = textsRangeStandardP7

        watchersPointsP6 = setupWatchers(editsP6, textsResultP6)
        watchersPointsP7 = setupWatchers(editsP7, textsResultP7)

        onSettingsChange()

        return viewOfLayout
    }


    private val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parentView: AdapterView<*>?,
            selectedItemView: View?,
            position: Int,
            id: Long
        ) {
            switchStorage(position)
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {
            switchStorage(-1)
        }
    }


    fun onSettingsChange() {
        setRangesToViews(dataStorage.tolerancesP6, dataStorage.toleranceMapP6, textsRangeP6, watchersPointsP6)
        setRangesToViews(dataStorage.tolerancesP7, dataStorage.toleranceMapP7, textsRangeP7, watchersPointsP7)
        showBasePoint(R.id.editStandardP6_0, SettingsFragment.getShowBasePoint())
        showBasePoint(R.id.editStandardP7_0, SettingsFragment.getShowBasePoint())
    }


    private val editsStandardP6 = listOf(
        R.id.editStandardP6_0,
        R.id.editStandardP6_1,
        R.id.editStandardP6_2,
        R.id.editStandardP6_3,
        R.id.editStandardP6_4,
        R.id.editStandardP6_5,
        R.id.editStandardP6_6,
        R.id.editStandardP6_7,
        R.id.editStandardP6_P1,
        R.id.editStandardP6_8,
    )

    private val textsResultStandardP6 = listOf(
        R.id.textResultStandardP6_0,
        R.id.textResultStandardP6_1,
        R.id.textResultStandardP6_2,
        R.id.textResultStandardP6_3,
        R.id.textResultStandardP6_4,
        R.id.textResultStandardP6_5,
        R.id.textResultStandardP6_6,
        R.id.textResultStandardP6_7,
        R.id.textResultStandardP6_P1,
        R.id.textResultStandardP6_8,
    )

    private val textsRangeStandardP6 = listOf(
        R.id.textRangeStandardP6_0,
        R.id.textRangeStandardP6_1,
        R.id.textRangeStandardP6_2,
        R.id.textRangeStandardP6_3,
        R.id.textRangeStandardP6_4,
        R.id.textRangeStandardP6_5,
        R.id.textRangeStandardP6_6,
        R.id.textRangeStandardP6_7,
        R.id.textRangeStandardP6_8,
    )

    private val editsMaxiP6 = listOf(
        R.id.editStandardP6_0,
        R.id.editStandardP6_1,
        R.id.editStandardP6_2,
        R.id.editStandardP6_3,
        R.id.editStandardP6_4,
        R.id.editStandardP6_5,
        R.id.editStandardP6_6,
        R.id.editStandardP6_7,
        R.id.editStandardP6_8,
        R.id.editStandardP6_9,
        R.id.editStandardP6_P2,
        R.id.editStandardP6_10,
    )

    private val textsResultMaxiP6 = listOf(
        R.id.textResultStandardP6_0,
        R.id.textResultStandardP6_1,
        R.id.textResultStandardP6_2,
        R.id.textResultStandardP6_3,
        R.id.textResultStandardP6_4,
        R.id.textResultStandardP6_5,
        R.id.textResultStandardP6_6,
        R.id.textResultStandardP6_7,
        R.id.textResultStandardP6_8,
        R.id.textResultStandardP6_9,
        R.id.textResultStandardP6_P2,
        R.id.textResultStandardP6_10,
    )

    private val textsRangeMaxiP6 = listOf(
        R.id.textRangeStandardP6_0,
        R.id.textRangeStandardP6_1,
        R.id.textRangeStandardP6_2,
        R.id.textRangeStandardP6_3,
        R.id.textRangeStandardP6_4,
        R.id.textRangeStandardP6_5,
        R.id.textRangeStandardP6_6,
        R.id.textRangeStandardP6_7,
        R.id.textRangeStandardP6_8,
        R.id.textRangeStandardP6_9,
        R.id.textRangeStandardP6_10,
    )

    private val editsStandardP7 = listOf(
        R.id.editStandardP7_0,
        R.id.editStandardP7_P1,
        R.id.editStandardP7_1,
        R.id.editStandardP7_2,
        R.id.editStandardP7_P2,
        R.id.editStandardP7_3,
    )

    private val textsResultStandardP7 = listOf(
        R.id.textResultStandardP7_0,
        R.id.textResultStandardP7_P1,
        R.id.textResultStandardP7_1,
        R.id.textResultStandardP7_2,
        R.id.textResultStandardP7_P2,
        R.id.textResultStandardP7_3,
    )

    private val textsRangeStandardP7 = listOf(
        R.id.textRangeStandardP7_0,
        R.id.textRangeStandardP7_1,
        R.id.textRangeStandardP7_2,
        R.id.textRangeStandardP7_3,
    )


    private fun setupWatchers(
        editsList: List<Int>,
        textsResultList: List<Int>
    ): Array<PointTextWatcher> {
        val watchers = Array(editsList.size) {
            val editText = viewOfLayout.findViewById<EditText>(editsList[it])
            val textView = viewOfLayout.findViewById<TextView>(textsResultList[it])
            val watcher = PointTextWatcher(this, editText, textView)
            editText.addTextChangedListener(watcher)
            watcher
        }

        val watchersChildren = Array(watchers.size - 1) { watchers[it + 1] }
        watchers[0].setChildrenPoints(watchersChildren)
        watchersChildren.forEach { it.setParent(watchers[0]) }

        return watchers
    }


    private fun switchStorage(newIndex: Int) {
        // save edits
        if (newIndex != storageCurrentIndex) {
            //val oldStorage = inputStorage[storageCurrentIndex]
            //getStringsFromEdits(viewOfLayout, editsP6, oldStorage.sectionP6.pointsRaw)
            //getStringsFromEdits(viewOfLayout, editsP7, oldStorage.sectionP7.pointsRaw)

            storageCurrentIndex = newIndex

            if (storageCurrentIndex < 0) {
                clearPoints(watchersPointsP6)
                clearPoints(watchersPointsP7)
            } else {
                val newStorage = dataStorage.getData(storageCurrentIndex)

                setPointInputsToEdits(newStorage.sectionP6.points, watchersPointsP6)
                setPointInputsToEdits(newStorage.sectionP7.points, watchersPointsP7)

                setPointResultsToView(newStorage.sectionP6.points, watchersPointsP6)
                setPointResultsToView(newStorage.sectionP7.points, watchersPointsP7)
            }

            isModified = false
        }
    }

    private fun recalculateValues() {
        hideKeyboard()

        val message: String = requireContext().resources.getString(R.string.check_msg)
        Snackbar.make(viewOfLayout, message, 250).show()

        val currentStorage = dataStorage.getData(storageCurrentIndex)

        getPointInputsFromEdits(watchersPointsP6, currentStorage.sectionP6.points)
        getPointInputsFromEdits(watchersPointsP7, currentStorage.sectionP7.points)

        currentStorage.sectionP6.result = PointsAligner.alignPoints(
            dataStorage.tolerancesP6, dataStorage.toleranceMapP6, currentStorage.sectionP6.points
        )
        currentStorage.sectionP7.result = PointsAligner.alignPoints(
            dataStorage.tolerancesP7, dataStorage.toleranceMapP7, currentStorage.sectionP7.points
        )

        setPointResultsToView(currentStorage.sectionP6.points, watchersPointsP6)
        setPointResultsToView(currentStorage.sectionP7.points, watchersPointsP7)

        currentStorage.timeStamp = ""
        refreshSpinner()

        isModified = false
    }


    private fun saveValues() {
        val currentStorage = dataStorage.getData(storageCurrentIndex)

        if (isModified) {
            val dialog = AlertDialog.Builder(context)
                .setTitle("Measurements are not checked")
                .setPositiveButton("OK") {_, _ ->
                }
                .create()
            dialog.show()
            return
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle("Save to history ?")
            .setMessage(dataStorage.storageTitle(currentStorage))
            .setPositiveButton("Save") { _, _ ->
                val message: String = requireContext().resources.getString(R.string.save_msg)
                Snackbar.make(viewOfLayout, message, 250).show()

                currentStorage.timeStamp = HistoryFragment.generateTimeStamp()
                HistoryFragment.savePoints(
                    this, dataStorage, currentStorage
                )
                refreshSpinner()
            }
            .setNeutralButton("Cancel") {_, _ ->
            }
            .create()
        dialog.show()
    }


    private fun refreshSpinner() {
        val currentStorage = dataStorage.getData(storageCurrentIndex)
        spinnerItems[storageCurrentIndex] = currentStorage.dataTitle()
        spinnerAdapter.notifyDataSetChanged()
    }


    private fun getPointInputsFromEdits(edits: Array<PointTextWatcher>, pointsArray: Array<PointData>) {
        pointsArray.forEachIndexed { index, _ ->
            pointsArray[index].rawInput = edits[index].getRawInput()
        }
    }

    private fun setPointInputsToEdits(pointsArray: Array<PointData>, edits: Array<PointTextWatcher>) {
        pointsArray.forEachIndexed { index, point ->
            edits[index].setRawInput(point.rawInput, point.rawValue)
        }
    }

    private fun clearPoints(pointsWatchers: Array<PointTextWatcher>) {
        pointsWatchers.forEach {
            it.clear()
        }
    }

    private fun setRangesToViews(
        tolerances: Array<DataStorage.PointTolerance>,
        toleranceMap: List<Int>,
        rangeViewIds: List<Int>,
        pointsWatchers: Array<PointTextWatcher>
    ) {
        tolerances.forEachIndexed { index, tolerance ->
            val textView = viewOfLayout.findViewById<TextView>(rangeViewIds[index])
            textView.text = String.format(" %.1f \u2026 %.1f",
                        tolerance.origin - tolerance.offset,
                        tolerance.origin + tolerance.offset
                    )

            val pointIndex = toleranceMap[index]
            pointsWatchers[pointIndex].setTolerance(tolerance)
        }
    }

    private fun setPointResultsToView (
        points: Array<PointData>,
        watchers: Array<PointTextWatcher>
    ) {
        points.forEachIndexed { index, point ->
            watchers[index].updateResult(point.rawValue, point.value, point.result, point.result)
        }
    }

    private fun buildStoragesSpinnerArray(): Array<String> {
        return Array(dataStorage.data.size) { index ->
            dataStorage.getData(index).dataTitle()
        }
    }

/*
    private fun showRow(
        rowId: Int,
        isVisible: Boolean
    ) {
        val rowView = viewOfLayout.findViewById<TableRow>(rowId)
        rowView.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
*/

    private fun removeRow(
        tableId: Int,
        rowId: Int
    ) {
        val table = viewOfLayout.findViewById<TableLayout>(tableId)
        val row = viewOfLayout.findViewById<TableRow>(rowId)
        table.removeView(row)
    }


    private fun showBasePoint(
        editId: Int,
        isVisible: Boolean
    ) {
        val editText = viewOfLayout.findViewById<EditText>(editId)
        editText.isEnabled = isVisible
        if (! isVisible) {
            editText.text.clear()
        }
    }


    fun setModified() {
        if (! isModified) {
            val currentStorage = dataStorage.getData(storageCurrentIndex)
            currentStorage.timeStamp = ""
            currentStorage.sectionP6.result = PointResult.UNKNOWN
            currentStorage.sectionP7.result = PointResult.UNKNOWN
            refreshSpinner()

            isModified = true
        }
    }

    private fun hideKeyboard() {
        val view = activity?.findViewById<View>(android.R.id.content)
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    /*
    fun sendToGraph(fragmentTitle: String, currentStorage: DataStorage.SillSealData) {
        BlankFragment.setData(fragmentTitle, currentStorage.timeStamp,
            currentStorage.sectionP6.points, DataStorage.getToleranceStandardP6(),
            currentStorage.sectionP7.points, DataStorage.getToleranceStandardP7())
        //MainActivity.openGraphTab()
    }
    */

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @param storageName Parameter 1.
         * @return A new instance of fragment StandardFragment.
         */
        @JvmStatic
        fun newInstance(storageName: String) =
            StandardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STORAGE_NAME, storageName)
                }
            }
    }
}