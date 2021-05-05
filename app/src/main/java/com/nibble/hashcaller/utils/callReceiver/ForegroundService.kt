package com.nibble.hashcaller.utils.callReceiver

import android.R
import android.app.*
import android.app.Service.START_NOT_STICKY
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.nibble.hashcaller.view.ui.MainActivity


class ForegroundService : Service() {
    override fun onCreate() {
        Log.d(TAG, "onCreate: ")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val input = intent.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_menu_call)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        Log.d(TAG, "onStartCommand: ")
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        super.onDestroy()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind: ")
        return null
    }

    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager? = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val TAG = "__ForegroundService"
    }
}