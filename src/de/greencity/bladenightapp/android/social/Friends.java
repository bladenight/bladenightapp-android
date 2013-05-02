package de.greencity.bladenightapp.android.social;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.greencity.bladenightapp.dev.android.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

public class Friends {

	public Friends(Context context) {
		this.context = context;
	}

	public Map<Integer, Friend> getHashMap() {
		return friends;
	}
	
	public void load() {
		SharedPreferences settings = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		String friendsJson = settings.getString(PREF_FRIENDS_JSON, "{}");
		Log.i(TAG, "load: " + friendsJson);
		// Log.i(TAG, "trace: " + Log.getStackTraceString(new Exception())); 
		Type type = new TypeToken<Map<Integer, Friend>>() {}.getType();
		try {
			friends = gson.fromJson(friendsJson, type);
		}
		catch (Exception e) {
			// Catch exception to avoid the crash
			// However, all the local friend data (names, colors...) will be lost
			Log.e(TAG, "Failed to read friends: " + e);
		}
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

	public Collection<Friend> values() {
		return friends.values();
	}


	public static synchronized int generateId(Context context) {
		Friends friends = new Friends(context);
		friends.load();
		
		SharedPreferences settings = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		int id = 1 + settings.getInt(PREF_FRIENDS_LASTID, 0);
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

	public int getFriendColor(int friendId) {
		return getFriendColorOrDefault(context, friendId, get(friendId));
	}

	static public int getFriendColorOrDefault(Context context, int friendId, Friend friend) {
		if ( friend == null || friend.getColor() == Color.BLACK ) {
			if ( friendId == SocialActivity.ID_ME )
				return context.getResources().getColor(R.color.default_own_color);
			else
				return context.getResources().getColor(R.color.default_friend_color);
		}
		return friend.getColor();
	}


	static public int getOwnColor(Context context) {
		int defaultColor = context.getResources().getColor(R.color.default_own_color);
		Friends friends = new Friends(context);
		friends.load();
		Friend me = friends.get(SocialActivity.ID_ME); 
		if ( me == null )
			return defaultColor;
		if ( me.getColor() == Color.BLACK)
			return defaultColor;
		return me.getColor();
	}
	
	public List<Integer> getUsedColors() {
		List<Integer> usedColors = new ArrayList<Integer>();
		for (int friendId: friends.keySet()) {
			// Tail and head don't have colors:
			if ( friendId == SocialActivity.ID_HEAD || friendId == SocialActivity.ID_TAIL )
				continue;
			usedColors.add(friends.get(friendId).getColor());
		}
		return usedColors;
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
