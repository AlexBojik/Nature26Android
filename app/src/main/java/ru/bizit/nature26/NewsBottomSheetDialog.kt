package ru.bizit.nature26

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.bizit.nature26.adapter.NewsListAdapter
import javax.inject.Inject
import javax.inject.Named


@Module
abstract class NewsModule {
    @ContributesAndroidInjector
    abstract fun contributeActivityInjector(): NewsBottomSheetDialog?
}

class NewsBottomSheetDialog : DaggerBottomSheetDialogFragment() {
    @Inject
    @field:Named("Long Live")
    lateinit var appData: AppData

    lateinit var adapter: NewsListAdapter
    lateinit var layoutManager: LinearLayoutManager

    companion object {
        const val TAG = "NewsBottomSheetDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sheet = inflater.inflate(R.layout.news_bottom_sheet_dialog, container, false)

        adapter = NewsListAdapter(appData.loadedNews, appData)
        layoutManager = LinearLayoutManager(context)

        val newsListView = sheet.findViewById<RecyclerView>(R.id.newsList)
        newsListView.adapter = adapter
        newsListView.layoutManager = layoutManager
        return sheet
    }
}