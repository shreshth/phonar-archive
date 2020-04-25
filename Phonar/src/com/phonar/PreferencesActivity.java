package com.phonar;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.phonar.utils.CommonUtils;
import com.phonar.utils.PhonarPreferencesManager;

public class PreferencesActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (CommonUtils.isTestPhone(this)) {
			addPreferencesFromResource(R.layout.preferences_test);
		} else {
			addPreferencesFromResource(R.layout.preferences);
		}

		Preference notificationVibratePreference = findPreference(PhonarPreferencesManager.KEY_NOTIFICATION_VIBRATE);
		Preference notificationRingtonePreference = findPreference(PhonarPreferencesManager.KEY_NOTIFICATION_RINGTONE);
		notificationVibratePreference.setShouldDisableView(true);
		notificationRingtonePreference.setShouldDisableView(true);
		// if default, make vibrate and ringtone preferences disabled
		if (PhonarPreferencesManager.getNotificationDefault(this)) {
			notificationVibratePreference.setEnabled(false);
			notificationRingtonePreference.setEnabled(false);
			notificationVibratePreference.setSelectable(false);
			notificationRingtonePreference.setSelectable(false);
		}
		// otherwise make them enabled
		else {
			notificationVibratePreference.setEnabled(true);
			notificationRingtonePreference.setEnabled(true);
			notificationVibratePreference.setSelectable(true);
			notificationRingtonePreference.setSelectable(true);
		}

		Preference notificationDefaultPreference = findPreference(PhonarPreferencesManager.KEY_NOTIFICATION_DEFAULT);

		notificationDefaultPreference
						.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
							@Override
							public boolean onPreferenceChange(Preference preference, Object newValue) {
								Preference notificationVibratePreference = findPreference(PhonarPreferencesManager.KEY_NOTIFICATION_VIBRATE);
								Preference notificationRingtonePreference = findPreference(PhonarPreferencesManager.KEY_NOTIFICATION_RINGTONE);

								// if using defaults, then disable ringtone and
								// vibrate
								if (newValue.equals(true)) {
									notificationVibratePreference.setEnabled(false);
									notificationRingtonePreference.setEnabled(false);
									notificationVibratePreference.setSelectable(false);
									notificationRingtonePreference.setSelectable(false);
								}
								// otherwise enable
								else {
									notificationVibratePreference.setEnabled(true);
									notificationRingtonePreference.setEnabled(true);
									notificationVibratePreference.setSelectable(true);
									notificationRingtonePreference.setSelectable(true);
								}
								return true;
							}
						});
	}
}