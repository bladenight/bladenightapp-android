package de.greencity.bladenightapp.android.network;

import static de.greencity.bladenightapp.android.global.LocalBroadcast.GOT_EVENT_LIST;

import java.lang.ref.WeakReference;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import de.greencity.bladenightapp.android.cache.EventsCache;
import de.greencity.bladenightapp.android.global.GlobalState;
import de.greencity.bladenightapp.network.messages.EventListMessage;

public class GlobalStateService extends Service {
	private static final String TAG = "GlobalStateService";
	private final IBinder binder = new NetworkServiceBinder();
	private NetworkClient networkClient;
	private static final GlobalState globalState = new GlobalState();

	public class NetworkServiceBinder extends Binder {
		public GlobalStateService getService() {
			return GlobalStateService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		networkClient = new NetworkClient(this);
		Log.i(TAG, "onBind from " + Thread.currentThread() + " this="+this);
		return binder;
	}

	static class EventListHandler extends Handler {
		private WeakReference<GlobalStateService> networkServiceRef;
		public EventListHandler(GlobalStateService outerObject) {
			networkServiceRef = new WeakReference<GlobalStateService>(outerObject);
		}
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "EventListHandler.handleMessage");
			GlobalStateService networkService = networkServiceRef.get();
			if ( networkService == null )
				return;
			EventListMessage eventListMessage = (EventListMessage) msg.obj;
			globalState.setEventList(eventListMessage.convertToEventsList());
			Log.i(TAG,"globalStateAccess="+globalState);
			Log.i(TAG,"eventListMessage.convertToEventsList()="+eventListMessage.convertToEventsList());
			new EventsCache(networkService).write(eventListMessage);
			GOT_EVENT_LIST.send(networkService);
		}
	};

	public void requestEventList() {
		Log.i(TAG, "requestEventList networkClient="+networkClient);
		networkClient.getAllEvents(new EventListHandler(this), null);
	}
	
	public GlobalState getGlobalState() {
		return globalState;
	}

}
