package de.greencity.bladenightapp.android.network;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.builder.ToStringBuilder;

import android.content.Context;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTask;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.LatLong;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;
import fr.ocroquette.wampoc.common.Channel;

public class NetworkClient2 {

	public NetworkClient2(Context context) {
		NetworkClient2.context = context;
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

	static class WebSocketClientChannelAdapter implements Channel {
		private WebSocketClient client;
		public void setClient(WebSocketClient client) {
			this.client = client;
		}
		@Override
		public void handle(String message) throws IOException {
			client.send(message);
		}
	}

	public void connect() {
		if ( server == null)
			findServer();

		NetworkClient2.WebSocketClientChannelAdapter adapter = new NetworkClient2.WebSocketClientChannelAdapter();
		final WampClient wampClient = new WampClient(adapter);
		this.wampClient = wampClient;

		WebSocketClient.Listener listener = new WebSocketClient.Listener() {
			@Override
			public void onConnect() {
				Log.d(TAG, "Connected!");
			}

			@Override
			public void onMessage(String message) {
				Log.d(TAG, String.format("Got string message! %s", message));
				wampClient.handleIncomingMessage(message);
			}

			@Override
			public void onMessage(byte[] data) {
				Log.d(TAG, String.format("Got binary message!"));
			}

			@Override
			public void onDisconnect(int code, String reason) {
				Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
			}

			@Override
			public void onError(Exception error) {
				Log.e(TAG, "Error!", error);
			}
		};
		String url = getUrl("ws");
		WebSocketClient webSocketClient = new WebSocketClient(URI.create(url), listener, null);
		adapter.setClient(webSocketClient);

		webSocketClient.connect();
	}

	public void getAllEvents(RpcResultReceiver rpcResultReceiver) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ALL_EVENTS.getText();
		item.handler = rpcResultReceiver;
		callOrStore(item);
	}
	public void getRoute(String routeName, RpcResultReceiver rpcResultReceiver) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ROUTE.getText();
		item.handler = rpcResultReceiver;
		item.payload = routeName;
		callOrStore(item);
	}
	
		public void getActiveRoute(RpcResultReceiver rpcResultReceiver) {
			BacklogItem item = new BacklogItem();
			item.url = BladenightUrl.GET_ACTIVE_ROUTE.getText();
			item.handler = rpcResultReceiver;
			callOrStore(item);
		}
	
	
		public void getRealTimeData(RpcResultReceiver rpcResultReceiver) {
			BacklogItem item = new BacklogItem();
			item.url = BladenightUrl.GET_REALTIME_UPDATE.getText();
			item.handler = rpcResultReceiver;
	
			gpsInfo.isParticipating(ServiceUtils.isServiceRunning(context, GpsTrackerService.class));
			if ( lastKnownPosition != null ) {
				gpsInfo.setLatitude(lastKnownPosition.getLatitude());
				gpsInfo.setLongitude(lastKnownPosition.getLongitude());
			}
	
			item.payload = gpsInfo;
			callOrStore(item);
		}
	
		public void updateFromGpsTrackerService(LatLong lastKnownPosition) {
			this.lastKnownPosition = lastKnownPosition;
			getRealTimeData(new RpcResultReceiver() {
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
				}

				@Override
				public void onError() {
					// TODO Auto-generated method stub
				}
			});
		}
	
	private void callOrStore(BacklogItem item) {
//		if ( true ) {
			call(item);
//		}
//		else {
//			item.timestamp = System.currentTimeMillis();
//			backlogItems.add(item);
//		}
	}

	private void call(BacklogItem item) {
		// public <PayloadType> void call(String procedureId, RpcResultReceiver rpcResultHandler, PayloadType payload, Class<PayloadType> payloadType) throws IOException {
		try {
			wampClient.call(item.url, item.handler, item.payload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void downloadFile(String localPath, String remotePath, final AsyncDownloadTask.StatusHandler handler) {
		String url = getUrl("http") + "/" + remotePath;
		Log.i(TAG,"downloadFile: " + url + " to " + localPath);
		AsyncDownloadTask asyncDownloadTask = new AsyncDownloadTask(handler);
		asyncDownloadTask.execute(url, localPath);
	}

	static private Context context;
	static private final String TAG = "NetworkClient2";
	static private String server;
	final static private int port = 8081;
	private final GpsInfo gpsInfo = new GpsInfo();
	private LatLong lastKnownPosition;
	private WampClient wampClient;


	static class BacklogItem {
		public long timestamp;
		public String url;
		public RpcResultReceiver handler; 
		public Object payload;
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return ToStringBuilder.reflectionToString(this);
		}
	};
	// private List<BacklogItem> backlogItems = new ArrayList<BacklogItem>();

}
