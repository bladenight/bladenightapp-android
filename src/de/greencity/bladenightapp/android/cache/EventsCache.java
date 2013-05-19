package de.greencity.bladenightapp.android.cache;

import android.content.Context;
import android.util.Log;
import de.greencity.bladenightapp.network.messages.EventsListMessage;

public class EventsCache {
	public EventsCache(Context context) {
		this.context = context;
	}

	public void write(EventsListMessage routeMessage) {
		Log.i(TAG, "Saving events to cache " + FILE);
		JsonCacheAccess<EventsListMessage> routeCache = newCacheAccess();
		routeCache.set(routeMessage);
	}

	public EventsListMessage read() {
		Log.i(TAG, "Getting cache for events.");
		JsonCacheAccess<EventsListMessage> routeCache = newCacheAccess();
		return routeCache.get();
	}

	private JsonCacheAccess<EventsListMessage> newCacheAccess() {
		return new JsonCacheAccess<EventsListMessage>(context, EventsListMessage.class, FILE);
	}
	

	public static final String FILE = "jsoncache-events";
	public static final String TAG = "JsonEventsCache";
	private Context context;
}
