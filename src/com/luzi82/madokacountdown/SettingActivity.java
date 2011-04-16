package com.luzi82.madokacountdown;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingActivity extends PreferenceActivity {

	public static final String PREFERENCE_NAME = "com.luzi82.madokacountdown.SettingActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		getPreferenceManager().setSharedPreferencesName(PREFERENCE_NAME);
		initValue(getPreferenceManager().getSharedPreferences());
		addPreferencesFromResource(R.xml.preferences);
		// getPreferenceManager().getSharedPreferences()
		// .registerOnSharedPreferenceChangeListener(this);

		// PREFERENCES_DEADLINE value
		ListPreference deadlinePreference = (ListPreference) findPreference(MadokaCountdown.PREFERENCES_DEADLINE);
		int valueSize = deadlinePreference.getEntries().length;
		String[] entriesValue = new String[valueSize];
		for (int i = 0; i < valueSize; ++i) {
			entriesValue[i] = Integer.toString(i);
		}
		deadlinePreference.setEntryValues(entriesValue);

		// PREFERENCES_DEADLINE refresh
		deadlinePreference.setOnPreferenceChangeListener(new ListPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateDeadlinePreferenceSummary((String) newValue);
				return true;
			}
		});

		// update stuff
		updateDeadlinePreferenceSummary(null);
	}

	static public void initValue(SharedPreferences sp) {
		SharedPreferences.Editor editor = sp.edit();
		if (!sp.contains(MadokaCountdown.PREFERENCES_DEADLINE)) {
			editor.putString(MadokaCountdown.PREFERENCES_DEADLINE, "0");
		}
		editor.commit();
	}

	static public int getRefreshPeroid(SharedPreferences sp) {
		try {
			String retString = sp.getString(MadokaCountdown.PREFERENCES_DEADLINE, "0");
			return Integer.parseInt(retString);
		} catch (Throwable t) {
			return 0;
		}
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
