package de.greencity.bladenightapp.android.global;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public enum LocalBroadcast {
    GOT_EVENT_LIST(C.PACKAGE+"gotevents"),
    GOT_ANNOUNCEMENTS(C.PACKAGE+"gotannouncements"),
    GOT_REALTIME_DATA(C.PACKAGE+"gotrealtimedata"),
    GOT_GPS_UPDATE(C.PACKAGE+"gotgpsupdate")
    ;

    static private class C {
        public final static String PACKAGE = "de.greencity.bladenightapp.android.global.";
    }
    private LocalBroadcast(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public void send(Context context) {
        Intent intent = new Intent(text);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Log.i(TAG, "Sending broadcast: " + text);
    }

    private final String text;
    private final static String TAG = "LocalBroadcast";
}
