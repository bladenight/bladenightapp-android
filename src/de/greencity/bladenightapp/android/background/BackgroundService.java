package de.greencity.bladenightapp.android.background;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.network.GlobalStateService;
import de.greencity.bladenightapp.android.network.GlobalStateService.NetworkServiceBinder;
import de.greencity.bladenightapp.android.utils.LocalBroadcastReceiversRegister;
import de.greencity.bladenightapp.events.EventList;

public class BackgroundService extends IntentService {

	private static final String TAG = "BackgroundService";
	private LocalBroadcastReceiversRegister broadcastReceiversRegister;
	private GlobalStateService globalStateService;
 
	private boolean gotEventList = false;
	public final static String EXTRA_RELEASE_WAKELOCK = "EXTRA_RELEASE_WAKELOCK";

	public BackgroundService() {
		super(TAG);
		Log.i(TAG, "BackgroundService.BackgroundService");
	}

	public BackgroundService(String name) {
		super(TAG);
		Log.i(TAG, "BackgroundService.BackgroundService");
	}

	private ServiceConnection globalStateServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			globalStateService = ((NetworkServiceBinder)binder).getService();
			globalStateService.requestEventList();
			Log.i(TAG, "onServiceConnected name="+name);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "onServiceDisconnected name="+name);
		}
	};


	@Override
	protected void onHandleIntent(Intent intent) {

		Log.i(TAG, "BackgroundService.onHandleIntent");

		bindToGlobalStateService();

		broadcastReceiversRegister = new LocalBroadcastReceiversRegister(this);
		broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_EVENT_LIST, new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "onReceive got event list");
				gotEventList = true;
			}
		});

		int i = 1;
		int imax = 10;
		while ( ! gotEventList && i < imax ) {
			try {
				i++;
				Log.i(TAG, "Running service " + i
						+ "/ " + imax + " @ " + SystemClock.elapsedRealtime());
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		if ( gotEventList ) {
			processEventList(new GlobalStateAccess(this).getEventList());
		}
		else {
			Log.w(TAG, "Timeout while retrieving event list.");
		}

		Log.i(TAG, "Completed service @ " + SystemClock.elapsedRealtime());

		new BackgroundHelper(this).scheduleNext();

		releaseWakeLockIfRequired(intent);
		broadcastReceiversRegister.unregisterReceivers();
		
	    unbindFromGlobalStateService();

	}

	private void unbindFromGlobalStateService() {
		if (globalStateServiceConnection != null) {
            unbindService(globalStateServiceConnection);
            globalStateServiceConnection = null;
        }
	}

	private void bindToGlobalStateService() {
		Intent bindIntent = new Intent(this, GlobalStateService.class);
		bindService(bindIntent, globalStateServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void releaseWakeLockIfRequired(Intent intent) {
		if ( intent.getBooleanExtra(EXTRA_RELEASE_WAKELOCK, true)) {
			Log.i(TAG, "Releasing wake lock");
			BackgroundWakefulReceiver.completeWakefulIntent(intent);
		}
	}

	private void processEventList(EventList eventList) {
		Log.i(TAG, "Processing event list: " + eventList);
	}

}
