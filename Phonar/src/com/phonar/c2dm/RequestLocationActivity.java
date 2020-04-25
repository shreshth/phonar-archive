package com.phonar.c2dm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.phonar.R;
import com.phonar.network.NetworkRequest;
import com.phonar.network.NetworkRequest.NetworkRequestListener;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.ContactsUtils;
import com.phonar.utils.GeoUtils;
import com.phonar.utils.GeoUtils.UserLocationListener;
import com.phonar.utils.PhonarPreferencesManager;
import com.phonar.utils.log;

/**
 * Activity that is called when user accepts notification
 * RequestLocationService.java (after C2DMReceiver.java gets the message). Just
 * displays a dialog confirming that user wants to share location, and starts a
 * location listener to send location.
 */
public class RequestLocationActivity extends Activity {
    private String userphone = null;
    private String username = null;
    private final Context context = this;
    private final long STALE_TIME = 60 * 1000;

    private final long TRACK_TIME = 15 * 60 * 1000;

    private final int KEY_ONE_TIME = 44;
    private final int KEY_TRACK = 55;

    public static final int ID_TRACK_NOTIFICATION = 99;

    // replicate in PhonarServer:consts.php
    public static final int FIRST_UPDATE = 0;
    public static final int LATER_UPDATE = 1;

    private boolean firstSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstSent = false;
        userphone = getIntent().getExtras().getString("userphone");
        username = ContactsUtils.getNameByPhoneNumber(userphone, getContentResolver());
        RequestLocationDeniedService.phoneNumbers.remove(userphone);

