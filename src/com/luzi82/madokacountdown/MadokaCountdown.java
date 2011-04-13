package com.luzi82.madokacountdown;

import android.util.Log;

public class MadokaCountdown {

	public static String TAG = "MadokaCountdown";

	public static final boolean DEBUG = false;

	static void logd(String msg) {
		if (!DEBUG) {
			return;
		}
		Log.d(TAG, msg);
	}

}
