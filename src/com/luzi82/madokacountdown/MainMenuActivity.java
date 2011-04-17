package com.luzi82.madokacountdown;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.Bundle;

public class MainMenuActivity extends Activity {

	static final Intent OFFICAL_LINK = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.madoka-magica.com/"));
	static final Intent TWITTER_LINK = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.twitter.com/madoka_magica"));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		showDialog(0);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ArrayList<String> widgetTypeList = new ArrayList<String>();
		widgetTypeList.add("Offical site");
		widgetTypeList.add("Twitter");
		widgetTypeList.add("Settings");

		AlertDialog.Builder ab = new Builder(this);
		ab.setTitle(R.string.app_name);
		ab.setItems(widgetTypeList.toArray(new String[0]), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					startActivity(OFFICAL_LINK);
					break;
				case 1:
					startActivity(TWITTER_LINK);
					break;
				case 2:
					startActivity(new Intent(MainMenuActivity.this, SettingActivity.class));
					break;
				}
				MainMenuActivity.this.finish();
			}
		});
		ab.setCancelable(true);
		ab.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				MainMenuActivity.this.finish();
			}
		});
		return ab.create();
	}

	protected void onPause() {
		super.onPause();
		if (!isFinishing()) {
			finish();
		}
	}

}
