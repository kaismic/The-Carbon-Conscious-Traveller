package com.mquniversity.tcct

import android.content.Context
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton

class TransportSelection(mainActivityContext: Context): RadioGroup(mainActivityContext) {
    private val mainActivity: MainActivity = mainActivityContext as MainActivity
    var currMode: TransportMode = TransportMode.CAR

    private val radioBtns: Array<MaterialRadioButton>
    private val airplaneBtn: MaterialButton

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

        radioBtns = Array(TransportMode.entries.size - 1) {
            mainActivity.layoutInflater.inflate(
                R.layout.transport_selection_radio_button,
                this,
                false
            ) as MaterialRadioButton
        }
        for (i in radioBtns.indices) {
            this.addView(radioBtns[i])
            radioBtns[i].buttonDrawable = ResourcesCompat.getDrawable(
                resources,
                icons[i],
                context.theme
            )
            radioBtns[i].setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currMode = TransportMode.entries[i]
                }
            }
        }

        // special case for airplane
        airplaneBtn = mainActivity.layoutInflater.inflate(
            R.layout.airplane_icon_button,
            this,
            false
        ) as MaterialButton
        this.addView(airplaneBtn)
        airplaneBtn.setOnClickListener {
            mainActivity.supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(android.R.id.content, AirplaneQueryFragment())
                .addToBackStack(null)
                .commit()
        }
        // set initial selection
        radioBtns[currMode.ordinal].isChecked = true
        this.setOnCheckedChangeListener { _, _ ->
            mainActivity.calculate(false)
        }
    }

    fun updateIcons(emissions: FloatArray) {
        val rangeText = CalculationUtils.formatEmission(emissions.min(), true) +
                    " - " +
                    CalculationUtils.formatEmission(emissions.max(), true)
        radioBtns[currMode.ordinal].text = rangeText
    }
}
