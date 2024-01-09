package com.mquniversity.tcct

object CalculationUtils {
    /**
     * @return formatted text of CO2e emission in kilogram or gram
     */
    fun calculateByDistance(distInMeter: Long, factorKgKm: Float): String {
        val emissionInGram: Float = distInMeter * factorKgKm
        return formatEmissionWithCO2(emissionInGram, true)
    }

    fun formatEmission(emissionInGram: Float): String {
        if (emissionInGram >= 1000) {
            return String.format("%.2f", emissionInGram / 1000) + " kg"
        }
        return String.format("%.0f", emissionInGram) + " g"
    }

    fun formatEmissionWithCO2(emissionInGram: Float, equivalent: Boolean): String {
        val e = if (equivalent) "e" else ""
        if (emissionInGram >= 1000) {
            return String.format("%.2f", emissionInGram / 1000) + " kg CO2" + e
        }
        return String.format("%.0f", emissionInGram) + " g CO2" + e
    }
}