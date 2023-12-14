package com.mquniversity.tcct

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

class QueryFragment: Fragment(R.layout.query_view) {
    private lateinit var mainLayout: LinearLayout
    private lateinit var headerText: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainLayout = inflater.inflate(R.layout.query_view, container, false) as LinearLayout
        val shapeDivider = GradientDrawable()
        shapeDivider.setSize(0, 32)
        mainLayout.dividerDrawable = shapeDivider
        mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

        headerText = mainLayout.findViewById(R.id.header_text)

        val args = requireArguments()
        headerText.text = args.getString("header")

        val labels = args.getStringArray("labels")
        val options = args.getIntArray("options")
        for (i in labels?.indices!!) {
            addQuery(labels[i], options?.get(i)!!)
        }
        return mainLayout
    }

    private fun addQuery(label: String, optionArrayResId: Int) {
        val menu = TextInputLayout(ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu))
        menu.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        menu.hint = label
        val dropDown = MaterialAutoCompleteTextView(ContextThemeWrapper(context, com.google.android.material.R.style.ThemeOverlay_Material3_AutoCompleteTextView_OutlinedBox))
        dropDown.inputType = InputType.TYPE_NULL
        dropDown.setSimpleItems(optionArrayResId)
        menu.addView(dropDown)
        mainLayout.addView(menu)
    }
}