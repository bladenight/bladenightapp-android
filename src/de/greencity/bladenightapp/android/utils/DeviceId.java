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
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String unsetString = "";
		String currentId = settings.getString(KEY_DEVICEID, unsetString);
		if ( currentId != unsetString && currentId.length() > 0 ) {
			Log.d(TAG, " cached device id : " + currentId);
			return currentId;
		}

		Log.d(TAG, " Generating a new id...");
		String deviceId = generateDeviceId(context);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(KEY_DEVICEID, deviceId);
		editor.commit();

		Log.d(TAG, " New id : " + deviceId);

		return deviceId;
	}

	static private String generateDeviceId(Context context) {
		Random random = new Random();
		String id = "";
		while ( id.length() < LENGTH ) {
			id = id + Long.toHexString(random.nextLong());
		}
		return id.substring(0, LENGTH);
	}

	final static String TAG = "DeviceId";
}


