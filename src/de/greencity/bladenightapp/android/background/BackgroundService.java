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
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.events.EventList;

public class BackgroundService extends IntentService {

    private static final String TAG = "BackgroundService";
    private BroadcastReceiversRegister broadcastReceiversRegister;
    private GlobalStateAccess globalStateAccess;

    private boolean gotEventList = false;
    public final static String EXTRA_RELEASE_WAKELOCK = "EXTRA_RELEASE_WAKELOCK";

    public BackgroundService() {
        super(TAG);
        Log.i(TAG, "BackgroundService.BackgroundService");
        globalStateAccess = new GlobalStateAccess(this);
    }

    private Handler handler;

    class LooperThread extends HandlerThread {
        public LooperThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "BackgroundService.onHandleIntent");

        createAndRegisterBroadcastReceiver();

        LooperThread thread = requestEventListInBackground();
        waitForServerResponse();
        killBackgroundThread(thread);

        if ( gotEventList ) {
            processEventList(new GlobalStateAccess(this).getEventList());
        }
        else {
            Log.w(TAG, "Timeout while retrieving event list.");
        }

        Log.i(TAG, "Completed service @ " + SystemClock.elapsedRealtime());

        scheduleNextExecution();

        unregisterReceiver();

        releaseWakeLockIfRequired(intent);
    }

    private void unregisterReceiver() {
        broadcastReceiversRegister.unregisterReceivers();
    }

    private void scheduleNextExecution() {
        new BackgroundHelper(this).scheduleNext();
    }

    private void killBackgroundThread(LooperThread thread) {
        // Make sure that the thread will wake up at quit:
        handler.sendEmptyMessageAtTime(1, System.currentTimeMillis()+5000);
        thread.quit();
    }

    private void waitForServerResponse() {
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
    }

    private LooperThread requestEventListInBackground() {
        LooperThread thread = new LooperThread("LooperThread");
        Log.i(TAG, "thread.start()");
        thread.start();
        Log.i(TAG, "thread.getLooper()");
        thread.getLooper(); // will block until the looper is ready
        Log.i(TAG, "mHandler.post");
        handler = new Handler(thread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                globalStateAccess.requestEventList();
            }
        });
        return thread;
    }

    private void createAndRegisterBroadcastReceiver() {
        broadcastReceiversRegister = new BroadcastReceiversRegister(this);
        broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_EVENT_LIST, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "onReceive got event list");
                gotEventList = true;
            }
        });
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
