package com.phonar.device;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

/*
 * TODO:
 * 2. SOS phone numbers
 * 
 * (Move strings into string.xml)
 */
public class SMSReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private String PASSWORD = "1337"; // device password

    private static final String MSG_BEGIN = "*#";
    private static final String MSG_SEPARATOR = "#";

    // Device response strings
    private static final String ERROR_CMD = "Command error";
    private static final String ERROR_PWD = "Password error";
    private static final String ERROR_TRACK = "Track request error";

    private static final String UNTRACK_OK = "Untrack request = ok";

    // Device command strings
    private static final String CMD_GET_LOCATION = "88";
    private static final String CMD_TRACK = "11";
    private static final String CMD_UNTRACK = "011";

    private static final String CMD_GET_STATUS = "45"; // unsupported
    private static final String CMD_CHANGE_PASS = "088"; // unsupported

    // Location information
    public static Location myLoc = null;
    private static final String LOC_NOTFOUND = "-1.000,-1,000";

    // Tracking information - interval (in minutes) to wait before sending
    // location info
    public static int track = -1;
    public static int numPolls = 0;
    public static boolean usingPolls = false;

    private static PendingIntent alarm;
    private static final int LOCATION_SEND_ALARM_CODE = 2345;
    public static String src;

    // Other random constants
    private static int MILLIS_TO_MINUTES = 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION)) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                Object[] smsextras = (Object[]) extras.get("pdus");

                for (int i = 0; i < smsextras.length; i++) {
                    String sms_body = ERROR_CMD;

                    // Form an SMS message from the extras
                    SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);

                    String body = smsmsg.getMessageBody().toString();
                    src = smsmsg.getOriginatingAddress();

                    // Check to see if the message is Phonar-related (begins
                    // with "*#")
                    if (body.substring(0, 2).equals(MSG_BEGIN)) {
                        // abortBroadcast();

                        String[] tokens = body.split(MSG_SEPARATOR);

                        if (tokens.length < 3)
                            sms_body = ERROR_CMD;
                        else if (!tokens[2].equals(PASSWORD))
                            sms_body = ERROR_PWD;
                        else {

                            // Check the different possible commands, and handle
                            // them
                            if (tokens[1].equals(CMD_GET_LOCATION)) {
                                sms_body = handleLocationRequest(context);
                            } else if (tokens[1].equals(CMD_TRACK)) {
                                if (tokens.length == 4) {
                                    sms_body = handleTrackRequest(context,
                                                    Integer.parseInt(tokens[3]));
                                } else {
                                    sms_body = handleTrackRequest(context,
                                                    Integer.parseInt(tokens[3]),
                                                    Integer.parseInt(tokens[4]));
                                }
                            } else if (tokens[1].equals(CMD_UNTRACK)) {
                                sms_body = handleUntrackRequest(context);
                            }

                        }

                        // Send a response SMS
                        if (!sms_body.equals(LOC_NOTFOUND)) {
                            SmsManager send = SmsManager.getDefault();
                            send.sendTextMessage(src, null, sms_body, null, null);
                        }
                    }

                }
            }
        }
    }

    // Handle a one-time request for the device's location
    public static String handleLocationRequest(Context context) {
        String sms = "";
        Location loc = GeoUtils.getCurrentLocation(context);

        if (loc != null) {
            sms = loc.getLatitude() + "," + loc.getLongitude();
        } else {
            sms = LOC_NOTFOUND;
        }

        return sms;
    }

    // Handle a track request (return location every trackNum minutes)
    private String handleTrackRequest(Context context, int trackNum) {
        if (trackNum <= 0) {
            return ERROR_TRACK;
        }
        track = trackNum * MILLIS_TO_MINUTES;
        usingPolls = false;
        startSendLocationAlarm(context);

        return "Track request = ok, interval = " + trackNum + " minutes.";
    }

    // Handle a track request (return location every trackNum minutes)
    private String handleTrackRequest(Context context, int trackNum, int polls) {
        if (trackNum <= 0 || polls <= 0) {
            return ERROR_TRACK;
        }
        track = trackNum * MILLIS_TO_MINUTES;
        numPolls = polls;
        usingPolls = true;

        startSendLocationAlarm(context);

        return "Track request = ok, interval = " + trackNum + " minutes.";
    }

    private void startSendLocationAlarm(Context context) {
        if (alarm == null) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent newintent = new Intent(context, SendLocationAlarm.class);
            alarm = PendingIntent.getBroadcast(context, LOCATION_SEND_ALARM_CODE, newintent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, getAlarmStartTime(), track, alarm);

        }

    }

    private static long getAlarmStartTime() {
        return System.currentTimeMillis() - System.currentTimeMillis() % (track) + track;
    }

    // Handle an untrack request
    private String handleUntrackRequest(Context context) {
        track = -1;
        numPolls = -1;
        stopSendLocationAlarm(context);

        return UNTRACK_OK;
    }

    public static void stopSendLocationAlarm(Context context) {
        if (alarm != null) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(alarm);
            alarm = null;
        }
    }
}
