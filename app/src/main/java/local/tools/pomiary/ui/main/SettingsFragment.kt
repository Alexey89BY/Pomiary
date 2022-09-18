package local.tools.pomiary.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            Pair(0.0F, 1.5F),
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
            Pair(0.0F, 1.5F),
            Pair(14.0F, 1.5F),
            Pair(116.0F, 2.5F),
            Pair(218.0F, 2.5F),
            Pair(320.0F, 2.5F),
            Pair(422.0F, 2.5F),
            Pair(524.0F, 2.5F),
            Pair(626.0F, 2.5F),
            Pair(728.0F, 2.5F),
            Pair(830.0F, 2.5F),
            Pair(842.5F, 3.0F)
        )

        private var toleranceMaxiP7 = arrayOf(
            Pair(0.0F, 1.5F),
            Pair(22.5F, 1.5F),
            Pair(107.5F, 2.5F),
            Pair(130.5F, 2.5F)
        )

        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
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
        fun loadSettings() {

        }
    }

    private lateinit var viewOfLayout: View
    // TODO: Rename and change types of parameters
    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
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
        return viewOfLayout
    }

    private fun saveSettings() {
        val message: String = requireContext().resources.getString(R.string.save_msg)
        Snackbar.make(viewOfLayout, message, 250).show()
    }
}