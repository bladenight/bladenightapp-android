package de.greencity.bladenightapp.android.social;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Friends {

	Friends(Context context) {
		this.context = context;
	}

	public Map<Integer, Friend> getHashMap() {
		return friends;
	}
	
	public void load() {
		SharedPreferences settings = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		String friendsJson = settings.getString(PREF_FRIENDS_JSON, "{}");
		Log.i(TAG, "load: " + friendsJson);
		Type type = new TypeToken<Map<Integer, Friend>>() {}.getType();
		friends = gson.fromJson(friendsJson, type);
	}

	public void save() {
		SharedPreferences settings = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		String json = gson.toJson(friends);
		Log.i(TAG, "save: " + json);
		editor.putString(PREF_FRIENDS_JSON, json);
		editor.commit();
	}

	public void put(int id, Friend friend) {
		friends.put(id, friend);
		save();
	}

	public Friend get(int id) {
		return friends.get(id);
	}

	public Collection<Integer> keySet() {
		return friends.keySet();
	}

	public int generateId() {
		int i = 1;
		Log.i(TAG, "generateId: friends="+friends);
		while ( friends.get(i) != null && i < Integer.MAX_VALUE) {
			i++;
		}
		if ( i == Integer.MAX_VALUE)
			return 0;
		return i;
	}
	
	public static int generateId(Context context) {
		Friends friends = new Friends(context);
		friends.load();
		
		SharedPreferences settings = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		int id = 1 + settings.getInt(PREF_FRIENDS_LASTID, 1);
		while ( friends.get(id) != null)
			id++;
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PREF_FRIENDS_LASTID, id);
		editor.commit();
		return id;
	}

	public void remove(int id) {
		friends.remove(id);
		save();
	}

	@SuppressLint("UseSparseArrays")
	private Map<Integer, Friend> friends = new HashMap<Integer, Friend>();
	public static final String SHARED_PREFS_NAME = "Bladenight_social";
	public static final String PREF_FRIENDS_JSON = "friends.json";
	public static final String PREF_FRIENDS_LASTID = "nextfriendid.int";
	private final Gson gson = new Gson();
	private Context context;
	static private final String TAG = "Friends"; 
}
