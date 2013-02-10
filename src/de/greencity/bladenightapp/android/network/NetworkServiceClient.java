package de.greencity.bladenightapp.android.network;

import de.greencity.bladenightapp.android.debug.DebugActivity;
import android.content.Context;
import android.content.Intent;
import de.greencity.bladenightapp.android.Actions;

public class NetworkServiceClient {
	public NetworkServiceClient(Context context) {
		this.context = context;
	}

	public void bindToService() {
		Intent intent = new Intent(context, NetworkService.class);
		serviceConnection = new NetworkServiceConnection();
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
	
	public void findServer() {
		sendSimpleIntent(Actions.FIND_SERVER);
	}

	public void getActiveEvent() {
		sendSimpleIntent(Actions.GET_ACTIVE_EVENT);
	}

	private void sendSimpleIntent(String action) {
		Intent intent = new Intent(context, NetworkService.class);
		intent.setAction(action);
		context.startService(intent);
	}

	private Context context;
	NetworkServiceConnection serviceConnection;
}
