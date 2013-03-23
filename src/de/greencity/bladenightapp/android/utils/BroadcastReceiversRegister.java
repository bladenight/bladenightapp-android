package de.greencity.bladenightapp.android.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

public class BroadcastReceiversRegister {
	public BroadcastReceiversRegister(Context context) {
		this.context = context;
	}

	public void registerReceiver(String action, BroadcastReceiver receiver) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(action);
		context.registerReceiver(receiver, intentFilter);
		registeredReceivers.add(receiver);
		Log.i(TAG,"Registered receiver: " + receiver);
	}

	public void unregisterReceivers() {
		while ( registeredReceivers.size() > 0 ) {
			BroadcastReceiver r = registeredReceivers.remove(0);
			try { 
				context.unregisterReceiver(r);
			}
			catch (IllegalArgumentException e) {
				Log.e(TAG,"Failed to unregister receiver " + r + "\n" + e);
			}
		}
	}

	private List<BroadcastReceiver> registeredReceivers = new ArrayList<BroadcastReceiver>();
	private Context context;
	private final String TAG = "BroadcastReceiversRegister";
}
