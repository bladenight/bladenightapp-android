package de.greencity.bladenightapp.android.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang3.builder.ToStringBuilder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import de.greencity.bladenightapp.android.admin.AdminUtilities;
import de.greencity.bladenightapp.android.network.BladenightWampClient.State;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTask;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.AdminMessage;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventMessage.EventStatus;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.network.messages.FriendsMessage;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.network.messages.RelationshipOutputMessage;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.network.messages.RouteNamesMessage;
import de.greencity.bladenightapp.network.messages.SetActiveRouteMessage;
import de.greencity.bladenightapp.network.messages.SetActiveStatusMessage;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WelcomeListener;
import fr.ocroquette.wampoc.common.Channel;
import fr.ocroquette.wampoc.messages.CallResultMessage;

public class NetworkClient implements LocationListener {

	public NetworkClient(Context context) {
		NetworkClient.context = context;
	}

	private static synchronized void findServer() {
		if ( System.currentTimeMillis() - lookingForServerTimestamp < 10000) {
			Log.i(TAG, "Already looking for server ("+lookingForServerTimestamp+")");
			return;
		}

		Log.i(TAG, " Looking for server...");
		lookingForServerTimestamp = System.currentTimeMillis();

		ServerFinderAsyncTask task = new ServerFinderAsyncTask(context) {
			@Override
			protected void onPostExecute(String foundServer) {
				Log.i(TAG, "Found server="+foundServer);
				if ( foundServer != null ) {
					server = foundServer;
					onServerFound();
				}
				lookingForServerTimestamp = 0;
			}
		};
		task.execute(port);
	}

	protected static void onServerFound() {
		connect();
	}

	private static String getUrl(String protocol) {
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

	private static synchronized void connect() {
		Log.i(TAG, "connect()");
		if ( bladenightWampClient.getState() == State.CONNECTING ||  bladenightWampClient.getState() == State.SHAKING_HANDS ) {
			Log.i(TAG, "Already connecting");
			if ( System.currentTimeMillis() - connectingSinceTimestamp > CONNECT_TIMEOUT)
				Log.i(TAG, "Connection request timed out");
			else
				return;
		}
		if ( server == null) {
			findServer();
			return;
		}

		String protocol = "ws";

		if ( "wss".equals(protocol) ) {
			try {
				WebSocketClient.setCustomSslFactory(getSSLSocketFactory());
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}

		URI uri = URI.create(getUrl(protocol));
		bladenightWampClient.setWelcomeListener(new WelcomeListener() {
			@Override
			public void onWelcome() {
				Log.i(TAG, "onWelcome()");
				NetworkClient.processBacklog();
			}
		});
		connectingSinceTimestamp = System.currentTimeMillis();
		bladenightWampClient.connect(uri);
	}

	private static javax.net.ssl.SSLSocketFactory getSSLSocketFactory() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		final InputStream trustStoreLocation = context.getResources().openRawResource(R.raw.client_truststore); 
		final String trustStorePassword = "iosfe45047asdf";

		final InputStream keyStoreLocation = context.getResources().openRawResource(R.raw.client_keystore); 
		final String keyStorePassword = "iosfe45047asdf";

		final KeyStore trustStore = KeyStore.getInstance("BKS");
		trustStore.load(trustStoreLocation, trustStorePassword.toCharArray());

		final KeyStore keyStore = KeyStore.getInstance("BKS");
		keyStore.load(keyStoreLocation, keyStorePassword.toCharArray());

		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);

		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, keyStorePassword.toCharArray());

		final SSLContext sslCtx = SSLContext.getInstance("TLS");
		sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

