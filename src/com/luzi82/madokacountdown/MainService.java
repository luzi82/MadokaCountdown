package com.luzi82.madokacountdown;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Random;
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
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;
import android.widget.RemoteViews;

public class MainService extends Service {

	public static String SETTINGCHANGE_CHAR = "MadokaCountdown.SETTINGCHANGE_CHAR";
	public static String UPDATE = "MadokaCountdown.UPDATE";
	public static String VOICE = "MadokaCountdown.VOICE";

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			MadokaCountdown.logd("action " + action);

			synchronized (MainService.this) {
				if (action.equals(VOICE)) {
					triggerVoice();
				} else if (action.equals(SETTINGCHANGE_CHAR)) {
					changeIconOnly(intent.getIntArrayExtra(MadokaCountdown.AVAILABLE_CHAR));
				}
				redraw(System.currentTimeMillis());
				startTimer();
			}
		}

	};

	synchronized void startTimer() {
		startAlarm(this);
	}

	synchronized void stopTimer() {
		endAlarm(this);
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

		initIntentFilter();

		changeIconOnly(null);
		
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				redraw(System.currentTimeMillis());
			}
		}, 0);
	}

	private void initIntentFilter() {
		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(UPDATE);
		commandFilter.addAction(VOICE);
		commandFilter.addAction(SETTINGCHANGE_CHAR);
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
		
		long next=System.currentTimeMillis();
		next/=60*60*1000;
		++next;
		next*=60*60*1000;
		
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC, next, AlarmManager.INTERVAL_HOUR, getAlarmPendingIntent(context));
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
		Deadline deadline = MadokaCountdown.getCurrentDeadline(getResources());
		if (now < deadline.time) {
			long diff = deadline.time - now;
			diff /= 24 * 60 * 60 * 1000;
			++diff;

			String s = String.format("%s: %d日", deadline.name, (int) diff);
			views.setTextViewText(R.id.txt, s);
		} else {
			String s = String.format("%s 発売中", deadline.name);
			views.setTextViewText(R.id.txt, s);
		}
		Intent voiceIntent = new Intent(MainService.VOICE);
		PendingIntent voicePendingIntent = PendingIntent.getBroadcast(this, 0, voiceIntent, 0);
		views.setOnClickPendingIntent(R.id.voiceButton, voicePendingIntent);

		Intent mainMenuIntent = new Intent(this, MainMenuActivity.class);
		PendingIntent mainMenuPendingIntent = PendingIntent.getActivity(this, 0, mainMenuIntent, 0);
		views.setOnClickPendingIntent(R.id.link, mainMenuPendingIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, views);
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
