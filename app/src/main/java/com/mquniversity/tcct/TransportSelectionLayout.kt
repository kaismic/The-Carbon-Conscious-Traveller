package com.mquniversity.tcct

import android.widget.LinearLayout
import android.content.Context
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.R.color.design_default_color_background
import com.google.android.material.R.style.Widget_Material3_Button_TextButton_Icon
import com.google.android.material.button.MaterialButton

class TransportSelectionLayout(mainActivityContext: Context): LinearLayout(mainActivityContext) {
    private val btns: Array<MaterialButton> = Array(TransportModes.entries.size) {
        MaterialButton(mainActivityContext, null, Widget_Material3_Button_TextButton_Icon)
    }
    private val icons: IntArray = intArrayOf(
        R.drawable.outline_directions_car_24,
        R.drawable.outline_directions_subway_24,
        R.drawable.outline_directions_walk_24,
        R.drawable.outline_directions_bike_24,
        R.drawable.outline_directions_boat_24,
        R.drawable.outline_flight_24
    )
    init {
        setBackgroundColor(resources.getColor(design_default_color_background, mainActivityContext.theme))
        orientation = HORIZONTAL
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        for (i in TransportModes.entries.indices) {
            addView(btns[i])
            btns[i].layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            btns[i].icon = ResourcesCompat.getDrawable(
                resources,
                icons[i],
                context.theme
            )
            btns[i].text = "36 min"
        }
    }
}