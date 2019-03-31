package de.greencity.bladenightapp.android.cache;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;

public class CacheWriterReceiver extends BroadcastReceiver {

    final static private String TAG = "CacheWriterReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Got intent="+intent.getAction());
        if ( LocalBroadcast.GOT_EVENT_LIST.toString().equals(intent.getAction()) ) {
            onGotEventList(context, intent);
        }
    }

    private void onGotEventList(Context context, Intent intent) {
        Log.i(TAG,"onGotEventList");
        new EventsCache(context).write(new GlobalStateAccess(context).getEventList());
    }

}
