package com.mquniversity.tcct

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.maps.model.DirectionsRoute

open class PrivateVehicleResultFragment: ResultFragment() {
    protected var factor = 0f
    protected var iconResId: Int? = null

    protected val emissionTexts: MutableList<TextView> = mutableListOf()
    protected val distTexts: MutableList<TextView> = mutableListOf()
    protected val durationTexts: MutableList<TextView> = mutableListOf()

    override fun updateRouteResults() {
        // return if the current start and end location is same as before
        if (currOrigin != null && currDest != null
            && mainActivity.origin?.address == currOrigin?.address
            && mainActivity.dest?.address == currDest?.address) {
            return
        }
        emissionTexts.clear()
        distTexts.clear()
        durationTexts.clear()
        super.updateRouteResults()
    }

    override fun insertRouteResult(route: DirectionsRoute, i: Int) {
        val resultLayout = layoutInflater.inflate(
            R.layout.private_vehicle_result_item,
            mainLayout,
            false) as LinearLayout

        val icon: ImageView = resultLayout.findViewById(R.id.private_vehicle_icon)
        icon.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                iconResId!!,
                context?.theme
            ))

        val summaryText: TextView = resultLayout.findViewById(R.id.summary_text)
        summaryText.text = "via ${route.summary}"

        val emissionText: TextView = resultLayout.findViewById(R.id.emission_text)
        emissionText.text = CalculationUtils.calculateByDistance(route.legs[0].distance.inMeters, factor)
        val distText: TextView = resultLayout.findViewById(R.id.distance_text)
        distText.text = route.legs[0].distance.humanReadable
        val durationText: TextView = resultLayout.findViewById(R.id.duration_text)
        durationText.text = route.legs[0].duration.humanReadable

        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)

        mainLayout.addView(resultLayout)
    }
}