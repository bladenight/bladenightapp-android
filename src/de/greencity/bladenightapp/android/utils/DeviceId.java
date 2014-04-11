package de.greencity.bladenightapp.android.utils;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class DeviceId {
	static final private String KEY_DEVICEID = "deviceId";
	static final private int LENGTH = 20;

	static public String getDeviceId(Context context) {
		
		if ( isValid(inMemoryCache) )
			return inMemoryCache;

		String fromPreferences = getFromPreferences(context);
		if ( isValid(fromPreferences) ) {
			inMemoryCache = fromPreferences;
			return fromPreferences;
		}
				
		String deviceId = generateDeviceId(context);

		saveToPreferences(context, deviceId);
		inMemoryCache = deviceId;

		Log.d(TAG, " New id : " + deviceId);

		return deviceId;
	}

	private static void saveToPreferences(Context context, String deviceId) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_DEVICEID, deviceId);
		editor.commit();
	}
	
	static private String getFromPreferences(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String unsetString = "";
		String currentId = settings.getString(KEY_DEVICEID, unsetString);
		if ( currentId != unsetString && currentId.length() > 0 ) {
			Log.d(TAG, " cached device id : " + currentId);
			return currentId;
		}
		return null;
	}

	static private String generateDeviceId(Context context) {
		Log.d(TAG, " Generating a new id...");
		Random random = new Random();
		String id = "";
		while ( id.length() < LENGTH ) {
			id = id + Long.toHexString(random.nextLong());
		}
		return id.substring(0, LENGTH);
	}
	
	static boolean isValid(String deviceId) {
		if ( deviceId == null )
			return false;
		if ( deviceId.length() <= 0)
			return false;
		return true;
	}

	final static String TAG = "DeviceId";
	static String inMemoryCache;
}


