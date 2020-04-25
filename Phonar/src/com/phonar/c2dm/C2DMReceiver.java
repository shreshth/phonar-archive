package com.phonar.c2dm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.phonar.DevicesList;
import com.phonar.models.Device;
import com.phonar.network.NetworkRequest;
import com.phonar.request.RequestActivity;
import com.phonar.upgrade.UpgradeService;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.PhonarPreferencesManager;

public class C2DMReceiver extends BroadcastReceiver {

    // email address associated with sender
    public static final String PROJECT_ID = "1046759708807";

    private static final String TAG_REQUEST = "1";
    private static final String TAG_RESPONSE = "2";
    private static final String TAG_DEVICE_RESPONSE = "3";
    private static final String TAG_UPGRADE = "4";

    // replicate in PhonarServer:consts.php
    public static int ERROR_NO_ERROR = 0;
    public static int ERROR_USER_DENY_LOCATION = 1;
    public static int ERROR_SENDING_REQUEST = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
            handleRegistration(context, intent);
        } else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            handleMessage(context, intent);
        }
    }

    private void handleRegistration(Context context, Intent intent) {
        String registration = intent.getStringExtra("registration_id");
        if (intent.getStringExtra("error") == null && registration != null) {
            PhonarPreferencesManager.setC2DMId(context, registration);
            if (PhonarPreferencesManager.getPhoneNumber(context) != null) {
                C2DMReceiver.sendNetworkRequestRegisterPhone(context);
            }
        } else {
            // registration failed
        }
    }

    public static void sendNetworkRequestRegisterPhone(Context context) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params
            .add(new BasicNameValuePair("phone", PhonarPreferencesManager.getPhoneNumber(context)));
        params.add(new BasicNameValuePair("c2dm_id", PhonarPreferencesManager.getC2DMId(context)));
        params.add(new BasicNameValuePair("device_id", CommonUtils.getPhoneID(context)));
        NetworkRequest request =
            new NetworkRequest(context, NetworkRequest.URL_REGISTER_PHONE, true, params, null);
        request.run();
    }

    private void handleMessage(Context context, Intent intent) {
        String msg = intent.getStringExtra("message");
        if (msg == null) {
            return;
        }
        String first_char = msg.substring(0, 1);
        // FRIENDS MODE
        if (PhonarPreferencesManager.getPhoneNumber(context) != null) {
            if (first_char.equals(TAG_REQUEST)) {
                handleFriendRequest(context, msg.substring(1));
            } else if (first_char.equals(TAG_RESPONSE)) {
                handleFriendResponse(context, msg.substring(1));
            } else if (first_char.equals(TAG_DEVICE_RESPONSE)) {
                handleDeviceResponse(context, msg.substring(1));
            } else if (first_char.equals(TAG_UPGRADE)) {
                handleUpgradeNotification(context, msg.substring(1));
            }
        }
    }

    // Called when the server notifies the app that an upgrade is available
    private void handleUpgradeNotification(Context context, String response) {
        int latest_version = Integer.parseInt(response);
        Intent serviceintent = new Intent(context, UpgradeService.class);
        serviceintent.putExtra(UpgradeService.KEY_VERSION_NUMBER, latest_version);
        context.startService(serviceintent);
    }

    /**
     * Handle response from a device
     * 
     * @param context
     * @param message
     */
    private void handleDeviceResponse(Context context, String msg) {
        String[] latlngbat = msg.split(",");
        double lat = Double.parseDouble(latlngbat[0]);
        double lng = Double.parseDouble(latlngbat[1]);
        String phoneNumber = latlngbat[2];
        long time = Long.parseLong(latlngbat[3]) * 1000;
        String bat = latlngbat[4];
        Integer battery = bat.equals("null") ? null : Integer.parseInt(bat.replace("%", ""));
        DevicesList.onDeviceLocation(context, phoneNumber, lat, lng, time, battery);
    }

    /**
     * Handle response from a phone giving location of another phone
     * 
     * @param context
     * @param message
     */
    private void handleFriendResponse(Context context, String message) {
        String[] args = message.split(",");
        Double latitude = Double.parseDouble(args[0]);
        Double longitude = Double.parseDouble(args[1]);
        String phoneNumber = args[2];
        int error = Integer.parseInt(args[3]);
        int count = Integer.parseInt(args[4]);

        // only do anything if still tracking this device
        Device device = DevicesList.get(context, phoneNumber);
        if (device == null) {
            return;
        }

        if (error == ERROR_NO_ERROR) { // no error
            DevicesList.onDeviceLocation(context, phoneNumber, latitude, longitude, null, null);
            if (count == RequestLocationActivity.FIRST_UPDATE) {
                Intent serviceintent = new Intent(context, ReceiveLocationService.class);
                serviceintent.putExtra("targetphone", args[2]);
                serviceintent.putExtra("error", error);
                context.startService(serviceintent);
            }

        } else {
            device.error = true;
            RequestActivity.refreshList();
            Intent serviceintent = new Intent(context, ReceiveLocationService.class);
            serviceintent.putExtra("targetphone", args[2]);
            serviceintent.putExtra("error", error);
            context.startService(serviceintent);
        }
    }

    /**
     * Handle request from other phone asking for this phone's location
     * 
     * @param context
     * @param message
     */
    private void handleFriendRequest(Context context, String message) {
        Intent serviceintent = new Intent(context, RequestLocationService.class);
        serviceintent.putExtra("userphone", message);
        context.startService(serviceintent);
    }
}
