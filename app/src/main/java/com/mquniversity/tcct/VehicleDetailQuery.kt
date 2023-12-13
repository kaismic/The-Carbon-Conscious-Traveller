package com.mquniversity.tcct

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.ContextThemeWrapper
import android.widget.LinearLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

class VehicleDetailQuery(mainActivityContext: Context, title: String): LinearLayout(mainActivityContext) {
    private val headerText: MaterialTextView = MaterialTextView(context)
    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        this.setPadding(64, 0, 64, 0)
        val shapeDivider = GradientDrawable()
        shapeDivider.setSize(0, 32)
        this.dividerDrawable = shapeDivider
        this.showDividers = SHOW_DIVIDER_MIDDLE

        headerText.text = title
        headerText.textSize = 32f
        addView(headerText)
    }

    fun addQuery(label: String, optionArrayResId: Int) {
        val menu = TextInputLayout(ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu))
        menu.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        menu.hint = label
        val dropDown = MaterialAutoCompleteTextView(ContextThemeWrapper(context, com.google.android.material.R.style.ThemeOverlay_Material3_AutoCompleteTextView_OutlinedBox))
        dropDown.inputType = InputType.TYPE_NULL
        dropDown.setSimpleItems(optionArrayResId)
        menu.addView(dropDown)
        this.addView(menu)
    }
}