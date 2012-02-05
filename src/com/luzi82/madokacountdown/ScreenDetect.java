package com.luzi82.madokacountdown;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.PowerManager;

public class ScreenDetect {

	// 0=not detected
	// -1=not exist
	// 1=exist
	public static int mApiState = 0;

	public static Method mMethod;

	private PowerManager mPowerManager;

	public ScreenDetect(Context context) {
		initApiState();
		if (mApiState == 1) {
			mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		}
	}

	// 1=on, -1=off, 0=unknown
	public int getScreenState() {
//		MadokaCountdown.logd("getScreenState");
		if (mPowerManager == null) {
			return 0;
		}
		boolean b;
		try {
			b = (Boolean) mMethod.invoke(mPowerManager);
//			MadokaCountdown.logd(b ? "screen on" : "screen off");
			return b ? 1 : -1;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}

	static void initApiState() {
		if (mApiState != 0) {
			return;
		}
		mApiState = -1;
		Method[] mv = PowerManager.class.getMethods();
		for (Method m : mv) {
			if (!m.getName().equals("isScreenOn"))
				continue;
			if (m.getParameterTypes().length != 0)
				continue;
			if (m.getReturnType() != boolean.class)
				continue;
			mMethod = m;
			mApiState = 1;
			break;
		}
	}

}
