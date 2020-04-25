package com.phonar.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.phonar.R;
import com.phonar.network.NetworkRequest;
import com.phonar.network.NetworkRequest.NetworkRequestListener;
import com.phonar.request.RequestActivity;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.GeoUtils;
import com.phonar.utils.PhonarPreferencesManager;

/**
 * 
 * Encapsulates information about a device in memory
 * 
 */
public class Device implements NetworkRequestListener {

    // Device types
    public static final String DEVICE_GENERIC = "generic";
    public static final String DEVICE_KIDS_SHOE = "kids_shoe";
    public static final String DEVICE_ADULT_SHOE = "adult_shoe";
    public static final String DEVICE_CAR = "car";
    public static final String DEVICE_PET = "pet";
    public static final String DEVICE_FRIEND = "friend";

    // Driving vs. walking threshold for navigation
    public final int NAVIG_THRESHOLD = 1000;

    public String phoneNumber;
    public String password;
    public String type;
    public String displayName;
    public Bitmap image;
    public Double latitude;
    public Double longitude;
    public Long timeOfLastLocation; // time in milliseconds
    public Long interval; // how often to take location
    public Long polls; // how many times to poll location
    public Integer battery;
    public Boolean trustedFriend;
    public String lookupKey;

    // Temporary variables stored for efficiency
    public Float bearingTo;
    public Float distanceTo;

    // Temporary variables stored for UI reasons
    public boolean error = false;
    public boolean requestSent = false;

    private Context mContext = null;
    private static ProgressDialog progressDialog = null;

    // generic constructor
    public Device(
        Context context, String phoneNumber, String password, String type, String displayName,
        Bitmap image, Double latitude, Double longitude, Long timeOfLastLocation, Long interval,
        Long polls, Integer battery, Boolean trustedFriend, String lookupKey) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.type = type;
        this.image = image;
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeOfLastLocation = timeOfLastLocation;
        this.battery = battery;
        this.interval = interval;
        this.polls = polls;
        this.trustedFriend = trustedFriend;
        this.lookupKey = lookupKey;

