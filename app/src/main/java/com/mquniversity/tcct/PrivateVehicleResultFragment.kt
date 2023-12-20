package com.mquniversity.tcct

import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textview.MaterialTextView
import com.google.maps.model.DirectionsRoute

open class PrivateVehicleResultFragment: ResultFragment() {
    protected var factor = 0f

    override fun insertRouteResult(route: DirectionsRoute, i: Int) {
        val resultLayout = LinearLayout(context)
        resultLayout.orientation = LinearLayout.HORIZONTAL
        resultLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val iconLayout = LinearLayout(context)
        iconLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        )
        iconLayout.setPadding(0, 16, 0, 0)
        val carIcon = ImageView(context)
        carIcon.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        carIcon.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                iconResId!!,
                context?.theme
            ))
        iconLayout.addView(carIcon)

        val descLayout = LinearLayout(context)
        descLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            4f
        )
        descLayout.orientation = LinearLayout.VERTICAL
        val summaryText = MaterialTextView(requireContext())
        summaryText.text = "via ${route.summary}"
        summaryText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        descLayout.addView(summaryText)
//        val tapForMoreText = MaterialTextView(requireContext())
//        tapForMoreText.text = getString(R.string.tap_for_more_details)
//        tapForMoreText.setTextColor(resources.getColor(com.google.android.libraries.places.R.color.quantum_googblue, context?.theme))
//        tapForMoreText.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        (tapForMoreText.layoutParams as LinearLayout.LayoutParams).apply {
//            gravity = Gravity.BOTTOM
//        }
//        descLayout.addView(tapForMoreText)

        val valuesLayout = LinearLayout(context)
        valuesLayout.orientation = LinearLayout.VERTICAL
        valuesLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            2f
        )
        val emissionText = MaterialTextView(requireContext())
        emissionText.text = CalculationUtils.calculateByDistance(route.legs[0].distance.inMeters, factor)
        emissionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        val distText = MaterialTextView(requireContext())
        distText.text = route.legs[0].distance.humanReadable
        distText.alpha = 0.5f
        val durationText = MaterialTextView(requireContext())
        durationText.text = route.legs[0].duration.humanReadable
        durationText.alpha = 0.5f
        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)
        valuesLayout.addView(emissionText)
        valuesLayout.addView(distText)
        valuesLayout.addView(durationText)

        resultLayout.addView(iconLayout)
        resultLayout.addView(descLayout)
        resultLayout.addView(valuesLayout)

        mainLayout.addView(resultLayout)
    }
}