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
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTask;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.network.messages.RouteNamesMessage;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WelcomeListener;
import fr.ocroquette.wampoc.common.Channel;
import fr.ocroquette.wampoc.messages.CallResultMessage;

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

	private void connect() {
		if ( server == null)
			findServer();

		String protocol = "ws";

		if ( "wss".equals(protocol) ) {
			try {
				WebSocketClient.setCustomSslFactory(getSSLSocketFactory());
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}

		URI uri = URI.create(getUrl(protocol));
		bladenightWampClient = new BladenightWampClient(uri);
		bladenightWampClient.setWelcomeListener(new WelcomeListener() {
			@Override
			public void onWelcome() {
				Log.i(TAG, "onWelcome()");
				while ( backlogItems.size() > 0 ) {
					BacklogItem item = backlogItems.remove(0);
					if ( System.currentTimeMillis() - item.timestamp < 5000)
						call(item);
				}
			}
		});
		bladenightWampClient.connect();
	}

	private javax.net.ssl.SSLSocketFactory getSSLSocketFactory() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
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
		item.expectedReturnType = String.class;
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

		gpsInfo.isParticipating(ServiceUtils.isServiceRunning(context, GpsTrackerService.class));
		if ( lastKnownPosition != null ) {
			gpsInfo.setLatitude(lastKnownPosition.getLatitude());
			gpsInfo.setLongitude(lastKnownPosition.getLongitude());
		}

		item.outgoingPayload = gpsInfo;
		callOrStore(item);
	}

	public void updateFromGpsTrackerService(LatLong lastKnownPosition) {
		NetworkClient.lastKnownPosition = lastKnownPosition;
		getRealTimeData(null, null);
	}

	private void callOrStore(BacklogItem item) {
		if ( bladenightWampClient.isConnectionUsable() ) {
			Log.i(TAG, "callOrStore: calling");
			call(item);
		}
		else {
			Log.i(TAG, "callOrStore: storing");
			item.timestamp = System.currentTimeMillis();
			backlogItems.add(item);
			bladenightWampClient.connect();
		}
	}

	private void call(final BacklogItem item) {
		Log.i(TAG, ToStringBuilder.reflectionToString(this));
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

	static private Context context;
	static private final String TAG = "NetworkClient";
	static private String server;
	final static private int port = 8081;
	final private GpsInfo gpsInfo = new GpsInfo();
	static private LatLong lastKnownPosition;
	private BladenightWampClient bladenightWampClient;



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
	private List<BacklogItem> backlogItems = new ArrayList<BacklogItem>();
}
