package com.phonar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PhonarPreferencesManager {

	public static String KEY_PHONE_NUMBER = "phone number";
	public static String KEY_C2DM_ID = "c2dm id";
	public static String KEY_SMS_WARNING = "sms_warning";
	public static String KEY_NOTIFICATION_DEFAULT = "notification default?";
	public static String KEY_NOTIFICATION_VIBRATE = "notification vibrate?";
	public static String KEY_NOTIFICATION_RINGTONE = "notification ringtone";
	public static String KEY_GOOGLEVOICE = "google voice?";
	public static String KEY_MAGNETIC_INTERFERENCE = "magnetic interference";
	public static String KEY_LAST_VERSION = "last version";
	public static String KEY_FIRST_RESPONSE = "first_response";
	public static String KEY_REVIEW_DIALOG = "review";
	public static String KEY_FRIENDS_MODE = "friends mode?";
	public static String KEY_LOCALE = "locale";

	/*
	 * Phone stuff
	 */
	public static void setPhoneNumber(Context context, String phoneNumber) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_PHONE_NUMBER, phoneNumber);
		editor.commit();
	}

	public static String getPhoneNumber(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String phone = settings.getString(KEY_PHONE_NUMBER, null);
		return phone;
	}

	public static void setC2DMId(Context context, String id) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_C2DM_ID, id);
		editor.commit();
	}

	public static String getC2DMId(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String phone = settings.getString(KEY_C2DM_ID, null);
		return phone;
	}

	/*
	 * Used to decide whether to show the review dialog
	 */
	public static void setFirstResponseSeen(Context context, boolean seen) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(KEY_FIRST_RESPONSE, seen);
		editor.commit();
	}

	public static boolean getFirstResponseSeen(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean seen = settings.getBoolean(KEY_FIRST_RESPONSE, false);
		return seen;
	}

	public static void setReviewDialogSeen(Context context, boolean seen) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(KEY_REVIEW_DIALOG, seen);
		editor.commit();
	}

	public static boolean getReviewDialogSeen(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean seen = settings.getBoolean(KEY_REVIEW_DIALOG, false);
		return seen;
	}

	/*
	 * If various warnings seen
	 */
	public static void setMagneticInteferenceSeen(Context context, boolean seen) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(KEY_MAGNETIC_INTERFERENCE, seen);
		editor.commit();
	}

	public static boolean getMagneticInterferenceSeen(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean seen = settings.getBoolean(KEY_MAGNETIC_INTERFERENCE, false);
		return seen;
	}

	public static void setSMSWarningSeen(Context context, boolean seen) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(KEY_SMS_WARNING, seen);
		editor.commit();
	}

	public static boolean getSMSWarningSeen(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean seen = settings.getBoolean(KEY_SMS_WARNING, false);
		return seen;
	}

	public static void setLastVersionSeen(Context context, int version) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(KEY_LAST_VERSION, version);
		editor.commit();
	}

	public static int getLastVersionSeen(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		int version = settings.getInt(KEY_LAST_VERSION, 0);
		return version;
	}

	/*
	 * We use Boolean instead of boolean here to distinguish between Google
	 * Voice (true), Standard Phone Number (false) and this setting not being
	 * set (null)
	 */
	private static int isGoogleVoiceHelper(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		int isGoogleVoice = settings.getInt(KEY_GOOGLEVOICE, -1);
		return isGoogleVoice;
	}

	private static void setGoogleVoiceHelper(Context context, int isGoogleVoice) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(KEY_GOOGLEVOICE, isGoogleVoice);
		editor.commit();
	}

	public static Boolean isGoogleVoice(Context context) {
		if (isGoogleVoiceHelper(context) == -1) {
			return null;
		} else if (isGoogleVoiceHelper(context) == 0) {
			return false;
		} else if (isGoogleVoiceHelper(context) == 1) {
			return true;
		}
		return null;
	}

	public static void setGoogleVoice(Context context, boolean isGoogleVoice) {
		if (isGoogleVoice) {
			setGoogleVoiceHelper(context, 1);
		} else {
			setGoogleVoiceHelper(context, 0);
		}
	}

	/*
	 * Locale options
	 */
	public static String getLocale(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		if (settings.contains(KEY_LOCALE)) {
			String locale = settings.getString(KEY_LOCALE, "US").toUpperCase();
			return locale;
		}

		String locale = GeoUtils.getCountryCode(context).toUpperCase();
		return locale;
	}

	public static void setLocale(Context context, String locale) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_LOCALE, locale);
		editor.commit();
	}

	/*
	 * Notification options
	 */
	public static boolean getNotificationDefault(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean notif_default = settings.getBoolean(KEY_NOTIFICATION_DEFAULT, true);
		return notif_default;
	}

	public static boolean getNotificationVibrate(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean vibrate = settings.getBoolean(KEY_NOTIFICATION_VIBRATE, true);
		return vibrate;
	}

	public static String getNotificationRingtone(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String ringtone = settings.getString(KEY_NOTIFICATION_RINGTONE, null);
		return ringtone;
	}

	/*
	 * Testing options
	 */
	public static void setFriendsMode(Context context, boolean enabled) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(KEY_FRIENDS_MODE, enabled);
		editor.commit();
	}

	public static boolean isFriendsMode(Context context) {
		if (CommonUtils.FRIENDS_PRODUCTION) {
			return true;
		} else if (CommonUtils.DEVICES_PRODUCTION) {
			return false;
		} else {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			boolean enabled = settings.getBoolean(KEY_FRIENDS_MODE, false);
			return enabled;
		}
	}
}
