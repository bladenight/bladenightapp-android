package de.greencity.bladenightapp.android.network;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
import de.greencity.bladenightapp.android.utils.AsyncDownloadTaskHttpClient;
import de.greencity.bladenightapp.android.utils.BladenightPreferences;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.AdminMessage;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventMessage.EventStatus;
import de.greencity.bladenightapp.network.messages.EventsListMessage;
import de.greencity.bladenightapp.network.messages.FriendsMessage;
import de.greencity.bladenightapp.network.messages.HandshakeClientMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RelationshipInputMessage;
import de.greencity.bladenightapp.network.messages.RelationshipOutputMessage;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.network.messages.RouteNamesMessage;
import de.greencity.bladenightapp.network.messages.SetActiveRouteMessage;
import de.greencity.bladenightapp.network.messages.SetActiveStatusMessage;
import de.greencity.bladenightapp.network.messages.SetMinimumLinearPosition;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WelcomeListener;
import fr.ocroquette.wampoc.messages.CallResultMessage;

public class NetworkClient implements LocationListener {

	public NetworkClient(Context context) {
		this.context = context;
		if ( ! sharedState.isServerConfigured() )
			getUrlFromConfiguration();
	}

	private void getUrlFromConfiguration() {
		String userUrl = new BladenightPreferences(context).getServerUrl();
		if ( sharedState.setServerInfoFromUrl(userUrl) )
			return;
		String systemUrl = context.getResources().getString(R.string.config_default_server_url);
		if ( sharedState.setServerInfoFromUrl(systemUrl) )
			return;
		String defaultUrl = "http://autoscan:8081";
		if ( sharedState.setServerInfoFromUrl(defaultUrl) )
			return;
	}

	private void connect() {
		Log.i(TAG, "connect()");
		if ( sharedState.bladenightWampClient.getState() == State.CONNECTING ||  sharedState.bladenightWampClient.getState() == State.SHAKING_HANDS ) {
			Log.i(TAG, "Already connecting");
			if ( System.currentTimeMillis() - sharedState.connectingSinceTimestamp > CONNECT_TIMEOUT)
				Log.i(TAG, "Connection request timed out");
			else
				return;
		}

		if ( "autoscan".equals(sharedState.getServer()) ) {
			findServer();
			return;
		}

		if ( sharedState.useSsl() ) {
			try {
				WebSocketClient.setCustomSslFactory(SslHelper.getSSLSocketFactory(context));
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}

		URI uri = URI.create(sharedState.getWebSocketUrl());
		sharedState.bladenightWampClient.setWelcomeListener(new WelcomeListener() {
			@Override
			public void onWelcome() {
				Log.i(TAG, "onWelcome()");
				NetworkClient.processBacklog();
			}
		});
		sharedState.connectingSinceTimestamp = System.currentTimeMillis();
		sharedState.bladenightWampClient.connect(uri);
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

		sharedState.gpsInfo.setDeviceId(getDeviceId());
		sharedState.gpsInfo.isParticipating(ServiceUtils.isServiceRunning(context, GpsTrackerService.class));
		if ( sharedState.lastKnownLocation != null ) {
			sharedState.gpsInfo.setLatitude(sharedState.lastKnownLocation.getLatitude());
			sharedState.gpsInfo.setLongitude(sharedState.lastKnownLocation.getLongitude());
			sharedState.gpsInfo.setAccuracy((int)sharedState.lastKnownLocation.getAccuracy());

			// TODO remove test coordinates
			//			sharedState.gpsInfo.setLatitude( 48.160027);
			//			sharedState.gpsInfo.setLongitude( 11.561509);
		}

		item.outgoingPayload = sharedState.gpsInfo;
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

	public void setMinimumLinearPosition(double value, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.SET_MIN_POSITION.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.outgoingPayload = new SetMinimumLinearPosition(value, AdminUtilities.getAdminPassword(context));
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

	public void killServer(Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.KILL_SERVER.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.outgoingPayload = new AdminMessage(AdminUtilities.getAdminPassword(context));
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

	public void shakeHands(HandshakeClientMessage clientMsg, Handler successHandler, Handler errorHandler) {
		BacklogItem item = new BacklogItem();
		item.url = BladenightUrl.SHAKE_HANDS.getText();
		item.successHandler = successHandler;
		item.errorHandler = errorHandler;
		item.expectedReturnType = String.class;
		item.outgoingPayload = clientMsg;
		callOrStore(item);
	}

	public void updateFromGpsTrackerService(Location lastKnownLocation) {
		NetworkClient.sharedState.lastKnownLocation = lastKnownLocation;
		getRealTimeData(null, null);
	}

	private void callOrStore(BacklogItem item) {
		if ( isConnectionUsable() ) {
			Log.i(TAG, "callOrStore: calling " + item.url + " arguments:" + item.outgoingPayload);
			call(item);
		}
		else {
			Log.i(TAG, "callOrStore: storing " + item.url + " arguments:" + item.outgoingPayload);
			item.timestamp = System.currentTimeMillis();
			backlogItems.add(item);
			connect();
		}
	}

	private boolean isConnectionUsable() {
		if ( sharedState.bladenightWampClient.getState() != State.USUABLE )
			return false;
		if ( sharedState.bladenightWampClient.verifyTimeOut() ) {
			Log.e(TAG, "Connection timed out");
			return false;
		}
		return true;
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
				message.obj = this.callErrorMessage;
				item.errorHandler.sendMessage(message);
			}

		};
		try {
			sharedState.bladenightWampClient.call(item.url, rpcResultReceiver, item.outgoingPayload);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			sharedState.bladenightWampClient.disconnect();
		}
	}

	public void downloadFile(String localPath, String remotePath, final AsyncDownloadTaskHttpClient.StatusHandler handler) {
		String url = sharedState.getHttpUrl() + "/" + remotePath;
		Log.i(TAG,"downloadFile: " + url + " to " + localPath);

		AsyncDownloadTaskHttpClient asyncDownloadTask = new AsyncDownloadTaskHttpClient(context, handler);
		asyncDownloadTask.execute(url, localPath);
	}

	private String getDeviceId() {
		if (sharedState.deviceId == null)
			sharedState.deviceId = DeviceId.getDeviceId(context);
		return sharedState.deviceId;
	}


	private void findServer() {
		if ( System.currentTimeMillis() - sharedState.lookingForServerTimestamp < 10000) {
			Log.i(TAG, "Already looking for server ("+sharedState.lookingForServerTimestamp+")");
			return;
		}

		Log.i(TAG, " Looking for server...");
		sharedState.lookingForServerTimestamp = System.currentTimeMillis();

		ServerFinderAsyncTask task = new ServerFinderAsyncTask(context) {
			@Override
			protected void onPostExecute(String foundServer) {
				Log.i(TAG, "Found server="+foundServer);
				if ( foundServer != null ) {
					sharedState.setServer(foundServer);
					onServerFound();
				}
				sharedState.lookingForServerTimestamp = 0;
			}
		};
		task.execute(port);
	}

	protected void onServerFound() {
		connect();
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

	private Context context;
	static private final String TAG = "NetworkClient";
	static private NetworkClientSharedState sharedState = new NetworkClientSharedState();
	private static final long CONNECT_TIMEOUT = 10000;
	static final int port = 8081;

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
