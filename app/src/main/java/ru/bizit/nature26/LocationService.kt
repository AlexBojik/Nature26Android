package ru.bizit.nature26

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.bizit.nature26.common.Common
import ru.bizit.nature26.interfaces.Check
import ru.bizit.nature26.interfaces.RetrofitServices

class LocationService : Service()  {
    private var locationManager: LocationManager? = null

    override fun onBind(intent: Intent): IBinder? { return null }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createNotificationChannel(CHANNEL)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("Сервис геолокации")
            .setContentText("Работает фоновое определение геопозиции")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        createNotificationChannel(ZONE)
        mService = Common.retrofitService
        context = this
        if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                INTERVAL, DISTANCE, locationListeners[1])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }

        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                INTERVAL, DISTANCE, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (i in 0..locationListeners.size) {
                try {
                    locationManager?.removeUpdates(locationListeners[i])
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(channelId, "Сервис геопозиции",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        private lateinit var mService: RetrofitServices

        const val TAG = "LocationService"
        const val CHANNEL = "ForegroundLocationService"
        const val ZONE = "ZoneService"

        const val INTERVAL = 10000.toLong() // In milliseconds
        const val DISTANCE = 100.toFloat() // In meters

        val locationListeners = arrayOf(
            LTRLocationListener(),
            LTRLocationListener()
        )

        fun startService(context: Context) {
            val startIntent = Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        class LTRLocationListener : android.location.LocationListener {

            override fun onProviderDisabled(provider: String) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onLocationChanged(location: Location) {
                mService.postCheck(Check(location.longitude, location.latitude)).enqueue(object :
                    Callback<MutableList<String>> {
                    override fun onFailure(call: Call<MutableList<String>>, t: Throwable) {}
                    override fun onResponse(
                        call: Call<MutableList<String>>,
                        response: Response<MutableList<String>>) {

                        val zones = response.body() as MutableList<String>

                        if (zones.size > 0) {
                            val builder = NotificationCompat.Builder(context!!, ZONE)
                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .setContentTitle("Приближение к территории")
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setStyle(NotificationCompat.BigTextStyle().bigText("Вы приближаетесь к территориям - ${zones.joinToString(", ")} - с особыми условиями нахождения в них посмотрите подробную информацию в приложении"))
                                .setPriority(NotificationCompat.PRIORITY_MAX)

                            with(NotificationManagerCompat.from(context!!)) {
                                notify(101, builder.build())
                            }
                        }
                    }
                })
            }
        }
    }
}