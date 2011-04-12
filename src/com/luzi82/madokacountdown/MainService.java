package com.luzi82.madokacountdown;

import java.lang.ref.WeakReference;
import java.util.Set;
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
import android.util.Log;

public class MainService extends Service {

	public static String UPDATE = "MadokaCountdown.UPDATE";

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Set<String> category = intent.getCategories();
			String action = intent.getAction();

			if (category != null) {
				for (String cat : category) {
					Log.d("CountdownAppWidgetProvider", "cat " + cat);
				}
			}
			Log.d("CountdownAppWidgetProvider", "action " + action);
			Log.d("CountdownAppWidgetProvider", "");

			if (action.equals(UPDATE)) {
				startTimer();
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				stopTimer();
			} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
				startTimer();
			}
		}
	};

	synchronized void startTimer() {
		if (t == null) {
			t = new Timer();

			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					Log.d("CountdownAppWidgetProvider", "MainService.run");
					AppWidgetManager awm = AppWidgetManager.getInstance(MainService.this);
					int[] ids = awm.getAppWidgetIds(new ComponentName(MainService.this, CountdownAppWidgetProvider.class));
					if ((ids != null) && (ids.length > 0)) {
						CountdownAppWidgetProvider.doUpdate(MainService.this, awm, ids);
					} else {
						stopTimer();
					}
				}
			};
			t.scheduleAtFixedRate(tt, 0, 1000);
		}
	}

	synchronized void stopTimer() {
		t.cancel();
		t = null;
	}

	Timer t;

	@Override
	public void onCreate() {
		super.onCreate();

		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(UPDATE);
		commandFilter.addAction(Intent.ACTION_SCREEN_OFF);
		commandFilter.addAction(Intent.ACTION_SCREEN_ON);
		commandFilter.addCategory(Intent.CATEGORY_HOME);
		registerReceiver(mIntentReceiver, commandFilter);

		startTimer();
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
