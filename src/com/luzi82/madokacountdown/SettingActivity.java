package com.luzi82.madokacountdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MadokaCountdown.initValue(this);

		// Load the preferences from an XML resource
		getPreferenceManager().setSharedPreferencesName(MadokaCountdown.PREFERENCE_NAME);
		addPreferencesFromResource(R.xml.preferences);

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(MadokaCountdown.PREFERENCES_DEADLINE)) {
					updateDeadlinePreferenceSummary(null);
					
					Intent updateIntent = new Intent(MainService.SETTING_CHANGE);
					updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
					sendBroadcast(updateIntent);
				}
			}
		});

		// PREFERENCES_DEADLINE value
		ListPreference deadlinePreference = (ListPreference) findPreference(MadokaCountdown.PREFERENCES_DEADLINE);
		int valueSize = deadlinePreference.getEntries().length;
		String[] entriesValue = new String[valueSize];
		for (int i = 0; i < valueSize; ++i) {
			entriesValue[i] = Integer.toString(i);
		}
		deadlinePreference.setEntryValues(entriesValue);

		// update stuff
		updateDeadlinePreferenceSummary(null);
	}

	// @Override
	// public void onSharedPreferenceChanged(SharedPreferences
	// sharedPreferences, String key) {
	// if (key.equals(REFRESH_PEROID_KEY)) {
	// resetRefreshPeriodPreferenceSummary();
	// }
	// if (key.equals(COLOR_KEY) || key.equals(COLOR_CHEAT_KEY)) {
	// resetColorPreferenceSummary();
	// }
	// if (key.equals(COLORMODE_KEY)) {
	// resetColorModePreferenceSummary();
	// }
	// }

	public void updateDeadlinePreferenceSummary(String value) {
		ListPreference deadlinePreference = (ListPreference) findPreference(MadokaCountdown.PREFERENCES_DEADLINE);
		if (value == null) {
			value = (String) deadlinePreference.getValue();
		}
		deadlinePreference.setSummary(deadlinePreference.getEntries()[Integer.parseInt(value)]);
	}

}
