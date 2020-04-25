package com.phonar.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.phonar.PhoneNumberActivity;
import com.phonar.utils.PhonarPreferencesManager;

public class SmsReceiver extends BroadcastReceiver {

    private static final String PRE_SMS_TAG = "Phonar Code: ";

    @Override
    public void onReceive(Context context, Intent intent) {
        // dont do anything if number is already verified
        if (PhonarPreferencesManager.getPhoneNumber(context) != null
            && PhonarPreferencesManager.isGoogleVoice(context) != null) {
            return;
        }
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                if (messages.length > 0) {
                    String msg = messages[0].getMessageBody();
                    if (msg.length() > PRE_SMS_TAG.length()
                        && msg.substring(0, PRE_SMS_TAG.length()).equals(PRE_SMS_TAG)) {
                        abortBroadcast();
                        String code = msg.substring(PRE_SMS_TAG.length());
                        if (PhoneNumberActivity.mPhoneNumberActivity != null) {
                            PhoneNumberActivity.mPhoneNumberActivity.checkCode(code, true);
                        }
                    }
                }
            }
        }
    }

}
