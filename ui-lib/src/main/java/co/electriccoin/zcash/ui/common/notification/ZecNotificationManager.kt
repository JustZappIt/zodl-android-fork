package co.electriccoin.zcash.ui.common.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationChannelCompat
import co.electriccoin.zcash.ui.R

object ZecNotificationManager {
    private const val CHANNEL_ID = "zec_received"
    private const val NOTIFICATION_ID = 1001

    fun showZecArrivedNotification(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        createChannel(manager)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_home_receive)
                .setContentTitle(context.getString(R.string.notification_zec_arrived_title))
                .setContentText(context.getString(R.string.notification_zec_arrived_body))
                .setAutoCancel(true)
                .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(manager: NotificationManagerCompat) {
        val channel =
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("ZEC Received")
                .build()
        manager.createNotificationChannel(channel)
    }
}