		return sslCtx.getSocketFactory();
	}




	public void getAllEvents(Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ALL_EVENTS.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = EventsListMessage.class;
		callOrStore(item);
	}

	public void getAllRouteNames(Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ALL_ROUTE_NAMES.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = RouteNamesMessage.class;
		callOrStore(item);
	}

	public void getRoute(String routeName, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ROUTE.getText();
		item.outgoingPayload = routeName;
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = RouteMessage.class;
		callOrStore(item);
	}

	public void getActiveRoute(Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ACTIVE_ROUTE.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = RouteMessage.class;
		callOrStore(item);
	}

	public void getActiveEvent(Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_ACTIVE_EVENT.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = EventMessage.class;
		callOrStore(item);
	}



	public void getRealTimeData(Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_REALTIME_UPDATE.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = RealTimeUpdateData.class;

		gpsInfo.setDeviceId(getDeviceId());
		gpsInfo.isParticipating(ServiceUtils.isServiceRunning(context, GpsTrackerService.class));
		if ( lastKnownLocation != null ) {
			gpsInfo.setLatitude(lastKnownLocation.getLatitude());
			gpsInfo.setLongitude(lastKnownLocation.getLongitude());
			gpsInfo.setAccuracy((int)lastKnownLocation.getAccuracy());

			// TODO remove test coordinates
//			gpsInfo.setLatitude(48.154249);
//			gpsInfo.setLongitude(11.554098);
		}

		item.outgoingPayload = gpsInfo;
		callOrStore(item);
	}

	public void setActiveRoute(String routeName, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.SET_ACTIVE_ROUTE.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.outgoingPayload = new SetActiveRouteMessage(routeName, AdminUtilities.getAdminPassword(context));
		callOrStore(item);
	}

	public void setActiveStatus(EventStatus status, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.SET_ACTIVE_STATUS.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.outgoingPayload = new SetActiveStatusMessage(status, AdminUtilities.getAdminPassword(context));
		callOrStore(item);
	}

	public void createRelationship(int friendId, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.CREATE_RELATIONSHIP.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = RelationshipOutputMessage.class;
		item.outgoingPayload = new RelationshipInputMessage(getDeviceId(), friendId, 0);
		callOrStore(item);
	}

	public void finalizeRelationship(long requestId, int friendId, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.CREATE_RELATIONSHIP.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = RelationshipOutputMessage.class;
		item.outgoingPayload = new RelationshipInputMessage(getDeviceId(), friendId, requestId);
		callOrStore(item);
	}

	public void deleteRelationship(int friendId, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.DELETE_RELATIONSHIP.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.outgoingPayload = new RelationshipInputMessage(getDeviceId(), friendId, 0);
		callOrStore(item);
	}


	public void verifyAdminPassword(String password, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.VERIFY_ADMIN_PASSWORD.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = String.class;
		item.outgoingPayload = new AdminMessage(password);
		callOrStore(item);
	}

	public void getFriendsList(Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.GET_FRIENDS.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = FriendsMessage.class;
		item.outgoingPayload = getDeviceId();
		callOrStore(item);
	}

	public void updateFromGpsTrackerService(Location lastKnownLocation) {
		NetworkClient.lastKnownLocation = lastKnownLocation;
		getRealTimeData(null, null);
	}

	private void callOrStore(BacklogItem item) {
		if ( isConnectionUsable() ) {
			Log.i(TAG, "callOrStore: calling " + item.url);
			call(item);
		}
		else {
			Log.i(TAG, "callOrStore: storing " + item.url);
			item.timestamp = System.currentTimeMillis();
			backlogItems.add(item);
			connect();
		}
	}

	private boolean isConnectionUsable() {
		return bladenightWampClient.getState() == State.USUABLE;
	}

	private static void processBacklog() {
		Log.i(TAG, "processBacklog: " + backlogItems.size() + " items");
		while ( backlogItems.size() > 0 ) {
			BacklogItem item = backlogItems.remove(0);
			if ( System.currentTimeMillis() - item.timestamp < 10000)
				call(item);
		}
	}

	private static void call(final BacklogItem item) {
		try {
			RpcResultReceiver rpcResultReceiver = new RpcResultReceiver() {
				@Override
				public void onSuccess() {
					if ( item.successHandler == null )
						return;
					Message message = new Message();
					if ( item.expectedReturnType == CallResultMessage.class )
						message.obj = this.callResultMessage;
					else if ( item.expectedReturnType != null )
						message.obj = callResultMessage.getPayload(item.expectedReturnType);
					item.successHandler.sendMessage(message);
				}

				@Override
				public void onError() {
					Log.e(TAG, callErrorMessage.toString());
					if ( item.errorHandler == null )
						return;
					Message message = new Message();
					message.obj = this.callResultMessage;
					item.errorHandler.sendMessage(message);
				}

			};
			bladenightWampClient.call(item.url, rpcResultReceiver, item.outgoingPayload);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}

	public void downloadFile(String localPath, String remotePath, final AsyncDownloadTask.StatusHandler handler) {
		String url = getUrl("http") + "/" + remotePath;
		Log.i(TAG,"downloadFile: " + url + " to " + localPath);
		AsyncDownloadTask asyncDownloadTask = new AsyncDownloadTask(handler);
		asyncDownloadTask.execute(url, localPath);
	}

	private String getDeviceId() {
		if (deviceId == null)
			deviceId = DeviceId.getDeviceId(context);
		return deviceId;
	}

	@Override
	public void onLocationChanged(Location location) {
		updateFromGpsTrackerService(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	static private Context context;
	static private final String TAG = "NetworkClient";
	static private String server;
	static final private int port = 8081;
	static final private GpsInfo gpsInfo = new GpsInfo();
	static private Location lastKnownLocation;
	static private BladenightWampClient bladenightWampClient = new BladenightWampClient();
	static private long lookingForServerTimestamp = 0;
	static private String deviceId = null;
	static private long connectingSinceTimestamp;
	static public final long CONNECT_TIMEOUT = 6000; 


	static class BacklogItem {
		public long 				timestamp;
		public String 				url;
		public Handler 				successHandler;
		public Class<?> 			expectedReturnType;
		public Handler 				errorHandler;
		public RpcResultReceiver 	rpcResultReceiver; 
		public Object 				outgoingPayload;
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	};
	static private List<BacklogItem> backlogItems = new ArrayList<BacklogItem>();
}
