package de.greencity.bladenightapp.android.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

public class BroadcastReceiversRegister {
	public BroadcastReceiversRegister(Context context) {
		this.context = context;
	}
	
	public void registerReceiver(String action, BroadcastReceiver receiver) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(action);
		context.registerReceiver(receiver, intentFilter);
		registeredReceivers.add(receiver);
	}

	public void unregisterReceivers() {
		while ( registeredReceivers.size() > 0 )
			context.unregisterReceiver(registeredReceivers.remove(0));
	}

	private List<BroadcastReceiver> registeredReceivers = new ArrayList<BroadcastReceiver>();
	private Context context;
}
