package com.phonar.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

// Used for track requests
public class SendLocationAlarm extends BroadcastReceiver {

    // Send an SMS with location information to the source phone
    @Override
    public void onReceive(Context context, Intent intent) {
        String sms_body = "";
        if (SMSReceiver.numPolls > 0 || !SMSReceiver.usingPolls) {

            if (SMSReceiver.usingPolls) {
                SMSReceiver.numPolls--;
            }

            sms_body = SMSReceiver.handleLocationRequest(context) + " (track response)";
        }

        else {
            SMSReceiver.stopSendLocationAlarm(context);
            sms_body = "Sent the right number of track updates, stopping now.";

        }

        SmsManager send = SmsManager.getDefault();
        send.sendTextMessage(SMSReceiver.src, null, sms_body, null, null);

    }
}
