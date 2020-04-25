package com.phonar.upgrade;

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
import com.phonar.utils.CommonUtils;
import com.phonar.utils.PhonarPreferencesManager;

public class UpgradeService extends Service {

    public static final long VIBRATE_TIME = 500;
    public static final String KEY_VERSION_NUMBER = "version?";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        int new_version = intent.getExtras().getInt(KEY_VERSION_NUMBER);

        // check version numbers
        int last_version = PhonarPreferencesManager.getLastVersionSeen(this);
        int cur_version = CommonUtils.getVersion(this, 0);

        if (last_version >= new_version || cur_version > new_version) {
            stopSelf();
            return START_STICKY;
        }

        PhonarPreferencesManager.setLastVersionSeen(this, new_version);

        // set basic notification params
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
            .setContentTitle(this.getString(R.string.app_name))
            .setContentText(UpgradeService.this.getString(R.string.upgrade_available))
            .setTicker(UpgradeService.this.getString(R.string.upgrade_available))
            .setSmallIcon(R.drawable.notification23).setOnlyAlertOnce(true).setAutoCancel(true)
            .setOngoing(false).setWhen(System.currentTimeMillis());

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
        Intent notificationintent = new Intent(Intent.ACTION_VIEW);
        notificationintent.setData(Uri.parse("market://details?id=com.phonar"));
        PendingIntent pendingIntent =
            PendingIntent.getActivity(
                this, 0, notificationintent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.getNotification();
        NotificationManager nm =
            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(0, notification);

        stopSelf();
        return START_STICKY;
    }

}
