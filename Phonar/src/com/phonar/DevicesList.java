package com.phonar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.phonar.db.PhonarDatabase;
import com.phonar.models.Device;
import com.phonar.utils.PhonarPreferencesManager;

/**
 * 
 * Contains static functions that handle devices stored in memory. For devices
 * stored in database, look at PhonarDatabase
 * 
 */
public class DevicesList {

    // devices will be polled every so often (in seconds)
    public static final int TRACK_INTERVAL = 60 * 3;

    // minimum battery level for a device
    public static final int MIN_BATTERY_LEVEL = 15;

    // tracking will be turned off automatically after this long (in seconds)
    private static final int AUTO_TURNOFF_TRACKING_INTERVAL = 60 * 60;

    // Number of polls to perform
    public static final int NUM_POLLS = AUTO_TURNOFF_TRACKING_INTERVAL / TRACK_INTERVAL;

    // all devices
    private static ArrayList<Device> allDevices = new ArrayList<Device>();

    // devices that are being tracked
    private static ArrayList<Device> trackedDevices = new ArrayList<Device>();

    // maps from phoneNumber to device object
    private static Map<String, Device> phoneToDeviceMap = new HashMap<String, Device>();

    // list of listeners listening to devices' location updates
    private static List<DeviceLocationListener> listeners = new ArrayList<DeviceLocationListener>();

    // get all devices stored in memory
    public static List<Device> getAll(Context context) {
        return allDevices;
    }

    // Add a device to the list
    public static void add(Context context, Device device) {
        Device original = DevicesList.get(context, device.phoneNumber);
        if (original == null) {
            allDevices.add(device);
            phoneToDeviceMap.put(device.phoneNumber, device);
        } else {
            original.displayName = device.displayName;
            original.image = device.image;
        }
    }

    // Remove a device from the list
    public static void remove(Context context, String phoneNumber) {
        if (DevicesList.get(context, phoneNumber) != null) {
            trackedDevices.remove(phoneToDeviceMap.get(phoneNumber));
            allDevices.remove(phoneToDeviceMap.get(phoneNumber));
            phoneToDeviceMap.remove(phoneNumber);
        }
    }

    // refresh the list of devices from database
    public static void refreshAll(Context context) {
        allDevices = PhonarDatabase.getAll(context);
        // remake the phone to Device map
        phoneToDeviceMap = new HashMap<String, Device>();
        for (Device d : allDevices) {
            phoneToDeviceMap.put(d.phoneNumber, d);
        }
    }

    // get the device object given phoneNumber
    public static Device get(Context context, String phoneNumber) {
        return phoneToDeviceMap.get(phoneNumber);
    }

    // register a listener to listen to devices' location updates
    public static void registerListener(DeviceLocationListener listener) {
        listeners.add(listener);
    }

    // remove listener
    public static void removeListener(DeviceLocationListener listener) {
        listeners.remove(listener);
    }

    // remember that the device is being tracked
    public synchronized static void startTrackingDevice(
        final Context context, final String phoneNumber) {
        Device device = phoneToDeviceMap.get(phoneNumber);
        if (isTracked(phoneNumber) || device == null) {
            return;
        }
        trackedDevices.add(device);
        device.sendNetworkRequestStartTracking(context);
        Handler handler = null;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PhonarTabActivity.mIsRunning) {
                    DevicesList.startTrackingDevice(context, phoneNumber);
                } else {
                    DevicesList.stopTrackingDevice(context, phoneNumber, true);
                }
            }
        }, AUTO_TURNOFF_TRACKING_INTERVAL * 1000);
    }

    // call this when device is no longer being tracked
    public synchronized static void stopTrackingDevice(
        Context context, String phoneNumber, boolean sendNetworkRequest) {
        Device device = phoneToDeviceMap.get(phoneNumber);
        if (device == null) {
            return;
        }
        trackedDevices.remove(device);
        if (sendNetworkRequest) {
            device.sendNetworkRequestStopTracking(context);
        }
    }

    // true if the device is being tracked
    public static boolean isTracked(String phoneNumber) {
        Device device = phoneToDeviceMap.get(phoneNumber);
        if (device == null) {
            return false;
        }
        return trackedDevices.contains(device);
    }

    // this is called when a new location update for a device happens
    // time and battery are needed just for devices and can be null otherwise
    public static void onDeviceLocation(
        Context context, String phoneNumber, Double latitude, Double longitude, Long time,
        Integer battery) {
        // update device in database
        Device device = phoneToDeviceMap.get(phoneNumber);
        if (device == null) {
            return;
        }
        // do not do anything if lat/lng exactly same as before
        if (time != null && device.timeOfLastLocation != null && device.timeOfLastLocation >= time) {
            return;
        }
        // mark that the user has seen at least one response
        if (!PhonarPreferencesManager.getFirstResponseSeen(context)) {
            PhonarPreferencesManager.setFirstResponseSeen(context, true);
        }
        int last_battery = MIN_BATTERY_LEVEL + 1;
        if (device.battery != null) {
            last_battery = device.battery;
        }
        if (device.isFriend()) {
            device.updateLocationFriend(context, latitude, longitude);
            PhonarDatabase.updateLocationFriend(
                context, phoneNumber, latitude, longitude, device.timeOfLastLocation);
        } else {
            device.updateLocationDevice(context, latitude, longitude, battery, time);
            PhonarDatabase.updateLocationDevice(
                context, phoneNumber, latitude, longitude, device.timeOfLastLocation, battery);
        }
        // show low battery notification if necessary
        if (battery != null && battery < MIN_BATTERY_LEVEL && last_battery >= MIN_BATTERY_LEVEL) {
            Intent lowBatteryIntent = new Intent(context, LowBatteryDialogService.class);
            lowBatteryIntent.putExtra(
                LowBatteryDialogService.KEY_LOW_BATTERY_NAME, device.displayName);
            lowBatteryIntent.putExtra(LowBatteryDialogService.KEY_LOW_BATTERY_PHONE, phoneNumber);
            context.startService(lowBatteryIntent);
        }
        // Call listeners
        for (DeviceLocationListener listener : listeners) {
            listener.onDeviceLocation();
        }
    }

    // called when the user's own location is changed
    public static void onUserLocation(Context context) {
        for (Device d : allDevices) {
            d.updateBearingAndDistance(context);
        }
    }

}
