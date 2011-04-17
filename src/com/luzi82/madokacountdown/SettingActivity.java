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

		MadokaCountdown.logd("onCreate");

		MadokaCountdown.initValue(this);

		// Load the preferences from an XML resource
		getPreferenceManager().setSharedPreferencesName(MadokaCountdown.PREFERENCE_NAME);
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences sp = getSharedPreferences(MadokaCountdown.PREFERENCE_NAME, 0);
		sp.registerOnSharedPreferenceChangeListener(changeListener);

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

//	@Override
//	protected void onPause() {
//		super.onPause();
//		if (!isFinishing()) {
//			finish();
//		}
//	}

	@Override
	protected void onStop() {
		super.onStop();

		SharedPreferences sp = getSharedPreferences(MadokaCountdown.PREFERENCE_NAME, 0);
		sp.unregisterOnSharedPreferenceChangeListener(changeListener);
	}

	public void updateDeadlinePreferenceSummary(String value) {
		ListPreference deadlinePreference = (ListPreference) findPreference(MadokaCountdown.PREFERENCES_DEADLINE);
		if (value == null) {
			value = (String) deadlinePreference.getValue();
		}
		deadlinePreference.setSummary(deadlinePreference.getEntries()[Integer.parseInt(value)]);
	}

	//

	SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			MadokaCountdown.logd("onSharedPreferenceChanged");
			if (key.equals(MadokaCountdown.PREFERENCES_DEADLINE)) {
				updateDeadlinePreferenceSummary(null);

				Intent updateIntent = new Intent(MainService.SETTING_CHANGE);
				updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
				sendBroadcast(updateIntent);
			}
		}
	};

}
