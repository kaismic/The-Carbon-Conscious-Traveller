package com.mquniversity.tcct

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.R
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
            initQuery(sizeInput, "Size", args.getStringArray("motorcycleSizes")!!)
        }

        return mainLayout
    }
}