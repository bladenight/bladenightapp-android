package de.greencity.bladenightapp.android.network;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;

public class GlobalStateService extends Service {
	private static final String TAG = "GlobalStateService";
	private final IBinder binder = new NetworkServiceBinder();
	private GlobalStateAccess globalStateAccess;

	public class NetworkServiceBinder extends Binder {
		public GlobalStateService getService() {
			return GlobalStateService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		globalStateAccess = new GlobalStateAccess(this);
		
		Log.i(TAG, "onBind this="+this);
		// Log.i(TAG, "onBind networkClient="+networkClient);
		
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}

//	static class EventListHandler extends Handler {
//		private WeakReference<GlobalStateService> networkServiceRef;
//		public EventListHandler(GlobalStateService outerObject) {
//			networkServiceRef = new WeakReference<GlobalStateService>(outerObject);
//		}
//		@Override
//		public void handleMessage(Message msg) {
//			Log.i(TAG, "EventListHandler.handleMessage");
//			GlobalStateService networkService = networkServiceRef.get();
//			if ( networkService == null )
//				return;
//			EventListMessage eventListMessage = (EventListMessage) msg.obj;
//			globalStateAccess.setEventList(eventListMessage.convertToEventsList());
//			Log.i(TAG,"globalStateAccess="+globalStateAccess);
//			Log.i(TAG,"eventListMessage.convertToEventsList()="+eventListMessage.convertToEventsList());
//			new EventsCache(networkService).write(eventListMessage);
//			GOT_EVENT_LIST.send(networkService);
//		}
//	};

//	public void requestEventList() {
//		NetworkClient networkClient = BladeNightApplication.networkClient;
//		Log.i(TAG, "requestEventList networkClient="+networkClient);
//		networkClient.getAllEvents(new EventListHandler(this), null);
//	}
//	
//	public GlobalState getGlobalState() {
//		return globalState;
//	}
//
}
