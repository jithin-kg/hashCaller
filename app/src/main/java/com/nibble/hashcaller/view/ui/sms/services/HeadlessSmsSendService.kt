package com.nibble.hashcaller.view.ui.sms.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 *  a fully-functional messaging app should also offer the ‘Quick Reply’
 *  option so that users can reject calls with a message
 *  or respond to incoming messages without opening the main app.
 *  This will require a service to handle that but fortunately,
 *  we can just create an empty service again to trick Android.
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}