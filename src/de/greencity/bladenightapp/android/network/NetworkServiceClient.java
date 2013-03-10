package de.greencity.bladenightapp.android.network;

import android.content.Context;
import android.content.Intent;

public class NetworkServiceClient {
	public NetworkServiceClient(Context context) {
		this.context = context;
	}

	public void bindToService() {
		Intent intent = new Intent(context, NetworkService.class);
		serviceConnection = new NetworkServiceClientConnection();
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

	private void sendSimpleIntent(String action) {
		Intent intent = new Intent(context, NetworkService.class);
		intent.setAction(action);
		context.startService(intent);
	}

	private Context context;
	private NetworkServiceClientConnection serviceConnection;
}
