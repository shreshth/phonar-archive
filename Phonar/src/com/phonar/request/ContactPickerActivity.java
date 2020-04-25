package com.phonar.request;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.phonar.ContactsList;
import com.phonar.DevicesList;
import com.phonar.PhonarActivity;
import com.phonar.PhonarTabActivity;
import com.phonar.R;
import com.phonar.db.PhonarDatabase;
import com.phonar.models.Contact;
import com.phonar.models.Device;
import com.phonar.utils.CommonUtils;

public class ContactPickerActivity extends PhonarActivity {

	// stuffs
	private ListView mFriendsListView;
	public ContactsListAdapter mFriendsAdapter;
	public static ContactPickerActivity mContactPickerActivity = null;

	// search box
	public static String mSearchString = "";
	public static EditText mSearchEditText;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_picker);

		// Create adapter
		mFriendsListView = (ListView) findViewById(R.id.contacts_list);
		mFriendsAdapter = new ContactsListAdapter(this, mFriendsListView);
		mFriendsListView.setAdapter(mFriendsAdapter);

		// programmatically set lists to be above button at bottom
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFriendsListView
						.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, R.id.contact_picker_title_view);
		params.addRule(RelativeLayout.ABOVE, R.id.friends_list_bottom);
		mFriendsListView.setLayoutParams(params);

		// set lists to not scroll
		mFriendsListView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

		// Set the on click listener for friends
		mFriendsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
				if (mContactPickerActivity == null) {
					return;
				}

				String str = mFriendsAdapter.map.get(i);
				Contact contact = ContactsList.getFriend(mContactPickerActivity, str);
				if (contact != null) {
					if (contact.phoneNumbers.size() == 1) {
						for (String phoneNumber : contact.phoneNumbers.keySet()) {
							Device device = new Device(mContactPickerActivity, phoneNumber,
											contact.name, contact.image, null, null, null, false,
											contact.lookupKey);
							PhonarDatabase.add(mContactPickerActivity, device);
							DevicesList.add(mContactPickerActivity, device);
							if (PhonarTabActivity.mPhonarTabActivity == null) {
								device.sendNetworkRequestFindFriend(ContactPickerActivity.this);
							} else {
								device.sendNetworkRequestFindFriend(PhonarTabActivity.mPhonarTabActivity);
							}
							ContactPickerActivity.this.finish();
							break;
						}
					} else {
						CharSequence[] items = new CharSequence[contact.phoneNumbers.size()];
						final String[] phoneNumbers = new String[contact.phoneNumbers.size()];
						final Contact contact_final = contact;

						int j = 0;
						for (String phoneNumber : contact.phoneNumbers.keySet()) {
							String label = contact.phoneNumbers.get(phoneNumber);
							items[j] = label + " (" + phoneNumber + ")";
							phoneNumbers[j] = phoneNumber;
							j++;
						}

						AlertDialog.Builder builder = new AlertDialog.Builder(
										mContactPickerActivity);
						builder.setTitle(R.string.select_contact_phone_number);
						builder.setItems(items, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int item) {
								Device device = new Device(mContactPickerActivity,
												phoneNumbers[item], contact_final.name,
												contact_final.image, null, null, null, false,
												contact_final.lookupKey);
								PhonarDatabase.add(mContactPickerActivity, device);
								DevicesList.add(mContactPickerActivity, device);
								ContactPickerActivity.this.finish();
								if (PhonarTabActivity.mPhonarTabActivity == null) {
									device.sendNetworkRequestFindFriend(ContactPickerActivity.this);
								} else {
									device.sendNetworkRequestFindFriend(PhonarTabActivity.mPhonarTabActivity);
								}
							}
						});

						CommonUtils.showDialog(builder);
					}

				}
			}
		});

		// Setup listener for search text
		mSearchEditText = (EditText) findViewById(R.id.search_friends);
		mSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				ContactPickerActivity.mSearchString = mSearchEditText.getText().toString();
				refreshFriendsList();
			}
		});

		// Refresh contact list button
		findViewById(R.id.refresh_contact_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(100);

				if (mContactPickerActivity != null) {
					ContactsList.refreshAll(mContactPickerActivity);
				}
			}
		});

		// Home button
		ImageView homeButton = (ImageView) this.findViewById(R.id.contact_picker_home);
		homeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ContactPickerActivity.this.finish();
				if (RequestActivity.mRequestActivity != null) {
					RequestActivity.mRequestActivity.finish();
				}
			}

		});
	}

	/**
	 * Called when activity is first created and every time it resumes after
	 * pause
	 */
	@Override
	public void onResume() {
		super.onResume();
		mContactPickerActivity = this;
		refreshFriendsList();
	}

	/**
	 * Whenever activity is paused or exited
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// hide soft keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
		mSearchEditText.setText("");
		mSearchString = "";

		mContactPickerActivity = null;
	}

	@Override
	public boolean onSearchRequested() {
		mSearchEditText.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);
		return true;
	}

	public static void refreshFriendsList() {
		if (mContactPickerActivity != null && mContactPickerActivity.mFriendsAdapter != null) {
			mContactPickerActivity.mFriendsAdapter.notifyDataSetChanged();
		}
	}
}
