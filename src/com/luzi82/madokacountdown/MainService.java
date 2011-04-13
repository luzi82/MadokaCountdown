package com.luzi82.madokacountdown;

import java.lang.ref.WeakReference;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class MainService extends Service {

	public static String UPDATE = "MadokaCountdown.UPDATE";

	// that is no good when install in off state
	// but the screen detection is in LEVEL 7
	boolean mScreenOn = true;

	ScreenDetect mScreenDetect;

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Set<String> category = intent.getCategories();
			String action = intent.getAction();

			// if (category != null) {
			// for (String cat : category) {
			// MadokaCountdown.logd("cat " + cat);
			// }
			// }
			// MadokaCountdown.logd("action " + action);
			// MadokaCountdown.logd("");

			synchronized (MainService.this) {
				if (action.equals(Intent.ACTION_SCREEN_OFF)) {
					mScreenOn = false;
				} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
					mScreenOn = true;
				}
				updateTimer();
			}
		}
	};

	synchronized void updateTimer() {
		switch (mScreenDetect.getScreenState()) {
		case 1:
			mScreenOn = true;
			break;
		case -1:
			mScreenOn = false;
			break;
		}
		if (getWidgetExist() && mScreenOn) {
			startTimer();
		} else {
			stopTimer();
		}
	}

	synchronized void startTimer() {
		if (t == null) {
			t = new Timer();

			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					MadokaCountdown.logd("MainService.run");
					AppWidgetManager awm = AppWidgetManager.getInstance(MainService.this);
					int[] ids = awm.getAppWidgetIds(new ComponentName(MainService.this, CountdownAppWidgetProvider.class));
					if ((ids != null) && (ids.length > 0)) {
						long time = scheduledExecutionTime();
						MadokaCountdown.logd("time " + time);
						CountdownAppWidgetProvider.doUpdate(MainService.this, awm, ids, time);
					}
					updateTimer();
				}
			};

			GregorianCalendar gc = new GregorianCalendar();
			gc.set(GregorianCalendar.SECOND, gc.get(GregorianCalendar.SECOND) + 1);
			gc.set(GregorianCalendar.MILLISECOND, 0);

			t.scheduleAtFixedRate(tt, gc.getTime(), 1000);
		}
	}

	synchronized void stopTimer() {
		if (t != null) {
			t.cancel();
			t = null;
		}
	}

	Timer t;

	boolean getWidgetExist() {
		AppWidgetManager awm = AppWidgetManager.getInstance(this);
		int[] ids = awm.getAppWidgetIds(new ComponentName(this, CountdownAppWidgetProvider.class));
		return (ids != null) && ids.length > 0;
	}

	// /////////////////////////////////////

	@Override
	public void onCreate() {
		super.onCreate();

		mScreenDetect = new ScreenDetect(this);

		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(UPDATE);
		commandFilter.addAction(Intent.ACTION_SCREEN_OFF);
		commandFilter.addAction(Intent.ACTION_SCREEN_ON);
		commandFilter.addCategory(Intent.CATEGORY_HOME);
		registerReceiver(mIntentReceiver, commandFilter);

		updateTimer();
	}

	static class ServiceStub extends IMainService.Stub {
		WeakReference<MainService> mService;

		ServiceStub(MainService service) {
			mService = new WeakReference<MainService>(service);
		}
	}

	private final IBinder mBinder = new ServiceStub(this);

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

}
