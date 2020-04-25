package com.phonar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.phonar.R;

public class GeoUtils {

	public static boolean warningStatus = false;
	private static final String LOG_TAG = "GeoUtils";
	private static PhonarGlobalLocationListener mGlobalListener;
	private static Location mLastKnownLocation = null;
	private static List<UserLocationListener> listeners = new ArrayList<UserLocationListener>();
	private static Map<String, UserLocationListener> trackedListeners = new HashMap<String, UserLocationListener>();
	private static final double LOCATION_SMOOTHING_FACTOR = 0.5;

	/*
	 * Start reading location updates
	 */
	private static void startLocationUpdates(Context context) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		long minTime = 5000L;
		float minDist = 0F;

		checkLocationSources(context, false);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		if (mGlobalListener == null) {
			mGlobalListener = new PhonarGlobalLocationListener(context);
		}
		// register listeners
		try {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDist,
							mGlobalListener);
		} catch (Exception e) {
		}
		try {
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDist,
							mGlobalListener);
		} catch (Exception e) {
		}
		try {
			lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDist,
							mGlobalListener);
		} catch (Exception e) {
		}
	}

	/*
	 * Stop reading location updates
	 */
	private static void stopLocationUpdates(Context context) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		try {
			lm.removeUpdates(mGlobalListener);
		} catch (Exception e) {
		}
	}

	/*
	 * Location was received
	 */
	private static void onLocation(Context context, Location location) {
		if (mLastKnownLocation == null) {
			mLastKnownLocation = location;
		} else {
			mLastKnownLocation.setLatitude((1 - LOCATION_SMOOTHING_FACTOR)
							* mLastKnownLocation.getLatitude() + LOCATION_SMOOTHING_FACTOR
							* location.getLatitude());
			mLastKnownLocation.setLongitude((1 - LOCATION_SMOOTHING_FACTOR)
							* mLastKnownLocation.getLongitude() + LOCATION_SMOOTHING_FACTOR
							* location.getLongitude());
		}
		for (UserLocationListener listener : listeners) {
			listener.onUserLocation(GeoUtils.getLastKnownLocation(context));
		}
	}

	/**
	 * Register for location updates
	 */
	public static void registerListener(Context context, UserLocationListener listener) {
		checkLocationSources(context, false);

		if (listeners.size() == 0) {
			GeoUtils.startLocationUpdates(context);
		}
		listeners.add(listener);
	}

	/**
	 * Register for location updates to be sent to userphone
	 * 
	 * @param context
	 * @param listener
	 * @param userphone
	 *            Phone which is tracking us
	 */
	public static void registerListenerForTracking(Context context, UserLocationListener listener,
					String userphone) {

		// if this phone was already tracking us, stop those location updates
		if (trackedListeners.containsKey(userphone)) {
			UserLocationListener temp = trackedListeners.get(userphone);
			removeListener(context, temp);
			trackedListeners.remove(userphone);
		}

		registerListener(context, listener);
		trackedListeners.put(userphone, listener);
	}

	/**
	 * Remove listener
	 */
	public static void removeListener(Context context, UserLocationListener listener) {
		listeners.remove(listener);
		if (listeners.size() == 0) {
			GeoUtils.stopLocationUpdates(context);
		}
	}

	/**
	 * Remove a listener that sends updates to userphone
	 * 
	 * @param context
	 * @param userphone
	 *            Phone that updates are sent to
	 */
	public static void removeListenerForTracking(Context context, String userphone) {
		if (trackedListeners.containsKey(userphone)) {
			UserLocationListener listener = trackedListeners.get(userphone);
			removeListener(context, listener);
			trackedListeners.remove(userphone);
		}
	}

	/**
	 * Get all userphones which are currently tracking us
	 */
	public static List<String> getTrackingPhoneNumbers() {
		List<String> phoneNumbers = new ArrayList<String>();
		for (String phoneNumber : trackedListeners.keySet()) {
			phoneNumbers.add(phoneNumber);
		}
		return phoneNumbers;
	}

	/**
	 * Get last known location
	 */
	public static Location getLastKnownLocation(Context context) {
		return mLastKnownLocation;
	}

	/*
	 * Read location once
	 */
	public static void singleLocationUpdate(Context context) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		checkLocationSources(context, false);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		if (mGlobalListener == null) {
			mGlobalListener = new PhonarGlobalLocationListener(context);
		}
		try {
			lm.requestSingleUpdate(criteria, mGlobalListener, null);
		} catch (Exception e) {
			// onGPSTurnedOff(context);
		}
	}

	/**
	 * What to do if GPS or wifi sources are turned off
	 * 
	 * @param context
	 * @param forceDialog
	 *            Whether to force the dialog to be shown rather than the toast
	 */
	public static void checkLocationSources(final Context context, boolean forceDialog) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean isWifiEnabled = false;
		try {
			// post API 8
			isWifiEnabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(),
							LocationManager.NETWORK_PROVIDER);
		} catch (Exception e) {
			// pre API 8
			isWifiEnabled = false;
			String enabledProviders = Settings.Secure.getString(context.getContentResolver(),
							Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
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
			return;
		}

		if (!warningStatus || forceDialog) {
			// first time - show a dialog
			warningStatus = true;

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.geo_source_alert_title)
							.setPositiveButton(R.string.geo_source_alert_positive,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog,
																int which) {
													Intent myIntent = new Intent(
																	Settings.ACTION_LOCATION_SOURCE_SETTINGS);
													context.startActivity(myIntent);
												}
											})
							.setNegativeButton(R.string.geo_source_alert_negative,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog,
																int which) {
													dialog.cancel();
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
		} else {
			// after first time, show only toast if no location sources
			// available
			if (!isWifiEnabled && !isGPSEnabled) {
				CommonUtils.toast(context, context.getString(R.string.geo_source_alert_both));
			}
		}
	}

	/**
	 * Return best known current location
	 * 
	 * @param context
	 * @return best known current location from any provider, and null if
	 *         absolutely nothing is found
	 */
	public static Location getCurrentLocation(Context context) {
		if (context == null) {
			return null;
		}

		// use internally stored location if available
		Location l = GeoUtils.getLastKnownLocation(context);
		if (l != null) {
			return GeoUtils.getLastKnownLocation(context);
		}

		// use fine accuracy
		l = GeoUtils.getCurrentLocation(context, Criteria.ACCURACY_FINE);
		if (l != null) {
			return l;
		}

		// use coarse accuracy
		l = GeoUtils.getCurrentLocation(context, Criteria.ACCURACY_COARSE);
		if (l != null) {
			return l;
		}

		// Cycle through all providers
		try {
			LocationManager lm = (LocationManager) context
							.getSystemService(Context.LOCATION_SERVICE);
			List<String> providers = lm.getAllProviders();
			int num_providers = providers.size();

			for (int i = num_providers - 1; i >= 0; i--) {
				l = lm.getLastKnownLocation(providers.get(i));
				if (l != null) {
					return l;
				}
			}
		} catch (Exception e) {
			log.e(LOG_TAG, e.getMessage());
			return null;
		}

		return null;
	}

	/**
	 * Get current location with given accuracy
	 * 
	 * @param context
	 *            : application context
	 * @param accuracy
	 *            : accuracy
	 * @return Current location, null if error or not found
	 */
	private static Location getCurrentLocation(Context context, int accuracy) {
		if (context == null) {
			return null;
		}

		// Get location from best provider matching accuracy requirement
		try {
			LocationManager lm = (LocationManager) context
							.getSystemService(Context.LOCATION_SERVICE);
			Criteria criteria = new Criteria();
			criteria.setAccuracy(accuracy);
			return lm.getLastKnownLocation(lm.getBestProvider(criteria, true));
		} catch (Exception e) {
			log.e(LOG_TAG, "Could not receive the current location");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get current country code
	 */
	public static String getCountryCode(Context context) {
		// 1. from SIM
		TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
		String countryCode = tm.getSimCountryIso();

		// 2. from network (doesn't work for CDMA networks)
		if ((countryCode == null || countryCode.isEmpty())
						&& tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
			countryCode = tm.getNetworkCountryIso();

		}

		// 3. from default locale
		if (countryCode == null || countryCode.isEmpty()) {
			countryCode = context.getResources().getConfiguration().locale.getCountry();
		}

		// 4. from geolocation
		if (countryCode == null || countryCode.isEmpty()) {
			try {
				LocationManager lm = (LocationManager) context
								.getSystemService(Context.LOCATION_SERVICE);
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				Location location = lm.getLastKnownLocation(lm.getBestProvider(criteria, true));
				if (location != null) {
					Geocoder gc = new Geocoder(context);
					List<Address> addresses = gc.getFromLocation(location.getLatitude(),
									location.getLongitude(), 1);
					if (addresses.size() > 0) {
						countryCode = addresses.get(0).getCountryCode();
					}
				}
			} catch (Exception e) {
			}
		}

		// 5. set US as default
		if (countryCode == null || countryCode.isEmpty()) {
			countryCode = "US";
		}

		return countryCode;
	}

	private static class PhonarGlobalLocationListener implements LocationListener {

		public Context mContext;

		public PhonarGlobalLocationListener(Context context) {
			this.mContext = context;
		}

		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				GeoUtils.onLocation(mContext, location);
			}
			;
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	/*
	 * Other activities can use such a listener to register for updates
	 */
	public interface UserLocationListener {

		public void onUserLocation(Location location);

	}

}
