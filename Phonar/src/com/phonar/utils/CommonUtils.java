package com.phonar.utils;

import java.util.Arrays;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Secure;
import android.widget.Toast;

import com.phonar.R;

public class CommonUtils {

	public static final boolean PRODUCTION_MODE = false;
	public static final boolean FRIENDS_PRODUCTION = false;
	public static final boolean DEVICES_PRODUCTION = false;

	public static final String[] TEST_DEVICE_WHITELIST = { "e8bce1e69b89be6f", // Hamza's
																				// personal
																				// phone
					"72890e4ed7e94cae", // Shreshth's personal phone
					"969e480dc8aef068" // Nitin's personal phone
	};

	/**
	 * is this a testing phone?
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean isTestPhone(Context context) {
		if (FRIENDS_PRODUCTION || DEVICES_PRODUCTION) {
			return false;
		}
		for (String allowed_device : TEST_DEVICE_WHITELIST) {
			if (getPhoneID(context).equals(allowed_device)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * return phone ID (unique for each android phone)
	 * 
	 * @param context
	 * @return
	 */
	public static String getPhoneID(Context context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	/**
	 * Get median of data
	 * 
	 * @param data
	 * @return
	 */
	public static float getMedian(float[] data) {
		Arrays.sort(data);
		if (data.length % 2 == 0) {
			return (data[data.length / 2] + data[data.length / 2 - 1]) / 2;
		} else {
			return data[data.length / 2];
		}
	}

	/**
	 * Convert distance measure to a scaled string to display on AR view
	 * 
	 * @param distance
	 * @return
	 */
	public static String getFormattedDistance(Context context, float distance) {
		double milesInAMeter = 0.000621371192;
		double feetInAMeter = 3.2808399;
		if (distance * milesInAMeter > 0.1) {
			return String.format("%.1f %s", distance * milesInAMeter,
							context.getString(R.string.miles_short));
		}
		return String.format("%d %s", (int) (distance * feetInAMeter),
						context.getString(R.string.feet_short));
	}

	/**
	 * formats the time (given in milliseconds)
	 * 
	 * @param context
	 * @param time
	 * @return
	 */
	public static String getTimeString(Context context, long time) {
		time = time / 1000;
		int minutes = (int) (time / 60);
		int hours = (int) (time / (60 * 60));
		int days = (int) (time / (24 * 60 * 60));
		StringBuilder sb = new StringBuilder();
		if (days > 0) {
			sb.append(days);
			if (days != 1) {
				sb.append(context.getString(R.string.days));
			} else {
				sb.append(context.getString(R.string.day));
			}
		} else if (hours > 0) {
			sb.append(hours);
			if (hours != 1) {
				sb.append(context.getString(R.string.hours));
			} else {
				sb.append(context.getString(R.string.hour));
			}
		} else if (minutes > 0) {
			sb.append(minutes);
			if (minutes != 1) {
				sb.append(context.getString(R.string.minutes));
			} else {
				sb.append(context.getString(R.string.minute));
			}
		} else {
			sb.append(context.getString(R.string.seconds));
		}
		sb.append(context.getString(R.string.ago));
		return sb.toString();
	}

	private static Toast toast = null;

	/**
	 * Helper for showing toast safely
	 * 
	 * @param context
	 * @param msg
	 */
	public static void toast(Context context, String msg) {

		if (toast != null && toast.getView().isShown()) {
			try {
				toast.setText(msg);
				toast.show();
			} catch (Exception e) {
			}
		} else {
			try {
				toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
				toast.show();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Helper for showing toast safely
	 * 
	 * @param context
	 * @param resId
	 */
	public static void toast(Context context, int resId) {

		if (toast != null && toast.getView().isShown()) {
			try {
				toast.setText(resId);
				toast.show();
			} catch (Exception e) {
			}
		} else {
			try {
				toast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
				toast.show();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Helper for showing a dialog safely
	 * 
	 * @param builder
	 */
	public static void showDialog(AlertDialog.Builder builder) {
		try {
			builder.create().show();
		} catch (Exception e) {
		}
	}

	/**
	 * Get current app version
	 * 
	 * @param context
	 * @param defaultVal
	 *            Default value
	 * @return
	 */
	public static int getVersion(Context context, int defaultVal) {
		int cur_version;
		try {
			cur_version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			cur_version = defaultVal;
		}
		return cur_version;
	}
}
