package de.greencity.bladenightapp.android.network;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class NetworkServiceClientConnection implements ServiceConnection {
	protected boolean isBound = false;

	public NetworkServiceClientConnection() {
	}

	@Override
    public void onServiceConnected(ComponentName className, IBinder boundService) {
        Log.d(TAG, "onServiceConnected " + className);
        isBound = true;
        service = IAdditionService.Stub.asInterface(boundService);
    }
	
    // Called when the connection with the service disconnects unexpectedly
	@Override
    public void onServiceDisconnected(ComponentName className) {
        Log.d(TAG, "onServiceDisconnected " + className);
        isBound = false;
    }
    
    final private String TAG = "NetworkServiceConnection";
    public IAdditionService service;
}
