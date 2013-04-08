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

public class Friends {

	Friends(Context context) {
		this.context = context;
	}

	public Map<Integer, Friend> getHashMap() {
		return friends;
	}
	
	public void load() {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String friendsJson = settings.getString(PREF_NAME, "{}");
		Type type = new TypeToken<Map<Integer, Friend>>() {}.getType();
		friends = gson.fromJson(friendsJson, type);
	}

	public void save() {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_NAME, gson.toJson(friends));
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


	@SuppressLint("UseSparseArrays")
	private Map<Integer, Friend> friends = new HashMap<Integer, Friend>();
	public static final String PREFS_NAME = "Bladenight_social";
	public static final String PREF_NAME = "friends.json";
	private final Gson gson = new Gson();
	private Context context;
}
