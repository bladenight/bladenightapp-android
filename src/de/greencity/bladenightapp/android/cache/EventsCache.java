package de.greencity.bladenightapp.android.cache;

import android.content.Context;
import android.util.Log;
import de.greencity.bladenightapp.network.messages.EventListMessage;

public class EventsCache {
	public EventsCache(Context context) {
		this.context = context;
	}

	public void write(EventListMessage routeMessage) {
		Log.i(TAG, "Saving events to cache " + FILE);
		JsonCacheAccess<EventListMessage> routeCache = newCacheAccess();
		routeCache.set(routeMessage);
	}

	public EventListMessage read() {
		Log.i(TAG, "Getting cache for events.");
		JsonCacheAccess<EventListMessage> routeCache = newCacheAccess();
		return routeCache.get();
	}

	private JsonCacheAccess<EventListMessage> newCacheAccess() {
		return new JsonCacheAccess<EventListMessage>(context, EventListMessage.class, FILE);
	}
	

	public static final String FILE = "jsoncache-events";
	public static final String TAG = "JsonEventsCache";
	private Context context;
}
