package local.tools.pomiary.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import local.tools.pomiary.DataStorage
import local.tools.pomiary.MainActivity
import local.tools.pomiary.R
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    companion object {
        private var isShowBasePoint = true

        private val editsStandardP6_zeros = listOf(
            R.id.editStandardP6_0_zero,
            R.id.editStandardP6_1_zero,
            R.id.editStandardP6_2_zero,
            R.id.editStandardP6_3_zero,
            R.id.editStandardP6_4_zero,
            R.id.editStandardP6_5_zero,
            R.id.editStandardP6_6_zero,
            R.id.editStandardP6_7_zero,
            R.id.editStandardP6_8_zero,
        )

        private val editsStandardP6_offsets = listOf(
            R.id.editStandardP6_0_offset,
            R.id.editStandardP6_1_offset,
            R.id.editStandardP6_2_offset,
            R.id.editStandardP6_3_offset,
            R.id.editStandardP6_4_offset,
            R.id.editStandardP6_5_offset,
            R.id.editStandardP6_6_offset,
            R.id.editStandardP6_7_offset,
            R.id.editStandardP6_8_offset,
        )

        private val editsStandardP7_zeros = listOf(
            R.id.editStandardP7_0_zero,
            R.id.editStandardP7_1_zero,
            R.id.editStandardP7_2_zero,
            R.id.editStandardP7_3_zero,
        )

        private val editsStandardP7_offsets = listOf(
            R.id.editStandardP7_0_offset,
            R.id.editStandardP7_1_offset,
            R.id.editStandardP7_2_offset,
            R.id.editStandardP7_3_offset,
        )

        private val editsMaxiP6_zeros = listOf(
            R.id.editMaxiP6_0_zero,
            R.id.editMaxiP6_1_zero,
            R.id.editMaxiP6_2_zero,
            R.id.editMaxiP6_3_zero,
            R.id.editMaxiP6_4_zero,
            R.id.editMaxiP6_5_zero,
            R.id.editMaxiP6_6_zero,
            R.id.editMaxiP6_7_zero,
            R.id.editMaxiP6_8_zero,
            R.id.editMaxiP6_9_zero,
            R.id.editMaxiP6_10_zero,
        )

        private val editsMaxiP6_offsets = listOf(
            R.id.editMaxiP6_0_offset,
            R.id.editMaxiP6_1_offset,
            R.id.editMaxiP6_2_offset,
            R.id.editMaxiP6_3_offset,
            R.id.editMaxiP6_4_offset,
            R.id.editMaxiP6_5_offset,
            R.id.editMaxiP6_6_offset,
            R.id.editMaxiP6_7_offset,
            R.id.editMaxiP6_8_offset,
            R.id.editMaxiP6_9_offset,
            R.id.editMaxiP6_10_offset,
        )

        private val editsMaxiP7_zeros = listOf(
            R.id.editMaxiP7_0_zero,
            R.id.editMaxiP7_1_zero,
            R.id.editMaxiP7_2_zero,
            R.id.editMaxiP7_3_zero,
        )

        private val editsMaxiP7_offsets = listOf(
            R.id.editMaxiP7_0_offset,
            R.id.editMaxiP7_1_offset,
            R.id.editMaxiP7_2_offset,
            R.id.editMaxiP7_3_offset,
        )

        private val editsNok_zeros = listOf(
            0,
        )

        private val editsNok_offsets = listOf(
            R.id.editNok_offset,
        )

        private const val settingsFileName = "tolerances"
        private const val settingsStandardP6_prefix = "standardP6_%d_%d"
        private const val settingsStandardP7_prefix = "standardP7_%d_%d"
        private const val settingsMaxiP6_prefix = "maxiP6_%d_%d"
        private const val settingsMaxiP7_prefix = "maxiP7_%d_%d"
        private const val settingsNok_prefix = "nok_%d_%d"
        private const val settingsShowBasePoint_prefix = "show_bp"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SettingsFragment.
         */
        @JvmStatic
        fun newInstance() =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                }
            }


        fun getShowBasePoint(): Boolean {
            return isShowBasePoint
        }


        fun loadSettings(context: Context) {
            val preferences = context.getSharedPreferences(settingsFileName, 0)

            readTolerances(preferences, settingsStandardP6_prefix, DataStorage.getToleranceStandardP6())
            readTolerances(preferences, settingsStandardP7_prefix, DataStorage.getToleranceStandardP7())
            readTolerances(preferences, settingsMaxiP6_prefix, DataStorage.getToleranceMaxiP6())
            readTolerances(preferences, settingsMaxiP7_prefix, DataStorage.getToleranceMaxiP7())
            readTolerances(preferences, settingsNok_prefix, DataStorage.getTolerancesNok())

            isShowBasePoint = preferences.getBoolean(settingsShowBasePoint_prefix, isShowBasePoint)

            //MainActivity.broadcastSettingsChange()
        }


        private fun readTolerances(preferences: SharedPreferences, prefix: String, tolerances: Array<DataStorage.PointTolerance>) {
            tolerances.forEachIndexed { index, tolerance ->
                val zeroPrefix = prefix.format(index, 0)
                val origin = preferences.getFloat(zeroPrefix, tolerance.origin.toFloat()).toDouble()
                val offsetPrefix = prefix.format(index, 1)
                val offset = preferences.getFloat(offsetPrefix, tolerance.offset.toFloat()).toDouble()
                tolerances[index] = DataStorage.PointTolerance(origin, offset)
            }
        }


        fun writeTolerances(editor: SharedPreferences.Editor, prefix: String, tolerances: Array<DataStorage.PointTolerance>) {
            tolerances.forEachIndexed { index, tolerance ->
                val zeroPrefix = prefix.format(index, 0)
                editor.putFloat(zeroPrefix, tolerance.origin.toFloat())
                val offsetPrefix = prefix.format(index, 1)
                editor.putFloat(offsetPrefix, tolerance.offset.toFloat())
            }
        }
    }

    private lateinit var viewOfLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private fun writeEdits(view: View, tolerances: Array<DataStorage.PointTolerance>, editsZeros: List<Int>, editsOffsets: List<Int>) {
        tolerances.forEachIndexed { index, tolerance ->
            if (editsZeros[index] != 0) {
                val editZero = view.findViewById<EditText>(editsZeros[index])
                editZero.setText(String.format(Locale.US, "%.1f", tolerance.origin))
            }

            val editOffset = view.findViewById<EditText>(editsOffsets[index])
            editOffset.setText(String.format(Locale.US, "%.1f", tolerance.offset))
        }
    }

    private fun readEdits(view: View, tolerances: Array<DataStorage.PointTolerance>, editsZeros: List<Int>, editsOffsets: List<Int>) {
        tolerances.forEachIndexed { index, _ ->
            val zeroValue = if (editsZeros[index] != 0) {
                val editZero = view.findViewById<EditText>(editsZeros[index])
                editZero.text.toString().toDoubleOrNull() ?: 0.0
            } else {
                0.0
            }

            val editOffset = view.findViewById<EditText>(editsOffsets[index])
            val offsetValue = editOffset.text.toString().toDoubleOrNull() ?: 0.0

            tolerances[index] = DataStorage.PointTolerance(zeroValue, offsetValue)
        }
    }

    private fun writeSwitch(id: Int, isChecked: Boolean) {
        val switch = viewOfLayout.findViewById<SwitchMaterial>(id)
        switch.isChecked = isChecked
    }

    private fun readSwitch(id: Int): Boolean {
        val switch = viewOfLayout.findViewById<SwitchMaterial>(id)
        return switch.isChecked
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_settings, container, false)

        val refreshButton = viewOfLayout.findViewById<FloatingActionButton>(R.id.buttonSaveSettings)
        refreshButton.setOnClickListener { saveSettings() }

        writeEdits(viewOfLayout, DataStorage.getToleranceStandardP6(), editsStandardP6_zeros, editsStandardP6_offsets)
        writeEdits(viewOfLayout, DataStorage.getToleranceStandardP7(), editsStandardP7_zeros, editsStandardP7_offsets)
        writeEdits(viewOfLayout, DataStorage.getToleranceMaxiP6(), editsMaxiP6_zeros, editsMaxiP6_offsets)
        writeEdits(viewOfLayout, DataStorage.getToleranceMaxiP7(), editsMaxiP7_zeros, editsMaxiP7_offsets)
        writeEdits(viewOfLayout, DataStorage.getTolerancesNok(), editsNok_zeros, editsNok_offsets)

        writeSwitch(R.id.switchShowBasePoint, isShowBasePoint)

        viewOfLayout.findViewById<RadioGroup>(R.id.radioTolerances).setOnCheckedChangeListener(radioTolerancesListener)
        viewOfLayout.findViewById<RadioButton>(R.id.radioTolerancesStandard).isChecked = true

        return viewOfLayout
    }


    private val radioTolerancesListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            val tableStandard = viewOfLayout.findViewById<TableLayout>(R.id.tableTolerancesStandard)
            val tableMaxi = viewOfLayout.findViewById<TableLayout>(R.id.tableTolerancesMaxi)
            tableStandard.visibility = View.GONE
            tableMaxi.visibility = View.GONE

            when (checkedId) {
                R.id.radioTolerancesStandard -> tableStandard.visibility = View.VISIBLE
                R.id.radioTolerancesMaxi -> tableMaxi.visibility = View.VISIBLE
            }
        }


    private fun saveSettings() {
        val message: String = requireContext().resources.getString(R.string.save_msg)
        Snackbar.make(viewOfLayout, message, 250).show()

        readEdits(viewOfLayout, DataStorage.getToleranceStandardP6(), editsStandardP6_zeros, editsStandardP6_offsets)
        readEdits(viewOfLayout, DataStorage.getToleranceStandardP7(), editsStandardP7_zeros, editsStandardP7_offsets)
        readEdits(viewOfLayout, DataStorage.getToleranceMaxiP6(), editsMaxiP6_zeros, editsMaxiP6_offsets)
        readEdits(viewOfLayout, DataStorage.getToleranceMaxiP7(), editsMaxiP7_zeros, editsMaxiP7_offsets)
        readEdits(viewOfLayout, DataStorage.getTolerancesNok(), editsNok_zeros, editsNok_offsets)

        isShowBasePoint = readSwitch(R.id.switchShowBasePoint)

        val preferences = viewOfLayout.context.getSharedPreferences(settingsFileName, 0)
        val editor = preferences.edit()
        writeTolerances(editor, settingsStandardP6_prefix, DataStorage.getToleranceStandardP6())
        writeTolerances(editor, settingsStandardP7_prefix, DataStorage.getToleranceStandardP7())
        writeTolerances(editor, settingsMaxiP6_prefix, DataStorage.getToleranceMaxiP6())
        writeTolerances(editor, settingsMaxiP7_prefix, DataStorage.getToleranceMaxiP7())
        writeTolerances(editor, settingsNok_prefix, DataStorage.getTolerancesNok())
        editor.putBoolean(settingsShowBasePoint_prefix, isShowBasePoint)
        editor.apply()

        MainActivity.broadcastSettingsChange()
    }
}