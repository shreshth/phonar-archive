package com.phonar.models;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * 
 * Encapsulates information about a device in memory
 * 
 */
public class Contact {

    public String lookupKey;
    public Map<String, String> phoneNumbers;
    public String name;
    public Bitmap image;

    public Contact(
        Context context, String lookupKey, Map<String, String> phoneNumbers, String name,
        Bitmap image) {
        this.lookupKey = lookupKey;
        this.phoneNumbers = phoneNumbers;
        this.name = name;
        this.image = image;
    }
}