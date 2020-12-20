package com.nibble.hashcaller.view.ui.sms.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TimePicker
import com.nibble.hashcaller.R
import com.nibble.hashcaller.repository.spam.AlarmReceiver
import com.nibble.hashcaller.view.ui.SplashActivity
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import kotlinx.android.synthetic.main.activity_schedule.*
import java.util.*

/**
 *
 * class that manages deleting of sms
 *
 */
class ScheduleActivity : AppCompatActivity(), 
    TimePickerDialog.OnTimeSetListener, View.OnClickListener {
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_schedule)
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        Log.d(TAG, "onCreate: ")
        val timePickerDialog = TimePickerDialog(this, this, hour, minute,
            android.text.format.DateFormat.is24HourFormat(this))

//        timePickerDialog.show()
        initListeners()
        checkTimeFormat()


    }

    private fun initListeners() {
        btnSaveSmsDelete.setOnClickListener(this)
    }

    private fun checkTimeFormat() {
        if(android.text.format.DateFormat.is24HourFormat(this)){
            timePickerSpinner.setIs24HourView(true)

        }else{
            timePickerSpinner.setIs24HourView(false)
        }
    }

    override fun onPostResume() {
        Log.d(TAG, "onPostResume: ")
        checkTimeFormat()
        super.onPostResume()
    }


    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        Log.d(TAG, "onTimeSet: hour: ${hourOfDay}")
        Log.d(TAG, "onTimeSet: minute: ${minute}")
    }


    companion object {
        const val TAG = "__ScheduleActivity"
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fabBtnDeleteSMS->{
                val hour = timePickerSpinner.hour
                val minute = timePickerSpinner.minute
                Log.d(TAG, "onClick: $hour")
                Log.d(TAG, "onClick: $minute")
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                startAlarm(calendar)
            }
        }

    }

    private fun startAlarm(calendar: Calendar) {
       val alarmManager:AlarmManager =  getSystemService(Context.ALARM_SERVICE) as AlarmManager
       val intent = Intent(this, AlarmReceiver::class.java)
       val pendinIntent = PendingIntent.getBroadcast(this,4, intent, 0)

       alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,pendinIntent)

        setInSharedPreferences()


    }

    private fun setInSharedPreferences() {
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("smsSpamAutoDelete", "122" )

        editor.commit()
    }

    private fun cancelAlarm(){
        val alarmManager:AlarmManager =  getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendinIntent = PendingIntent.getBroadcast(this,4, intent, 0)

        alarmManager.cancel(pendinIntent)

    }
}