package de.greencity.bladenightapp.android.network;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

public class ServiceUtils {
	// TODO Java is complaining here: reference to generic types should be parameterized
	public static boolean isServiceRunning(Context context, Class serviceClass) {
		ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ( serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	public static void startService(Context context, Class serviceClass) {
		if ( ! isServiceRunning(context, serviceClass)) {
			Intent intent = new Intent(context.getApplicationContext(), serviceClass);
			context.startService(intent);
		}
	}
	public static void stopService(Activity activity, Class serviceClass) {
		if ( isServiceRunning(activity, serviceClass)) {
			Intent intent = new Intent(activity.getApplicationContext(), serviceClass);
			activity.stopService(intent);
		}
	}


}
