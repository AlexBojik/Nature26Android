package ru.bizit.nature26

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import javax.inject.Named


@Module
abstract class BaseLayersModule {
    @ContributesAndroidInjector
    abstract fun contributeActivityInjector(): BaseLayersBottomSheetDialog?
}

class BaseLayersBottomSheetDialog : DaggerBottomSheetDialogFragment() {
    @Inject
    @field:Named("Long Live")
    lateinit var appData: AppData

    companion object {
        const val TAG = "BaseLayersBottomSheetDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sheet = inflater.inflate(R.layout.base_layers_bottom_sheet_dialog, container, false)
        val current = appData.baseLayers.find { bl -> bl.name == appData.oldBase}
        val group = sheet.findViewById<RadioGroup>(R.id.baseLayersGroup)

        appData.baseLayers.forEach { bl ->
            val rb = RadioButton(sheet.context)
            rb.id = bl.id
            rb.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            rb.text = bl.description
            rb.buttonTintList= ColorStateList.valueOf(Color.GRAY)
            rb.textSize = 20.0F
            rb.setPadding(16, 16, 16, 16)
            group.addView(rb)
        }
        current?.id?.let { group.check(it) }
        group.setOnCheckedChangeListener { _: RadioGroup, i: Int ->
            val cr = appData.baseLayers.find { bl -> bl.id == i}
            if (cr != null) {
                appData.newBase.onNext(cr.name)
            }
            dialog?.dismiss()
        }
        return sheet
    }
}

abstract class DaggerBottomSheetDialogFragment : BottomSheetDialogFragment(), HasAndroidInjector {
    @Inject lateinit var androidInjector: DispatchingAndroidInjector<Any>
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }
}
