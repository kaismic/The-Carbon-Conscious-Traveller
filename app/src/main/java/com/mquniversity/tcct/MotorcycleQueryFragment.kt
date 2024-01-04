package com.mquniversity.tcct

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class MotorcycleQueryFragment: PrivateVehicleQueryFragment() {
    private lateinit var sizeInput: TextInputLayout
    private lateinit var sizeInputDropdown: MaterialAutoCompleteTextView
    private lateinit var sizeOptions: Array<String>
    private var currSizeIdx = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true

            headerText.text = getString(R.string.motorcycle)

            sizeInput = TextInputLayout(
                ContextThemeWrapper(
                    context,
                    com.google.android.material
                        .R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu
                )
            )
            sizeOptions = mainActivity.calculationValues.motorcycleSizes.toTypedArray()
            insertQuery(sizeInput, "Size", sizeOptions)
            sizeInputDropdown = (sizeInput.editText as MaterialAutoCompleteTextView)
            sizeInputDropdown.setOnItemClickListener { _, _, idx, _ ->
                currSizeIdx = idx
                calBtn.isEnabled = true
            }

            calBtn.setOnClickListener {
                mainActivity.bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                calBtn.isEnabled = false
                var motorcycleResultFrag = parentFragmentManager.findFragmentByTag(getString(R.string.tag_motorcycle_result)) as MotorcycleResultFragment?
                if (motorcycleResultFrag == null) {
                    motorcycleResultFrag = MotorcycleResultFragment(sizeOptions[currSizeIdx])
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, motorcycleResultFrag, getString(R.string.tag_motorcycle_result))
                        .addToBackStack(getString(R.string.tag_motorcycle_result))
                        .commit()
                } else {
                    motorcycleResultFrag.updateFactor(sizeOptions[currSizeIdx])
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, motorcycleResultFrag, getString(R.string.tag_motorcycle_result))
                        .commit()
                }
                calBtn.isEnabled = true
            }
        }
        return mainLayout
    }
}