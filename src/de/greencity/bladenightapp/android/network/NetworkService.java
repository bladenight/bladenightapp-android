package de.greencity.bladenightapp.android.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.WampOptions;

public class NetworkService extends Service {
	private final String TAG = "NetworkService";
	private BladenightWampConnection wampConnection = new BladenightWampConnection();
	private String server;
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this); 
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
		connect();
		broadcastReceiversRegister.registerReceiver(Actions.GET_ALL_EVENTS, getAllEventsReceiver);
		broadcastReceiversRegister.registerReceiver(Actions.GET_ACTIVE_ROUTE, getActiveRouteReceiver);
		broadcastReceiversRegister.registerReceiver(Actions.GET_REAL_TIME_DATA, getRealTimeDataReceiver);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		broadcastReceiversRegister.unregisterReceivers();
	}


	@Override
	public void onRebind(Intent intent) {
		Log.i(TAG, "onRebind");
		super.onRebind(intent);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onCreateCommand");
		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "onUnbind");
		return super.onUnbind(intent);
	}


	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return new Binder();
	}

	private void findServer() {
		if ( server != null)
			return;

		Log.i(TAG, "Looking for server...");

		int port = 8081;
		ServerFinder serverFinder = new ServerFinder(this, port);
		try {
			server = serverFinder.findServer();
		} catch (InterruptedException e) {
			Log.w(TAG, e);
			return;
		}
		Log.i(TAG, "Server="+server);
	}
	
	void connect() {
		findServer();
		
		final String uri = "ws://" + server + ":8081";
		Log.i(TAG, "Connecting to: " + uri);

		Wamp.ConnectionHandler handler  = new Wamp.ConnectionHandler() {
			@Override
			public void onOpen() {
				Log.d(TAG, "Status: Connected to " + uri);
				sendBroadcast(new Intent(Actions.CONNECTED));
				wampConnection.isUsable(true);
			}

			@Override
			public void onClose(int code, String reason) {
				Log.d(TAG, "Connection lost to " + uri);
				Log.d(TAG, "Reason:" + reason);
				sendBroadcast(new Intent(Actions.DISCONNECTED));
				wampConnection.isUsable(false);
			}

		};

		WampOptions wampOptions = new WampOptions();

		// Default options, copied from de/tavendo/autobahn/WampConnection.java
		wampOptions.setReceiveTextMessagesRaw(true);
		wampOptions.setMaxMessagePayloadSize(64*1024);
		wampOptions.setMaxFramePayloadSize(64*1024);
		wampOptions.setTcpNoDelay(true);

		// Our own options:
		wampOptions.setReconnectInterval(5000);
		wampOptions.setSocketConnectTimeout(60*60*1000);

		wampConnection.connect(uri, handler, wampOptions);
	}

	private final BroadcastReceiver getAllEventsReceiver = new BroadcastWampBridgeBuilder<String, EventsListMessage>(String.class, EventsListMessage.class)
			.setLogPrefix("getAllEventsReceiver")
			.setWampConnection(wampConnection)
			.setUrl(BladenightUrl.GET_ALL_EVENTS.getText())
			.setOutputIntentName(Actions.GOT_ALL_EVENTS)
			.build();

	private final BroadcastReceiver getActiveRouteReceiver = new BroadcastWampBridgeBuilder<String, RouteMessage>(String.class, RouteMessage.class)
			.setLogPrefix("getActiveRouteReceiver")
			.setWampConnection(wampConnection)
			.setUrl(BladenightUrl.GET_ACTIVE_ROUTE.getText())
			.setOutputIntentName(Actions.GOT_ACTIVE_ROUTE)
			.build();

	private final BroadcastReceiver getRealTimeDataReceiver = new BroadcastWampBridgeBuilder<String, RealTimeUpdateData>(String.class, RealTimeUpdateData.class)
			.setLogPrefix("getRealTimeDataReceiver")
			.setWampConnection(wampConnection)
			.setUrl(BladenightUrl.GET_REALTIME_UPDATE.getText())
			.setOutputIntentName(Actions.GOT_REAL_TIME_DATA)
			.build();

}