        String[] items =
            {
                getString(R.string.yes_one_time), getString(R.string.yes_track),
                getString(R.string.no_share) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.location_request_confirm) + username).setItems(
            items, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        clickListenerOnce.onClick(dialog, item);
                    } else if (item == 1) {
                        clickListenerTrack.onClick(dialog, item);
                    } else if (item == 2) {
                        // if user denies permission, start service
                        Intent rejectintent =
                            new Intent(
                                RequestLocationActivity.this, RequestLocationDeniedService.class);
                        RequestLocationDeniedService.phoneNumbers.add(userphone);
                        startService(rejectintent);
                        RequestLocationActivity.this.setResult(RESULT_OK);
                        RequestLocationActivity.this.finish();
                    }
                }
            }).setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                Intent rejectintent =
                    new Intent(RequestLocationActivity.this, RequestLocationDeniedService.class);
                RequestLocationDeniedService.phoneNumbers.add(userphone);
                startService(rejectintent);
                RequestLocationActivity.this.setResult(RESULT_OK);
                RequestLocationActivity.this.finish();
            }
        });
        CommonUtils.showDialog(builder);
    }

    /**
     * Location listener for one-time location update
     */
    private void startListenerOnce() {
        final LocationManager lm =
            (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    sendLocationUpdate(context, userphone, location, FIRST_UPDATE);
                    lm.removeUpdates(this);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                return;
            }

            @Override
            public void onProviderEnabled(String provider) {
                return;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                return;
            }
        };
        // register listeners
        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            lm.requestSingleUpdate(criteria, listener, getMainLooper());
        } catch (Exception e) {
            try {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                lm.requestSingleUpdate(criteria, listener, getMainLooper());
            } catch (Exception e2) {
                CommonUtils.toast(context, R.string.location_not_sent);
                Intent rejectintent =
                    new Intent(RequestLocationActivity.this, RequestLocationDeniedService.class);
                RequestLocationDeniedService.phoneNumbers.add(userphone);
                startService(rejectintent);
                RequestLocationActivity.this.setResult(RESULT_OK);
                RequestLocationActivity.this.finish();
            }
        }
    }

    /**
     * Click listener for one-time location update
     */
    private final OnClickListener clickListenerOnce = new OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {

            // if current location is not stale, send immediately
            Location last_location = GeoUtils.getCurrentLocation(context);
            if (last_location != null) {
                if (System.currentTimeMillis() - last_location.getTime() < STALE_TIME) {
                    sendLocationUpdate(context, userphone, last_location, FIRST_UPDATE);
                    RequestLocationActivity.this.setResult(RESULT_OK);
                    RequestLocationActivity.this.finish();
                    return;
                }
            }

            final LocationManager lm =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // check for location sources
            boolean isWifiEnabled = false;
            try {
                // post API 8
                isWifiEnabled =
                    Settings.Secure.isLocationProviderEnabled(
                        context.getContentResolver(), LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                // pre API 8
                isWifiEnabled = false;
                String enabledProviders =
                    Settings.Secure.getString(
                        context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (!TextUtils.isEmpty(enabledProviders)) {
                    // not the fastest way to do that :)
                    String[] providersList = TextUtils.split(enabledProviders, ",");
                    for (String provider : providersList) {
                        if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
                            isWifiEnabled = true;
                        }
                    }
                }
            }
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isWifiEnabled || !isGPSEnabled) {
                ((Activity) context).startActivityForResult(new Intent(
                    context, RequestLocationSourcesActivity.class), KEY_ONE_TIME);
            } else {
                startListenerOnce();
                RequestLocationActivity.this.setResult(RESULT_OK);
                RequestLocationActivity.this.finish();
                return;
            }
        }
    };

    /**
     * Location listener for location tracking
     * 
     * @param oneSent
     *            if one update was sent before this
     */
    private void startListenerTrack() {
        final UserLocationListener tracklistener = new UserLocationListener() {
            final Long end_time = System.currentTimeMillis() + TRACK_TIME;
            boolean first = false;
            final boolean firstSentA = firstSent;

            @Override
            public void onUserLocation(Location location) {
                if (System.currentTimeMillis() > end_time) {
                    NotificationManager nm =
                        (NotificationManager) RequestLocationActivity.this
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel(ID_TRACK_NOTIFICATION);
                    GeoUtils.removeListener(context, this);
                    return;
                }

                if (location != null) {
                    if (first || firstSentA) {
                        sendLocationUpdate(context, userphone, location, LATER_UPDATE);
                    } else {
                        sendLocationUpdate(context, userphone, location, FIRST_UPDATE);
                        first = true;
                    }
                }
            }
        };

        GeoUtils.registerListenerForTracking(context, tracklistener, userphone);

        // set handler to stop listener after a certain time
        Handler stopLocationHandler = new Handler();
        stopLocationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationManager nm =
                    (NotificationManager) RequestLocationActivity.this
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(ID_TRACK_NOTIFICATION);
                GeoUtils.removeListener(context, tracklistener);
            }
        }, TRACK_TIME);

        // set basic notification params
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(this.getString(R.string.app_name)).setContentText(
            this.getString(R.string.location_being_tracked)).setTicker(
            this.getString(R.string.location_being_tracked_with) + username).setSmallIcon(
            R.drawable.notification23).setOnlyAlertOnce(true).setOngoing(true).setWhen(
            System.currentTimeMillis());

        // set intent for when notification is clicked
        Intent notificationintent = new Intent(this, RequestLocationTrackingActivity.class);

        PendingIntent pendingIntent =
            PendingIntent.getActivity(
                this, 0, notificationintent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.getNotification();
        NotificationManager nm =
            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(ID_TRACK_NOTIFICATION, notification);
    }

    /**
     * Click listener for location tracking
     */
    private final OnClickListener clickListenerTrack = new OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {

            // if current location is not stale, send immediately
            Location last_location = GeoUtils.getCurrentLocation(context);
            if (last_location != null) {
                if (System.currentTimeMillis() - last_location.getTime() < STALE_TIME) {
                    sendLocationUpdate(context, userphone, last_location, FIRST_UPDATE);
                    firstSent = true;
                }
            }

            final LocationManager lm =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // check for location sources
            boolean isWifiEnabled = false;
            try {
                // post API 8
                isWifiEnabled =
                    Settings.Secure.isLocationProviderEnabled(
                        context.getContentResolver(), LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                // pre API 8
                isWifiEnabled = false;
                String enabledProviders =
                    Settings.Secure.getString(
                        context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (!TextUtils.isEmpty(enabledProviders)) {
                    // not the fastest way to do that :)
                    String[] providersList = TextUtils.split(enabledProviders, ",");
                    for (String provider : providersList) {
                        if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
                            isWifiEnabled = true;
                        }
                    }
                }
            }
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isWifiEnabled || !isGPSEnabled) {
                ((Activity) context).startActivityForResult(new Intent(
                    context, RequestLocationSourcesActivity.class), KEY_TRACK);
            } else {
                startListenerTrack();
                RequestLocationActivity.this.setResult(RESULT_OK);
                RequestLocationActivity.this.finish();
                return;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == KEY_ONE_TIME) {
            startListenerOnce();
            RequestLocationActivity.this.setResult(RESULT_OK);
            RequestLocationActivity.this.finish();
        } else if (requestCode == KEY_TRACK) {
            startListenerTrack();
            RequestLocationActivity.this.setResult(RESULT_OK);
            RequestLocationActivity.this.finish();
        }
    }

    /**
     * Send location update to server
     * 
     * @param targetphone
     *            Phone number which requested our location
     * @param location
     *            Our location
     */
    private
        void sendLocationUpdate(Context context, String userphone, Location location, int count) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("userphone", userphone));
        params.add(new BasicNameValuePair("targetphone", PhonarPreferencesManager
            .getPhoneNumber(context)));
        params.add(new BasicNameValuePair("lat", Double.toString(location.getLatitude())));
        params.add(new BasicNameValuePair("lng", Double.toString(location.getLongitude())));
        params.add(new BasicNameValuePair("count", Integer.toString(count)));

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
}
