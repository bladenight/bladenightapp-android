package de.greencity.bladenightapp.android.network;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import de.greencity.bladenightapp.android.Actions;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.Wamp.CallHandler;
import de.tavendo.autobahn.WampConnection;


public class NetworkService extends Service {

	static final String TAG = "NetworkService";

	private Wamp wampConnection;
	private String server = "192.168.43.175";
	private int port = 8081;

	private final IBinder binder = new Binder();

	@Override
	public void onCreate() {
		Log.d(TAG,"onCreate");
		wampConnection = new WampConnection();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"onDestroy");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"onStartCommand " + intent.getAction());
		if ( Actions.FIND_SERVER.equals(intent.getAction()) )
			findAndSetServer();
		else if ( Actions.CONNECT.equals(intent.getAction()) )
			tryToConnect();
		else if ( Actions.GET_ACTIVE_EVENT.equals(intent.getAction()) )
			getActiveEvent();
		else
			Log.e(TAG, "Unknown action " + intent.getAction());
		return START_STICKY;
	}

	private void getActiveEvent() {
		if ( wampConnection.isConnected() == false )
			Log.d(TAG, "Cannot retrieve commands, not connected");

		wampConnection.call(BladenightUrl.GET_ACTIVE_EVENT.getText(), EventMessage.class, new CallHandler() {
			@Override
			public void onError(String arg0, String arg1) {
				Log.e(TAG, arg0 + " " + arg1);
			}

			@Override
			public void onResult(Object object) {
				EventMessage msg = (EventMessage) object;
				Toast.makeText(NetworkService.this, msg.toString(), Toast.LENGTH_LONG).show();
				Log.d(TAG, "Got message " + msg.toString());
				Intent intent = new Intent(Actions.GOT_ACTIVE_EVENT);
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
				Intent intent = new Intent(Actions.CONNECTED);
				intent.putExtra("uri", uri);
				NetworkService.this.sendBroadcast(intent);
			}

			@Override
			public void onClose(int code, String reason) {
				Log.d(TAG, "Connection lost to " + uri);
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
		if ( server != null ) {
			Intent intent = new Intent(Actions.SERVER_FOUND);
			intent.putExtra("uri", getServerUri());
			NetworkService.this.sendBroadcast(intent);
		}
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

//wampConnection.call(BladenightUrl.GET_ACTIVE_EVENT.getText(), EventMessage.class, new CallHandler() {
//@Override
//public void onError(String arg0, String arg1) {
//	// TODO Auto-generated method stub
//	
//}
//
//@Override
//public void onResult(Object object) {
//	EventMessage msg = (EventMessage) object;
//	// Toast.makeText(MainActivity.this, msg.toString(), Toast.LENGTH_LONG);
//	Log.d(TAG, "Got message " + msg.toString());
//}
//}, 1);
