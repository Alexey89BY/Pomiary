package local.tools.pomiary.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import local.tools.pomiary.PointsAligner
import local.tools.pomiary.R

/**
 * A simple [Fragment] subclass.
 * Use the [StandardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StandardFragment : Fragment() {
    private lateinit var viewOfLayout: View

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
        return viewOfLayout
    }

    private val editsStandardP6 = arrayOf(
        R.id.editStandardP6_0,
        R.id.editStandardP6_1,
        R.id.editStandardP6_2,
        R.id.editStandardP6_3,
        R.id.editStandardP6_4,
        R.id.editStandardP6_5,
        R.id.editStandardP6_6,
        R.id.editStandardP6_7,
        R.id.editStandardP6_8,
    )

    private val editsStandardP7 = arrayOf(
        R.id.editStandardP7_0,
        R.id.editStandardP7_1,
        R.id.editStandardP7_2,
        R.id.editStandardP7_3,
    )

    private val textsResultStandardP6 = arrayOf(
        R.id.textResultStandardP6_0,
        R.id.textResultStandardP6_1,
        R.id.textResultStandardP6_2,
        R.id.textResultStandardP6_3,
        R.id.textResultStandardP6_4,
        R.id.textResultStandardP6_5,
        R.id.textResultStandardP6_6,
        R.id.textResultStandardP6_7,
        R.id.textResultStandardP6_8,
    )

    private val textsResultStandardP7 = arrayOf(
        R.id.textResultStandardP7_0,
        R.id.textResultStandardP7_1,
        R.id.textResultStandardP7_2,
        R.id.textResultStandardP7_3,
    )

    private val textsNokStandardP6 = arrayOf(
        R.id.textNokStandardP6_0,
        R.id.textNokStandardP6_1,
        R.id.textNokStandardP6_2,
        R.id.textNokStandardP6_3,
        R.id.textNokStandardP6_4,
        R.id.textNokStandardP6_5,
        R.id.textNokStandardP6_6,
        R.id.textNokStandardP6_7,
        R.id.textNokStandardP6_8,
    )

    private val textsNokStandardP7 = arrayOf(
        R.id.textNokStandardP7_0,
        R.id.textNokStandardP7_1,
        R.id.textNokStandardP7_2,
        R.id.textNokStandardP7_3,
    )

    private fun recalculateValues() {
        val message: String = requireContext().resources.getString(R.string.check_std)
        Snackbar.make(viewOfLayout, message, 250).show()

        val toleranceStandardP6 = SettingsFragment.getToleranceStandardP6()
        PointsAligner.alignPoints(viewOfLayout, toleranceStandardP6, editsStandardP6, textsResultStandardP6, textsNokStandardP6)

        val toleranceStandardP7 = SettingsFragment.getToleranceStandardP7()
        PointsAligner.alignPoints(viewOfLayout, toleranceStandardP7, editsStandardP7, textsResultStandardP7, textsNokStandardP7)
    }

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