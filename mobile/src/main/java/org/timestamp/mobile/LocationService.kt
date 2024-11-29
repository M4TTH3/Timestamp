package org.timestamp.mobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.timestamp.mobile.utility.PermissionProvider


const val CHANNEL_ID = "location"
const val CHANNEL_NAME = "Timestamp Service"

/**
 * A foreground service used to send the backend updates on the users'
 * current location
 */
class LocationService: Service() {

    private val permissionProvider = PermissionProvider(this)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!permissionProvider.fineLocationPermission) {
            Log.d("LocationService", "Permissions not granted")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            startForeground(1, createNotification())
        } catch (e: SecurityException) {
            Log.d("LocationService", "Permissions not granted")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationBuilder = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("Timestamp")
            .setContentText("Timestamp is tracking location...")
            .setSmallIcon(R.drawable.cs346logoteef)

        return notificationBuilder.build()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        getSystemService(NotificationManager::class.java).createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}