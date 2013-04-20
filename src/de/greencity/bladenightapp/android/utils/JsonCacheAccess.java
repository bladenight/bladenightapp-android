package de.greencity.bladenightapp.android.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonCacheAccess<T> {
	public JsonCacheAccess(Context context, Class<T> clazz, String name) {
		this.filename = name + ".json";
		this.cacheFile = new InternalStorageFile(context, filename);
		this.gson = new GsonBuilder().setPrettyPrinting().create();
		this.clazz = clazz;
	}
	
	public T get() {
		Log.i(TAG, "Fetching JsonCache: " + filename);
		if ( cacheFile == null )
			return null;
		String jsonString = cacheFile.read();
		if (jsonString == null)
			return null;
		try {
			T o = gson.fromJson(jsonString, clazz);
			Log.i(TAG, "Got data from cache, length=" + jsonString.length());
			return o;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public void set(T object) {
		// String json = gson.toJson(object);
		// Log.i(TAG, "Writing data to cache, length=" + json.length());
		// cacheFile.write(json);
	}
	
	public static String getNameForRoute(String routeName) {
		return PREFIX_ROUTE + routeName;
	}
	
	final private InternalStorageFile cacheFile;
	final private Gson gson;
	final private Class<T> clazz;
	final private String filename;

	public static final String FILE_EVENTS = "jsoncache-events";
	public static final String PREFIX_ROUTE = "jsoncache-route-";

	private static final String TAG = "JsonCacheAccess";

}
