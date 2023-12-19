package com.mquniversity.tcct

object CalculationUtils {
    /**
     * @return formatted text of CO2e emission in kilogram or gram
     */
    fun calculateByDistance(distInMeter: Long, factor: Float): String {
        val emission: Float = distInMeter * factor
        if (emission > 1000) {
            return String.format("%.2f", emission / 1000) + " kg CO2e"
        }
        return String.format("%.2f", emission) + " g CO2e"
    }
}