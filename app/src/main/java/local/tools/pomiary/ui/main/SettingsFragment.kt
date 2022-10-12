package local.tools.pomiary.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import local.tools.pomiary.R


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    companion object {
        private var toleranceStandardP6 = arrayOf(
            Pair(0.0F, 3.0F),
            Pair(21.0F, 1.5F),
            Pair(123.0F, 2.5F),
            Pair(225.0F, 2.5F),
            Pair(327.0F, 2.5F),
            Pair(429.0F, 2.5F),
            Pair(531.0F, 2.5F),
            Pair(633.0F, 2.5F),
            Pair(655.0F, 3.0F)
        )

        private var toleranceStandardP7 = arrayOf(
            Pair(0.0F, 2.5F),
            Pair(29.0F, 2.5F),
            Pair(114.0F, 2.5F),
            Pair(149.0F, 2.5F)
        )

        private var toleranceMaxiP6 = arrayOf(
            Pair(0.0F, 3.0F),
            Pair(14.0F, 1.5F),
            Pair(116.0F, 2.5F),
            Pair(218.0F, 2.5F),
            Pair(320.0F, 2.5F),
            Pair(422.0F, 2.5F),
            Pair(524.0F, 2.5F),
            Pair(626.0F, 2.5F),
            Pair(728.0F, 2.5F),
            Pair(830.0F, 2.5F),
            Pair(839.5F, 3.0F)
        )

        private var toleranceMaxiP7 = arrayOf(
            Pair(0.0F, 2.5F),
            Pair(22.5F, 1.5F),
            Pair(107.5F, 2.5F),
            Pair(130.5F, 2.5F)
        )

        private var toleranceNok = arrayOf(
            Pair(0.0F, 0.5F),
        )

        private val editsStandardP6_zeros = arrayOf(
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

        private val editsStandardP6_offsets = arrayOf(
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

        private val editsStandardP7_zeros = arrayOf(
            R.id.editStandardP7_0_zero,
            R.id.editStandardP7_1_zero,
            R.id.editStandardP7_2_zero,
            R.id.editStandardP7_3_zero,
        )

        private val editsStandardP7_offsets = arrayOf(
            R.id.editStandardP7_0_offset,
            R.id.editStandardP7_1_offset,
            R.id.editStandardP7_2_offset,
            R.id.editStandardP7_3_offset,
        )

        private val editsMaxiP6_zeros = arrayOf(
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

        private val editsMaxiP6_offsets = arrayOf(
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

        private val editsMaxiP7_zeros = arrayOf(
            R.id.editMaxiP7_0_zero,
            R.id.editMaxiP7_1_zero,
            R.id.editMaxiP7_2_zero,
            R.id.editMaxiP7_3_zero,
        )

        private val editsMaxiP7_offsets = arrayOf(
            R.id.editMaxiP7_0_offset,
            R.id.editMaxiP7_1_offset,
            R.id.editMaxiP7_2_offset,
            R.id.editMaxiP7_3_offset,
        )

        private val editsNok_zeros = arrayOf(
            R.id.editNok_zero,
        )

        private val editsNok_offsets = arrayOf(
            R.id.editNok_offset,
        )

        private const val settingsFileName = "tolerances"
        private const val settingsStandardP6_prefix = "standardP6_%d_%d"
        private const val settingsStandardP7_prefix = "standardP7_%d_%d"
        private const val settingsMaxiP6_prefix = "maxiP6_%d_%d"
        private const val settingsMaxiP7_prefix = "maxiP7_%d_%d"
        private const val settingsNok_prefix = "nok_%d_%d"

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

        @JvmStatic
        fun getToleranceStandardP6(): Array<Pair<Float,Float>> {
            return toleranceStandardP6
        }

        @JvmStatic
        fun getToleranceStandardP7(): Array<Pair<Float,Float>> {
            return toleranceStandardP7
        }

        @JvmStatic
        fun getToleranceMaxiP6(): Array<Pair<Float,Float>> {
            return toleranceMaxiP6
        }

        @JvmStatic
        fun getToleranceMaxiP7(): Array<Pair<Float,Float>> {
            return toleranceMaxiP7
        }

        @JvmStatic
        fun getToleranceNok(): Float {
            return toleranceNok[0].second
        }

        @JvmStatic
        fun loadSettings(context: Context) {
            val preferences = context.getSharedPreferences(settingsFileName, 0)
            readTolerances(preferences, settingsStandardP6_prefix, toleranceStandardP6)
            readTolerances(preferences, settingsStandardP7_prefix, toleranceStandardP7)
            readTolerances(preferences, settingsMaxiP6_prefix, toleranceMaxiP6)
            readTolerances(preferences, settingsMaxiP7_prefix, toleranceMaxiP7)
            readTolerances(preferences, settingsNok_prefix, toleranceNok)
        }

        @JvmStatic
        fun readTolerances(preferences: SharedPreferences, prefix: String, tolerances: Array<Pair<Float, Float>>) {
            tolerances.forEachIndexed { index, tolerance ->
                val zeroPrefix = prefix.format(index, 0)
                val first = preferences.getFloat(zeroPrefix, tolerance.first)
                val offsetPrefix = prefix.format(index, 1)
                val second = preferences.getFloat(offsetPrefix, tolerance.second)
                tolerances[index] = Pair(first, second)
            }
        }

        @JvmStatic
        fun writeTolerances(editor: SharedPreferences.Editor, prefix: String, tolerances: Array<Pair<Float, Float>>) {
            tolerances.forEachIndexed { index, tolerance ->
                val zeroPrefix = prefix.format(index, 0)
                editor.putFloat(zeroPrefix, tolerance.first)
                val offsetPrefix = prefix.format(index, 1)
                editor.putFloat(offsetPrefix, tolerance.second)
            }
        }
    }

    private lateinit var viewOfLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private fun writeEdits(view: View, tolerances: Array<Pair<Float, Float>>, editsZeros: Array<Int>, editsOffsets: Array<Int>) {
        tolerances.forEachIndexed { index, tolerance ->
            val editZero = view.findViewById<EditText>(editsZeros[index])
            editZero.setText(tolerance.first.toString())

            val editOffset = view.findViewById<EditText>(editsOffsets[index])
            editOffset.setText(tolerance.second.toString())
        }
    }

    private fun readEdits(view: View, tolerances: Array<Pair<Float, Float>>, editsZeros: Array<Int>, editsOffsets: Array<Int>) {
        tolerances.forEachIndexed { index, _ ->
            val editZero = view.findViewById<EditText>(editsZeros[index])
            val textZero = editZero.text.toString()
            val zeroValue = textZero.toFloatOrNull()

            val editOffset = view.findViewById<EditText>(editsOffsets[index])
            val textOffset = editOffset.text.toString()
            val offsetValue = textOffset.toFloatOrNull()

            tolerances[index] = Pair(zeroValue ?: 0.0F, offsetValue ?: 0.0F)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_settings, container, false)

        val refreshButton = viewOfLayout.findViewById<FloatingActionButton>(R.id.buttonSaveSettings)
        refreshButton.setOnClickListener { saveSettings() }

        writeEdits(viewOfLayout, toleranceStandardP6, editsStandardP6_zeros, editsStandardP6_offsets)
        writeEdits(viewOfLayout, toleranceStandardP7, editsStandardP7_zeros, editsStandardP7_offsets)
        writeEdits(viewOfLayout, toleranceMaxiP6, editsMaxiP6_zeros, editsMaxiP6_offsets)
        writeEdits(viewOfLayout, toleranceMaxiP7, editsMaxiP7_zeros, editsMaxiP7_offsets)
        writeEdits(viewOfLayout, toleranceNok, editsNok_zeros, editsNok_offsets)

        return viewOfLayout
    }

    private fun saveSettings() {
        val message: String = requireContext().resources.getString(R.string.save_msg)
        Snackbar.make(viewOfLayout, message, 250).show()

        readEdits(viewOfLayout, toleranceStandardP6, editsStandardP6_zeros, editsStandardP6_offsets)
        readEdits(viewOfLayout, toleranceStandardP7, editsStandardP7_zeros, editsStandardP7_offsets)
        readEdits(viewOfLayout, toleranceMaxiP6, editsMaxiP6_zeros, editsMaxiP6_offsets)
        readEdits(viewOfLayout, toleranceMaxiP7, editsMaxiP7_zeros, editsMaxiP7_offsets)
        readEdits(viewOfLayout, toleranceNok, editsNok_zeros, editsNok_offsets)

        val preferences = viewOfLayout.context.getSharedPreferences(settingsFileName, 0)
        val editor = preferences.edit()
        writeTolerances(editor, settingsStandardP6_prefix, toleranceStandardP6)
        writeTolerances(editor, settingsStandardP7_prefix, toleranceStandardP7)
        writeTolerances(editor, settingsMaxiP6_prefix, toleranceMaxiP6)
        writeTolerances(editor, settingsMaxiP7_prefix, toleranceMaxiP7)
        writeTolerances(editor, settingsNok_prefix, toleranceNok)
        editor.apply()
    }
}