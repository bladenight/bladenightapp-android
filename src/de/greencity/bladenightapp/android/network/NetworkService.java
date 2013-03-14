package de.greencity.bladenightapp.android.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;

import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.Wamp.CallHandler;
import de.tavendo.autobahn.WampConnection;
import de.tavendo.autobahn.WampOptions;

public class NetworkService extends Service {
	private final String TAG = "NetworkService";
	private WampConnection wampConnection;
	private boolean isConnected = false;
	private String server;
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this); 
	
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
		connect();
		broadcastReceiversRegister.registerReceiver(Actions.GET_ALL_EVENTS, getAllEventsReceiver);
		broadcastReceiversRegister.registerReceiver(Actions.GET_ACTIVE_ROUTE, getActiveRouteReceiver);
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
				isConnected = true;
			}

			@Override
			public void onClose(int code, String reason) {
				Log.d(TAG, "Connection lost to " + uri);
				Log.d(TAG, "Reason:" + reason);
				sendBroadcast(new Intent(Actions.DISCONNECTED));
				isConnected = false;
			}

		};
		wampConnection = new WampConnection();
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

	private final BroadcastReceiver getAllEventsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"getAllEventsReceiver.onReceive");

			if ( ! isConnected ) {
				Log.w(TAG, "getAllEventsReceiver: Not connected");
				return;
			}

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
					Intent intent = new Intent(Actions.GOT_ALL_EVENTS);
					intent.putExtra("json", new Gson().toJson(object));
					NetworkService.this.sendBroadcast(intent);
				}
			});
		}
	};

	private final BroadcastReceiver getActiveRouteReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"getActiveRouteReceiver.onReceive");

			if ( ! isConnected ) {
				Log.w(TAG, "getActiveRouteReceiver: Not connected");
				return;
			}

			wampConnection.call(BladenightUrl.GET_ACTIVE_ROUTE.getText(), RouteMessage.class, new CallHandler() {
				@Override
				public void onError(String arg0, String arg1) {
					Log.e(TAG, arg0 + " " + arg1);
				}

				@Override
				public void onResult(Object object) {
					RouteMessage msg = (RouteMessage) object;
					if ( msg == null ) {
						Log.e(TAG, "getActiveRoute: Failed to cast");
						return;
					}
					Log.d(TAG, "getActiveRoute: " + object.toString());
					Log.d(TAG, "getActiveRoute: " + msg.toString());
					Intent intent = new Intent(Actions.GOT_ACTIVE_ROUTE);
					intent.putExtra("json", new Gson().toJson(msg));
					NetworkService.this.sendBroadcast(intent);
				}
			});
		}
	};
}
