package de.greencity.bladenightapp.android.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceUtils {
	// TODO Java is complaining here: reference to generic types should be parameterized
	public static boolean isServiceRunning(Activity activity, Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ( serviceClass.getName().equals(service.service.getClassName())) {
				Log.i(TAG, "Service " + serviceClass.getCanonicalName() + " is running");
				return true;
			}
		}
		return false;
	}
	public static void startService(Activity activity, Class<?> serviceClass) {
		Log.i(TAG, "startService " + serviceClass.getCanonicalName());
		if ( ! isServiceRunning(activity, serviceClass)) {
			Intent intent = new Intent(activity.getApplicationContext(), serviceClass);
			activity.startService(intent);
		}
	}
	public static void stopService(Activity activity, Class<?> serviceClass) {
		if ( isServiceRunning(activity, serviceClass)) {
			Intent intent = new Intent(activity.getApplicationContext(), serviceClass);
			activity.stopService(intent);
		}
	}

	final static String TAG = "ServiceUtils";
}
