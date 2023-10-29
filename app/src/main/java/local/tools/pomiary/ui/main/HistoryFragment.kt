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
import local.tools.pomiary.DataStorage
import local.tools.pomiary.R
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

        private fun getHistoryFile(context: Context): File {
            val filesDir = context.getExternalFilesDir(null)
            return File(filesDir, "history.csv")
        }


        fun checkHistory(
            context: Context
        )  {
            val file = getHistoryFile(context)
            if (! file.exists()) {
                file.createNewFile()
            }
        }


        fun generateTimeStamp(): String {
            return DateTimeFormatter.ofPattern("dd.MM.yy HH:mm").format(LocalDateTime.now())
        }


        private const val delimiter = ","

        fun savePoints(
            fragment: Fragment,
            subsetTitle: String,
            dataTitle: String,
            timeStamp: String,
            points1: Array<DataStorage.PointData>,
            points2: Array<DataStorage.PointData>,
        ) {
            val context = fragment.requireContext()
            val file = getHistoryFile(context)

            if (file.exists()) {
                val line = buildString {
                    append(timeStamp)
                    append(delimiter)
                    append(subsetTitle)
                    append(delimiter)
                    append(dataTitle)
                    writePointsToFile(this, points1)
                    writePointsToFile(this, points2)
                    appendLine()
                }

                file.appendText(line)
            }
        }

        private fun writePointsToFile(builder: StringBuilder, pointsArray: Array<DataStorage.PointData>) {
            pointsArray.forEachIndexed {_, it ->
                //val nokText = "*"
                //val baseText = "!"
                builder.append(delimiter)
                builder.append(String.format(Locale.US, "%.2f", it.value))
                //if (index == 0) {
                //    builder.append(baseText)
                //} else
                //if (it.result != DataStorage.PointResult.OK) {
                //    builder.append(nokText)
                // }
            }
        }
    }
}