package local.tools.pomiary

import android.graphics.Color

enum class PointResult {
    UNKNOWN,
    OK,
    NOK,
    WARNING,
    CRITICAL,
    INVALID
    ;

    fun toInt(): Int {
        return when (this) {
            OK -> 1
            NOK -> 2
            WARNING -> 3
            CRITICAL -> 4
            INVALID -> 5
            else -> 0
        }
    }

    companion object {
        fun fromInt(result: Int): PointResult {
            return when (result) {
                1 -> OK
                2 -> NOK
                3 -> WARNING
                4 -> CRITICAL
                5 -> INVALID
                else -> UNKNOWN
            }
        }
    }

    fun toColor(): Int {
        return when (this) {
            OK -> Color.GREEN
            NOK -> Color.RED
            WARNING -> Color.YELLOW
            CRITICAL -> Color.RED
            INVALID -> Color.RED
            else -> Color.LTGRAY
        }
    }


    //private const val wrongText: String = view.resources.getString(R.string.result_Nok)
    //private const val goodText: String = view.resources.getString(R.string.result_Ok)
    fun toMessage(): String {
        return when (this) {
            OK -> "OK"
            NOK -> "NOK"
            WARNING -> "OK"
            CRITICAL -> "NOK"
            INVALID -> "\u2048"
            else -> "?"
        }
    }
}