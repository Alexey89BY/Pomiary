package local.tools.pomiary.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import local.tools.pomiary.PointsAligner
import local.tools.pomiary.R
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryFragment : Fragment() {
    private lateinit var viewOfLayout: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewOfLayout = inflater.inflate(R.layout.fragment_history, container, false)

        val refreshButton = viewOfLayout.findViewById<FloatingActionButton>(R.id.buttonRefreshHistory)
        refreshButton.setOnClickListener { refreshHistory() }

        return viewOfLayout
    }


    private fun refreshHistory() {
        val context = requireContext()

        val message: String = context.resources.getString(R.string.load_history)
        Snackbar.make(viewOfLayout, message, 250).show()

        var historyLines: List<String> = emptyList()

        val file = getHistoryFile(context)
        if (file.exists()) {
            historyLines = file.readLines()
        }

        val listView = viewOfLayout.findViewById<ListView>(R.id.listHistory)
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, historyLines.asReversed())
        listView.adapter = adapter
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment StandardFragment.
         */
        @JvmStatic
        fun newInstance() =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                }
            }

        @JvmStatic
        private fun getHistoryFile(context: Context): File {
            val filesDir = context.getExternalFilesDir(null)
            return File(filesDir, "history.csv")
        }


        @JvmStatic
        fun checkHistory(
            context: Context
        )  {
            val file = getHistoryFile(context)
            if (! file.exists()) {
                file.createNewFile()
            }
        }


        private const val delimiter = " "

        @JvmStatic
        fun savePoints(
            fragment: Fragment,
            title: String,
            points1: Array<PointsAligner.Point>,
            points2: Array<PointsAligner.Point>
        ) {
            val context = fragment.requireContext()
            val file = getHistoryFile(context)

            if (file.exists()) {
                file.appendText(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()))
                file.appendText(delimiter)
                file.appendText(title)
                writePointsToFile(file, points1)
                writePointsToFile(file, points2)
                file.appendText("\n")
            }
        }

        private fun writePointsToFile(file: File, pointsArray: Array<PointsAligner.Point>) {
            pointsArray.forEachIndexed {index, it ->
                val nokText = "*"
                val baseText = "!"
                file.appendText(delimiter)
                file.appendText(it.value.toString())
                if (index == 0) {
                    file.appendText(baseText)
                } else
                if (it.result != PointsAligner.PointResult.OK) {
                    file.appendText(nokText)
                }
            }
        }
    }
}