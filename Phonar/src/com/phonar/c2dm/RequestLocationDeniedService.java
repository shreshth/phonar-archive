package com.phonar.c2dm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.phonar.network.NetworkRequest;
import com.phonar.network.NetworkRequest.NetworkRequestListener;
import com.phonar.utils.PhonarPreferencesManager;
import com.phonar.utils.log;

/**
 * Service which is called to make a network request in case the user denies
 * permission to share location
 */
public class RequestLocationDeniedService extends Service {

    // List of phone numbers to send network request to
    public static List<String> phoneNumbers = new ArrayList<String>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        for (String userphone : phoneNumbers) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userphone", userphone));
            params.add(new BasicNameValuePair("targetphone", PhonarPreferencesManager
                .getPhoneNumber(this)));
            params.add(new BasicNameValuePair("lat", "-1"));
            params.add(new BasicNameValuePair("lng", "-1"));
            params.add(new BasicNameValuePair("error", Integer
                .toString(C2DMReceiver.ERROR_USER_DENY_LOCATION)));
            params.add(new BasicNameValuePair("count", Integer
                .toString(RequestLocationActivity.FIRST_UPDATE)));

            NetworkRequestListener networkListener = new NetworkRequestListener() {
                @Override
                public void onRequestComplete(String response) {
                    if (response == null) {
                        log.e("Network", "Error sending location update");
                    }
                }
            };

            NetworkRequest request =
                new NetworkRequest(
                    this, NetworkRequest.URL_ON_FRIEND_LOCATION, true, params, networkListener);
            request.run();
        }
        phoneNumbers = new ArrayList<String>();

        stopSelf();
        return START_STICKY;
    }
}
