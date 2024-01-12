package com.mquniversity.tcct

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

open class PrivateVehicleResultFragment: ResultFragment() {
    protected var factor = 0f
    protected var iconResId: Int? = null

    protected val emissionTexts: MutableList<TextView> = mutableListOf()
    protected val distTexts: MutableList<TextView> = mutableListOf()
    protected val durationTexts: MutableList<TextView> = mutableListOf()

    override fun update(reload: Boolean) {
        if (!reload && areLocationsSameAsBefore()) {
            showPolylines()
            mainActivity.enableButtons(true)
            return
        }
        emissionTexts.clear()
        distTexts.clear()
        durationTexts.clear()
        super.update(reload)
    }

    override fun insertRouteResult(idx: Int): Float {
        resultLayouts[idx] = layoutInflater.inflate(
            R.layout.private_vehicle_result_item,
            mainLayout,
            false) as LinearLayout

        super.insertRouteResult(idx)

        val resultLayout = resultLayouts[idx]!!
        val route = currRoutes[idx]

        resultLayout.findViewById<ImageView>(R.id.private_vehicle_icon)
            .apply {
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        iconResId!!,
                        context?.theme
                    )
                )
            }

        val summaryText: TextView = resultLayout.findViewById(R.id.summary_text)
        summaryText.text = getString(R.string.summary_text, route.summary)

        val emissionText: TextView = resultLayout.findViewById(R.id.emission_text)
        emissionText.text = CalculationUtils.formatEmission(route.legs[0].distance.inMeters * factor)
        val distText: TextView = resultLayout.findViewById(R.id.distance_text)
        distText.text = route.legs[0].distance.humanReadable
        val durationText: TextView = resultLayout.findViewById(R.id.duration_text)
        durationText.text = route.legs[0].duration.humanReadable

        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)
        return route.legs[0].distance.inMeters * factor
    }
}
