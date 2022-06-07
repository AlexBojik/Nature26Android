package ru.bizit.nature26

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import dagger.Module
import dagger.android.ContributesAndroidInjector
import javax.inject.Inject
import javax.inject.Named


@Module
abstract class AuthenticationActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeActivityInjector(): AuthenticationActivity?
}

class AuthenticationActivity : AppCompatActivity() {
    @Inject
    @field:Named("Long Live")
    lateinit var appData: AppData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val authorizationUrl =  "https://nature.mpr26.ru/api/auth"
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(
            this, Uri.parse(authorizationUrl)
        )
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val code = intent?.data?.getQueryParameter("t")
        val data = Intent()
        data.setData(Uri.parse(code))
        setResult(RESULT_OK, data);
        finish()
    }

}