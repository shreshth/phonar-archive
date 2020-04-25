package com.phonar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.phonar.c2dm.RequestLocationService;
import com.phonar.utils.PhonarPreferencesManager;

public class LowBatteryDialogService extends Service {

    public static final String KEY_LOW_BATTERY_PHONE = "phonenumber";
    public static final String KEY_LOW_BATTERY_NAME = "displayname";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        String phoneNumber = intent.getStringExtra(KEY_LOW_BATTERY_PHONE);
        String displayName = intent.getStringExtra(KEY_LOW_BATTERY_NAME);

        int unique_id =
            (int) (Long.parseLong(phoneNumber
                .replace("+", "").replace("-", "").replace(" ", "").replace("(", "").replace(
                    ")", "")) % Integer.MAX_VALUE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
            .setContentTitle(this.getString(R.string.app_name)).setContentText(
                displayName + this.getString(R.string.low_power)).setTicker(
                displayName + this.getString(R.string.low_power)).setSmallIcon(
                R.drawable.notification23).setOnlyAlertOnce(true).setAutoCancel(true).setOngoing(
                false).setWhen(System.currentTimeMillis());

        if (PhonarPreferencesManager.getNotificationDefault(this)) {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        } else {
            // vibrate?
            if (PhonarPreferencesManager.getNotificationVibrate(this)) {
                long[] vibratePattern = new long[2];
                vibratePattern[0] = 0;
                vibratePattern[1] = RequestLocationService.VIBRATE_TIME;

                builder.setVibrate(vibratePattern);
            }

            // ringtone
            String ringtone = PhonarPreferencesManager.getNotificationRingtone(this);
            if (ringtone != null && !ringtone.isEmpty()) {
                Uri ringtoneUri = Uri.parse(ringtone);
                builder.setSound(ringtoneUri);
            }
        }

        Intent contentintent = new Intent(this, PhonarTabActivity.class);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(
                this, unique_id, contentintent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.getNotification();
        NotificationManager nm =
            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(unique_id, notification);

        stopSelf();
        return START_STICKY;
    }
}
