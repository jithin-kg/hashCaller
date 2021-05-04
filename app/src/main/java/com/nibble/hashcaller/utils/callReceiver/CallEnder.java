package com.nibble.hashcaller.utils.callReceiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Created by Jithin KG on 21,July,2020
 */
public class CallEnder implements ITelephony {
    private Context context;
    private String TAG = "__CallEnder";

    public CallEnder(Context context){
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public boolean endIncomingCall(){

            boolean callEnded =false;
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                telecomManager.endCall();
                callEnded = true;
            } else {
                endIncommingCall(context);
                callEnded = true;

            }

        } catch ( Exception e) {
            Log.d(TAG, "endIncomingCall:exception  " + e.toString());
            e.printStackTrace();
        }
        return callEnded;
    }

    @SuppressLint("MissingPermission")
    public boolean silenceIncomingCall(){

        boolean callMuted =false;
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                telecomManager.silenceRinger();
                //https://stackoverflow.com/questions/41791798/how-to-mute-a-phone-ringing-on-incoming-call


                //https://developer.android.com/reference/android/telecom/CallScreeningService#respondToCall(android.telecom.Call.Details,%20android.telecom.CallScreeningService.CallResponse)
                //Calls to this method are ignored unless the Call.Details#getCallDirection() is Call.Details#DIRECTION_INCOMING.
                //
                //For incoming calls, a CallScreeningService MUST call this method within 5
                // seconds of onScreenCall(android.telecom.Call.Details) being invoked by the platform.




            } else {
                silenceIncomingCall(context);
                callMuted = true;
            }

        } catch ( Exception e) {
            Log.d(TAG, "silenceIncomingCall: "+e.toString());
            e.printStackTrace();
        }
        return callMuted;
    }
    private void  endIncommingCall(Context context) {
        try {


            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            com.android.internal.telephony.ITelephony telephonyService = (ITelephony) m.invoke(tm);
            telephonyService = (ITelephony) m.invoke(tm);
            telephonyService.silenceRinger();
            telephonyService.endCall();

        } catch (Exception e) {
            Log.d(TAG, "endIncommingCall: "+ e.toString());
            e.printStackTrace();
        }
    }

    private void  silenceIncomingCall(Context context) {
        try {


            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            com.android.internal.telephony.ITelephony telephonyService = (ITelephony) m.invoke(tm);
            telephonyService = (ITelephony) m.invoke(tm);

            if (telephonyService != null) {
                Method m1 = telephonyService.getClass().getDeclaredMethod("silenceRinger");
                m1.invoke(telephonyService);

//                telephonyService.silenceRinger();
            }
//            //https://stackoverflow.com/questions/41791798/how-to-mute-a-phone-ringing-on-incoming-call
//            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean endCall() {
        return false;
    }

    @Override
    public void answerRingingCall() {

    }

    @Override
    public void silenceRinger() {

    }


}
