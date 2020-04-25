package com.phonar.device;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.widget.Toast;

import com.phonar.device.GeoUtils.PhonarLocationListener;

public class ServiceCommunicator extends Service implements PhonarLocationListener {
    private SMSReceiver mSMSreceiver;
    private IntentFilter mIntentFilter;

    protected static Location mLastLoc;

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();

        // SMS event receiver
        mSMSreceiver = new SMSReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSreceiver, mIntentFilter);

        // GPS Location receiver
        GeoUtils.registerListener(this, this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister the SMS receiver
        unregisterReceiver(mSMSreceiver);

        GeoUtils.removeListener(this, this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocation(Location location) {
        mLastLoc = location;

    }
}