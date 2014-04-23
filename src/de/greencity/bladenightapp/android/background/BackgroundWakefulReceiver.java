package de.greencity.bladenightapp.android.background;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BackgroundWakefulReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "BackgroundWakefulReceiver";

	@Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "BackgroundWakefulReceiver got intent: " + intent.toString());
        
    	Intent startServiceIntent = new Intent(context, BackgroundService.class);
        startServiceIntent.putExtra(BackgroundService.EXTRA_RELEASE_WAKELOCK, true);

        // Start the service, keeping the device awake while it is launching.
        Log.i("BackgroundWakefulReceiver", "Starting service @ " + SystemClock.elapsedRealtime());
        startWakefulService(context, startServiceIntent);
    }
}