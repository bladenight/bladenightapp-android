package de.greencity.bladenightapp.android.cache;

import android.content.Context;
import android.util.Log;

import de.greencity.bladenightapp.network.messages.EventListMessage;

public class EventsMessageCache {
    public EventsMessageCache(Context context) {
        this.context = context;
    }

    public void write(EventListMessage eventListMessage) {
        Log.i(TAG, "Saving events to cache " + FILE);
        JsonCacheAccess<EventListMessage> routeCache = newCacheAccess();
        routeCache.set(eventListMessage);
    }

    public EventListMessage read() {
        Log.i(TAG, "Getting cache for events.");
        JsonCacheAccess<EventListMessage> routeCache = newCacheAccess();
        return routeCache.get();
    }

    private JsonCacheAccess<EventListMessage> newCacheAccess() {
        return new JsonCacheAccess<EventListMessage>(context, EventListMessage.class, FILE);
    }


    public static final String FILE = "jsoncache-eventsmessage";
    public static final String TAG = "JsonEventsMessageCache";
    private Context context;
}
