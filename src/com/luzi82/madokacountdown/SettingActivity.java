package com.luzi82.madokacountdown;

import java.util.LinkedList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sp = getSharedPreferences(MadokaCountdown.PREFERENCE_NAME, 0);
		sp.registerOnSharedPreferenceChangeListener(changeListener);

		updateCharSelectionEnable(sp);
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences sp = getSharedPreferences(MadokaCountdown.PREFERENCE_NAME, 0);
		sp.unregisterOnSharedPreferenceChangeListener(changeListener);

		if (!isFinishing()) {
			finish();
		}
	}

	public void updateCharSelectionEnable(SharedPreferences sp) {
		int charSum = 0;
		for (String c : MadokaCountdown.PREF_ID) {
			boolean v = sp.getBoolean(c, true);
			if (v)
				++charSum;
		}
		if (charSum == 1) {
			for (String c : MadokaCountdown.PREF_ID) {
				boolean v = sp.getBoolean(c, true);
				CheckBoxPreference cbp = (CheckBoxPreference) findPreference(c);
				cbp.setEnabled(!v);
			}
		} else {
			for (String c : MadokaCountdown.PREF_ID) {
				CheckBoxPreference cbp = (CheckBoxPreference) findPreference(c);
				cbp.setEnabled(true);
			}
		}
	}

	//

	SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			MadokaCountdown.logd("onSharedPreferenceChanged");
			boolean charChange = false;
			for (String c : MadokaCountdown.PREF_ID) {
				if (key.equals(c)) {
					charChange = true;
					break;
				}
			}
			if (charChange) {
				updateCharSelectionEnable(sharedPreferences);
			}
			LinkedList<Integer> lli = new LinkedList<Integer>();
			for (int i = 0; i < MadokaCountdown.PREF_ID.length; ++i) {
				if (sharedPreferences.getBoolean(MadokaCountdown.PREF_ID[i], true))
					lli.add(i);
			}
			int[] lliv = new int[lli.size()];
			for (int i = 0; i < lliv.length; ++i) {
				lliv[i] = lli.get(i);
			}
			Intent updateIntent = new Intent(MainService.SETTINGCHANGE_CHAR);
			updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
			updateIntent.putExtra(MadokaCountdown.AVAILABLE_CHAR, lliv);
			sendBroadcast(updateIntent);
		}
	};

}
