package com.phonar.c2dm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.phonar.PhonarTabActivity;
import com.phonar.R;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.ContactsUtils;

public class ReceiveLocationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // get targetphone
        String targetphone = intent.getExtras().getString("targetphone");
        String targetname = ContactsUtils.getNameByPhoneNumber(targetphone, getContentResolver());

        // get error
        int error = intent.getExtras().getInt("error");

        // show error toast
        if (error != C2DMReceiver.ERROR_NO_ERROR) {
            CommonUtils.toast(this, targetname + this.getString(R.string.location_denied));
            stopSelf();
            return START_STICKY;
        }

        // only show toast if RequestActivity is running
        if (PhonarTabActivity.mIsRunning) {
            CommonUtils.toast(this, targetname + this.getString(R.string.location_received));
        }
        // otherwise show push notification
        else {

            // set basic notification params
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle(this.getString(R.string.app_name)).setContentText(
                targetname + this.getString(R.string.location_received)).setTicker(
                targetname + this.getString(R.string.location_received)).setSmallIcon(
                R.drawable.notification23).setOnlyAlertOnce(true).setAutoCancel(true).setOngoing(
                false).setWhen(System.currentTimeMillis());

            // set intent for when notification is clicked
            Intent notificationintent = new Intent(this, PhonarTabActivity.class);
            notificationintent.putExtra(PhonarTabActivity.KEY_SHOW_MAP, true);
            PendingIntent pendingIntent =
                PendingIntent.getActivity(
                    this, 0, notificationintent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setContentIntent(pendingIntent);

            Notification notification = builder.getNotification();
            NotificationManager nm =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(0, notification);
        }

        stopSelf();
        return START_STICKY;
    }
}
