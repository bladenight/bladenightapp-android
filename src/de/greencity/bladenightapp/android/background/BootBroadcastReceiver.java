package de.greencity.bladenightapp.android.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			configureAlarm(context);
		}
	}

	private void configureAlarm(Context context) {
		//		AlarmManager alarmManager;
		//		PendingIntent alarmIntent;
		//
		//		alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		//		Intent intent = new Intent(context, AlarmReceiver.class);
		//		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		//
		//		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		//				AlarmManager.INTERVAL_FIFTEEN_MINUTES,
		//				AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
	}

}
