package com.phonar.utils;

import android.util.Log;

public class log {

    public static void d(String tag, String msg) {
        if (msg != null) {
            Log.d(tag, msg);
        } else {
            Log.d(tag, "null");
        }
    }

    public static void d(String tag, Integer i) {
        if (i != null) {
            Log.d(tag, Integer.toString(i));
        } else {
            Log.d(tag, "null");
        }
    }

    public static void d(String tag, Double d) {
        if (d != null) {
            Log.d(tag, Double.toString(d));
        } else {
            Log.d(tag, "null");
        }
    }

    public static void d(String tag, Float f) {
        if (f != null) {
            Log.d(tag, Float.toString(f));
        } else {
            Log.d(tag, "null");
        }
    }

    public static void d(String tag, Boolean b) {
        if (b != null) {
            Log.d(tag, Boolean.toString(b));
        } else {
            Log.d(tag, "null");
        }
    }

    public static void e(String tag, String msg) {
        if (msg != null) {
            Log.e(tag, msg);
        } else {
            Log.e(tag, "null");
        }
    }

    public static void e(String tag, Integer i) {
        if (i != null) {
            Log.e(tag, Integer.toString(i));
        } else {
            Log.e(tag, "null");
        }
    }

    public static void e(String tag, Double d) {
        if (d != null) {
            Log.e(tag, Double.toString(d));
        } else {
            Log.e(tag, "null");
        }
    }

    public static void e(String tag, Float f) {
        if (f != null) {
            Log.e(tag, Float.toString(f));
        } else {
            Log.e(tag, "null");
        }
    }

    public static void e(String tag, Boolean b) {
        if (b != null) {
            Log.e(tag, Boolean.toString(b));
        } else {
            Log.e(tag, "null");
        }
    }

}
