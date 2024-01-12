package com.mquniversity.tcct

object CalculationUtils {
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