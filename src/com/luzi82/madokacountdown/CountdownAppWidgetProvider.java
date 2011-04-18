package com.luzi82.madokacountdown;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class CountdownAppWidgetProvider extends AppWidgetProvider {

	// static Intent link = new Intent(Intent.ACTION_VIEW,
	// Uri.parse("http://mobile.twitter.com/madoka_magica"));

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		Intent i = new Intent(context, MainService.class);
		context.startService(i);

		Intent updateIntent = new Intent(MainService.UPDATE);
		updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		context.sendBroadcast(updateIntent);

//		Intent settingChangeIntent = new Intent(MainService.SETTINGCHANGE_CHAR);
//		settingChangeIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
//		context.sendBroadcast(settingChangeIntent);
	}

}
