package org.timestamp.mobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.ktor.client.HttpClient
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.mobile.utility.ActivityRecognitionProvider
import org.timestamp.mobile.utility.KtorClient
import org.timestamp.mobile.utility.KtorClient.handler
import org.timestamp.mobile.utility.KtorClient.success
import org.timestamp.mobile.utility.LocationProvider
import org.timestamp.mobile.utility.PermissionProvider


const val CHANNEL_ID = "location"
const val CHANNEL_NAME = "Timestamp Service"
const val ACTION_LOCATION_UPDATE = "org.timestamp.mobile.LOCATION_UPDATE"
const val ACTION_DETECTED_ACTIVITY = "org.timestamp.mobile.DETECTED_ACTIVITY"
const val INTENT_EXTRA_LOCATION = "location"

/**
 * A foreground service used to send the backend updates on the users'
 * current location
 */
class TimestampService: Service() {

    private lateinit var pmp: PermissionProvider
    private lateinit var lp: LocationProvider
    private lateinit var arp: ActivityRecognitionProvider
    private lateinit var ktorClient: HttpClient

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        lp = LocationProvider(this)
        pmp = PermissionProvider(this)
        arp = ActivityRecognitionProvider(this)
        ktorClient = KtorClient.backend
    }

    override fun onDestroy() {
        super.onDestroy()
        arp.cleanup()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!pmp.fineLocationPermission || !pmp.activityRecognitionPermission) {
            Log.d("LocationService", "Permissions not granted")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            startForeground(1, createNotification())
            arp.startActivityRecognition { travelMode ->
                lp.travelMode = travelMode
            }
            lp.startLocationUpdates(30000) {
                broadcastLocation(it)
                updateLocation(it)
            }
        } catch (e: SecurityException) {
            Log.d("LocationService", "Permissions not granted")
            arp.stopActivityRecognition()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        getSystemService(NotificationManager::class.java).createNotificationChannel(serviceChannel)
    }

    /**
     * Create a notification to show the user that the service is running
     * and tracking their location.
     */
    private fun createNotification(
        text: String = "Timestamp is tracking your location..."
    ): Notification {
        val notificationBuilder = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("Timestamp")
            .setContentText(text)
            .setSmallIcon(R.drawable.cs346logoteefsmaller)

        return notificationBuilder.build()
    }

    /**
     * Broadcast the current location to Broadcast Receivers listening
     * for location updates. For instance, the AppViewModel listens for
     * location updates to use for UI
     */
    private fun broadcastLocation(locationDTO: LocationDTO) {
        val intent = Intent(ACTION_LOCATION_UPDATE)
        val content = Json.encodeToString(locationDTO)
        intent.putExtra(INTENT_EXTRA_LOCATION, content)
        sendBroadcast(intent)
    }

    /**
     * Update the backend with the current location.
     */
    private fun updateLocation(location: LocationDTO) = CoroutineScope(Dispatchers.IO).launch {
        val base = getString(R.string.backend_url)
        val tag = "Update Location"
        handler(tag) {
            val endpoint = "$base/users/me/location"
            val res = ktorClient.patch(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(location)
            }

            if (!res.success(tag)) return@handler
            Log.d(tag, "Updated location with $location")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}