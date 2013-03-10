package de.greencity.bladenightapp.android.network;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.Wamp.CallHandler;
import de.tavendo.autobahn.WampConnection;


public class NetworkService extends Service {

	static final String TAG = "NetworkService";

	private WampConnection wampConnection;
	private Boolean isConnectionEstablished = false;
	private String server = "192.168.178.30";
	private int port = 8081;

	private final IBinder binder = new Binder();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"onDestroy");
		if ( wampConnection.isConnected() )
			wampConnection.disconnect();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"onStartCommand " + intent.getAction());
		if ( Actions.CONNECT.equals(intent.getAction()) ) {
			ensureConnection();
		}
		else if ( Actions.DISCONNECT.equals(intent.getAction()) ) {
			wampConnection.disconnect();
		}
		else {
			if ( ! ensureConnection() ) {
				Log.e(TAG, "onStartCommand: No connection available");
			}
			handleCommandIntent(intent);
		}
		return START_STICKY;
	}

	private void handleCommandIntent(Intent intent) {
		Log.d(TAG, "handleCommandIntent: action=" + intent.getAction());
		if ( ! ensureConnection() ) {
			Log.e(TAG, "handleCommandIntent: could not connect, giving up");
			return;
		}
		if ( Actions.GET_ACTIVE_EVENT.equals(intent.getAction()) )
			getActiveEvent();
		else if ( Actions.GET_ALL_EVENTS.equals(intent.getAction()) )
			getAllEvents();
		else
			Log.e(TAG, "Unknown action " + intent.getAction());
	}

	public synchronized boolean ensureConnection() {
		if (wampConnection == null)
			wampConnection = new WampConnection();

		if ( wampConnection.isConnected() && isConnectionEstablished) {
			Log.d(TAG,"Already connected");
			return true;
		}
		if ( server == null )
			findAndSetServer();
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
				EventMessage msg = (EventMessage) object;
				// Toast.makeText(NetworkService.this, msg.toString(), Toast.LENGTH_LONG).show();
				// Log.d(TAG, "Got message " + msg.toString());
				Intent intent = new Intent(Actions.GOT_ACTIVE_EVENT);
				intent.putExtra("json", new Gson().toJson(object));
				NetworkService.this.sendBroadcast(intent);
			}
		}, 1);
	}

	private void getAllEvents() {
		wampConnection.call(BladenightUrl.GET_ALL_EVENTS.getText(), EventsListMessage.class, new CallHandler() {
			@Override
			public void onError(String arg0, String arg1) {
				Log.e(TAG, arg0 + " " + arg1);
			}

			@Override
			public void onResult(Object object) {
				EventsListMessage msg = (EventsListMessage) object;
				if ( msg == null ) {
					Log.e(TAG, "getAllEvents: Failed to cast");
					return;
				}
				// Toast.makeText(NetworkService.this, msg.toString(), Toast.LENGTH_LONG).show();
				// Log.d(TAG, "Got message " + msg.toString());
				Intent intent = new Intent(Actions.GOT_ALL_EVENTS);
				intent.putExtra("json", new Gson().toJson(object));
				NetworkService.this.sendBroadcast(intent);
			}
		}, 1);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG,"onBind");
		return binder;
	}

	private void tryToConnect() {
		final String uri = getServerUri();
		Log.i(TAG, "Connecting to: " + uri);

		Wamp.ConnectionHandler handler  = new Wamp.ConnectionHandler() {
			@Override
			public void onOpen() {
				Log.d(TAG, "Status: Connected to " + uri);
				Log.d(TAG,"TID=" + android.os.Process.myTid());
				Log.d(TAG,"PID=" + android.os.Process.myPid());
				isConnectionEstablished = true;
				Intent intent = new Intent(Actions.CONNECTED);
				intent.putExtra("uri", uri);
				NetworkService.this.sendBroadcast(intent);
			}

			@Override
			public void onClose(int code, String reason) {
				Log.d(TAG, "Connection lost to " + uri);
				Log.d(TAG, "Reason:" + reason);
				isConnectionEstablished = false;
				Intent intent = new Intent(Actions.DISCONNECTED);
				intent.putExtra("uri", uri);
				NetworkService.this.sendBroadcast(intent);
			}
		};
		wampConnection.connect(uri, handler);
	}

	public String getServerUri() {
		return String.format("ws://%s:%d", server,port);
	}

	private void findAndSetServer() {
		server = findServer();
	}

	private String findServer() {
		Log.i(TAG, "Looking for a host listening on port " + port + "...");
		String host;
		try {
			host = new ServerFinder(this, port).findServer();
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
