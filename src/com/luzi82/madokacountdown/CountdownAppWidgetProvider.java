package com.luzi82.madokacountdown;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

public class CountdownAppWidgetProvider extends AppWidgetProvider {

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

	static Intent link = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.twitter.com/madoka_magica"));

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
			PendingIntent pi = PendingIntent.getActivity(context, 0, link, 0);
			views.setOnClickPendingIntent(R.id.link, pi);

			appWidgetManager.updateAppWidget(appWidgetIds, views);
		}
	}
}
