package com.hashcaller.utils.callscreening

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.hashcaller.R
import com.hashcaller.utils.constants.IntentKeys.Companion.INTENT_COMMAND
import com.hashcaller.view.ui.call.floating.Window

//const val INTENT_COMMAND = "com.localazy.quicknote.COMMAND"
const val INTENT_COMMAND_EXIT = "EXIT"
const val INTENT_COMMAND_NOTE = "NOTE"

private const val NOTIFICATION_CHANNEL_GENERAL = "quicknote_general"
private const val CODE_FOREGROUND_SERVICE = 1
private const val CODE_EXIT_INTENT = 2
private const val CODE_NOTE_INTENT = 3
//https://localazy.com/blog/floating-windows-on-android-5-moving-window
class CallScreeningFloatingService: Service() {
    private  var _window: Window? = null
    private  val window:Window get() = _window!!

        override fun onBind(intent: Intent?): IBinder? = null
    /**
     * Remove the foreground notification and stop the service.
     */
    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        showNotification()
        val command: String? = intent.getStringExtra(INTENT_COMMAND)

//        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//        supervisorScope.launch {
//            delay(6000L)
//            Log.d(TAG, "onStartCommand: $randomvalue")
//        }
////        val callresponse:com.hashcaller.utils.callscreening.CallResponse= intent.getSerializableExtra("response") as CallResponse
//        callresponse.builder.setDisallowCall(true)
//        respondToCall(mCallDetails,  callresponse.builder.build())
//        // Exit the service if we receive the EXIT command.
//        // START_NOT_STICKY is important here, we don't want
//        // the service to be relaunched.
            //only perform operations in this service iff call screening role is not held
//            command?.let {
//                if (_window == null) {
//                    _window = Window(this)
//                }
////            _window = Window(this)
//                if (command == STOP_FLOATING_SERVICE_AND_WINDOW) {
//                    window.close()
//                    stopService()
//                    return START_NOT_STICKY
//                } else if (command == STOP_FLOATING_SERVICE) {
////                stopService()
//                    //important to call stop foreground, this only removes the notification
//                    //calling StopService will result in unable to close window automaticallly when call ended
//                    stopForeground(true)
//                    return START_NOT_STICKY
//
//                } else if (command == START_FLOATING_SERVICE) {
//                    window.open()
//                }
//            }

        return START_STICKY
    }


    /**
     * Create and show the foreground notification.
     */
    private fun showNotification() {

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//        val exitIntent = Intent(this, FloatingService::class.java).apply {
//            putExtra(INTENT_COMMAND, INTENT_COMMAND_EXIT)
//        }

//        val noteIntent = Intent(this, FloatingService::class.java).apply {
//            putExtra(INTENT_COMMAND, INTENT_COMMAND_NOTE)
//        }

//        val exitPendingIntent = PendingIntent.getService(
//            this, CODE_EXIT_INTENT, exitIntent, 0
//        )

//        val notePendingIntent = PendingIntent.getService(
//            this, CODE_NOTE_INTENT, noteIntent, 0
//        )

        // From Android O, it's necessary to create a notification channel first.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_GENERAL,
                        " getString(R.string.notification_channel_general)",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                ) {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    manager.createNotificationChannel(this)
                }
            } catch (ignored: Exception) {
                // Ignore exception.
            }
        }

        with(
            NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_GENERAL
            )
        ) {
            setTicker(null)
            setContentTitle(getString(R.string.app_name))
            setContentText("Caller ID is active")
            setAutoCancel(false)
            setOngoing(true)
            setWhen(System.currentTimeMillis())
            setSmallIcon(R.drawable.ic_baseline_call_24)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                priority = NotificationManager.IMPORTANCE_LOW
            }else{
                priority = Notification.PRIORITY_LOW
            }
//            setContentIntent(notePendingIntent)
//            addAction(
//                NotificationCompat.Action(
//                    0,
//                    "getString(R.string.notification_exit)",
//                    exitPendingIntent
//                )
//            )
            startForeground(CODE_FOREGROUND_SERVICE, build())
        }

    }



    companion object{

        const val TAG = "__FloatingService"
        var phoneNumber: String = ""


    }
}