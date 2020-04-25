package com.phonar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.phonar.ContactsList;
import com.phonar.R;
import com.phonar.models.Contact;
import com.phonar.request.ContactPickerActivity;

public class ContactsUtils {

	private static final int LOAD_BATCH = 10;
	private static final int MAX_CONTACT_IMAGE_SIZE = 2048;

	/**
	 * format a phone number
	 * 
	 * @param phone
	 * @return Formatted phone number, or null if invalid number
	 */
	public static String formatPhoneNumberForStorage(Context context, String phone) {
		try {
			String locale = PhonarPreferencesManager.getLocale(context);
			if (locale == "" || locale.isEmpty()) {
				return null;
			}
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			PhoneNumber phoneNumber = phoneUtil.parse(phone, locale);
			String formattedPhone = phoneUtil.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL);
			formattedPhone = formattedPhone.replace(" ", "").replace("-", "").replace("(", "")
							.replace(")", "");
			if (phoneUtil.isValidNumber(phoneUtil.parse(formattedPhone, locale))) {
				return formattedPhone;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * format a phone number
	 * 
	 * @param phone
	 * @return Formatted phone number, or null if invalid number
	 */
	public static String formatPhoneNumberForDisplay(String phone) {
		return PhoneNumberUtils.formatNumber(phone);
	}

	/**
	 * Get contact name by phone number
	 * 
	 * @param phone
	 *            Number of contact
	 * @return Name of contact
	 */
	public static String getNameByPhoneNumber(String phoneNumber, ContentResolver resolver) {
		Cursor cursor = null;
		try {
			String name = "";
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
			cursor = resolver.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null,
							null);
			if (cursor.moveToFirst()) {
				name = cursor.getString(cursor
								.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			}
			cursor.close();
			cursor = null;
			if (name == null || name.isEmpty()) {
				return phoneNumber;
			}
			return name;
		} catch (Exception e) {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			return phoneNumber;
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

	}

	/**
	 * Get contact (non-Facebook) photo by contact data cursor
	 * 
	 * @param context
	 * @param c
	 *            Cursor with contact info
	 * @param resolver
	 * @return Bitmap user image
	 */
	private static Bitmap getContactPhoto(Context context, Cursor c, ContentResolver resolver) {

		long photo_id = c.getLong(c.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));

		// method 1
		try {
			int colIndex = c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
			if (colIndex != -1) {
				String photoUri = c.getString(colIndex);
				if (photoUri != null) {
					Uri imageUri = Uri.parse(photoUri);
					try {
						return MediaStore.Images.Media.getBitmap(resolver, imageUri);
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
		}

		Cursor c2 = null;
		// method 2
		try {
			Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photo_id);
			c2 = resolver.query(photoUri,
							new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, null,
							null, null);
			byte[] photoBytes = null;

			try {
				if (c2.moveToFirst()) {
					photoBytes = c2.getBlob(0);
				}
				c2.close();
				c2 = null;
			} catch (Exception e2) {
			} finally {
				if (c2 != null) {
					c2.close();
					c2 = null;
				}
			}
			if (photoBytes != null && photoBytes.length < MAX_CONTACT_IMAGE_SIZE) {
				return BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
			}
		} catch (Exception e) {
		} finally {
			if (c2 != null) {
				c2.close();
				c2 = null;
			}
		}

		// default image
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.androidmarker);
	}

	// Get all the friends (all the contacts)
	@SuppressWarnings("deprecation")
	public static CopyOnWriteArrayList<Contact> getAllContacts(Context context) {
		final CopyOnWriteArrayList<Contact> friends = new CopyOnWriteArrayList<Contact>();
		// keep track if which phoneNumbers have been added to remove duplicate
		// contacts
		ArrayList<String> phoneNumbers = new ArrayList<String>();
		Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
						null, null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC");

		while (cursor.moveToNext()) {
			// get the contact name
			String name = cursor.getString(cursor
							.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
			String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			Map<String, String> phoneNumberMap = new HashMap<String, String>();

			// get the phone numbers
			if (Integer.parseInt(cursor.getString(cursor
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) == 0) {
				continue;
			}
			Cursor phoneCursor = context.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
							new String[] { id }, null);
			while (phoneCursor.moveToNext()) {
				String phone = phoneCursor.getString(phoneCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				phone = ContactsUtils.formatPhoneNumberForStorage(context, phone);

				String label = phoneCursor.getString(phoneCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
				if (label == null) {
					int type = phoneCursor.getInt(phoneCursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

					label = context.getString(ContactsContract.CommonDataKinds.Phone
									.getTypeLabelResource(type));
				}
				if (!phoneNumberMap.containsKey(phone) && !phoneNumbers.contains(phone)
								&& !(phone == null) && !phone.isEmpty()) {
					phoneNumberMap.put(phone, label);
				}
			}
			phoneCursor.close();

			// get contact image, android icon if null
			Bitmap image = ImageUtils.compressBitmap(ContactsUtils.getContactPhoto(context, cursor,
							context.getContentResolver()));

			// get contact lookup key
			String lookupKey = cursor.getString(cursor
							.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

			if (name == null || image == null || lookupKey == null || phoneNumberMap.isEmpty()) {
				continue;
			}
			for (String phoneNumber : phoneNumberMap.keySet()) {
				phoneNumbers.add(phoneNumber);
			}

			// add to list
			Contact friend = new Contact(context, lookupKey, phoneNumberMap, name, image);

			friends.add(friend);

			// update friends list after each batch
			if (friends.size() % LOAD_BATCH == 0) {
				ContactsList.mHandler.post(new Runnable() {
					@Override
					public void run() {
						ContactsList.onPartiallyLoading(friends);
						ContactPickerActivity.refreshFriendsList();
					}
				});
			}
		}
		if (cursor != null && !cursor.isClosed()) {
			if (context instanceof Activity) {
				((Activity) context).stopManagingCursor(cursor);
				cursor.close();
			}
		}

		synchronized (ContactsList.class) {
			return friends;
		}
	}
}
