package com.nibble.hashcaller.view.ui.sms.send

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.SmsStatusDeliveredReceiver
import com.nibble.hashcaller.utils.SmsStatusSentReceiver
import kotlinx.android.synthetic.main.activity_send_test.*

class SendTestActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_test)
        btnSendTest.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        val settings = Settings()
        settings.setUseSystemSending(true);
        settings.useSystemSending = true
        settings.deliveryReports = true //it is importatnt to set this for the sms delivered status

        val transaction = Transaction(this, settings)
        val message = Message(edtTextsmsTest.text.toString(), "919495617494")
//        message.setImage(mBitmap);

        val smsSentIntent = Intent(this, SmsStatusSentReceiver::class.java)
        val deliveredIntent = Intent(this, SmsStatusDeliveredReceiver::class.java)
        transaction.setExplicitBroadcastForSentSms(smsSentIntent)
        transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)

        transaction.sendNewMessage(message, 133)
    }
}