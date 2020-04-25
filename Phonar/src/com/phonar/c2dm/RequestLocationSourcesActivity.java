package com.phonar.c2dm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.phonar.R;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.GeoUtils;

/**
 * Activity to show a dialog to ask user to turn on location sources. Called
 * from RequestLocationActivity when it needs to show two dialogs, which is not
 * possible from one activity.
 * 
 * Replicates GeoUtils:checkLocationSources()
 */
public class RequestLocationSourcesActivity extends Activity {

    private final Context mContext = this;
    private boolean startedSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Replicated from GeoUtils:checkLocationSources()
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean isWifiEnabled = false;
        try {
            // post API 8
            isWifiEnabled =
                Settings.Secure.isLocationProviderEnabled(
                    this.getContentResolver(), LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            // pre API 8
            isWifiEnabled = false;
            String enabledProviders =
                Settings.Secure.getString(
                    this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
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

        if (isWifiEnabled && isGPSEnabled) {
            this.setResult(RESULT_OK);
            this.finish();
        }

        // first time - show a dialog
        GeoUtils.warningStatus = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.geo_source_alert_title).setPositiveButton(
            R.string.geo_source_alert_positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startedSettings = true;
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    ((Activity) mContext).startActivityForResult(myIntent, 0);
                }
            }).setNegativeButton(
            R.string.geo_source_alert_negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ((Activity) mContext).setResult(RESULT_OK);
                    ((Activity) mContext).finish();
                }
            });

        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                RequestLocationSourcesActivity.this.setResult(RESULT_OK);
                RequestLocationSourcesActivity.this.finish();
            }
        });

        if (!isWifiEnabled && !isGPSEnabled) {
            builder.setMessage(R.string.geo_source_alert_both);
        } else if (!isGPSEnabled) {
            builder.setMessage(R.string.geo_source_alert_gps_only);
        } else if (!isWifiEnabled) {
            builder.setMessage(R.string.geo_source_alert_wifi_only);
        }
        CommonUtils.showDialog(builder);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.startedSettings) {
            this.setResult(RESULT_OK);
            this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.setResult(RESULT_OK);
        this.finish();
    }
}
