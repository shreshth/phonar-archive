package com.phonar.c2dm;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.phonar.R;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.ContactsUtils;
import com.phonar.utils.GeoUtils;

/**
 * Activity to show a dialog if a user clicks on a tracking push notification
 */
public class RequestLocationTrackingActivity extends Activity {
    List<String> phoneNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phoneNumbers = GeoUtils.getTrackingPhoneNumbers();
        String[] items = new String[phoneNumbers.size()];
        for (int i = 0; i < phoneNumbers.size(); i++) {
            items[i] =
                ContactsUtils.getNameByPhoneNumber(phoneNumbers.get(i), getContentResolver());
        }

        if (phoneNumbers.size() == 1) {
            CharSequence[] items_one = { getString(R.string.yes), getString(R.string.no) };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.location_track_stop_one) + items[0] + "?");
            builder.setItems(items_one, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        GeoUtils.removeListenerForTracking(
                            RequestLocationTrackingActivity.this, phoneNumbers.get(which));

                        NotificationManager nm =
                            (NotificationManager) RequestLocationTrackingActivity.this
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.cancel(RequestLocationActivity.ID_TRACK_NOTIFICATION);
                    }
                    RequestLocationTrackingActivity.this.finish();
                }
            }).setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    RequestLocationTrackingActivity.this.finish();
                }
            });
            CommonUtils.showDialog(builder);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.location_track_stop)).setItems(
                items, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GeoUtils.removeListenerForTracking(
                            RequestLocationTrackingActivity.this, phoneNumbers.get(which));
                        if (phoneNumbers.size() == 1) {
                            NotificationManager nm =
                                (NotificationManager) RequestLocationTrackingActivity.this
                                    .getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.cancel(RequestLocationActivity.ID_TRACK_NOTIFICATION);
                        }
                        RequestLocationTrackingActivity.this.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    RequestLocationTrackingActivity.this.finish();
                }
            });
            CommonUtils.showDialog(builder);
        }
    }
}
