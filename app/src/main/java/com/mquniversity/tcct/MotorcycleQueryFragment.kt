package com.mquniversity.tcct

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class MotorcycleQueryFragment: QueryFragment() {
    private lateinit var sizeInput: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true

            headerText.text = "Motorcycle"

            val args = requireArguments()

            sizeInput = TextInputLayout(
                ContextThemeWrapper(
                    context,
                    R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu
                )
            )
            insertQuery(sizeInput, "Size", args.getStringArray("motorcycleSizes")!!)
            (sizeInput.editText as MaterialAutoCompleteTextView).setOnItemClickListener { _, _, _, _ ->
                calBtn.isEnabled = true
            }
        }

        return mainLayout
    }
}