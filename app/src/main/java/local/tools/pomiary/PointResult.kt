package local.tools.pomiary

import android.graphics.Color

enum class PointResult {
    UNKNOWN,
    OK,
    NOK,
    WARNING_DOWN,
    WARNING_UP,
    CRITICAL_DOWN,
    CRITICAL_UP,
    INVALID
    ;

    fun toInt(): Int {
        return when (this) {
            OK -> 1
            NOK -> 2
            WARNING_DOWN -> 3
            WARNING_UP -> 4
            CRITICAL_DOWN -> 5
            CRITICAL_UP -> 6
            INVALID -> 7
            else -> 0
        }
    }

    companion object {
        fun fromInt(result: Int): PointResult {
            return when (result) {
                1 -> OK
                2 -> NOK
                3 -> WARNING_DOWN
                4 -> WARNING_UP
                5 -> CRITICAL_DOWN
                6 -> CRITICAL_UP
                7 -> INVALID
                else -> UNKNOWN
            }
        }
    }

    fun toColor(): Int {
        return when (this) {
            OK -> Color.GREEN
            WARNING_DOWN,
            WARNING_UP -> Color.YELLOW
            CRITICAL_DOWN,
            CRITICAL_UP -> Color.RED
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
            WARNING_DOWN,
            CRITICAL_DOWN -> "NOK\u2193"
            WARNING_UP,
            CRITICAL_UP -> "NOK\u2191"
            INVALID -> "\u2048"
            else -> "?"
        }
    }
}