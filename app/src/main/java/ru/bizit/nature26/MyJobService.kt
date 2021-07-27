package ru.bizit.nature26

import android.app.Notification
import android.app.job.JobParameters
import android.app.job.JobService
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MyJobService : JobService() {
    override fun onStartJob(parameters: JobParameters?): Boolean {
        // Создаём уведомление
        val builder = NotificationCompat.Builder(this, "channelID")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Напоминание")
            .setContentText("Пора покормить кота")
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        with(NotificationManagerCompat.from(this)) {
            notify(101, builder.build()) // посылаем уведомление
        }

        // perform work here, i.e. network calls asynchronously

        // returning false means the work has been done, return true if the job is being run asynchronously
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}