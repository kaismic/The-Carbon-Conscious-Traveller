package com.mquniversity.tcct

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton

class TransportSelection(mainActivityContext: Context): RadioGroup(mainActivityContext) {
    private val mainActivity: MainActivity = mainActivityContext as MainActivity
    var currMode: TransportMode = TransportMode.CAR

    private val radioBtns: Array<RadioButton> = Array(TransportMode.entries.size - 1) {
        RadioButton(ContextThemeWrapper(
            context,
            com.google.android.material.R.style.Widget_Material3_Button_IconButton_Outlined
        ))
    }
    private val airplaneBtn = MaterialButton(
        ContextThemeWrapper(
            context,
            com.google.android.material.R.style.Widget_Material3_Button_IconButton_Outlined
        )
    )

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

        for (i in radioBtns.indices) {
            this.addView(radioBtns[i])
            radioBtns[i].layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            radioBtns[i].buttonDrawable = ResourcesCompat.getDrawable(
                resources,
                icons[i],
                context.theme
            )
            radioBtns[i].setTextColor(ContextCompat.getColorStateList(context, R.color.transport_foreground_color))
            radioBtns[i].backgroundTintList = ContextCompat.getColorStateList(context, R.color.transport_background_color)?.withAlpha(97)
            radioBtns[i].setBackgroundResource(R.drawable.transport_background_shape)
            radioBtns[i].setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currMode = TransportMode.entries[i]
                }
            }
        }

        // special case for airplane
        this.addView(airplaneBtn)
        airplaneBtn.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 16, 16, 16)
        }
        airplaneBtn.icon = ResourcesCompat.getDrawable(
            resources,
            icons[TransportMode.AIRPLANE.ordinal],
            context.theme
        )
        airplaneBtn.setOnClickListener {
            mainActivity.supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(android.R.id.content, AirplaneQueryFragment())
                .addToBackStack(null)
                .commit()
        }

        radioBtns[currMode.ordinal].isChecked = true
        this.setOnCheckedChangeListener { _, _ ->
            mainActivity.calculate(false)
        }
    }

    fun updateIcons(emissions: FloatArray) {
        val rangeText = String.format("%.2fkg-%.2fkg", emissions.min() / 1000, emissions.max() / 1000)
        radioBtns[currMode.ordinal].text = rangeText
    }
}
