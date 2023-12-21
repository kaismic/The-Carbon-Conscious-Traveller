package com.mquniversity.tcct

import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.maps.model.DirectionsRoute

open class PrivateVehicleResultFragment: ResultFragment() {
    protected var factor = 0f

    override fun insertRouteResult(route: DirectionsRoute, i: Int) {
        val resultLayout = layoutInflater.inflate(
            R.layout.private_vehicle_result_item,
            mainLayout,
            false) as LinearLayout

        val carIcon: ImageView = resultLayout.findViewById(R.id.transport_mode_icon)
        carIcon.setImageDrawable(
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
        Log.i("distance testing", route.legs[0].distance.humanReadable)
        val durationText: TextView = resultLayout.findViewById(R.id.duration_text)
        durationText.text = route.legs[0].duration.humanReadable
        Log.i("duration testing", route.legs[0].duration.humanReadable)

        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)

        mainLayout.addView(resultLayout)
    }
}