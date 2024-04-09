package local.tools.pomiary

import kotlin.math.roundToInt


data class PointData(
    var rawInput: String = String(),
    var rawValue: Double = 0.0,
    var value: Double = 0.0,
    var result: PointResult = PointResult.UNKNOWN
) {
    companion object {
		private const val intScale = 100.0
		private const val stringScale = 100.0
		private const val decimalSeparator = '.' // DecimalFormatSymbols(Locale.US).decimalSeparator

        fun valueFromInt(value: Int): Double {
            return value / intScale
        }

        fun valueToInt(value: Double): Int {
            return (value * intScale).roundToInt()
        }

        fun valueFromString(string: String): Double {
            val value = string.toDoubleOrNull() ?: 0.0
            return if (string.contains(decimalSeparator)) value else (value / stringScale)
        }

        fun valueFromIntString(string: String): Double {
            val intValue = string.toIntOrNull() ?: 0
            return valueFromInt(intValue)
        }

        fun valueToIntString(value: Double): String {
            val intValue = valueToInt(value)
            return intValue.toString()
        }
    }
}