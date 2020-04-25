package com.phonar.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import com.phonar.models.Device;
import com.phonar.utils.ImageUtils;

/**
 * 
 * Helps with storing, editing, deleting devices from database
 * 
 */
public class PhonarDatabase extends SQLiteOpenHelper {

    private static SQLiteDatabase db = null;

    // Database Version (added lookup key for friends)
    private static final int DATABASE_VERSION = 20;

    // Database Name
    private static final String DATABASE_NAME = "phonar";

    // Devices table name
    private static final String TABLE_NAME = "devices";

    // Columns names
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_TYPE = "type";
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_LATITUDE = "lat";
    private static final String KEY_LONGITUDE = "lng";
    private static final String KEY_TIME = "time";
    private static final String KEY_INTERVAL = "interval";
    private static final String KEY_POLLS = "polls";
    private static final String KEY_BATTERY = "battery";
    private static final String KEY_TRUSTED = "trusted";
    private static final String KEY_LOOKUP_KEY = "lookup_key";

    public PhonarDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DEVICES_TABLE =
            "CREATE TABLE "
                + TABLE_NAME + "(" + KEY_PHONE_NUMBER + " TEXT PRIMARY KEY," + KEY_PASSWORD
                + " TEXT," + KEY_TYPE + " TEXT," + KEY_NAME + " TEXT," + KEY_IMAGE + " TEXT,"
                + KEY_LATITUDE + " TEXT," + KEY_LONGITUDE + " TEXT," + KEY_TIME + " TEXT,"
                + KEY_INTERVAL + " INTEGER," + KEY_POLLS + " INTEGER," + KEY_BATTERY + " TEXT,"
                + KEY_TRUSTED + " TEXT," + KEY_LOOKUP_KEY + " TEXT" + ")";

        db.execSQL(CREATE_DEVICES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    // Update the friend's location
    private void updateLocationFriend(
        String phoneNumber, Double latitude, Double longitude, Long timeOfLastLocation) {
        if (db == null) {
            db = this.getWritableDatabase();
        }

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put(KEY_TIME, timeOfLastLocation);

        db.update(TABLE_NAME, values, KEY_PHONE_NUMBER + "= ?", new String[] { phoneNumber });

        if (db != null) {
            db.close();
            db = null;
        }
    }

    // Adding new device
    private void add(Device device) {
        if (db == null) {
            db = this.getWritableDatabase();
        }

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE_NUMBER, device.phoneNumber);
        if (device.password == null) {
            values.put(KEY_PASSWORD, "null");
        } else {
            values.put(KEY_PASSWORD, device.password);
        }
        values.put(KEY_TYPE, device.type);
        values.put(KEY_NAME, device.displayName);
        values.put(KEY_IMAGE, ImageUtils.encodeBitmap(device.image));

        // update only above values (name, image, type, pw) if already exists
        int numRows = 0;
        numRows =
            db.update(
                TABLE_NAME, values, KEY_PHONE_NUMBER + "= ?", new String[] { device.phoneNumber });

        // if not updated
        if (numRows == 0) {
            if (device.latitude == null) {
                values.put(KEY_LATITUDE, "null");
            } else {
                values.put(KEY_LATITUDE, device.latitude.toString());
            }
            if (device.longitude == null) {
                values.put(KEY_LONGITUDE, "null");
            } else {
                values.put(KEY_LONGITUDE, device.longitude.toString());
            }
            if (device.timeOfLastLocation == null) {
                values.put(KEY_TIME, "null");
            } else {
                values.put(KEY_TIME, device.timeOfLastLocation.toString());
            }
            if (device.interval == null) {
                values.put(KEY_INTERVAL, "null");
            } else {
                values.put(KEY_INTERVAL, device.interval.toString());
            }
            if (device.polls == null) {
                values.put(KEY_POLLS, "null");
            } else {
                values.put(KEY_POLLS, device.polls.toString());
            }
            if (device.battery == null) {
                values.put(KEY_BATTERY, "null");
            } else {
                values.put(KEY_BATTERY, device.battery.toString());
            }
            if (device.trustedFriend == null) {
                values.put(KEY_TRUSTED, "null");
            } else {
                values.put(KEY_TRUSTED, device.trustedFriend.toString());
            }
            if (device.lookupKey == null) {
                values.put(KEY_LOOKUP_KEY, "null");
            } else {
                values.put(KEY_LOOKUP_KEY, device.lookupKey.toString());
            }

            // if a friend, try to update existing person (according to lookup
            // key)
            if (device.isFriend() && device.lookupKey != null) {
                numRows =
                    db.update(
                        TABLE_NAME, values, KEY_LOOKUP_KEY + "= ?",
                        new String[] { device.lookupKey });
            }
            if (numRows == 0) { // if still nothing affected, then create
                db.insert(TABLE_NAME, null, values);
            }
        }

        if (db != null) {
            db.close();
            db = null;
        }
    }

