package de.greencity.bladenightapp.android.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ServiceUtils {
    // TODO Java is complaining here: reference to generic types should be parameterized
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ( serviceClass.getName().equals(service.service.getClassName())) {
                // Log.i(TAG, "Service " + serviceClass.getCanonicalName() + " is running");
                return true;
            }
        }
        return false;
    }
    public static void startService(Context context, Class<?> serviceClass) {
        Log.i(TAG, "startService " + serviceClass.getCanonicalName());
        if ( ! isServiceRunning(context, serviceClass)) {
            Intent intent = new Intent(context.getApplicationContext(), serviceClass);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            }
            else {
                context.startService(intent);
            }
        }
    }
    public static void stopService(Context context, Class<?> serviceClass) {
        Log.i(TAG, "stopService " + serviceClass.getCanonicalName());
        if ( isServiceRunning(context, serviceClass)) {
            Intent intent = new Intent(context.getApplicationContext(), serviceClass);
            context.stopService(intent);
        }
    }

    final static String TAG = "ServiceUtils";
}
