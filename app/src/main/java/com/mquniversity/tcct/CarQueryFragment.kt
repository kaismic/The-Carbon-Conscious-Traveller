package com.mquniversity.tcct

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class CarQueryFragment: QueryFragment() {
    private lateinit var sizeInput: TextInputLayout
    private lateinit var sizeInputDropdown: MaterialAutoCompleteTextView
    private lateinit var sizeOptions: Array<String>
    private var currSizeIdx = -1

    private lateinit var fuelTypeInput: TextInputLayout
    private lateinit var fuelTypeInputDropdown: MaterialAutoCompleteTextView
    private lateinit var fuelTypeOptions: Array<String>
    private var currFuelTypeIdx = -1

    private lateinit var fuelTypes: Array<String>
    private lateinit var carValues: Array<FloatArray>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true

            headerText.text = "Car"

            val args = requireArguments()
            fuelTypes = args.getStringArray("carFuelTypes")!!
            carValues = args.getSerializable("carValues", Array<FloatArray>::class.java) as Array<FloatArray>

            sizeInput = TextInputLayout(ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu))
            sizeOptions = args.getStringArray("carSizes")!!
            insertQuery(sizeInput, "Size", sizeOptions)
            sizeInputDropdown = sizeInput.editText as MaterialAutoCompleteTextView

            fuelTypeInput = TextInputLayout(ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu))
            insertQuery(fuelTypeInput, "Fuel Type", emptyArray())
            fuelTypeInputDropdown = fuelTypeInput.editText as MaterialAutoCompleteTextView

            fuelTypeInput.isEnabled = false

            sizeInputDropdown.setOnItemClickListener { _, _, idx, _ ->
                fuelTypeInput.isEnabled = false
                if (currSizeIdx != idx) {
                    setFuelTypeItems(idx)
                }
                fuelTypeInput.isEnabled = true
            }
            fuelTypeInputDropdown.setOnItemClickListener { _, _, idx, _ ->
                currFuelTypeIdx = idx
                calBtn.isEnabled = true
            }

            calBtn.setOnClickListener {
                calBtn.isEnabled = false
                val carResultFragment = CarResultFragment(sizeOptions[currSizeIdx], fuelTypeOptions[currFuelTypeIdx])
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, carResultFragment, getString(R.string.fragment_tag_car_result))
                    .commit()
            }
        }

        return mainLayout
    }

    private fun setFuelTypeItems(selectedSizeIdx: Int) {
        currSizeIdx = selectedSizeIdx
        calBtn.isEnabled = false
        val options = mutableListOf<String>()
        fuelTypeInputDropdown.text = null
        for (i in carValues[selectedSizeIdx].indices) {
            if (carValues[selectedSizeIdx][i] != 0f) {
                options.add(fuelTypes[i])
            }
        }
        fuelTypeOptions = options.toTypedArray()
        fuelTypeInputDropdown.setSimpleItems(fuelTypeOptions)
    }
}