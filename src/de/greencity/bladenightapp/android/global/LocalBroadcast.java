package de.greencity.bladenightapp.android.global;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public enum LocalBroadcast {
	GOT_EVENT_LIST("got-events"),
	GOT_ANNOUNCEMENTS("got-announcements"),
	GOT_REALTIME_DATA("got-realtime-data"),
	GOT_GPS_UPDATE("got-gps-update")
	;
	
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
    	Log.i(TAG, "Sending local broadcast: " + text);
    }
    
    private final String text;
    private final static String TAG = "LocalBroadcast";
}
