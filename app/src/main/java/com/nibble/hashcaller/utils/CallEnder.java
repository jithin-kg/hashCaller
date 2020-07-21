package com.nibble.hashcaller.utils;

import android.content.Context;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

/**
 * Created by Jithin KG on 21,July,2020
 */
public class CallEnder implements ITelephony {
    private Context context;

    public CallEnder(Context context){
        this.context = context;
    }
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
            e.printStackTrace();
        }
        return callEnded;
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
