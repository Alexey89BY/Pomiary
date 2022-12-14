package local.tools.pomiary.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import local.tools.pomiary.MainActivity
import local.tools.pomiary.R

/**
 * A simple [Fragment] subclass.
 * Use the [MaxiFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MaxiFragment : Fragment() {
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
        viewOfLayout = inflater.inflate(R.layout.fragment_maxi, container, false)
        val refreshButton = viewOfLayout.findViewById<FloatingActionButton>(R.id.buttonRefreshMaxi)
        refreshButton.setOnClickListener { recalculateValues() }
        return viewOfLayout
    }

    private val editsMaxiP6 = arrayOf(
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

    private val editsMaxiP7 = arrayOf(
        R.id.editMaxiP7_0,
        R.id.editMaxiP7_1,
        R.id.editMaxiP7_2,
        R.id.editMaxiP7_3,
    )

    private val textsResultMaxiP6 = arrayOf(
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

    private var textsResultMaxiP7 = arrayOf(
        R.id.textResultMaxiP7_0,
        R.id.textResultMaxiP7_1,
        R.id.textResultMaxiP7_2,
        R.id.textResultMaxiP7_3,
    )

    private val textsNokMaxiP6 = arrayOf(
        R.id.textNokMaxiP6_0,
        R.id.textNokMaxiP6_1,
        R.id.textNokMaxiP6_2,
        R.id.textNokMaxiP6_3,
        R.id.textNokMaxiP6_4,
        R.id.textNokMaxiP6_5,
        R.id.textNokMaxiP6_6,
        R.id.textNokMaxiP6_7,
        R.id.textNokMaxiP6_8,
        R.id.textNokMaxiP6_9,
        R.id.textNokMaxiP6_10,
    )

    private var textsNokMaxiP7 = arrayOf(
        R.id.textNokMaxiP7_0,
        R.id.textNokMaxiP7_1,
        R.id.textNokMaxiP7_2,
        R.id.textNokMaxiP7_3,
    )

    private fun recalculateValues() {
        val message: String = requireContext().resources.getString(R.string.check_maxi)
        Snackbar.make(viewOfLayout, message, 250).show()

        val toleranceMaxiP6 = SettingsFragment.getToleranceMaxiP6()
        MainActivity.alignPoints(viewOfLayout, toleranceMaxiP6, editsMaxiP6, textsResultMaxiP6, textsNokMaxiP6)

        val toleranceMaxiP7 = SettingsFragment.getToleranceMaxiP7()
        MainActivity.alignPoints(viewOfLayout, toleranceMaxiP7, editsMaxiP7, textsResultMaxiP7, textsNokMaxiP7)
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