package local.tools.pomiary.ui.main


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointsAligner
import local.tools.pomiary.R


/**
 * A simple [Fragment] subclass.
 * Use the [StandardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StandardFragment : Fragment() {
    private lateinit var viewOfLayout: View
    private lateinit var dataStorage: DataStorage.DataSubSet
    private lateinit var spinnerItems: Array<String>
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private var storageCurrentIndex = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_standard, container, false)

        val refreshButton = viewOfLayout.findViewById<FloatingActionButton>(R.id.buttonRefreshStandard)
        refreshButton.setOnClickListener { recalculateValues() }

        spinnerItems = buildStoragesSpinnerArray()

        val spinner = viewOfLayout.findViewById<Spinner>(R.id.spinnerStandard)
        spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerItems)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        val tolerancesP6 = dataStorage.tolerancesP6
        tolerancesP6.forEachIndexed { index, tolerance ->
            if (index != 0) {
                val editText = viewOfLayout.findViewById<EditText>(editsStandardP6[index])
                val textView = viewOfLayout.findViewById<TextView>(textsResultStandardP6[index])
                editText.addTextChangedListener(PointTextWatcher(tolerance, textView))
            }
        }

        val tolerancesP7 = dataStorage.tolerancesP7
        tolerancesP7.forEachIndexed { index, tolerance ->
            if (index != 0) {
                val editText = viewOfLayout.findViewById<EditText>(editsStandardP7[index])
                val textView = viewOfLayout.findViewById<TextView>(textsResultStandardP7[index])
                editText.addTextChangedListener(PointTextWatcher(tolerance, textView))
            }
        }

        val isMaxi = editsStandardP6.size <= tolerancesP6.size
        showRow(R.id.rowStandardP6_9, isMaxi)
        showRow(R.id.rowStandardP6_10, isMaxi)

        onSettingsChange()

        return viewOfLayout
    }


    fun onSettingsChange() {
        setRangesToViews(dataStorage.tolerancesP6, textsRangeStandardP6)
        setRangesToViews(dataStorage.tolerancesP7, textsRangeStandardP7)
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
        R.id.editStandardP6_8,
        R.id.editStandardP6_9,
        R.id.editStandardP6_10,
    )

    private val editsStandardP7 = listOf(
        R.id.editStandardP7_0,
        R.id.editStandardP7_1,
        R.id.editStandardP7_2,
        R.id.editStandardP7_3,
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
        R.id.textResultStandardP6_8,
        R.id.textResultStandardP6_9,
        R.id.textResultStandardP6_10,
    )

    private val textsResultStandardP7 = listOf(
        R.id.textResultStandardP7_0,
        R.id.textResultStandardP7_1,
        R.id.textResultStandardP7_2,
        R.id.textResultStandardP7_3,
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
        R.id.textRangeStandardP6_9,
        R.id.textRangeStandardP6_10,
    )

    private val textsRangeStandardP7 = listOf(
        R.id.textRangeStandardP7_0,
        R.id.textRangeStandardP7_1,
        R.id.textRangeStandardP7_2,
        R.id.textRangeStandardP7_3,
    )


    private fun switchStorage(newIndex: Int) {
        // save edits
        if (newIndex != storageCurrentIndex) {
            //val oldStorage = inputStorage[storageCurrentIndex]
            //getStringsFromEdits(viewOfLayout, editsStandardP6, oldStorage.sectionP6.pointsRaw)
            //getStringsFromEdits(viewOfLayout, editsStandardP7, oldStorage.sectionP7.pointsRaw)

            storageCurrentIndex = newIndex
            val newStorage = storageDataBySpinnerIndex(storageCurrentIndex)

            setPointInputsToEdits(newStorage.sectionP6.points, editsStandardP6)
            setPointInputsToEdits(newStorage.sectionP7.points, editsStandardP7)

            if (newStorage.timeStamp.isNotBlank()) {
                setPointResultsToView(newStorage.sectionP6.points, textsResultStandardP6)
                setPointResultsToView(newStorage.sectionP7.points, textsResultStandardP7)
            } else {
                // clear views
                clearViews(textsResultStandardP6)
                clearViews(textsResultStandardP7)
            }
        }
    }

    private fun recalculateValues() {
        val message: String = requireContext().resources.getString(R.string.check_msg)
        Snackbar.make(viewOfLayout, message, 250).show()

        val dataStorage = storageBySpinnerIndex(storageCurrentIndex)
        val currentStorage = storageDataBySpinnerIndex(storageCurrentIndex)

        currentStorage.timeStamp = HistoryFragment.generateTimeStamp()

        getPointInputsFromEdits(editsStandardP6, currentStorage.sectionP6.points)
        currentStorage.sectionP6.result = PointsAligner.alignPoints(
            dataStorage.tolerancesP6, currentStorage.sectionP6.points)

        getPointInputsFromEdits(editsStandardP7, currentStorage.sectionP7.points)
        currentStorage.sectionP7.result = PointsAligner.alignPoints(
            dataStorage.tolerancesP7, currentStorage.sectionP7.points)

        HistoryFragment.savePoints(
            this, dataStorage.title, currentStorage.timeStamp,
            currentStorage.sectionP6.points, currentStorage.sectionP7.points)

        setPointResultsToView(currentStorage.sectionP6.points, textsResultStandardP6)
        setPointResultsToView(currentStorage.sectionP7.points, textsResultStandardP7)

        //setTimeStampToView(viewOfLayout, currentStorage.timeStamp, R.id.textTimeStampStandard)
        spinnerItems[storageCurrentIndex] = DataStorage.getStorageDataTitle(dataStorage, currentStorage)
        spinnerAdapter.notifyDataSetChanged()
    }

    fun attachToStorage(
        dataSubset: DataStorage.DataSubSet
    ) {
        dataStorage = dataSubset
    }


    private fun getPointInputsFromEdits(editsIds: List<Int>, pointsArray: Array<DataStorage.PointData>) {
        pointsArray.forEachIndexed { index, _ ->
            val editText = viewOfLayout.findViewById<EditText>(editsIds[index])
            pointsArray[index].rawInput = editText.text.toString()
        }
    }

    private fun setPointInputsToEdits(pointsArray: Array<DataStorage.PointData>, editsIds: List<Int>) {
        pointsArray.forEachIndexed { index, point ->
            val editText = viewOfLayout.findViewById<EditText>(editsIds[index])
            editText.setText(point.rawInput)
        }
    }

    private fun clearViews(textViewIds: List<Int>) {
        textViewIds.forEach { id ->
            val textView = viewOfLayout.findViewById<TextView>(id)
            textView.text = String()
        }
    }

    private fun setRangesToViews(
        tolerances: Array<DataStorage.PointTolerance>,
        textViewIds: List<Int>
    ) {
        tolerances.forEachIndexed { index, tolerance ->
            val textView = viewOfLayout.findViewById<TextView>(textViewIds[index])
            textView.text = when (index) {
                /*
                0 -> {
                    buildString { append(" %.1f \u00B1 %.1f") }.format(
                        tolerance.origin,
                        tolerance.offset
                    )
                }
                */
                else -> {
                    buildString { append(" %.1f \u2026 %.1f") }.format(
                        tolerance.origin - tolerance.offset,
                        tolerance.origin + tolerance.offset
                    )
                }
            }
        }
    }

    private fun setPointResultsToView (
        points: Array<DataStorage.PointData>,
        textsResultIds: List<Int>
    ) {
        val pointsColors = List(points.size) { index ->
            PointsAligner.colorByResult(points[index].result)
        }

        points.forEachIndexed { index, point ->
            val textView = viewOfLayout.findViewById<TextView>(textsResultIds[index])
            //textView.setBackgroundColor(Color.BLACK)
            if (index == 0) {
                textView.text = buildString { append(" %.1f ") }.format(
                    point.value
                )
            } else {
                textView.setTextColor(pointsColors[index])
                textView.text = buildString { append(" %s %.1f ") }.format(
                    PointsAligner.messageByResult(point.result),
                    point.value
                )
            }
        }
    }

    private fun buildStoragesSpinnerArray(): Array<String> {
        return Array(dataStorage.data.size) { index ->
            DataStorage.getStorageDataTitle(
                storageBySpinnerIndex(index),
                storageDataBySpinnerIndex(index)
            )
        }
    }


    private fun storageBySpinnerIndex(
        spinnerIndex: Int
    ): DataStorage.DataSubSet {
        return dataStorage
    }


    private fun storageDataBySpinnerIndex(
        spinnerIndex: Int
    ): DataStorage.SillSealData {
        return dataStorage.data[spinnerIndex]
    }


    private fun showRow(
        rowId: Int,
        isVisible: Boolean
    ) {
        val rowView = viewOfLayout.findViewById<TableRow>(rowId)
        rowView.visibility = if (isVisible) View.VISIBLE else View.GONE
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
         * @return A new instance of fragment StandardFragment.
         */
        @JvmStatic
        fun newInstance() =
            StandardFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}