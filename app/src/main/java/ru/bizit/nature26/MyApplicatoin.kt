package ru.bizit.nature26

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
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