        if (latitude != null && longitude != null) {
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            if (GeoUtils.getLastKnownLocation(context) != null) {
                bearingTo =
                    (float) Math.toRadians(GeoUtils.getLastKnownLocation(context).bearingTo(
                        location));
                distanceTo = GeoUtils.getLastKnownLocation(context).distanceTo(location);
            }
        }
    }

    // constructor for non-friend device
    public Device(
        Context context, String phoneNumber, String password, String type, String displayName,
        Bitmap image, Double latitude, Double longitude, Long timeOfLastLocation, Long interval,
        Long polls, Integer battery) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.type = type;
        this.image = image;
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeOfLastLocation = timeOfLastLocation;
        this.battery = battery;
        this.interval = interval;
        this.polls = polls;
        this.trustedFriend = null;
        this.lookupKey = null;

        if (latitude != null && longitude != null) {
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            if (GeoUtils.getLastKnownLocation(context) != null) {
                bearingTo =
                    (float) Math.toRadians(GeoUtils.getLastKnownLocation(context).bearingTo(
                        location));
                distanceTo = GeoUtils.getLastKnownLocation(context).distanceTo(location);
            }
        }
    }

    // constructor to friend device
    public Device(
        Context context, String phoneNumber, String displayName, Bitmap image, Double latitude,
        Double longitude, Long timeOfLastLocation, Boolean trustedFriend, String lookupKey) {
        this.phoneNumber = phoneNumber;
        this.password = null;
        this.type = Device.DEVICE_FRIEND;
        this.image = image;
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeOfLastLocation = timeOfLastLocation;
        this.battery = null;
        this.trustedFriend = trustedFriend;
        this.interval = null;
        this.polls = null;
        this.lookupKey = lookupKey;

        if (latitude != null && longitude != null) {
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            if (GeoUtils.getLastKnownLocation(context) != null) {
                bearingTo =
                    (float) Math.toRadians(GeoUtils.getLastKnownLocation(context).bearingTo(
                        location));
                distanceTo = GeoUtils.getLastKnownLocation(context).distanceTo(location);
            }
        }
    }

    public boolean isFriend() {
        return this.type.equals(Device.DEVICE_FRIEND);
    }

    public void updateLocationDevice(
        Context context, double latitude, double longitude, Integer battery, long time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeOfLastLocation = time;
        this.battery = battery;
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        if (GeoUtils.getLastKnownLocation(context) != null) {
            bearingTo =
                (float) Math.toRadians(GeoUtils.getLastKnownLocation(context).bearingTo(location));
            distanceTo = GeoUtils.getLastKnownLocation(context).distanceTo(location);
        }
        this.requestSent = false;
    }

    public void updateLocationFriend(Context context, double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeOfLastLocation = Calendar.getInstance().getTimeInMillis();
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        if (GeoUtils.getLastKnownLocation(context) != null) {
            bearingTo =
                (float) Math.toRadians(GeoUtils.getLastKnownLocation(context).bearingTo(location));
            distanceTo = GeoUtils.getLastKnownLocation(context).distanceTo(location);
        }
        this.requestSent = false;
    }

    public void updateErrorFriend(Context context, boolean error) {
        this.error = error;
    }

    public void showOptionsDialog(final Context context) {
        // create the intent for navigation mode
        String navigationURI = "google.navigation:mode=w&q="; // walking
        if (distanceTo == null) {
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            Location cur_location = GeoUtils.getCurrentLocation(context);
            if (cur_location != null) {
                distanceTo = cur_location.distanceTo(location);
            }
        }
        if (distanceTo != null) {
            if (distanceTo > NAVIG_THRESHOLD) { // driving
                navigationURI = "google.navigation:mode=d&q=";
            }
        }
        final Intent intent =
            new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(navigationURI
                + Device.this.latitude.toString() + "," + Device.this.longitude.toString()));

        // decide whether to show both refresh and navigate options or just
        // refresh
        String[] items;
        final PackageManager packageManager = context.getPackageManager();
        @SuppressWarnings("rawtypes")
        List resolveInfo =
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        // only show navigate option if intent is handleable
        if (resolveInfo.size() > 0 && this.latitude != null && this.longitude != null) {
            final String[] text =
                {
                    context.getString(R.string.refresh_location),
                    context.getString(R.string.navigate_to) };
            items = text;
        } else {
            final String[] text = { context.getString(R.string.refresh_location) };
            items = text;
        }
        // show options
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = displayName;
        if (battery != null) {
            title = title + " (" + context.getString(R.string.battery) + " " + battery + "%)";
        }
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    // refresh button pressed
                    if (isFriend()) {
                        sendNetworkRequestFindFriend(context);
                    } else {
                        sendNetworkRequestFindDevice(context);
                    }
                } else if (item == 1) {
                    // navigate button pressed
                    context.startActivity(intent);
                }
            }
        });
        CommonUtils.showDialog(builder);
    }

    public void sendNetworkRequestFindDevice(Context context) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("devicenumber", phoneNumber));
        params.add(new BasicNameValuePair("password", password));
        params
            .add(new BasicNameValuePair("pushtoken", PhonarPreferencesManager.getC2DMId(context)));
        if (timeOfLastLocation == null) {
            params.add(new BasicNameValuePair("staleIsOK", "1"));
        }

        if (progressDialog != null) {
            try {
                progressDialog.cancel();
            } catch (Exception e) {
            }
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(context.getString(R.string.sending));
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        try {
            progressDialog.show();
        } catch (Exception e) {
        }

        NetworkRequest request =
            new NetworkRequest(context, NetworkRequest.URL_FINDONCE, true, params, this);
        request.run();

        // refresh list
        RequestActivity.refreshList();
    }

    public void sendNetworkRequestFindFriend(Context context) {
        mContext = context;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("userphone", PhonarPreferencesManager
            .getPhoneNumber(context)));
        params.add(new BasicNameValuePair("c2dm_id", PhonarPreferencesManager.getC2DMId(context)));
        params.add(new BasicNameValuePair("device_id", CommonUtils.getPhoneID(context)));
        params.add(new BasicNameValuePair("targetphone", phoneNumber));
        NetworkRequest request =
            new NetworkRequest(context, NetworkRequest.URL_FIND_FRIEND, true, params, this);

        if (progressDialog != null) {
            try {
                progressDialog.cancel();
            } catch (Exception e) {
            }
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(context.getString(R.string.sending));
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        try {
            progressDialog.show();
        } catch (Exception e) {
        }

        request.run();

        // refresh list
        RequestActivity.refreshList();
    }

    @Override
    public void onRequestComplete(final String response) {
        if (progressDialog != null) {
            try {
                progressDialog.cancel();
            } catch (Exception e) {
            } finally {
                progressDialog = null;
            }
        }
        if (response == null) {
            this.error = true;
            RequestActivity.refreshList();
            return;
        }
        if (!response.isEmpty()) {
            // Display dialog
            LayoutInflater factory = LayoutInflater.from(mContext);
            ViewGroup textDialog = (ViewGroup) factory.inflate(R.layout.friendmessage_dialog, null);
            // If message has been seen already, don't show it
            TextView content = (TextView) textDialog.getChildAt(0);
            if (PhonarPreferencesManager.getSMSWarningSeen(mContext) == false) {
                PhonarPreferencesManager.setSMSWarningSeen(mContext, true);
            } else {
                content.setVisibility(View.GONE);
            }

            final EditText text = (EditText) textDialog.getChildAt(1);
            CommonUtils.showDialog(new AlertDialog.Builder(mContext)
                .setTitle(R.string.smsfriend).setView(textDialog)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String msg_optional;
                        try {
                            msg_optional =
                                new String(text.getText().toString().getBytes(), "UTF-8");
                        } catch (Exception e) {
                            msg_optional = "";
                        }
                        String msg_url =
                            mContext.getString(R.string.clicklink)
                                + NetworkRequest.URL_SHORT_URL + response;
                        SmsManager sms = SmsManager.getDefault();
                        if (msg_optional.length() + msg_url.length() < 138) {
                            sms.sendTextMessage(
                                phoneNumber, null, msg_optional + "\n\n" + msg_url, null, null);
                        } else {
                            sms.sendMultipartTextMessage(
                                phoneNumber, null, sms.divideMessage(msg_optional), null, null);
                            sms.sendTextMessage(phoneNumber, null, msg_url, null, null);
                        }
                        Device.this.requestSent = true;
                        Device.this.error = false;
                        RequestActivity.refreshList();
                        RequestActivity.showReviewDialogIfNotShownBefore();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Device.this.error = false;
                        RequestActivity.refreshList();
                    }
                }).setCancelable(false));
        } else {
            this.error = false;
            this.requestSent = true;
            RequestActivity.refreshList();
            RequestActivity.showReviewDialogIfNotShownBefore();
        }
    }

    public void sendNetworkRequestStopTracking(Context context) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("devicenumber", phoneNumber));
        params.add(new BasicNameValuePair("password", password));
        params
            .add(new BasicNameValuePair("pushtoken", PhonarPreferencesManager.getC2DMId(context)));
        NetworkRequest request =
            new NetworkRequest(context, NetworkRequest.URL_STOPTRACKING, true, params, null);
        request.run();
    }

    public void sendNetworkRequestStartTracking(Context context) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("devicenumber", phoneNumber));
        params.add(new BasicNameValuePair("password", password));
        params
            .add(new BasicNameValuePair("pushtoken", PhonarPreferencesManager.getC2DMId(context)));
        params.add(new BasicNameValuePair("interval", Long.toString(interval)));
        params.add(new BasicNameValuePair("polls", Long.toString(polls)));
        NetworkRequest request =
            new NetworkRequest(context, NetworkRequest.URL_TRACK, true, params, null);
        request.run();
    }

    public void updateBearingAndDistance(Context context) {
        if (latitude != null && longitude != null) {
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            if (GeoUtils.getLastKnownLocation(context) != null) {
                bearingTo =
                    (float) Math.toRadians(GeoUtils.getLastKnownLocation(context).bearingTo(
                        location));
                distanceTo = GeoUtils.getLastKnownLocation(context).distanceTo(location);
            }
        }
    }
}