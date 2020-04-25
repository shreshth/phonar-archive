package com.phonar.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.phonar.c2dm.C2DMReceiver;

public class OnUpgradeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        if (context == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            return;
        }

        if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            C2DMReceiver.sendNetworkRequestRegisterPhone(context);
        }
    }

}
