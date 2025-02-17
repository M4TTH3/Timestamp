package org.timestamp.mobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.timestamp.shared.dto.LocationDTO
import org.timestamp.shared.dto.NotificationDTO
import org.timestamp.shared.dto.TravelMode
import org.timestamp.shared.repository.LocationRepository
import org.timestamp.shared.repository.NotificationRepository
import org.timestamp.mobile.utility.ActivityRecognitionProvider
import org.timestamp.shared.util.KtorClient
import org.timestamp.mobile.utility.LocationProvider
import org.timestamp.mobile.utility.PermissionProvider
import org.timestamp.mobile.utility.getIdTokenResult
import java.time.format.DateTimeFormatter

const val NOTIFICATION_ID = 1
const val EVENT_NOTIFICATION_ID = 2
const val CHANNEL_ID = "location"
const val CHANNEL_NAME = "Timestamp Service"
const val ACTION_LOCATION_UPDATE = "org.timestamp.mobile.LOCATION_UPDATE"
const val ACTION_DETECTED_ACTIVITY = "org.timestamp.mobile.DETECTED_ACTIVITY"
const val INTENT_EXTRA_LOCATION = "location"
const val FIVE_MINUTES = 300000L
const val THIRTY_SECONDS = 30000L

/**
 * A foreground service used to send the backend updates on the users'
 * current location
 */
class TimestampService: Service() {

    private lateinit var pmp: PermissionProvider
    private lateinit var lp: LocationProvider
    private lateinit var arp: ActivityRecognitionProvider
    private lateinit var largeIcon: Bitmap
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private var pollingNotification = false
    private lateinit var notificationRepo: NotificationRepository
    private lateinit var locationRepo: LocationRepository

    override fun onCreate() {
        super.onCreate()

        // Initialize clients
        KtorClient.init(
            getString(R.string.backend_url),
            { getIdTokenResult()?.token }
        )

        // Setup utility classes
        lp = LocationProvider(this)
        pmp = PermissionProvider(this)
        arp = ActivityRecognitionProvider(this)
        notificationRepo = NotificationRepository()
        locationRepo = LocationRepository()

        // Load the large icon for the notification
        val decodedIcon = BitmapFactory.decodeResource(this.resources, R.drawable.cs346logoteef)
        largeIcon = Bitmap.createScaledBitmap(decodedIcon, 128, 128, false)

        // Create the notification channel
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingNotification = false
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
            startForeground(NOTIFICATION_ID, createBaseNotification())
            getTimestampNotification(FIVE_MINUTES)
            arp.startActivityRecognition { travelMode ->
                lp.travelMode = travelMode
            }
            lp.startLocationUpdates(THIRTY_SECONDS) {
                updateLocation(it)
            }
        } catch (e: Throwable) {
            val message = if (e is SecurityException) "Permissions not granted"
            else "Error starting location updates"
            Log.e("LocationService", message)

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
        serviceChannel.setSound(null, null)
        serviceChannel.setShowBadge(true)
        notificationManager.createNotificationChannel(serviceChannel)
    }

    private fun replaceNotification(notification: NotificationDTO) {
        notificationManager.notify(EVENT_NOTIFICATION_ID, createEventNotification(notification))
    }

    /**
     * Create a notification to show the user that the service is running
     * and tracking their location.
     */
    private fun createBaseNotification(
        title: String = "Timestamp",
        text: String = "Timestamp is tracking your location...",

    ): Notification {
        // Open timestamp when the notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, TimestampActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.cs346logoteefsmaller)
            .setLargeIcon(largeIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    /**
     * Create a custom notification to show the next upcoming event
     * and the estimated time to get there.
     */
    private fun createEventNotification(
        notification: NotificationDTO
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, TimestampActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dateTimeFormatter = DateTimeFormatter.ofPattern("H:mm a")
        val layout = RemoteViews(this.packageName, R.layout.notification)
        val layoutCollapsed = RemoteViews(this.packageName, R.layout.notification_collapsed)
        val eventName = notification.event.name
        val eventTime = notification.event.arrival.format(dateTimeFormatter)
        layoutCollapsed.setTextViewText(R.id.notification_title, eventName)
        layoutCollapsed.setTextViewText(R.id.notification_event_time, eventTime)
        layout.setTextViewText(R.id.notification_title, eventName)
        layout.setTextViewText(R.id.notification_event_time, eventTime)

        for (routeInfo in notification.routeInfos) {
            val timeEst = routeInfo.timeEst
            val time = if (timeEst == null) "Not Calculated" else formatDuration(timeEst)
            val viewId = when (routeInfo.travelMode) {
                TravelMode.Car -> R.id.notification_car_time
                TravelMode.Bike -> R.id.notification_bike_time
                else -> R.id.notification_walk_time
            }

            layout.setTextViewText(viewId, "${routeInfo.travelMode!!.name}: $time")
        }

        val notificationBuilder = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setCustomContentView(layoutCollapsed)
            .setCustomBigContentView(layout)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.cs346logoteefsmaller)
            .setLargeIcon(largeIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        return notificationBuilder.build()
    }

    /**
     * Update the backend with the current location.
     */
    private fun updateLocation(
        location: LocationDTO
    ) = CoroutineScope(Dispatchers.IO).launch {
        locationRepo.updateLocation(location)
    }

    private fun getTimestampNotification(
        interval: Long = 30000L
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (pollingNotification) return@launch

        pollingNotification = true

        while(pollingNotification) {
            notificationRepo.getNotifications()?.let {
                replaceNotification(it)
            }

            delay(interval)
        }
    }

    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}"
            else -> "$seconds second${if (seconds > 1) "s" else ""}"
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}