package de.greencity.bladenightapp.android.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class BackgroundHelper {
    private static final String TAG = "BackgroundHelper";
    private Context context;

    public BackgroundHelper(Context context) {
        this.context = context;
    }

    final public void scheduleNext() {

        Log.i(TAG, "scheduleNext");
        Log.i(TAG, "SystemClock.elapsedRealtime()="+SystemClock.elapsedRealtime());

        Intent intent = new Intent(context, BackgroundWakefulReceiver.class);
        intent.setAction("android.intent.action.NOTIFY");

        final int requestCode = 123;
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        Log.i(TAG, "alarmIntent="+alarmIntent);

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                12 * 3600 * 1000, alarmIntent);
    }

}
