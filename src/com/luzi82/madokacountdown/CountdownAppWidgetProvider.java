package com.luzi82.madokacountdown;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class CountdownAppWidgetProvider extends AppWidgetProvider {

	static long mDeadline;
	static {
		GregorianCalendar deadline = new GregorianCalendar(TimeZone.getTimeZone("GMT+09"));
		deadline.set(2011, Calendar.APRIL, 22, 2, 40);
		mDeadline = deadline.getTime().getTime();
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		doUpdate(context, appWidgetManager, appWidgetIds, System.currentTimeMillis());

		Intent i = new Intent(context, MainService.class);
		context.startService(i);

		Intent updateIntent = new Intent(MainService.UPDATE);
		updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		context.sendBroadcast(updateIntent);
	}

	public static void doUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, long now) {
		int diff = (int) (mDeadline - (now + 500));
		diff /= 1000;

		int sec = diff % 60;
		diff /= 60;
		int min = diff % 60;
		diff /= 60;
		int hr = diff % 24;
		diff /= 24;
		int day = diff;

		String s = String.format("%d日 %02d:%02d:%02d", day, hr, min, sec);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
		views.setTextViewText(R.id.title, s);

		appWidgetManager.updateAppWidget(appWidgetIds, views);
	}
}
