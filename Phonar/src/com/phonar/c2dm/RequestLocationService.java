package com.phonar.c2dm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.phonar.R;
import com.phonar.utils.ContactsUtils;
import com.phonar.utils.PhonarPreferencesManager;

/**
 * Service that runs when we receive a friend finder request asking for our
 * location (in C2DMReceiver.java). Shows a push notification asking user if
 * they would like to share location, which takes them to a dialog in
 * RequestLocationActivity.java
 */
public class RequestLocationService extends Service {

    public static final long VIBRATE_TIME = 500;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // get userphone
        String userphone = intent.getExtras().getString("userphone");
        String username = ContactsUtils.getNameByPhoneNumber(userphone, getContentResolver());

        // Convert phone number into int to use as ID
        int unique_id =
            (int) (Long.parseLong(userphone
                .replace("+", "").replace("-", "").replace(" ", "").replace("(", "").replace(
                    ")", "")) % Integer.MAX_VALUE);

        // set basic notification params
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
            .setContentTitle(this.getString(R.string.app_name)).setContentText(
                username + this.getString(R.string.location_request)).setTicker(
                username + this.getString(R.string.location_request)).setSmallIcon(
                R.drawable.notification23).setOnlyAlertOnce(true).setAutoCancel(true).setOngoing(
                false).setWhen(System.currentTimeMillis());

        if (PhonarPreferencesManager.getNotificationDefault(this)) {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        } else {
            // vibrate?
            if (PhonarPreferencesManager.getNotificationVibrate(this)) {
                long[] vibratePattern = new long[2];
                vibratePattern[0] = 0;
                vibratePattern[1] = VIBRATE_TIME;

                builder.setVibrate(vibratePattern);
            }

            // ringtone
            String ringtone = PhonarPreferencesManager.getNotificationRingtone(this);
            if (ringtone != null && !ringtone.isEmpty()) {
                Uri ringtoneUri = Uri.parse(ringtone);
                builder.setSound(ringtoneUri);
            }
        }

        // set intent for if notification is clicked
        Intent notificationintent = new Intent(this, RequestLocationActivity.class);
        notificationintent.putExtra("userphone", userphone);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(
                this, unique_id, notificationintent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        // set intent for if notification is cleared
        Intent deleteintent = new Intent(this, RequestLocationDeniedService.class);
        RequestLocationDeniedService.phoneNumbers.add(userphone);
        PendingIntent pendingDeleteIntent =
            PendingIntent.getService(
                this, unique_id, deleteintent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setDeleteIntent(pendingDeleteIntent);

        Notification notification = builder.getNotification();
        NotificationManager nm =
            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(unique_id, notification);

        stopSelf();
        return START_STICKY;
    }
}
