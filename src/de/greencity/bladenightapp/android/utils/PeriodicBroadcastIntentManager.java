package de.greencity.bladenightapp.android.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class PeriodicBroadcastIntentManager {
    protected List<PendingIntent> periodicIntents;
    protected Context context;

    public PeriodicBroadcastIntentManager(Context context) {
        periodicIntents = new ArrayList<PendingIntent> ();
        this.context = context;
    }

    public void schedulePeriodicBroadcastIntent(Intent intent, long period) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), period, pendingIntent);
        periodicIntents.add(pendingIntent);
    }

    public void cancelPeriodicBroadcastIntents() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        while ( periodicIntents.size() > 0 ) {
            PendingIntent pendingIntent = periodicIntents.remove(0);
            alarmManager.cancel(pendingIntent);
        }
    }

}
