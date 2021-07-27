package ru.bizit.nature26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.bizit.nature26.adapter.FeatureListAdapter
import javax.inject.Inject
import javax.inject.Named


@Module
abstract class FeaturesModule {
    @ContributesAndroidInjector
    abstract fun contributeActivityInjector(): FeaturesBottomSheetDialog?
}

class FeaturesBottomSheetDialog : DaggerBottomSheetDialogFragment() {
    @Inject
    @Named("Long Live")
    lateinit var appData: AppData

    private lateinit var adapter: FeatureListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    companion object {
        const val TAG = "FeaturesBottomSheetDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sheet = inflater.inflate(R.layout.features_bottom_sheet_dialog, container, false)

        val featureList = mutableListOf<Feature>()
        val layers = mutableListOf<Int>()
        appData.loadedFeatures.forEach {go ->
            val layer = appData.linearLayers[go.layerId]
            if (layer != null) {
                if (!layers.contains(layer.id)) {
                    layers.add(layer.id)
                    if (layer.commonName.isNotBlank() && layer.commonDescription != null) {
                        val lf = Feature(go.id, layer.commonName, layer.commonDescription!!, layer.color, layer.symbol, go)
                        featureList.add(0, lf)
                    }
                }
                val f = Feature(go.id, go.name, go.description, layer.color, layer.symbol, go)
                featureList.add(f)
            }
        }

        adapter = FeatureListAdapter(featureList, appData)
        layoutManager = LinearLayoutManager(context)

        val featureListView = sheet.findViewById<RecyclerView>(R.id.featureList)
        featureListView.adapter = adapter
        featureListView.layoutManager = layoutManager

        return sheet
    }
}