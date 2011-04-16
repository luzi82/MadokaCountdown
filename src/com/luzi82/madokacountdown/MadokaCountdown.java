package com.luzi82.madokacountdown;

import java.util.Map;
import java.util.TreeMap;

import android.util.Log;

public class MadokaCountdown {

	public static String TAG = "MadokaCountdown";

	public static final boolean DEBUG = true;

	public static final String PREFERENCES_DEADLINE = "deadline";
	public static final String PREFERENCES_CHARATER = "charater";
	public static final String PREFERENCES_NTP = "ntp";
	public static final String PREFERENCES_NEWS = "news";
	public static final String PREFERENCES_SECS = "secs";
	public static final Map<String,String> PREFERENCES_DEFAULT=new TreeMap<String, String>(); 
//	static{
//		PREFERENCES_DEFAULT.put(PREFERENCES_DEADLINE, "0");
//		PREFERENCES_DEFAULT.put(PREFERENCES_CHARATER, "0");
//		PREFERENCES_DEFAULT.put(PREFERENCES_NTP, "0");
//		PREFERENCES_DEFAULT.put(PREFERENCES_NEWS, "0");
//		PREFERENCES_DEFAULT.put(PREFERENCES_SECS, "0");
//	}

	static void logd(String msg) {
		if (!DEBUG) {
			return;
		}
		Log.d(TAG, msg);
	}

}
