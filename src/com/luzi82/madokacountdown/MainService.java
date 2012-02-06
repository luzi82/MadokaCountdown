package com.luzi82.madokacountdown;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.regex.Pattern;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;
import android.widget.RemoteViews;

public class MainService extends Service {

	public static String SETTING_CHANGE = "MadokaCountdown.SETTING_CHANGE";
	public static String SETTINGCHANGE_CHAR = "MadokaCountdown.SETTINGCHANGE_CHAR";
	public static String SETTINGCHANGE_COUNTDOWN = "MadokaCountdown.SETTINGCHANGE_COUNTDOWN";
	public static String SETTINGCHANGE_SECONDSTIMER = "MadokaCountdown.SETTINGCHANGE_SECONDSTIMER";
	public static String UPDATE = "MadokaCountdown.UPDATE";
	public static String VOICE = "MadokaCountdown.VOICE";

	// that is no good when install in off state
	// but the screen detection is in LEVEL 7
	boolean mScreenOn = true;

	ScreenDetect mScreenDetect;

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			MadokaCountdown.logd("action " + action);

			synchronized (MainService.this) {
				if (action.equals(UPDATE)) {
				} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
					mScreenOn = false;
				} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
					mScreenOn = true;
				} else if (action.equals(VOICE)) {
					triggerVoice();
				} else if (action.equals(SETTINGCHANGE_COUNTDOWN)) {
					updateCountdownEnabled(intent.getStringArrayExtra(MadokaCountdown.AVAILABLE_COUNTDOWN));
				} else if (action.equals(SETTINGCHANGE_CHAR)) {
					changeIconOnly(intent.getIntArrayExtra(MadokaCountdown.AVAILABLE_CHAR));
				} else if (action.equals(SETTINGCHANGE_SECONDSTIMER)) {
					updateSecondsTimer();
					// redraw(System.currentTimeMillis());
					// stopTimer();
				}
				redraw(System.currentTimeMillis());
				// startTimer();
				updateTimer(System.currentTimeMillis());
			}
		}

	};

	enum TimerPeriod {
		SECOND, MINUTE
	}

	TimerPeriod mTimerPeriod = null;

	synchronized void updateTimer(long now) {
		switch (mScreenDetect.getScreenState()) {
		case 1:
			mScreenOn = true;
			break;
		case -1:
			mScreenOn = false;
			break;
		}
		// boolean timeGood = now < getBoardcastEnd();
		boolean widgetExist = getWidgetExist();
		if (widgetExist && mScreenOn) {
			startTimer();
		} else {
			stopTimer();
		}
	}

	synchronized void startTimer() {
		MadokaCountdown.logd("MainService.startTimer");
		startAlarm();
		TimerPeriod timerPeriod = getSecondsTimer() ? TimerPeriod.SECOND : TimerPeriod.MINUTE;
		if (mTimerPeriod != timerPeriod) {
			if (t != null) {
				t.cancel();
				t = null;
			}
			mTimerPeriod = null;
		}
		if (t == null) {
			t = new Timer();

			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					long time = scheduledExecutionTime();
					redraw(time);
					updateTimer(time);
				}
			};

			switch (timerPeriod) {
			case SECOND: {
				GregorianCalendar gc = new GregorianCalendar();
				gc.set(GregorianCalendar.SECOND, gc.get(GregorianCalendar.SECOND) + 1);
				gc.set(GregorianCalendar.MILLISECOND, 10);
				t.scheduleAtFixedRate(tt, gc.getTime(), 1000);
				break;
			}
			case MINUTE: {
				GregorianCalendar gc = new GregorianCalendar();
				int s = gc.get(GregorianCalendar.SECOND);
				s /= 10;
				s += 1;
				s *= 10;
				gc.set(GregorianCalendar.MINUTE, gc.get(GregorianCalendar.MINUTE) + s / 60);
				gc.set(GregorianCalendar.SECOND, s % 60);
				gc.set(GregorianCalendar.MILLISECOND, 10);
				t.scheduleAtFixedRate(tt, gc.getTime(), 10000);
				break;
			}
			}
		}
		mTimerPeriod = timerPeriod;
	}

	synchronized void stopTimer() {
		MadokaCountdown.logd("MainService.stopTimer");
		endAlarm();
		if (t != null) {
			t.cancel();
			t = null;
		}
		mTimerPeriod = null;
	}

	synchronized void redraw(long time) {
		MadokaCountdown.logd("MainService.redraw");
		AppWidgetManager awm = AppWidgetManager.getInstance(MainService.this);
		int[] ids = awm.getAppWidgetIds(new ComponentName(MainService.this, CountdownAppWidgetProvider.class));
		if ((ids != null) && (ids.length > 0)) {
			// MadokaCountdown.logd("time " + time);
			doUpdate(awm, ids, time);
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
	public synchronized void onCreate() {
		super.onCreate();

		MadokaCountdown.initValue(this);

		mScreenDetect = new ScreenDetect(this);

		initIntentFilter();

		changeIconOnly(null);
		updateCountdownEnabled(null);
		updateSecondsTimer();

		redraw(System.currentTimeMillis());
		updateTimer(System.currentTimeMillis());
	}

	private void initIntentFilter() {
		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(UPDATE);
		// commandFilter.addAction(SETTING_CHANGE);
		commandFilter.addAction(VOICE);
		commandFilter.addAction(SETTINGCHANGE_CHAR);
		commandFilter.addAction(SETTINGCHANGE_COUNTDOWN);
		commandFilter.addAction(SETTINGCHANGE_SECONDSTIMER);
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

	private boolean mAlarmEnabled = false;

	public void startAlarm() {
		MadokaCountdown.logd("MainService.startAlarm");
		if (!mAlarmEnabled) {
			endAlarm();
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, getAlarmPendingIntent());
			mAlarmEnabled = true;
		}
	}

	public void endAlarm() {
		MadokaCountdown.logd("MainService.endAlarm");
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(getAlarmPendingIntent());
		mAlarmEnabled = false;
	}

	private PendingIntent getAlarmPendingIntent() {
		Intent intent = new Intent(this, MainService.class);
		return PendingIntent.getService(this, 0, intent, 0);
	}

	// //////////////////////////////////////

	int mIconImgId = -1;

	Deadline[] mAllDeadline = null;

	private synchronized void doUpdate(AppWidgetManager appWidgetManager, int[] appWidgetIds, long now) {
		MadokaCountdown.logd("MainService.doUpdate");
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget);
		views.setImageViewResource(R.id.voiceButton, mIconImgId);
		Deadline deadline = getCurrentDeadline();
		if (deadline == null) {
			views.setTextViewText(R.id.txt0, " ");
			views.setTextViewText(R.id.txt1, " ");
		} else if (deadline.mType == Deadline.Type.COUNTDOWN) {
			long diff = deadline.mTimeEnd - now;
			// diff /= 24 * 60 * 60 * 1000;
			// ++diff;

			diff /= 1000;
			int sec = (int) diff % 60;
			diff /= 60;
			int min = (int) diff % 60;
			diff /= 60;
			int hr = (int) diff % 24;
			diff /= 24;
			int day = (int) diff;

			// String s = deadline.mName+" ";
			String s = null;

			if (getSecondsTimer()) {
				if (day > 0) {
					s = String.format("%d日%02d:%02d:%02d", day, hr, min, sec);
				} else if (hr > 0) {
					s = String.format("%d:%02d:%02d", hr, min, sec);
				} else if (min > 0) {
					s = String.format("%d:%02d", min, sec);
				} else {
					s = String.format("%d", sec);
				}
			} else {
				if (day > 0) {
					s = String.format("%d日%02d:%02d", day, hr, min);
				} else if (hr > 0) {
					s = String.format("%d:%02d", hr, min);
				} else {
					s = String.format("%d", min);
				}
			}
			// views.setViewVisibility(R.id.txt, View.GONE);
			// views.setViewVisibility(R.id.txt0, View.VISIBLE);
			// views.setViewVisibility(R.id.txt1, View.VISIBLE);
			views.setTextViewText(R.id.txt0, deadline.mName);
			views.setTextViewText(R.id.txt1, s);
		} else {
			String[] ss = deadline.mName.split(Pattern.quote("^"));
			// String s = ss.;
			// views.setViewVisibility(R.id.txt, View.VISIBLE);
			// views.setViewVisibility(R.id.txt0, View.GONE);
			// views.setViewVisibility(R.id.txt1, View.GONE);
			views.setTextViewText(R.id.txt0, ss[0].trim());
			views.setTextViewText(R.id.txt1, ss[1].trim());
		}

		Intent voiceIntent = new Intent(MainService.VOICE);
		PendingIntent voicePendingIntent = PendingIntent.getBroadcast(this, 0, voiceIntent, 0);
		views.setOnClickPendingIntent(R.id.voiceButton, voicePendingIntent);

		Intent mainMenuIntent = new Intent(this, MainMenuActivity.class);
		PendingIntent mainMenuPendingIntent = PendingIntent.getActivity(this, 0, mainMenuIntent, 0);
		views.setOnClickPendingIntent(R.id.link, mainMenuPendingIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, views);
	}

	private synchronized Deadline getCurrentDeadline() {
		if (mAllDeadline == null) {
			mAllDeadline = MadokaCountdown.getAllDeadline(getResources());
		}

		if (mCountdownEnabled == null) {
			updateCountdownEnabled(null);
		}

		TreeMap<String, Deadline> mActiveDeadline = new TreeMap<String, Deadline>();
		long now = System.currentTimeMillis();
		for (Deadline d : mAllDeadline) {
			if (d.mTimeEnd < now)
				continue;
			String cat = d.mCatalogy;
			if (!mCountdownEnabled.contains(cat)) {
				continue;
			}
			Deadline dd = mActiveDeadline.get(cat);
			if ((dd == null) || (d.mTimeEnd < dd.mTimeEnd)) {
				mActiveDeadline.put(cat, d);
			}
		}
		Deadline[] dv = mActiveDeadline.values().toArray(new Deadline[0]);
		if (dv.length == 0) {
			return null;
		}
		int dvi = (int) (now / 10000) % dv.length;
		return dv[dvi];
	}

	// /////////////////////////////////

	MediaPlayer mMediaPlayer;
	MediaPlayerListener mMediaPlayerListener = new MediaPlayerListener();
	static final int CHAR_SIZE = MadokaCountdown.PREF_ID.length;

	private synchronized void triggerVoice() {
		MadokaCountdown.logd("playVoice");

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			return;
		}

		LinkedList<Integer> availableChar = new LinkedList<Integer>();

		SharedPreferences sp = getSharedPreferences(MadokaCountdown.PREFERENCE_NAME, 0);
		for (int i = 0; i < CHAR_SIZE; ++i) {
			if (sp.getBoolean(MadokaCountdown.PREF_ID[i], true)) {
				availableChar.add(i);
			}
		}

		if (availableChar.isEmpty()) {
			return;
		}

		int charNum = availableChar.get(random.nextInt(availableChar.size()));
		mIconImgId = MadokaCountdown.ICON_ID[charNum];
		int[] voiceIdV = MadokaCountdown.VOICE_ID[charNum];
		int voiceId = voiceIdV[random.nextInt(voiceIdV.length)];

		mMediaPlayer = MediaPlayer.create(this, voiceId);
		if (mMediaPlayer != null) {
			// rare case... report say it happens, better handle it.
			mMediaPlayer.setOnCompletionListener(mMediaPlayerListener);
			mMediaPlayer.setOnErrorListener(mMediaPlayerListener);

			mMediaPlayer.setVolume(1.0f, 1.0f);
			mMediaPlayer.start();
		}

		redraw(System.currentTimeMillis());
	}

	class MediaPlayerListener implements OnCompletionListener, OnErrorListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			mp.release();
			mMediaPlayer = null;
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			mp.release();
			mMediaPlayer = null;
			return true;
		}
	};

	// ///

	synchronized void changeIconOnly(int[] available) {
		if (available == null) {
			SharedPreferences sharedPreferences = getSharedPreferences(MadokaCountdown.PREFERENCE_NAME, 0);
			LinkedList<Integer> lli = new LinkedList<Integer>();
			for (int i = 0; i < MadokaCountdown.PREF_ID.length; ++i) {
				if (sharedPreferences.getBoolean(MadokaCountdown.PREF_ID[i], true))
					lli.add(i);
			}
			available = new int[lli.size()];
			for (int i = 0; i < available.length; ++i) {
				available[i] = lli.get(i);
			}
		}
		for (int a : available) {
			if (MadokaCountdown.ICON_ID[a] == mIconImgId)
				return;
		}
		int charNum = available[random.nextInt(available.length)];
		mIconImgId = MadokaCountdown.ICON_ID[charNum];
	}

	// ///

	// preference cache
	HashSet<String> mCountdownEnabled;

	synchronized void updateCountdownEnabled(String[] aKey) {
		if (aKey == null) {
			mCountdownEnabled = new HashSet<String>();
			SharedPreferences sharedPreferences = getSharedPreferences(MadokaCountdown.PREFERENCE_NAME, 0);
			for (int i = 0; i < MadokaCountdown.PREF_COUNTDOWN_ID.length; ++i) {
				if (sharedPreferences.getBoolean("cd_" + MadokaCountdown.PREF_COUNTDOWN_ID[i], true))
					mCountdownEnabled.add(MadokaCountdown.PREF_COUNTDOWN_ID[i]);
			}
		} else {
			mCountdownEnabled = new HashSet<String>(Arrays.asList(aKey));
		}
	}

	// //\

	Boolean mSecondsTimer = null;

	boolean getSecondsTimer() {
		if (mSecondsTimer == null) {
			updateSecondsTimer();
		}
		return mSecondsTimer;
	}

	synchronized void updateSecondsTimer() {
		MadokaCountdown.logd("MainService.updateSecondsTimer");
		SharedPreferences sharedPreferences = getSharedPreferences(MadokaCountdown.PREFERENCE_NAME, 0);
		mSecondsTimer = sharedPreferences.getBoolean(MadokaCountdown.PREF_SECONDSTIMER, false);
	}

	// ///

	Random random = new Random();

}
