package ru.bizit.nature26

import android.content.Intent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class MyApplicatoin : DaggerApplication() {
    override fun applicationInjector() = AndroidInjector<DaggerApplication> {
        DaggerMyApplicationComponent.builder()
            .androidModule(AndroidModule(this))
            .build()
            .inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        startService(Intent(this, MyService::class.java))
    }

}