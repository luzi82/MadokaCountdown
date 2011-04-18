package com.luzi82.madokacountdown;

import java.lang.ref.WeakReference;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.luzi82.madokacountdown.MadokaCountdown.DeadlineType;

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
import android.view.View;
import android.widget.RemoteViews;

public class MainService extends Service {

	public static String SETTING_CHANGE = "MadokaCountdown.SETTING_CHANGE";
	public static String SETTINGCHANGE_CHAR = "MadokaCountdown.SETTINGCHANGE_CHAR";
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

			synchronized (MainService.this) {
				if (action.equals(SETTING_CHANGE)) {
					mBoardcastStart = -2;
					mBoardcastEnd = -2;
				} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
					mScreenOn = false;
				} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
					mScreenOn = true;
				} else if (action.equals(VOICE)) {
					triggerVoice();
				} else if (action.equals(SETTINGCHANGE_CHAR)) {
					changeIconOnly(intent.getIntArrayExtra(MadokaCountdown.AVAILABLE_CHAR));
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
		boolean timeGood = now < getBoardcastEnd();
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
		// MadokaCountdown.logd("MainService.run");
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
		updateTimer(System.currentTimeMillis());
	}

	private void initIntentFilter() {
		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(UPDATE);
		commandFilter.addAction(SETTING_CHANGE);
		commandFilter.addAction(VOICE);
		commandFilter.addAction(SETTINGCHANGE_CHAR);
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

	int mIconImgId = -1;

	private synchronized void doUpdate(AppWidgetManager appWidgetManager, int[] appWidgetIds, long now) {
		// MadokaCountdown.logd("doUpdate " + mBoardcastStart);
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget);
		views.setImageViewResource(R.id.voiceButton, mIconImgId);
		int diff = (int) (getBoardcastStart() - (now + 500));
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
				views.setViewVisibility(R.id.day, View.VISIBLE);
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

			DeadlineType mDeadlineType = getDeadlineType();
			diff = (int) (getBoardcastEnd() - (now + 500));

			String s;
			if (mDeadlineType == DeadlineType.WEB) {
				s = "配信中";
			} else if (diff > 0) {
				s = "放送中";
			} else {
				s = "放送終了";
			}
			views.setTextViewText(R.id.time, s);
		}
		if (views != null) {
			Intent voiceIntent = new Intent(MainService.VOICE);
			PendingIntent voicePendingIntent = PendingIntent.getBroadcast(this, 0, voiceIntent, 0);
			views.setOnClickPendingIntent(R.id.voiceButton, voicePendingIntent);

			Intent mainMenuIntent = new Intent(this, MainMenuActivity.class);
			PendingIntent mainMenuPendingIntent = PendingIntent.getActivity(this, 0, mainMenuIntent, 0);
			views.setOnClickPendingIntent(R.id.link, mainMenuPendingIntent);

			appWidgetManager.updateAppWidget(appWidgetIds, views);
		}
	}

	// ////////////////////////////////

	private long mBoardcastStart = -2;
	private long mBoardcastEnd = -2;
	private DeadlineType mDeadlineType;

	private long getBoardcastStart() {
		if (mBoardcastStart == -2) {
			mBoardcastStart = MadokaCountdown.getDeadlineSettingStart(this);
		}
		return mBoardcastStart;
	}

	private long getBoardcastEnd() {
		if (mBoardcastEnd == -2) {
			mBoardcastEnd = MadokaCountdown.getDeadlineSettingEnd(this);
		}
		return mBoardcastEnd;
	}

	private DeadlineType getDeadlineType() {
		if (mDeadlineType == null) {
			mDeadlineType = MadokaCountdown.getDeadlineType(this);
		}
		return mDeadlineType;
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
		mMediaPlayer.setOnCompletionListener(mMediaPlayerListener);
		mMediaPlayer.setOnErrorListener(mMediaPlayerListener);

		mMediaPlayer.setVolume(1.0f, 1.0f);
		mMediaPlayer.start();

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

	Random random = new Random();

}
