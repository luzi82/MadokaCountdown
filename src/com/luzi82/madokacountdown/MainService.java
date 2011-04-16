package com.luzi82.madokacountdown;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

public class MainService extends Service {

	public static String UPDATE = "MadokaCountdown.UPDATE";

	// that is no good when install in off state
	// but the screen detection is in LEVEL 7
	boolean mScreenOn = true;

	ScreenDetect mScreenDetect;

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			synchronized (MainService.this) {
				if (action.equals(Intent.ACTION_SCREEN_OFF)) {
					mScreenOn = false;
				} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
					mScreenOn = true;
				}
				updateTimer(System.currentTimeMillis());
			}
		}
	};

	synchronized void updateTimer(long now) {
		switch (mScreenDetect.getScreenState()) {
		case 1:
			mScreenOn = true;
			break;
		case -1:
			mScreenOn = false;
			break;
		}
		boolean timeGood = now < mBoardcastEnd;
		boolean widgetExist = getWidgetExist();
		if (widgetExist && mScreenOn && timeGood) {
			startTimer();
		} else if (widgetExist && mScreenOn) {
			stopTimer();

			Timer t2 = new Timer();
			t2.schedule(new TimerTask() {
				@Override
				public void run() {
					long time = scheduledExecutionTime();
					redraw(time);
				}
			}, 0);
		} else {
			stopTimer();
		}
	}

	synchronized void startTimer() {
		startAlarm(this);
		if (t == null) {
			t = new Timer();

			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					long time = scheduledExecutionTime();
					redraw(time);
					updateTimer(time);
				}
			};

			GregorianCalendar gc = new GregorianCalendar();
			gc.set(GregorianCalendar.SECOND, gc.get(GregorianCalendar.SECOND) + 1);
			gc.set(GregorianCalendar.MILLISECOND, 0);

			t.scheduleAtFixedRate(tt, gc.getTime(), 1000);
		}
	}

	synchronized void stopTimer() {
		endAlarm(this);
		if (t != null) {
			t.cancel();
			t = null;
		}
	}

	synchronized void redraw(long time) {
		MadokaCountdown.logd("MainService.run");
		AppWidgetManager awm = AppWidgetManager.getInstance(MainService.this);
		int[] ids = awm.getAppWidgetIds(new ComponentName(MainService.this, CountdownAppWidgetProvider.class));
		if ((ids != null) && (ids.length > 0)) {
			MadokaCountdown.logd("time " + time);
			doUpdate(MainService.this, awm, ids, time);
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

		initIntentFilter();

		updateTimer(System.currentTimeMillis());
	}

	private void initIntentFilter() {
		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(UPDATE);
		commandFilter.addAction(Intent.ACTION_SCREEN_OFF);
		commandFilter.addAction(Intent.ACTION_SCREEN_ON);
		commandFilter.addCategory(Intent.CATEGORY_HOME);
		registerReceiver(mIntentReceiver, commandFilter);
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

	public static void startAlarm(Context context) {
		endAlarm(context);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, getAlarmPendingIntent(context));
	}

	public static void endAlarm(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(getAlarmPendingIntent(context));
	}

	private static PendingIntent getAlarmPendingIntent(Context context) {
		Intent intent = new Intent(context, MainService.class);
		return PendingIntent.getService(context, 0, intent, 0);
	}

	// //////////////////////////////////////

	static long mBoardcastStart;
	static long mBoardcastEnd;
	static {
		GregorianCalendar deadline = new GregorianCalendar(TimeZone.getTimeZone("GMT+09"));
		deadline.set(2011, Calendar.APRIL, 22, 2, 40, 0);
		deadline.set(Calendar.MILLISECOND, 0);
		mBoardcastStart = deadline.getTime().getTime();
		deadline = new GregorianCalendar(TimeZone.getTimeZone("GMT+09"));
		deadline.set(2011, Calendar.APRIL, 22, 3, 40, 0);
		deadline.set(Calendar.MILLISECOND, 0);
		mBoardcastEnd = deadline.getTime().getTime();
	}

	private static void doUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, long now) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
		int diff = (int) (mBoardcastStart - (now + 500));
		if (diff > 0) {
			diff /= 1000;

			int sec = diff % 60;
			diff /= 60;
			int min = diff % 60;
			diff /= 60;
			int hr = diff % 24;
			diff /= 24;
			int day = diff;

			if (day > 0) {
				views.setTextViewText(R.id.day, Integer.toString(day));
				views.setViewVisibility(R.id.daytxt, View.VISIBLE);
				String s = String.format("%02d:%02d:%02d", hr, min, sec);
				views.setTextViewText(R.id.time, s);
			} else {
				views.setViewVisibility(R.id.day, View.GONE);
				views.setViewVisibility(R.id.daytxt, View.GONE);
				String s;
				if (hr > 0) {
					s = String.format("%d:%02d:%02d", hr, min, sec);
				} else if (min > 0) {
					s = String.format("%d:%02d", min, sec);
				} else {
					s = String.format("%d", sec);
				}
				views.setTextViewText(R.id.time, s);
			}

		} else {
			views.setViewVisibility(R.id.day, View.GONE);
			views.setViewVisibility(R.id.daytxt, View.GONE);
			diff = (int) (mBoardcastEnd - (now + 500));
			String s;
			if (diff > 0) {
				s = "放送中";
			} else {
				s = "放送終了";
			}
			views.setTextViewText(R.id.time, s);
		}
		if (views != null) {
			Intent intent = new Intent(context, MainMenuActivity.class);
			PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

			// PendingIntent pi = PendingIntent.getActivity(context, 0, link,
			// 0);
			views.setOnClickPendingIntent(R.id.link, pi);

			appWidgetManager.updateAppWidget(appWidgetIds, views);
		}
	}
}
