package com.luzi82.madokacountdown;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

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

	public static final String[] PREF_COUNTDOWN_ID = { "nico_en", "BD_usa", "PSP" };

	public static final String PREF_SECONDSTIMER = "seconds_timer";

	public static final String AVAILABLE_CHAR = "available_char";
	public static final String AVAILABLE_COUNTDOWN = "available_countdown";
	public static final String VALUE = "value";

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
		for (String c : PREF_COUNTDOWN_ID) {
			String cc = "cd_" + c;
			if (!sp.contains(cc)) {
				editor.putBoolean(cc, true);
			}
		}
		if (!sp.contains(PREF_SECONDSTIMER)) {
			editor.putBoolean(PREF_SECONDSTIMER, true);
		}
		editor.commit();
	}

	static private final SimpleDateFormat mDeadlineFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	// static public Deadline getCurrentDeadline(Resources res) {
	// long now = System.currentTimeMillis();
	// Deadline[] deadlines = getDeadlineArray(res);
	// for (int i = 0; i < deadlines.length; ++i) {
	// long time = deadlines[i].mTimeEnd;
	// time += 7 * 24 * 60 * 60 * 1000;
	// if (time > now) {
	// return deadlines[i];
	// }
	// }
	// return deadlines[deadlines.length - 1];
	// }

	static public Deadline[] getAllDeadline(Resources res) {
		// String[] deadline_name = res.getStringArray(R.array.deadline_name);
		String[] deadline_line = res.getStringArray(R.array.deadline);

		Deadline[] ret = new Deadline[deadline_line.length];
		for (int i = 0; i < deadline_line.length; ++i) {
			ret[i] = new Deadline();
			String[] line = deadline_line[i].split(Pattern.quote("|"));
			for (int j = 0; j < line.length; ++j) {
				line[j] = line[j].trim();
			}
			int k = 0;
			ret[i].mCatalogy = line[k++];
			// ret[i].mType=Deadline.Type.valueOf(line[k++]);
			ret[i].mName = line[k++];
			try {
				ret[i].mTimeEnd = mDeadlineFormat.parse(line[k++]).getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			ret[i].mType = ret[i].mName.contains("^") ? Deadline.Type.PERIOD : Deadline.Type.COUNTDOWN;
		}

		return ret;
	}

}
