package de.greencity.bladenightapp.android.background;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.utils.LocalBroadcastReceiversRegister;
import de.greencity.bladenightapp.events.EventList;

public class BackgroundService extends IntentService {

	private static final String TAG = "BackgroundService";
	private LocalBroadcastReceiversRegister broadcastReceiversRegister;
	// private GlobalStateService globalStateService;
	private GlobalStateAccess globalStateAccess;

	private boolean gotEventList = false;
	public final static String EXTRA_RELEASE_WAKELOCK = "EXTRA_RELEASE_WAKELOCK";

	public BackgroundService() {
		super(TAG);
		Log.i(TAG, "BackgroundService.BackgroundService");
		globalStateAccess = new GlobalStateAccess(this);
	}

	//	private ServiceConnection globalStateServiceConnection = new ServiceConnection() {
	//		@Override
	//		public void onServiceConnected(ComponentName name, IBinder binder) {
	//			globalStateService = ((NetworkServiceBinder)binder).getService();
	//			globalStateService.requestEventList();
	//			Log.i(TAG, "onServiceConnected name="+name);
	//		}
	//
	//		@Override
	//		public void onServiceDisconnected(ComponentName name) {
	//			Log.i(TAG, "onServiceDisconnected name="+name);
	//		}
	//	};

	public Handler mHandler;


	class LooperThread extends HandlerThread {
		public LooperThread(String name) {
			super(name);
		}

		@Override
		protected void onLooperPrepared() {
			Log.i(TAG, "onLooperPrepared");
			super.onLooperPrepared();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.i(TAG, "BackgroundService.onHandleIntent");

		// bindToGlobalStateService();

		broadcastReceiversRegister = new LocalBroadcastReceiversRegister(this);
		broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_EVENT_LIST, new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "onReceive got event list");
				gotEventList = true;
			}
		});

		LooperThread thread = new LooperThread("LooperThread");
		Log.i(TAG, "thread.start()");
		thread.start();
		Log.i(TAG, "thread.getLooper()");
		thread.getLooper();
		Log.i(TAG, "mHandler.post");
		mHandler = new Handler(thread.getLooper());
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				globalStateAccess.requestEventList();
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

		mHandler.sendEmptyMessageAtTime(1, System.currentTimeMillis()+2000);
		thread.quit();
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

		// unbindFromGlobalStateService();

	}

	//	private void unbindFromGlobalStateService() {
	//		if (globalStateServiceConnection != null) {
	//            unbindService(globalStateServiceConnection);
	//            globalStateServiceConnection = null;
	//        }
	//	}
	//
	//	private void bindToGlobalStateService() {
	//		Intent bindIntent = new Intent(this, GlobalStateService.class);
	//		bindService(bindIntent, globalStateServiceConnection, Context.BIND_AUTO_CREATE);
	//	}
	//
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
