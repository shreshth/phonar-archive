package com.phonar;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.os.Handler;

import com.phonar.models.Contact;
import com.phonar.request.ContactPickerActivity;
import com.phonar.utils.ContactsUtils;

public class ContactsList {

    public static boolean mLoading = false;

    public static Handler mHandler = null;

    // all friends
    private static CopyOnWriteArrayList<Contact> allFriends = new CopyOnWriteArrayList<Contact>();

    // Refresh the list of friends from database
    public synchronized static void refreshAll(final Context context) {
        mLoading = true;
        if (mHandler == null) {
            mHandler = new Handler();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                allFriends = ContactsUtils.getAllContacts(context);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoading = false;
                        ContactPickerActivity.refreshFriendsList();
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    // Update friends List with partially loading contacts
    public synchronized static void onPartiallyLoading(CopyOnWriteArrayList<Contact> friends) {
        allFriends = friends;
    }

    // get a friend by phone number
    public synchronized static Contact getFriend(Context context, String lookupKey) {
        if (allFriends.isEmpty() && !mLoading) {
            refreshAll(context);
        }
        for (Contact friend : allFriends) {
            if (friend.lookupKey.equals(lookupKey)) {
                return friend;
            }
        }
        return null;
    }

    // get all friends and refreshes list if need to
    public synchronized static List<Contact> getAllContacts(Context context) {
        if (allFriends.isEmpty() && !mLoading) {
            refreshAll(context);
        }
        return allFriends;
    }
}
