package com.phonar.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class NetworkUtils {
    /**
     * is WiFi connected?
     */
    public static boolean isConnectedWifi(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ni == null ? false : ni.isConnected();
    }

    /**
     * is WiFi enabled?
     */
    public static boolean isEnabledWifi(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wm == null ? false : wm.isWifiEnabled();
    }

    /**
     * is mobile data connected?
     */
    public static boolean isConnectedData(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ni == null ? false : ni.isConnected();
    }

    /**
     * Is mobile data enabled?
     */
    public static boolean isEnabledData(Context context) {
        TelephonyManager tm =
            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm == null ? false : (tm.getDataState() != TelephonyManager.DATA_DISCONNECTED);
    }

    /**
     * stop WiFi, if enabled
     */
    public static void disableWifi(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wm.isWifiEnabled()) {
            wm.setWifiEnabled(false);
        }
    }

    /**
     * start WiFi, if disabled
     */
    public static void enableWifi(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
        }
    }
}
