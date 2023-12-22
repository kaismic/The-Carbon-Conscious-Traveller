package com.mquniversity.tcct

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class TransportSelection(mainActivityContext: Context): RadioGroup(mainActivityContext) {
    var currMode: TransportMode = TransportMode.CAR

    private val btns: Array<RadioButton> = Array(TransportMode.entries.size) {
        RadioButton(ContextThemeWrapper(
            context,
            com.google.android.material.R.style.Widget_Material3_Button_TextButton_Icon
        ))
    }
    private val icons: IntArray = intArrayOf(
        R.drawable.outline_directions_car_24,
        R.drawable.outline_sports_motorsports_24,
        R.drawable.outline_directions_subway_24,
        R.drawable.outline_flight_24
    )
    init {
        this.orientation = HORIZONTAL
        this.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.setPadding(16, 0, 16, 0)
        val shapeDivider = GradientDrawable()
        shapeDivider.setSize(16, 0)
        this.dividerDrawable = shapeDivider
        this.showDividers = SHOW_DIVIDER_MIDDLE

        for (i in TransportMode.entries.indices) {
            this.addView(btns[i])
            val btnLayoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            btnLayoutParams.setMargins(16, 16, 16, 16)
            btns[i].layoutParams = btnLayoutParams
            btns[i].buttonDrawable = ResourcesCompat.getDrawable(
                resources,
                icons[i],
                context.theme
            )
            btns[i].setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currMode = TransportMode.entries[i]
                }
            }
            btns[i].setTextColor(ContextCompat.getColorStateList(context, R.color.transport_foreground_color))
            btns[i].backgroundTintList = ContextCompat.getColorStateList(context, R.color.transport_background_color)?.withAlpha(97)
            btns[i].setBackgroundResource(R.drawable.transport_background_shape)
        }
        btns[currMode.ordinal].isChecked = true

        this.setOnCheckedChangeListener { _, _ ->
            (mainActivityContext as MainActivity).calculate()
        }
    }
}