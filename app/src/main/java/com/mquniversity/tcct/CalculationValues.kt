package com.mquniversity.tcct

import android.content.Context
import com.opencsv.CSVReader

const val MOTORCYCLE = "motorcycle"
const val BUS = "bus"
const val RAIL = "rail"
const val FERRY = "ferry"

class CalculationValues(context: Context) {
    private val simpleTransportModes = arrayOf(MOTORCYCLE, BUS, RAIL, FERRY)

    val carSizes = mutableListOf<String>()
    val carFuelTypes: Array<String>
    val carValuesMatrix = mutableListOf<FloatArray>()

    val simpleTransportTypes = HashMap<String, MutableList<String>>()
    val simpleTransportValues = HashMap<String, MutableList<Float>>()

    init {
        // read car calculation values
        val carReader = CSVReader(context.assets.open("car.csv").reader())
        var nextLine = carReader.readNext()
        carFuelTypes = nextLine.sliceArray(1..<nextLine.size)
        while (true) {
            nextLine = carReader.readNext()
            if (nextLine == null) {
                break
            }
            carSizes.add(nextLine[0])
            val strValues = nextLine.sliceArray(1..<nextLine.size)
            val floatValues = strValues.map { if (it.isEmpty()) 0f else it.toFloat() }.toFloatArray()
            carValuesMatrix.add(floatValues)
        }

        for (mode in simpleTransportModes) {
            simpleTransportTypes[mode] = mutableListOf()
            val reader = CSVReader(context.assets.open("${mode}.csv").reader())
            reader.skip(1)
            while (true) {
                nextLine = reader.readNext()
                if (nextLine == null) {
                    break
                }
                simpleTransportTypes[mode]?.add(nextLine[0])
                simpleTransportValues[mode]?.add(nextLine[1].toFloat())
            }
        }
    }

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
