package de.greencity.bladenightapp.android.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import de.greencity.bladenightapp.android.global.LocalBroadcast;

public class BroadcastReceiversRegister {
    public BroadcastReceiversRegister(Context context) {
        this.context = context;
    }

    public void registerReceiver(LocalBroadcast broadcast, BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter(broadcast.toString());
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter);
        registeredReceivers.add(receiver);
        Log.i(TAG,"Registered receiver for " + broadcast.toString() + "; " + receiver);
    }

    public void unregisterReceivers() {
        while ( registeredReceivers.size() > 0 ) {
            BroadcastReceiver r = registeredReceivers.remove(0);
            try {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(r);
            }
            catch (IllegalArgumentException e) {
                Log.e(TAG,"Failed to unregister receiver " + r + "\n" + e);
            }
        }
    }

    public int getNumberORegisteredReceivers() {
        return registeredReceivers.size();
    }

    private List<BroadcastReceiver> registeredReceivers = new ArrayList<BroadcastReceiver>();
    private Context context;
    private final String TAG = "BroadcastReceiversRegister";
}
