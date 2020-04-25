package com.phonar.request;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phonar.DeviceLocationListener;
import com.phonar.DevicesList;
import com.phonar.PhonarActivity;
import com.phonar.R;
import com.phonar.db.PhonarDatabase;
import com.phonar.models.Device;
import com.phonar.network.NetworkRequest;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.GeoUtils.UserLocationListener;
import com.phonar.utils.PhonarPreferencesManager;

/**
 * 
 * activity for track tab
 * 
 */
public class RequestActivity extends PhonarActivity implements DeviceLocationListener,
				UserLocationListener {
	private ListView mListView;
	public DevicesListAdapter mAdapter;

	public static int QR_INTENT_CODE = 1;

	public static RequestActivity mRequestActivity = null;

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
		setContentView(R.layout.track);

		// Create adapter
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new DevicesListAdapter(this, mListView);
		mListView.setAdapter(mAdapter);

		// programmatically set lists to be above button at bottom
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListView
						.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, R.id.request_title_view);
		params.addRule(RelativeLayout.ABOVE, R.id.add_button);
		mListView.setLayoutParams(params);

		// set lists to not scroll
		mListView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

		// Set long click listener for devices list
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(100);

				final Device device = DevicesList.getAll(RequestActivity.this).get(i);
				if (device.isFriend()) {
					RequestActivity.this.showOptionsDialogFriend(device);
				} else {
					RequestActivity.this.showOptionsDialogDevice(device);
				}
				return true;
			}

		});
		// Set click listener for devices list
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
				final Device device = DevicesList.getAll(RequestActivity.this).get(i);
				if (device.isFriend()) {
					RequestActivity.this.showOptionsDialogFriend(device);
				} else {
					RequestActivity.this.showOptionsDialogDevice(device);
				}
			}

		});

		// set click listener for adding device/friend
		View addButton = this.findViewById(R.id.add_button);
		OnClickListener addClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PhonarPreferencesManager.isFriendsMode(RequestActivity.this)) {
					// Contact picker
					Intent intent = new Intent(RequestActivity.this, ContactPickerActivity.class);
					RequestActivity.this.startActivity(intent);
				} else {
					// Start AddOptionsActivity
					Intent intent = new Intent(RequestActivity.this, AddOptionsActivity.class);
					RequestActivity.this.startActivity(intent);
				}
			}
		};
		addButton.setOnClickListener(addClick);

		// Big add button
		View bigAddButton = this.findViewById(R.id.add_button_big);
		OnClickListener bigAddClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (PhonarPreferencesManager.isFriendsMode(RequestActivity.this)) {
					// Contact picker
					Intent intent = new Intent(RequestActivity.this, ContactPickerActivity.class);
					RequestActivity.this.startActivity(intent);
				} else {
					// Start AddOptionsActivity
					Intent intent = new Intent(RequestActivity.this, AddOptionsActivity.class);
					RequestActivity.this.startActivity(intent);
				}
			}
		};
		bigAddButton.setOnClickListener(bigAddClick);
		findViewById(R.id.add_button_big_image).setOnClickListener(bigAddClick);

		// Home button
		ImageView homeButton = (ImageView) this.findViewById(R.id.request_home);
		homeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				RequestActivity.this.finish();
			}

		});

		// Title
		if (PhonarPreferencesManager.isFriendsMode(this)) {
			((TextView) this.findViewById(R.id.request_title_text))
							.setText(R.string.request_title_contact);
		} else {
			((TextView) this.findViewById(R.id.request_title_text))
							.setText(R.string.request_title_device);
		}
	}

	private void showOptionsDialogDevice(final Device device) {
		final CharSequence[] items = {
						RequestActivity.this.getString(R.string.request_find_device),
						RequestActivity.this.getString(R.string.request_edit_device),
						RequestActivity.this.getString(R.string.request_remove_device) };
		AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
		String title = device.displayName;
		if (device.battery != null) {
			title = title + " (" + RequestActivity.this.getString(R.string.battery) + " "
							+ device.battery + "%)";
		}
		builder.setTitle(title);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					// Locate Once button pressed
					device.sendNetworkRequestFindDevice(RequestActivity.this);
				} else if (item == 1) {
					// Edit Device button pressed
					Intent i = new Intent(RequestActivity.this, EditDeviceActivity.class);
					i.putExtra(EditDeviceActivity.EXTRA_NEW, false);
					i.putExtra(EditDeviceActivity.EXTRA_NAME, device.displayName);
					i.putExtra(EditDeviceActivity.EXTRA_IMAGE, device.image);
					i.putExtra(EditDeviceActivity.EXTRA_PHONE, device.phoneNumber);
					i.putExtra(EditDeviceActivity.EXTRA_PASSWORD, device.password);
					i.putExtra(EditDeviceActivity.EXTRA_TYPE, device.type);
					i.putExtra(EditDeviceActivity.EXTRA_INTERVAL, device.interval);
					i.putExtra(EditDeviceActivity.EXTRA_POLLS, device.polls);
					RequestActivity.this.startActivity(i);
				} else if (item == 2) {
					// Remove device button pressed
					DevicesList.stopTrackingDevice(RequestActivity.this, device.phoneNumber, true);
					PhonarDatabase.remove(RequestActivity.this, device.phoneNumber);
					DevicesList.remove(RequestActivity.this, device.phoneNumber);
					RequestActivity.refreshList();
					if (DevicesList.getAll(RequestActivity.this).isEmpty()) {
						// show big button
						findViewById(R.id.add_button_big).setVisibility(View.VISIBLE);
						if (PhonarPreferencesManager.isFriendsMode(RequestActivity.this)) {
							((TextView) findViewById(R.id.add_button_big_text))
											.setText(R.string.request_add_contact);
						} else {
							((TextView) findViewById(R.id.add_button_big_text))
											.setText(R.string.request_add_device);
						}

						// hide add button
						findViewById(R.id.add_button).setVisibility(View.GONE);
					}
				}
			}
		});
		CommonUtils.showDialog(builder);

	}

	private void showOptionsDialogFriend(final Device friend) {
		CharSequence[] items;
		CharSequence[] items_selector = {
						RequestActivity.this.getString(R.string.request_find_device),
						RequestActivity.this.getString(R.string.text_msg),
						RequestActivity.this.getString(R.string.call),
						RequestActivity.this.getString(R.string.request_remove_device) };
		items = items_selector;

		AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					// refresh location button pressed
					friend.sendNetworkRequestFindFriend(RequestActivity.this);
				} else if (item == 1) {
					// text button pressed
					Intent msgIntent = new Intent(Intent.ACTION_VIEW);
					msgIntent.setData(Uri.parse("sms:" + friend.phoneNumber));
					RequestActivity.this.startActivity(msgIntent);
				} else if (item == 2) {
					// call button pressed
					Intent callIntent = new Intent(Intent.ACTION_DIAL);
					callIntent.setData(Uri.parse("tel:" + friend.phoneNumber));
					RequestActivity.this.startActivity(callIntent);
				} else if (item == 3) {
					// remove friend button pressed
					PhonarDatabase.remove(RequestActivity.this, friend.phoneNumber);
					DevicesList.remove(RequestActivity.this, friend.phoneNumber);
					RequestActivity.refreshList();
					if (DevicesList.getAll(RequestActivity.this).isEmpty()) {

						// show big button
						findViewById(R.id.add_button_big).setVisibility(View.VISIBLE);
						if (PhonarPreferencesManager.isFriendsMode(RequestActivity.this)) {
							((TextView) findViewById(R.id.add_button_big_text))
											.setText(R.string.request_add_contact);
						} else {
							((TextView) findViewById(R.id.add_button_big_text))
											.setText(R.string.request_add_device);
						}

						// hide add button
						findViewById(R.id.add_button).setVisibility(View.GONE);
					}
				}
				RequestActivity.refreshList();
			}
		});
		CommonUtils.showDialog(builder);
	}

	/**
	 * When ZXing returns with QR code result, or contact picker with contact,
	 * handle it
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		/*
		 * returning from ZXing
		 */
		if (requestCode == QR_INTENT_CODE) {
			if (resultCode == RESULT_OK) {
				RequestActivity.onQRScanned(this, intent);
			}
		}
	}

	public static void onQRScanned(Context context, Intent intent) {
		String url = intent.getStringExtra("SCAN_RESULT");
		// url is of the form http://phonar.me/add.php?XXXXXXXXXX?XXXX
		String[] params = url.split("\\?");
		String phoneNumber;
		String password;
		String type;
		if (params.length == 4
						&& (params[0].equals(NetworkRequest.URL_ADDDEVICE_PROD) || params[0]
										.equals(NetworkRequest.URL_ADDDEVICE_DEV))) {
			phoneNumber = params[1];
			password = params[2];
			type = params[2];
			Intent i = new Intent(context, EditDeviceActivity.class);
			i.putExtra(EditDeviceActivity.EXTRA_NEW, true);
			i.putExtra(EditDeviceActivity.EXTRA_PHONE, phoneNumber);
			i.putExtra(EditDeviceActivity.EXTRA_PASSWORD, password);
			i.putExtra(EditDeviceActivity.EXTRA_TYPE, type);
			context.startActivity(i);
		} else {
			CommonUtils.toast(context, R.string.invalid_qrcode);
		}
	}

	/**
	 * Called when activity is first created and every time it resumes after
	 * pause
	 */
	@Override
	public void onResume() {
		super.onResume();

		mRequestActivity = this;

		RequestActivity.refreshList();
		DevicesList.registerListener(this);

		// Setup device stuff
		if (DevicesList.getAll(this).isEmpty()) {
			// show big button
			findViewById(R.id.add_button_big).setVisibility(View.VISIBLE);
			if (PhonarPreferencesManager.isFriendsMode(RequestActivity.this)) {
				((TextView) findViewById(R.id.add_button_big_text))
								.setText(R.string.request_add_contact);
			} else {
				((TextView) findViewById(R.id.add_button_big_text))
								.setText(R.string.request_add_device);
			}

			// hide add button
			findViewById(R.id.add_button).setVisibility(View.GONE);
		} else {
			// hide big button
			findViewById(R.id.add_button_big).setVisibility(View.GONE);

			// show add button
			findViewById(R.id.add_button).setVisibility(View.VISIBLE);
			if (PhonarPreferencesManager.isFriendsMode(RequestActivity.this)) {
				((TextView) findViewById(R.id.add_button_text))
								.setText(R.string.request_add_contact);
			} else {
				((TextView) findViewById(R.id.add_button_text))
								.setText(R.string.request_add_device);
			}

		}
		findViewById(R.id.list).setVisibility(View.VISIBLE);
	}

	/**
	 * Whenever activity is paused or exited
	 */
	@Override
	protected void onPause() {
		super.onPause();
		DevicesList.removeListener(this);
	}

	@Override
	public void onDeviceLocation() {
		RequestActivity.refreshList();
	}

	@Override
	public void onUserLocation(Location location) {
		DevicesList.onUserLocation(this);
		if (this.mAdapter != null) {
			refreshList();
		}
	}

	public static void refreshList() {
		if (mRequestActivity != null && mRequestActivity.mAdapter != null) {
			mRequestActivity.mAdapter.notifyDataSetChanged();
		}
	}

	public static void showReviewDialogIfNotShownBefore() {
		// Show review dialog if user has seen a location response and makes a
		// second location request
		if (mRequestActivity != null
						&& PhonarPreferencesManager.getFirstResponseSeen(mRequestActivity)
						&& !PhonarPreferencesManager.getReviewDialogSeen(mRequestActivity)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mRequestActivity);
			builder.setTitle(R.string.review_title);
			builder.setMessage(R.string.review_message);
			builder.setPositiveButton(R.string.review_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse("market://details?id=com.phonar"));
					mRequestActivity.startActivity(intent);
					PhonarPreferencesManager.setReviewDialogSeen(mRequestActivity, true);
				}
			});
			builder.setNegativeButton(R.string.review_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									PhonarPreferencesManager.setReviewDialogSeen(mRequestActivity,
													true);
								}
							});
			CommonUtils.showDialog(builder);
		}
	}
}
