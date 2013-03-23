package de.greencity.bladenightapp.android.network;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;

import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTask;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.Wamp.CallHandler;
import de.tavendo.autobahn.WampOptions;

public class NetworkService extends Service {
	private final String TAG = "NetworkService";
	private BladenightWampConnection wampConnection = new BladenightWampConnection();
	private String server;
	private final BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
	private final LatLong lastKnownPosition = new LatLong(0, 0);
	private final GpsInfo gpsInfo = new GpsInfo("", true, 0, 0);

	final private int port = 8081;
//	final private long locationTimeout = 60000;

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
		connect();
		broadcastReceiversRegister.registerReceiver(NetworkIntents.GET_ALL_EVENTS, getAllEventsReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.GET_ACTIVE_ROUTE, getActiveRouteReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.GET_ROUTE, getRouteReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.GET_REAL_TIME_DATA, getRealTimeDataReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.DOWNLOAD_REQUEST, getDownloadRequestReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.LOCATION_UPDATE, updateLocationReceiver);
		
		gpsInfo.setDeviceId(DeviceId.getDeviceId(this));
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
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return new Binder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "onUnbind");
		return super.onUnbind(intent);
	}


	private void findServer() {
		if ( server != null)
			return;

		Log.i(TAG, "Looking for server...");

		ServerFinder serverFinder = new ServerFinder(this, port);
		try {
			server = serverFinder.findServer();
		} catch (InterruptedException e) {
			Log.w(TAG, e);
			return;
		}
		Log.i(TAG, "Server="+server);
	}

	private String getUrl(String protocol) {
		return protocol + "://" + server + ":" + port;
	}

	void connect() {
		findServer();

		final String uri = getUrl("ws");
		Log.i(TAG, "Connecting to: " + uri);

		Wamp.ConnectionHandler handler  = new Wamp.ConnectionHandler() {
			@Override
			public void onOpen() {
				Log.d(TAG, "Status: Connected to " + uri);
				sendBroadcast(new Intent(NetworkIntents.CONNECTED));
				wampConnection.isUsable(true);
			}

			@Override
			public void onClose(int code, String reason) {
				Log.d(TAG, "Connection lost to " + uri);
				Log.d(TAG, "Reason:" + reason);
				sendBroadcast(new Intent(NetworkIntents.DISCONNECTED));
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
			.setOutputIntentName(NetworkIntents.GOT_ALL_EVENTS)
			.build();

	private final BroadcastReceiver getActiveRouteReceiver = new BroadcastWampBridgeBuilder<String, RouteMessage>(String.class, RouteMessage.class)
			.setLogPrefix("getActiveRouteReceiver")
			.setWampConnection(wampConnection)
			.setUrl(BladenightUrl.GET_ACTIVE_ROUTE.getText())
			.setOutputIntentName(NetworkIntents.GOT_ACTIVE_ROUTE)
			.build();

	private final BroadcastReceiver getRouteReceiver = new BroadcastWampBridgeBuilder<String, RouteMessage>(String.class, RouteMessage.class)
			.setLogPrefix("getRouteReceiver")
			.setWampConnection(wampConnection)
			.setUrl(BladenightUrl.GET_ROUTE.getText())
			.setOutputIntentName(NetworkIntents.GOT_ROUTE)
			.build();

	private final BroadcastReceiver getRealTimeDataReceiver = new BroadcastReceiver() {
		// final private String logPrefix = "getRealTimeDataReceiver";
		@Override
		public void onReceive(Context context, Intent intent) {
			getRealTimeData();
		}

	};

	private final BroadcastReceiver updateLocationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LatLong latLong = null;
			Bundle extras = intent.getExtras();
			if ( extras != null ) {
				String inputJson = extras.getString("json");
				if ( inputJson != null ) {
					latLong = new Gson().fromJson(inputJson, LatLong.class);
				}
			}
			if ( latLong == null ) {
				Log.e(TAG, "updateLocationReceiver: Failed to get new coordinates");
				return;
			}
			lastKnownPosition.setLatitude(latLong.getLatitude());
			lastKnownPosition.setLongitude(latLong.getLongitude());
			getRealTimeData();
		}
	};

	private final BroadcastReceiver getDownloadRequestReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String remotePath = intent.getExtras().getString("remotePath");

			if ( remotePath == null ) {
				Log.e(TAG, "remotePath is null");
				return;
			}

			String localPath = intent.getExtras().getString("localPath");

			if ( localPath == null ) {
				Log.e(TAG, "localPath is null");
				return;
			}

			String url = getUrl("http") + "/" + remotePath;

			AsyncDownloadTask asyncDownloadTask = new AsyncDownloadTask(context, remotePath) {
				@Override
				public void onDownloadFailure() {
					Log.i(TAG, "onDownloadFailure");
					Intent intent = new Intent(NetworkIntents.DOWNLOAD_FAILURE);
					intent.putExtra("id", remotePath);
					sendBroadcast(intent);
				}
				@Override
				public void onDownloadSuccess() {
					Log.i(TAG, "onDownloadSuccess");
					Intent intent = new Intent(NetworkIntents.DOWNLOAD_SUCCESS);
					intent.putExtra("id", remotePath);
					sendBroadcast(intent);
				}
			};
			asyncDownloadTask.execute(url, localPath);
		}

	};

	protected void getRealTimeData() {
		final String logPrefix = "getRealTimeData";
		if ( ! wampConnection.isUsable() ) {
			Log.w(TAG, logPrefix + ": Not connected");
			sendBroadcast(new Intent(NetworkIntents.CONNECT));
			return;
		}

		CallHandler callHandler = new CallHandler() {
			@Override
			public void onError(String arg0, String arg1) {
				Log.e(TAG, logPrefix + " onError: " + arg0 + " " + arg1);
			}

			@Override
			public void onResult(Object object) {
				RealTimeUpdateData msg = (RealTimeUpdateData) object;
				if ( msg == null ) {
					Log.e(TAG, logPrefix+" Failed to cast");
					return;
				}
				Intent intent = new Intent(NetworkIntents.GOT_REAL_TIME_DATA);
				intent.putExtra("json", new Gson().toJson(msg));
				sendBroadcast(intent);
			}
		};
		
		gpsInfo.setLatitude(lastKnownPosition.getLatitude());
		gpsInfo.setLongitude(lastKnownPosition.getLongitude());
		gpsInfo.isParticipating(ServiceUtils.isServiceRunning(this, GpsTrackerService.class));

		String url = BladenightUrl.GET_REALTIME_UPDATE.getText();
			wampConnection.call(url, RealTimeUpdateData.class, callHandler, gpsInfo);
	}
}
