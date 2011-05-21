package com.luzi82.madokacountdown;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

	public static final String PREFERENCES_NTP = "ntp";
	public static final String PREFERENCES_NEWS = "news";
	public static final String PREFERENCES_SECS = "secs";
	public static final Map<String, String> PREFERENCES_DEFAULT = new TreeMap<String, String>();

	public static final int[][] VOICE_ID = { { R.raw.char_01, R.raw.char_02, R.raw.char_03, R.raw.char_04 }, { R.raw.char_05, R.raw.char_06, R.raw.char_07, R.raw.char_08 }, { R.raw.char_09, R.raw.char_10, R.raw.char_11, R.raw.char_12 }, { R.raw.char_13, R.raw.char_14, R.raw.char_15, R.raw.char_16 }, { R.raw.char_17, R.raw.char_18, R.raw.char_19, R.raw.char_20 } };

	public static final int[] ICON_ID = { R.drawable.char_mk, R.drawable.char_ha, R.drawable.char_mt, R.drawable.char_sm, R.drawable.qb_128 };

	public static final String[] PREF_ID = { "char_madoka", "char_homura", "char_mami", "char_sayaka", "char_qb" };

	public static final String AVAILABLE_CHAR = "available_char";

	static void logd(String msg) {
		if (!DEBUG) {
			return;
		}
		Log.d(TAG, msg);
	}

	static public void initValue(Context context) {
		SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();
		for (String c : MadokaCountdown.PREF_ID) {
			if (!sp.contains(c)) {
				editor.putBoolean(c, true);
			}
		}
		editor.commit();
	}

	static private final SimpleDateFormat mDeadlineFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	static public Deadline getCurrentDeadline(Resources res) {
		long now = System.currentTimeMillis();
		Deadline[] deadlines = getDeadlineArray(res);
		for (int i = 0; i < deadlines.length; ++i) {
			long time = deadlines[i].time;
			time += 7 * 24 * 60 * 60 * 1000;
			if (time > now) {
				return deadlines[i];
			}
		}
		return deadlines[deadlines.length - 1];
	}

	static public Deadline[] getDeadlineArray(Resources res) {
		String[] deadline_name = res.getStringArray(R.array.deadline_name);
		String[] deadline_time = res.getStringArray(R.array.deadline_time);
		Deadline[] ret = new Deadline[deadline_time.length];
		for (int i = 0; i < deadline_time.length; ++i) {
			ret[i] = new Deadline();
			ret[i].name = deadline_name[i];
			try {
				ret[i].time = mDeadlineFormat.parse(deadline_time[i]).getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

}
