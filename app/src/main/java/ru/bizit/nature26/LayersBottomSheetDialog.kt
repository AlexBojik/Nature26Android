package ru.bizit.nature26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListView
import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.bizit.nature26.adapter.ExpandableListAdapter
import javax.inject.Inject
import javax.inject.Named


@Module
abstract class LayersModule {
    @ContributesAndroidInjector
    abstract fun contributeActivityInjector(): LayersBottomSheetDialog?
}

class LayersBottomSheetDialog : DaggerBottomSheetDialogFragment() {
    @Inject
    @field:Named("Long Live")
    lateinit var appData: AppData

    lateinit var adapter: ExpandableListAdapter

    companion object {
        const val TAG = "LayersBottomSheetDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sheet = inflater.inflate(R.layout.layers_bottom_sheet_dialog, container, false)

        adapter = ExpandableListAdapter(appData.layers)
        val expandableListView = sheet.findViewById<ExpandableListView>(R.id.layerList)
        expandableListView.setAdapter(adapter)

        val ok = sheet.findViewById<Button>(R.id.buttonOk)
        ok.setOnClickListener {
            appData.layersChanged.onNext(true)
            dialog?.dismiss()
        }
        return sheet
    }
}