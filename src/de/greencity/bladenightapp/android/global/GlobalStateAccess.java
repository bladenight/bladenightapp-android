package de.greencity.bladenightapp.android.global;

import static de.greencity.bladenightapp.android.global.LocalBroadcast.GOT_EVENT_LIST;
import static de.greencity.bladenightapp.android.global.LocalBroadcast.GOT_GPS_UPDATE;
import static de.greencity.bladenightapp.android.global.LocalBroadcast.GOT_REALTIME_DATA;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.greencity.bladenightapp.android.app.BladeNightApplication;
import de.greencity.bladenightapp.android.cache.EventsMessageCache;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventListMessage;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class GlobalStateAccess implements LocationListener {

	static private GlobalState globalStateSingleton;
	final private Context context;
	private NetworkClient networkClient;
	private static final String TAG = "GlobalStateAccess";
	
	public GlobalStateAccess(Context context) {
		synchronized (this) {
			if (globalStateSingleton == null)
				globalStateSingleton = new GlobalState();
		}
		this.context = context;
		this.networkClient = BladeNightApplication.networkClient;
	}

	public Location getLocationFromGps() {
		return globalStateSingleton.getLocationFromGps();
	}

	public void setLocationFromGps(Location locationFromGps) {
		globalStateSingleton.setLocationFromGps(locationFromGps);
		sendBroadcast(GOT_GPS_UPDATE);
	}

	public RealTimeUpdateData getRealTimeUpdateData() {
		return globalStateSingleton.getRealTimeUpdateData();
	}

	public void setRealTimeUpdateData(RealTimeUpdateData realTimeUpdateData) {
		globalStateSingleton.setRealTimeUpdateData(realTimeUpdateData);
		sendBroadcast(GOT_REALTIME_DATA);
	}

	static class RealTimeUpdateDataHandler extends Handler {
		private WeakReference<GlobalStateAccess> globalStateAccessRef;
		public RealTimeUpdateDataHandler(GlobalStateAccess outerObject) {
			globalStateAccessRef = new WeakReference<GlobalStateAccess>(outerObject);
		}
		@Override
		public void handleMessage(Message msg) {
			RealTimeUpdateData realTimeUpdateData = (RealTimeUpdateData) msg.obj;
			GlobalStateAccess globalStateAccess = globalStateAccessRef.get();
			if ( globalStateAccess == null ) {
				Log.i(TAG, "access to globalStateAccess vanished (RealTimeUpdateDataHandler)");
				return;
			}
			globalStateAccess.setRealTimeUpdateData(realTimeUpdateData);
		}
	};
	
	public void requestRealTimeUpdateData() {
		boolean isServiceRunning = ServiceUtils.isServiceRunning(context, GpsTrackerService.class);
		GpsInfo gpsInfo = new GpsInfo();
		gpsInfo.setDeviceId(DeviceId.getDeviceId(context));
		gpsInfo.isParticipating(isServiceRunning);
		Location location = globalStateSingleton.getLocationFromGps();
		if ( location != null ) {
			gpsInfo.setLatitude(location.getLatitude());
			gpsInfo.setLongitude(location.getLongitude());
			gpsInfo.setAccuracy((int)location.getAccuracy());

			// remove test coordinates
			// sharedState.gpsInfo.setLatitude( 48.160027);
			// sharedState.gpsInfo.setLongitude( 11.561509);
		}
		
		networkClient.getRealTimeData(gpsInfo, new RealTimeUpdateDataHandler(this), null);
	}
	
	static class EventListHandler extends Handler {
		private WeakReference<GlobalStateAccess> globalStateAccessRef;
		public EventListHandler(GlobalStateAccess outerObject) {
			globalStateAccessRef = new WeakReference<GlobalStateAccess>(outerObject);
		}
		@Override
		public void handleMessage(Message msg) {
			// Log.i(TAG, "EventListHandler.handleMessage");
			GlobalStateAccess globalStateAccess = globalStateAccessRef.get();
			if ( globalStateAccess == null ) {
				Log.i(TAG, "access to globalStateAccess vanished (EventListHandler)");
				return;
			}
			EventListMessage eventListMessage = (EventListMessage) msg.obj;
			globalStateAccess.setEventList(eventListMessage.convertToEventsList());
			new EventsMessageCache(globalStateAccess.context).write(eventListMessage);
			globalStateAccess.sendBroadcast(GOT_EVENT_LIST);
		}
	};
	
	public void requestEventList() {
		networkClient.getAllEvents(new EventListHandler(this), null);
	}
	
	public EventList getEventList() {
		return globalStateSingleton.getEventList();
	}
	
	public void setEventList(EventList eventList) {
		globalStateSingleton.setEventList(eventList);
		sendBroadcast(GOT_EVENT_LIST);
	}
	
	
	
	private void sendBroadcast(LocalBroadcast localBroadcast) {
		localBroadcast.send(context);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Log.i(TAG, "location=" + location.toString());
		setLocationFromGps(location);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}
	
	public void disconnect() {
		networkClient.disconnect();
	}
}
