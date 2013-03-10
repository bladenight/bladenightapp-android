package de.greencity.bladenightapp.android.network;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

public class NetworkServiceClient {
	public NetworkServiceClient(Context context) {
		this.context = context;
	}

	public void bindToService() {
		Log.d(TAG,"bindToService");
		Intent intent = new Intent(context, NetworkService.class);
		context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void unbindFromService() {
		context.unbindService(serviceConnection);
	}

	public void connectToServer() {
		sendSimpleIntent(Actions.CONNECT);
	}
	
	public void disconnectFromServer() {
		sendSimpleIntent(Actions.DISCONNECT);
	}
	
	public void getActiveEvent() {
		sendSimpleIntent(Actions.GET_ACTIVE_EVENT);
	}

	public void getAllEvents() {
		sendSimpleIntent(Actions.GET_ALL_EVENTS);
	}
	
	public void add() throws RemoteException {
		Log.d(TAG,"add");
		int result = serviceConnection.service.add(2, 2);
		Log.d(TAG,"add result:" + result);
	}

	private void sendSimpleIntent(String action) {
		Log.d(TAG,"sendSimpleIntent");
		Intent intent = new Intent();
		intent.setAction(action);
		context.sendBroadcast(intent);
	}

	private Context context;
	private NetworkServiceClientConnection serviceConnection;
	private static final String TAG = "NetworkServiceClient";
}
