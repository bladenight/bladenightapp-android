package de.greencity.bladenightapp.android.network;

import android.content.Context;
import android.util.Log;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.Wamp.CallHandler;
import de.tavendo.autobahn.WampConnection;

public class MyWampConnection {
	static final String TAG = "NetworkService";

	private WampConnection wampConnection;
	private Boolean isConnectionEstablished = false;
	private String server = "192.168.178.30";
	private int port = 8081;

	public synchronized boolean ensureConnection(Context context) {
		if (wampConnection == null)
			wampConnection = new WampConnection();

		if ( wampConnection.isConnected() && isConnectionEstablished) {
			Log.d(TAG,"Already connected");
			return true;
		}
		if ( server == null )
			findAndSetServer(context);
		if ( server == null ) {
			Log.e(TAG,"ensureConnection: Could not find the server.");
			return false;
		}
		tryToConnect();
		long timeout = 5000;
		long startTime = System.currentTimeMillis(); 
		while ( System.currentTimeMillis() - startTime < timeout && ! isConnectionEstablished ) {
			try {
				Log.d(TAG,"ensureConnection: waiting for connection to server...");
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Log.w(TAG,"ensureConnection: Interrupted while sleeping.");
				return false;
			}
		}
		Log.d(TAG,"ensureConnection: isConnectionEstablished="+isConnectionEstablished);
		return isConnectionEstablished;
	}

	private void getActiveEvent() {
		wampConnection.call(BladenightUrl.GET_ACTIVE_EVENT.getText(), EventMessage.class, new CallHandler() {
			@Override
			public void onError(String arg0, String arg1) {
				Log.e(TAG, arg0 + " " + arg1);
			}

			@Override
			public void onResult(Object object) {
				Log.d(TAG, "getActiveEvent.onResult : " + object.toString());
			}
		});
	}

	private void getAllEvents() {
		wampConnection.call(BladenightUrl.GET_ALL_EVENTS.getText(), EventMessage.class, new CallHandler() {
			@Override
			public void onError(String arg0, String arg1) {
				Log.e(TAG, arg0 + " " + arg1);
			}

			@Override
			public void onResult(Object object) {
				Log.d(TAG, "getAllEvents.onResult : " + object.toString());
			}
		});
	}

	private void tryToConnect() {
		final String uri = getServerUri();
		Log.i(TAG, "Connecting to: " + uri);

		Wamp.ConnectionHandler handler  = new Wamp.ConnectionHandler() {
			@Override
			public void onOpen() {
				Log.d(TAG, "Status: Connected to " + uri);
				isConnectionEstablished = true;
			}

			@Override
			public void onClose(int code, String reason) {
				isConnectionEstablished = false;
			}
		};
		wampConnection.connect(uri, handler);
	}

	public String getServerUri() {
		return String.format("ws://%s:%d", server,port);
	}

	private void findAndSetServer(Context context) {
		server = findServer(context);
	}

	private String findServer(Context context) {
		Log.i(TAG, "Looking for a host listening on port " + port + "...");
		String host;
		try {
			host = new ServerFinder(context, port).findServer();
		} catch (InterruptedException e) {
			Log.e(TAG, e.toString());
			return null;
		}
		if ( host == null ) {
			Log.e(TAG, "Could not find the server");
		}
		else {
			Log.i(TAG, "Server scan finished. Found server: " + host);
			return host;
		}
		return host;
	}

}
