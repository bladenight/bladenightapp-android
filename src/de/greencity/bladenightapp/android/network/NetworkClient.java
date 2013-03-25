package de.greencity.bladenightapp.android.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import android.content.Context;
import android.util.Log;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTask;
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

public class NetworkClient {

	public NetworkClient(Context context) {
		NetworkClient.context = context;
		gpsInfo.setDeviceId(DeviceId.getDeviceId(context));
		connect();
	}

	private void findServer() {
		Log.i(TAG, "Looking for server...");

		ServerFinder serverFinder = new ServerFinder(context, port);
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

	public void connect() {
		if ( wampConnection.isConnected() && wampConnection.isUsable() )
			return;

		if ( server == null)
			findServer();

		final String uri = getUrl("ws");
		Log.i(TAG, "Connecting to: " + uri);

		Wamp.ConnectionHandler handler  = new Wamp.ConnectionHandler() {
			@Override
			public void onOpen() {
				Log.d(TAG, "Status: Connected to " + uri);
				wampConnection.isUsable(true);
				while ( backlogItems.size() > 0 ) {
					BacklogItem item = backlogItems.remove(0);
					Log.d(TAG, "Emptying backlog: " + item);
					call(item);
				}
			}

			@Override
			public void onClose(int code, String reason) {
				Log.d(TAG, "Connection lost to " + uri);
				Log.d(TAG, "Reason:" + reason);
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

	//	private boolean ensureConnect() {
	//		if ( ! wampConnection.isUsable() )
	//			connect();
	//		return wampConnection.isUsable();
	//	}

	public void getAllEvents(CallHandler callHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ALL_EVENTS.getText();
		item.clazz = EventsListMessage.class;
		item.handler = callHandler;
		callOrStore(item);
	}

	public void getActiveRoute(CallHandler callHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ACTIVE_ROUTE.getText();
		item.clazz = RouteMessage.class;
		item.handler = callHandler;
		callOrStore(item);
	}

	public void getRoute(String routeName, CallHandler callHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ROUTE.getText();
		item.clazz = RouteMessage.class;
		item.handler = callHandler;
		item.parameters = new Object[] {routeName};
		callOrStore(item);
	}

	public void getRealTimeData(CallHandler callHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_REALTIME_UPDATE.getText();
		item.clazz = RealTimeUpdateData.class;
		item.handler = callHandler;

		gpsInfo.isParticipating(ServiceUtils.isServiceRunning(context, GpsTrackerService.class));
		if ( lastKnownPosition != null ) {
			gpsInfo.setLatitude(lastKnownPosition.getLatitude());
			gpsInfo.setLongitude(lastKnownPosition.getLongitude());
		}

		item.parameters = new Object[]{gpsInfo};
		callOrStore(item);
	}

	public void updateFromGpsTrackerService(LatLong lastKnownPosition) {
		this.lastKnownPosition = lastKnownPosition;
		getRealTimeData(new CallHandler() {
			@Override
			public void onResult(Object arg0) {
			}

			@Override
			public void onError(String arg0, String arg1) {
			}
		});
	}

	private void callOrStore(BacklogItem item) {
		if ( wampConnection.isUsable() ) {
			call(item);
		}
		else {
			item.timestamp = System.currentTimeMillis();
			backlogItems.add(item);
		}
	}

	private void call(BacklogItem item) {
		wampConnection.call(item.url, item.clazz, item.handler, item.parameters);
	}

	public void downloadFile(String localPath, String remotePath, final AsyncDownloadTask.StatusHandler handler) {
		String url = getUrl("http") + "/" + remotePath;
		Log.i(TAG,"downloadFile: " + url + " to " + localPath);
		AsyncDownloadTask asyncDownloadTask = new AsyncDownloadTask(handler);
		asyncDownloadTask.execute(url, localPath);
	}

	static private Context context;
	static private final String TAG = "NetworkClient";
	static private BladenightWampConnection wampConnection = new BladenightWampConnection();
	static private String server;
	final static private int port = 8081;
	private final GpsInfo gpsInfo = new GpsInfo();
	private LatLong lastKnownPosition;


	static class BacklogItem {
		public long timestamp;
		public String url;
		public Class<?> clazz;
		public CallHandler handler; 
		public Object[] parameters = new Object[0];
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return ToStringBuilder.reflectionToString(this);
		}
	};

	private List<BacklogItem> backlogItems = new ArrayList<BacklogItem>();

}