    // Update location info for a device
    private void updateLocationDevice(
        String phoneNumber, Double latitude, Double longitude, Long timeOfLastLocation,
        Integer battery) {
        if (db == null) {
            db = this.getWritableDatabase();
        }

        ContentValues values = new ContentValues();
        if (latitude == null) {
            values.put(KEY_LATITUDE, "null");
        } else {
            values.put(KEY_LATITUDE, latitude.toString());
        }
        if (longitude == null) {
            values.put(KEY_LONGITUDE, "null");
        } else {
            values.put(KEY_LONGITUDE, longitude.toString());
        }
        if (timeOfLastLocation == null) {
            values.put(KEY_TIME, "null");
        } else {
            values.put(KEY_TIME, timeOfLastLocation.toString());
        }
        if (battery == null) {
            values.put(KEY_BATTERY, "null");
        } else {
            values.put(KEY_BATTERY, battery.toString());
        }
        db.update(TABLE_NAME, values, KEY_PHONE_NUMBER + "= ?", new String[] { phoneNumber });

        if (db != null) {
            db.close();
            db = null;
        }
    }

    // Update name and image for a device
    private void updateDeviceInfo(String phoneNumber, String name, Bitmap image) {
        if (db == null) {
            db = this.getWritableDatabase();
        }

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_IMAGE, ImageUtils.encodeBitmap(image));
        db.update(TABLE_NAME, values, KEY_PHONE_NUMBER + "= ?", new String[] { phoneNumber });

        if (db != null) {
            db.close();
            db = null;
        }
    }

    // Update whether a friend is trusted
    private void updateFriendTrusted(String phoneNumber, Boolean trusted) {
        if (db == null) {
            db = this.getWritableDatabase();
        }

        ContentValues values = new ContentValues();
        values.put(KEY_TRUSTED, trusted.toString());
        db.update(TABLE_NAME, values, KEY_PHONE_NUMBER + "= ?", new String[] { phoneNumber });

        if (db != null) {
            db.close();
            db = null;
        }
    }

    // Delete device
    private void delete(String phoneNumber) {
        if (db == null) {
            db = this.getWritableDatabase();
        }

        db.delete(TABLE_NAME, KEY_PHONE_NUMBER + "= ?", new String[] { phoneNumber });

        if (db != null) {
            db.close();
            db = null;
        }
    }

    private ArrayList<Device> getAllDevices(Context context) {
        ArrayList<Device> list = new ArrayList<Device>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String phoneNumber = cursor.getString(0);
                String password = cursor.getString(1);
                password = password.equals("null") ? null : password;
                String type = cursor.getString(2);
                String displayName = cursor.getString(3);
                String image = cursor.getString(4);
                String lat = cursor.getString(5);
                String lng = cursor.getString(6);
                String time = cursor.getString(7);
                Double latitude = lat.equals("null") ? null : Double.parseDouble(lat);
                Double longitude = lng.equals("null") ? null : Double.parseDouble(lng);
                Long timeOfLastLocation = time.equals("null") ? null : Long.parseLong(time);
                String intervalString = cursor.getString(8);
                Long interval =
                    intervalString.equals("null") ? null : Long.parseLong(intervalString);
                String pollsString = cursor.getString(9);
                Long polls = pollsString.equals("null") ? null : Long.parseLong(pollsString);
                String bat = cursor.getString(10);
                Integer battery = bat.equals("null") ? null : Integer.parseInt(bat);
                String trusted = cursor.getString(11);
                Boolean trustedFriend =
                    trusted.equals("null") ? null : Boolean.parseBoolean(trusted);
                String lookupKey = cursor.getString(12);
                lookupKey = lookupKey.equals("null") ? null : lookupKey;
                Device device =
                    new Device(
                        context, phoneNumber, password, type, displayName, ImageUtils
                            .decodeBitmap(image), latitude, longitude, timeOfLastLocation,
                        interval, polls, battery, trustedFriend, lookupKey);
                list.add(device);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection

        return list;
    }

    // ------- Synchronized access to whole class -----------------------

    public synchronized static void updateLocationFriend(
        Context context, String phoneNumber, Double latitude, Double longitude,
        Long timeOfLastLocation) {
        PhonarDatabase db = new PhonarDatabase(context);
        db.updateLocationFriend(phoneNumber, latitude, longitude, timeOfLastLocation);
    }

    public synchronized static void add(Context context, Device device) {
        PhonarDatabase db = new PhonarDatabase(context);
        db.add(device);
    }

    public synchronized static ArrayList<Device> getAll(Context context) {
        PhonarDatabase db = new PhonarDatabase(context);
        return db.getAllDevices(context);
    }

    public synchronized static void updateLocationDevice(
        Context context, String phoneNumber, Double latitude, Double longitude,
        Long timeOfLastLocation, Integer battery) {
        PhonarDatabase db = new PhonarDatabase(context);
        db.updateLocationDevice(phoneNumber, latitude, longitude, timeOfLastLocation, battery);
    }

    public synchronized static void remove(Context context, String phoneNumber) {
        PhonarDatabase db = new PhonarDatabase(context);
        db.delete(phoneNumber);
    }

    public synchronized static void updateDeviceInfo(
        Context context, String phoneNumber, String name, Bitmap image) {
        PhonarDatabase db = new PhonarDatabase(context);
        db.updateDeviceInfo(phoneNumber, name, image);
    }

    public synchronized static void updateFriendTrusted(
        Context context, String phoneNumber, boolean isTrusted) {
        PhonarDatabase db = new PhonarDatabase(context);
        db.updateFriendTrusted(phoneNumber, isTrusted);
    }
}