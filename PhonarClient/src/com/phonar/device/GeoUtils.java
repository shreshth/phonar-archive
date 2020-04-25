package com.phonar.device;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GeoUtils {

    private static final String LOG_TAG = "GeoUtils";

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
    };

    private static PhonarGlobalLocationListener mGlobalListener;
    private static Location mLastKnownLocation = null;
    private static List<PhonarLocationListener> listeners = new ArrayList<PhonarLocationListener>();

    /*
     * Start reading location updates
     */
    private static void startLocationUpdates(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        if (mGlobalListener == null) {
            mGlobalListener = new PhonarGlobalLocationListener(context);
        }
        try {
            lm.requestLocationUpdates(lm.getBestProvider(criteria, true), 5000L, 0, mGlobalListener);

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
        mLastKnownLocation = location;
        for (PhonarLocationListener listener : listeners) {
            listener.onLocation(GeoUtils.getLastKnownLocation(context));
        }
    }

    /*
     * Register for location updates
     */
    public static void registerListener(Context context, PhonarLocationListener listener) {
        if (listeners.size() == 0) {
            GeoUtils.startLocationUpdates(context);
        }
        listeners.add(listener);
    }

    /*
     * Remove listener
     */
    public static void removeListener(Context context, PhonarLocationListener listener) {
        listeners.remove(listener);
        if (listeners.size() == 0) {
            GeoUtils.stopLocationUpdates(context);
        }
    }

    /*
     * Get last known location
     */
    public static Location getLastKnownLocation(Context context) {
        return mLastKnownLocation;
    }

    /*
     * Other activities can use such a listener to register for updates
     */
    public interface PhonarLocationListener {

        public void onLocation(Location location);

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
            Log.e(LOG_TAG, "The passed activity was null");
            return null;
        }

        // use fine accuracy
        Location l = GeoUtils.getCurrentLocation(context, Criteria.ACCURACY_FINE);
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
            Log.e(LOG_TAG, e.getMessage());
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
    public static Location getCurrentLocation(Context context, int accuracy) {
        if (context == null) {
            Log.e(LOG_TAG, "The passed activity was null");
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
            Log.e(LOG_TAG, "Could not receive the current location");
            e.printStackTrace();
            return null;
        }
    }
}
