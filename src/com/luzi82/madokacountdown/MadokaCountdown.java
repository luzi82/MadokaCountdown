package com.luzi82.madokacountdown;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

public class MadokaCountdown {

	public static String TAG = "MadokaCountdown";

	public static final boolean DEBUG = true;

	public static final String PREFERENCE_NAME = "com.luzi82.madokacountdown.SettingActivity";

	public static final String PREFERENCES_DEADLINE = "deadline";
	public static final String PREFERENCES_CHARATER = "charater";
	public static final String PREFERENCES_NTP = "ntp";
	public static final String PREFERENCES_NEWS = "news";
	public static final String PREFERENCES_SECS = "secs";
	public static final Map<String, String> PREFERENCES_DEFAULT = new TreeMap<String, String>();

	enum DeadlineType {
		TV, WEB,
	}

	// static{
	// PREFERENCES_DEFAULT.put(PREFERENCES_DEADLINE, "0");
	// PREFERENCES_DEFAULT.put(PREFERENCES_CHARATER, "0");
	// PREFERENCES_DEFAULT.put(PREFERENCES_NTP, "0");
	// PREFERENCES_DEFAULT.put(PREFERENCES_NEWS, "0");
	// PREFERENCES_DEFAULT.put(PREFERENCES_SECS, "0");
	// }

	static void logd(String msg) {
		if (!DEBUG) {
			return;
		}
		Log.d(TAG, msg);
	}

	static public void initValue(Context context) {
		SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();
		if (!sp.contains(MadokaCountdown.PREFERENCES_DEADLINE)) {
			editor.putString(MadokaCountdown.PREFERENCES_DEADLINE, "0");
		}
		editor.commit();
	}

	static public int getDeadlineSetting(SharedPreferences sp) {
		try {
			String retString = sp.getString(MadokaCountdown.PREFERENCES_DEADLINE, "0");
			return Integer.parseInt(retString);
		} catch (Throwable t) {
			return 0;
		}
	}

	static private SimpleDateFormat mDeadlineFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	static public long getDeadlineSettingStart(Context context) {
		int valueInt = getDeadlineSettingSelection(context);

		Resources res = context.getResources();
		String deadline = res.getStringArray(R.array.deadline_start)[valueInt];
		logd("getDeadlineSettingStart " + deadline);

		try {
			Date date = mDeadlineFormat.parse(deadline);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return -1; // should not happen
		}
	}

	static public long getDeadlineSettingEnd(Context context) {
		int valueInt = getDeadlineSettingSelection(context);

		Resources res = context.getResources();
		String deadline = res.getStringArray(R.array.deadline_end)[valueInt];

		if (!deadline.equals("download")) {
			try {
				Date date = mDeadlineFormat.parse(deadline);
				return date.getTime();
			} catch (ParseException e) {
				return -1; // should not happen
			}
		} else {
			return getDeadlineSettingStart(context);
		}
	}

	static public DeadlineType getDeadlineType(Context context) {
		int valueInt = getDeadlineSettingSelection(context);

		Resources res = context.getResources();
		String type = res.getStringArray(R.array.deadline_type)[valueInt];

		return DeadlineType.valueOf(type);
	}

	static public int getDeadlineSettingSelection(Context context) {
		SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
		String valueString = sp.getString(MadokaCountdown.PREFERENCES_DEADLINE, "0");
		return Integer.parseInt(valueString);
	}

}
