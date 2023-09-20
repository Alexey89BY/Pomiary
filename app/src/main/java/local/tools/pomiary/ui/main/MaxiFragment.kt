package local.tools.pomiary.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import local.tools.pomiary.DataStorage
import local.tools.pomiary.PointsAligner
import local.tools.pomiary.R

/**
 * A simple [Fragment] subclass.
 * Use the [MaxiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MaxiFragment : Fragment() {
    private lateinit var viewOfLayout: View
    private lateinit var dataStorages: Array<DataStorage.DataSubSet>
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
        viewOfLayout = inflater.inflate(R.layout.fragment_maxi, container, false)

        val refreshButton = viewOfLayout.findViewById<FloatingActionButton>(R.id.buttonRefreshMaxi)
        refreshButton.setOnClickListener { recalculateValues() }

        spinnerItems = StandardFragment.buildStoragesSpinnerArray(dataStorages)

        val spinner = viewOfLayout.findViewById<Spinner>(R.id.spinnerMaxi)
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

        val tolerancesMaxiP6 = DataStorage.getToleranceMaxiP6()
        editsMaxiP6.forEachIndexed { index, id ->
            val editText = viewOfLayout.findViewById<EditText>(id)
            val textView = viewOfLayout.findViewById<TextView>(textsResultMaxiP6[index])
            editText.addTextChangedListener(PointTextWatcher(tolerancesMaxiP6[index], textView))
        }

        val tolerancesMaxiP7 = DataStorage.getToleranceMaxiP7()
        editsMaxiP7.forEachIndexed { index, id ->
            val editText = viewOfLayout.findViewById<EditText>(id)
            val textView = viewOfLayout.findViewById<TextView>(textsResultMaxiP7[index])
            editText.addTextChangedListener(PointTextWatcher(tolerancesMaxiP7[index], textView))
        }

        onSettingsChange()

        return viewOfLayout
    }


    fun onSettingsChange() {
        StandardFragment.setRangesToViews(viewOfLayout, DataStorage.getToleranceMaxiP6(), textsRangeMaxiP6)
        StandardFragment.setRangesToViews(viewOfLayout, DataStorage.getToleranceMaxiP7(), textsRangeMaxiP7)
        StandardFragment.showRow(viewOfLayout, R.id.rowMaxiP6_0, SettingsFragment.getShowBasePoint())
        StandardFragment.showRow(viewOfLayout, R.id.rowMaxiP7_0, SettingsFragment.getShowBasePoint())
    }


    private val editsMaxiP6 = listOf(
        R.id.editMaxiP6_0,
        R.id.editMaxiP6_1,
        R.id.editMaxiP6_2,
        R.id.editMaxiP6_3,
        R.id.editMaxiP6_4,
        R.id.editMaxiP6_5,
        R.id.editMaxiP6_6,
        R.id.editMaxiP6_7,
        R.id.editMaxiP6_8,
        R.id.editMaxiP6_9,
        R.id.editMaxiP6_10,
    )

    private val editsMaxiP7 = listOf(
        R.id.editMaxiP7_0,
        R.id.editMaxiP7_1,
        R.id.editMaxiP7_2,
        R.id.editMaxiP7_3,
    )

    private val textsResultMaxiP6 = listOf(
        R.id.textResultMaxiP6_0,
        R.id.textResultMaxiP6_1,
        R.id.textResultMaxiP6_2,
        R.id.textResultMaxiP6_3,
        R.id.textResultMaxiP6_4,
        R.id.textResultMaxiP6_5,
        R.id.textResultMaxiP6_6,
        R.id.textResultMaxiP6_7,
        R.id.textResultMaxiP6_8,
        R.id.textResultMaxiP6_9,
        R.id.textResultMaxiP6_10,
    )

    private var textsResultMaxiP7 = listOf(
        R.id.textResultMaxiP7_0,
        R.id.textResultMaxiP7_1,
        R.id.textResultMaxiP7_2,
        R.id.textResultMaxiP7_3,
    )

    private val textsRangeMaxiP6 = listOf(
        R.id.textRangeMaxiP6_0,
        R.id.textRangeMaxiP6_1,
        R.id.textRangeMaxiP6_2,
        R.id.textRangeMaxiP6_3,
        R.id.textRangeMaxiP6_4,
        R.id.textRangeMaxiP6_5,
        R.id.textRangeMaxiP6_6,
        R.id.textRangeMaxiP6_7,
        R.id.textRangeMaxiP6_8,
        R.id.textRangeMaxiP6_9,
        R.id.textRangeMaxiP6_10,
    )

    private var textsRangeMaxiP7 = listOf(
        R.id.textRangeMaxiP7_0,
        R.id.textRangeMaxiP7_1,
        R.id.textRangeMaxiP7_2,
        R.id.textRangeMaxiP7_3,
    )


    private fun switchStorage(newIndex: Int) {
        // save edits
        if (newIndex != storageCurrentIndex) {
            //val oldStorage = inputStorage[storageCurrentIndex]
            //StandardFragment.getStringsFromEdits(viewOfLayout, editsMaxiP6, oldStorage.sectionP6.pointsRaw)
            //StandardFragment.getStringsFromEdits(viewOfLayout, editsMaxiP7, oldStorage.sectionP7.pointsRaw)

            storageCurrentIndex = newIndex
            val newStorage = StandardFragment.storageDataBySpinnerIndex(dataStorages, storageCurrentIndex)
            StandardFragment.setPointInputsToEdits(viewOfLayout, newStorage.sectionP6.points, editsMaxiP6)
            StandardFragment.setPointInputsToEdits(viewOfLayout, newStorage.sectionP7.points, editsMaxiP7)

            if (newStorage.timeStamp.isNotBlank()) {
                StandardFragment.setPointResultsToView(viewOfLayout, newStorage.sectionP6.points, textsResultMaxiP6)
                StandardFragment.setPointResultsToView(viewOfLayout, newStorage.sectionP7.points, textsResultMaxiP7)
            } else {
                // clear views
                StandardFragment.clearViews(viewOfLayout, textsResultMaxiP6)
                StandardFragment.clearViews(viewOfLayout, textsResultMaxiP7)
            }
        }
    }

    private fun recalculateValues() {
        val message: String = requireContext().resources.getString(R.string.check_maxi)
        Snackbar.make(viewOfLayout, message, 250).show()

        val dataStorage = StandardFragment.storageBySpinnerIndex(dataStorages, storageCurrentIndex)
        val currentStorage = StandardFragment.storageDataBySpinnerIndex(dataStorages, storageCurrentIndex)

        currentStorage.timeStamp = HistoryFragment.generateTimeStamp()

        StandardFragment.getPointInputsFromEdits(viewOfLayout, editsMaxiP6, currentStorage.sectionP6.points)
        currentStorage.sectionP6.result = PointsAligner.alignPoints(
            dataStorage.tolerancesP6, currentStorage.sectionP6.points)

        StandardFragment.getPointInputsFromEdits(viewOfLayout, editsMaxiP7, currentStorage.sectionP7.points)
        currentStorage.sectionP7.result = PointsAligner.alignPoints(
            dataStorage.tolerancesP7, currentStorage.sectionP7.points)

        HistoryFragment.savePoints(this, dataStorage.title, currentStorage.timeStamp,
            currentStorage.sectionP6.points, currentStorage.sectionP7.points)

        StandardFragment.setPointResultsToView(viewOfLayout, currentStorage.sectionP6.points, textsResultMaxiP6)
        StandardFragment.setPointResultsToView(viewOfLayout, currentStorage.sectionP7.points, textsResultMaxiP7)

        //StandardFragment.setTimeStampToView(viewOfLayout, currentStorage.timeStamp, R.id.textTimeStampMaxi)
        spinnerItems[storageCurrentIndex] = DataStorage.getStorageDataTitle(dataStorage, currentStorage)
        spinnerAdapter.notifyDataSetChanged()
    }

    fun attachToStorage(
        dataSubsetLH: DataStorage.DataSubSet,
        dataSubsetRH: DataStorage.DataSubSet
    ) {
        dataStorages = arrayOf(dataSubsetLH, dataSubsetRH)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment MaxiFragment.
         */
        @JvmStatic
        fun newInstance() =
            MaxiFragment().apply {
                arguments = Bundle().apply {
                }
            }

    }
}