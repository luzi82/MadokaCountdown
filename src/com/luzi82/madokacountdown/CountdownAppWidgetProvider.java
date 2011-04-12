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
		// GregorianCalendar deadline = new GregorianCalendar();
		deadline.set(2011, Calendar.APRIL, 22, 2, 40);
		mDeadline = deadline.getTime().getTime();
	}

	// @Override
	// public void onEnabled(Context context) {
	// super.onEnabled(context);
	//
	// Intent i = new Intent(context, MainService.class);
	// context.startService(i);
	// }

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		doUpdate(context, appWidgetManager, appWidgetIds);

		Intent i = new Intent(context, MainService.class);
		context.startService(i);

		Intent updateIntent = new Intent(MainService.UPDATE);
		updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		context.sendBroadcast(updateIntent);
	}

	public static void doUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// GregorianCalendar nn = new
		// GregorianCalendar(TimeZone.getTimeZone("UTC+08"));
		GregorianCalendar nn = new GregorianCalendar();
		long now = nn.getTime().getTime();
		// long now = System.currentTimeMillis();
		int diff = (int) (mDeadline - now);
		diff /= 1000;

		int sec = diff % 60;
		diff /= 60;
		int min = diff % 60;
		diff /= 60;
		int hr = diff % 24;
		diff /= 24;
		int day = diff;

		// StringBuffer sb = new StringBuffer();
		// sb.append();
		// // sb.append(day);
		// // sb.append(",");
		// // sb.append(hr);
		// // sb.append(":");
		// // sb.append(min);
		// // sb.append(":");
		// // sb.append(sec);
		// // sb.append(diff);
		// // sb.append("s");
		String s = String.format("%d日 %02d:%02d:%02d", day, hr, min, sec);

		// Log.d("CountdownAppWidgetProvider", s);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
		views.setTextViewText(R.id.title, s);

		appWidgetManager.updateAppWidget(appWidgetIds, views);
	}
}
