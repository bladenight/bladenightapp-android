package de.greencity.bladenightapp.android.cache;

import android.content.Context;
import android.util.Log;
import de.greencity.bladenightapp.network.messages.FriendsMessage;

public class FriendsCache {
	public FriendsCache(Context context) {
		this.context = context;
	}

	public void write(FriendsMessage friendsMessage) {
		Log.i(TAG, "Saving friends to cache " + FILE);
		JsonCacheAccess<FriendsMessage> friendsCache = newCacheAccess();
		friendsCache.set(friendsMessage);
	}

	public FriendsMessage read() {
		Log.i(TAG, "Getting cache for friends.");
		JsonCacheAccess<FriendsMessage> friendsCache = newCacheAccess();
		return friendsCache.get();
	}

	private JsonCacheAccess<FriendsMessage> newCacheAccess() {
		return new JsonCacheAccess<FriendsMessage>(context, FriendsMessage.class, FILE);
	}
	

	public static final String FILE = "jsoncache-friends";
	public static final String TAG = "JsonFriendsCache";
	private Context context;
}
