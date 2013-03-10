package de.greencity.bladenightapp.android.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class NetworkServiceClientActivity extends Activity {

	@Override
	protected void onStart() {
		super.onStart();
		bindToService();
	}	

	@Override
	protected void onStop() {
		super.onStop();
		unbindFromService();
	}

	protected void bindToService() {
		Intent intent = new Intent(this, NetworkService.class);
		serviceConnection = new NetworkServiceClientConnection();
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	protected void unbindFromService() {
		unbindService(serviceConnection);
	}

	protected void connectToServer() {
		sendSimpleIntent(Actions.CONNECT);
	}
	
	protected void disconnectFromServer() {
		sendSimpleIntent(Actions.DISCONNECT);
	}
	
	protected void getActiveEvent() {
		sendSimpleIntent(Actions.GET_ACTIVE_EVENT);
	}

	protected void sendSimpleIntent(String action) {
		Intent intent = new Intent(this, NetworkService.class);
		intent.setAction(action);
		startService(intent);
	}

	NetworkServiceClientConnection serviceConnection;
}
