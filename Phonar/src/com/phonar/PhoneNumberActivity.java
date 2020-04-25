package com.phonar;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.phonar.c2dm.C2DMReceiver;
import com.phonar.network.NetworkRequest;
import com.phonar.network.NetworkRequest.NetworkRequestListener;
import com.phonar.network.NetworkRequestDialogActivity;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.ContactsUtils;
import com.phonar.utils.NetworkUtils;
import com.phonar.utils.PhonarPreferencesManager;

public class PhoneNumberActivity extends Activity implements NetworkRequestListener {

	public static String mVerifyingPhone = null;
	public static boolean mIsGoogleVoice;
	private boolean msg_sent = false;;
	private static boolean mDone = false;

	public static PhoneNumberActivity mPhoneNumberActivity;

	// Remember to fill in all these three
	private static final String[] mCountries = { "US", "Canada", "India", "Norway", "UK" };
	private static final int[] mCountriesImages = { R.drawable.us, R.drawable.ca, R.drawable.in,
					R.drawable.no, R.drawable.gb };
	private static final String[] mCountriesLocales = { "us", "ca", "in", "no", "gb" };

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phonenumber);
		mPhoneNumberActivity = this;
		overridePendingTransition(R.anim.splash_fadein, R.anim.do_nothing);

		// begin loading friends list
		ContactsList.refreshAll(this);

		// ensure wifi or 3G is on
		this.startActivityForResult(new Intent(this, NetworkRequestDialogActivity.class), 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mDone) {
			finish();
		}
	}

	// if verification code already sent, only show the verification code dialog
	private void checkPhoneNumberStored() {
		if (msg_sent) {
			setupVerificationScreen();
			return;
		}
		setupPhoneNumberScreen();
	}

	private void sendC2DMRegistrationIntent() {
		// register
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		registrationIntent.putExtra("sender", C2DMReceiver.PROJECT_ID);
		startService(registrationIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if in friends mode, check if phone is registered for c2dm
		if (PhonarPreferencesManager.getC2DMId(this) == null) {
			this.sendC2DMRegistrationIntent();
		}
		this.checkPhoneNumberStored();
	}

	private void setupPhoneNumberScreen() {
		findViewById(R.id.phonenumber_screen).setVisibility(View.VISIBLE);
		findViewById(R.id.verifycode_screen).setVisibility(View.GONE);

		// try to read user's phone number
		String number = "";
		try {
			TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			number = tMgr.getLine1Number();
			number = ContactsUtils.formatPhoneNumberForDisplay(number);
		} catch (Exception e) {
		}
		final TextView phoneNumberEditText = (TextView) findViewById(R.id.phonenumber);
		phoneNumberEditText.setText(number);

		// show google voice warning
		final CheckBox isGoogleVoiceCheckbox = (CheckBox) findViewById(R.id.google_voice);
		final TextView isGoogleVoiceTextView = (TextView) findViewById(R.id.google_voice_text);
		isGoogleVoiceTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isGoogleVoiceCheckbox.toggle();
			}
		});
		isGoogleVoiceCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				if (checked) {
					AlertDialog.Builder builder = new AlertDialog.Builder(PhoneNumberActivity.this);
					builder.setMessage(PhoneNumberActivity.this
									.getString(R.string.google_voice_msg));
					builder.setPositiveButton(PhoneNumberActivity.this.getString(R.string.ok), null);
					builder.setCancelable(true);
					CommonUtils.showDialog(builder);
				}
			}
		});

		// detect locale and set initial stuff
		int index = -1;
		String locale = PhonarPreferencesManager.getLocale(PhoneNumberActivity.this);
		for (int i = 0; i < mCountriesLocales.length; i++) {
			if (locale.compareToIgnoreCase(mCountriesLocales[i]) == 0) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			((ImageView) findViewById(R.id.phonecountryimage))
							.setImageResource(mCountriesImages[index]);
		} else {
			// USA by default
			((ImageView) findViewById(R.id.phonecountryimage)).setImageResource(R.drawable.us);
		}

		// setup listener for country flag list
		findViewById(R.id.phonecountry).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// detect locale
				int index = -1;
				String locale = PhonarPreferencesManager.getLocale(PhoneNumberActivity.this);
				for (int i = 0; i < mCountriesLocales.length; i++) {
					if (locale.compareToIgnoreCase(mCountriesLocales[i]) == 0) {
						index = i;
						break;
					}
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(PhoneNumberActivity.this);
				builder.setTitle(R.string.phonenumber_country_dialog_title);
				builder.setSingleChoiceItems(mCountries, index,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int item) {
										// set flag image
										((ImageView) findViewById(R.id.phonecountryimage))
														.setImageResource(mCountriesImages[item]);

										// set locale
										PhonarPreferencesManager.setLocale(
														PhoneNumberActivity.this,
														mCountriesLocales[item]);

										dialog.dismiss();
									}
								});
				CommonUtils.showDialog(builder);
			}
		});

		// setup listener for main OK button click button
		findViewById(R.id.ok_phonenumer).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mVerifyingPhone = ContactsUtils.formatPhoneNumberForStorage(
								PhoneNumberActivity.this, phoneNumberEditText.getText().toString());
				mIsGoogleVoice = isGoogleVoiceCheckbox.isChecked();
				if (mVerifyingPhone == null) {
					// invalid phone number
					CommonUtils.toast(PhoneNumberActivity.this, R.string.invalid_phone);
					return;
				}
				// check network status
				if (!NetworkUtils.isEnabledData(PhoneNumberActivity.this)
								&& !NetworkUtils.isEnabledWifi(PhoneNumberActivity.this)) {
					CommonUtils.toast(PhoneNumberActivity.this, R.string.turn_on_network_toast);
					return;
				}
				if (!NetworkUtils.isConnectedData(PhoneNumberActivity.this)
								&& !NetworkUtils.isConnectedWifi(PhoneNumberActivity.this)) {
					CommonUtils.toast(PhoneNumberActivity.this, R.string.connect_network_toast);
					return;
				}

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("phone", mVerifyingPhone));
				NetworkRequest request = new NetworkRequest(PhoneNumberActivity.this,
								NetworkRequest.URL_VERIFY_PHONENUMBER, true, params, null);
				request.run();

				// store the fact that verification message was sent
				PhoneNumberActivity.this.msg_sent = true;

				setupVerificationScreen();
			}
		});
	}

	private void setupVerificationScreen() {
		findViewById(R.id.phonenumber_screen).setVisibility(View.GONE);
		findViewById(R.id.verifycode_screen).setVisibility(View.VISIBLE);
		final TextView codeEditText = (TextView) findViewById(R.id.verification_code);
		View resend_button = findViewById(R.id.resend_code);
		View ok_button = findViewById(R.id.ok_code);
		// Setup listener for ok button
		ok_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String code = codeEditText.getText().toString();
				PhoneNumberActivity.this.checkCode(code, false);
			}
		});
		// Setup listener for resend button
		resend_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PhoneNumberActivity.this.setContentView(R.layout.phonenumber);
				((TextView) PhoneNumberActivity.this.findViewById(R.id.phonenumber_screen_title))
								.setText(R.string.phonenumber_re);
				setupPhoneNumberScreen();
			}
		});
	}

	@Override
	public void onRequestComplete(String response) {
		if (response == null) {
			CommonUtils.toast(this, R.string.invalid_verification);
		} else {
			success();
		}
	}

	public void checkCode(String code, boolean automatic) {
		// show message if sms was automatically read
		if (automatic) {
			CommonUtils.toast(this, R.string.smscode_auto_verified_msg);
		}
		// try to register phone
		String c2dm_id = PhonarPreferencesManager.getC2DMId(this);
		if (c2dm_id != null) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("phone", PhoneNumberActivity.mVerifyingPhone));
			params.add(new BasicNameValuePair("c2dm_id", c2dm_id));
			params.add(new BasicNameValuePair("device_id", CommonUtils.getPhoneID(this)));
			params.add(new BasicNameValuePair("code", code));
			NetworkRequest request = new NetworkRequest(this, NetworkRequest.URL_REGISTER_PHONE,
							true, params, this);
			request.run();
		}
	}

	private void success() {
		PhonarPreferencesManager.setPhoneNumber(this, PhoneNumberActivity.mVerifyingPhone);
		PhonarPreferencesManager.setGoogleVoice(this, mIsGoogleVoice);
		mDone = true;
		startActivity(new Intent(this, PhonarTabActivity.class));
	}

}
