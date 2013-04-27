package de.greencity.bladenightapp.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class BladenightPreferences {

	public BladenightPreferences(Context context) {
		preferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	public String getServerUrl() {
		return preferences.getString(KEY_SERVER_URL, "");
	}
	
	private static final String SHARED_PREFS_NAME = "Bladenight";
	private static final String KEY_SERVER_URL = "serverUrl";
	
	private SharedPreferences preferences;
}
