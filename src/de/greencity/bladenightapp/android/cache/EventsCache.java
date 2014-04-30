package de.greencity.bladenightapp.android.cache;

import de.greencity.bladenightapp.events.EventList;
import android.content.Context;
import android.util.Log;

public class EventsCache {
	public EventsCache(Context context) {
		this.context = context;
	}

	public void write(EventList EventList) {
		Log.i(TAG, "Saving events to cache " + FILE);
		JsonCacheAccess<EventList> routeCache = newCacheAccess();
		routeCache.set(EventList);
	}

	public EventList read() {
		Log.i(TAG, "Getting cache for events.");
		JsonCacheAccess<EventList> routeCache = newCacheAccess();
		return routeCache.get();
	}

	private JsonCacheAccess<EventList> newCacheAccess() {
		return new JsonCacheAccess<EventList>(context, EventList.class, FILE);
	}
	

	public static final String FILE = "jsoncache-events";
	public static final String TAG = "JsonEventsCache";
	private Context context;
}
