package de.greencity.bladenightapp.android.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AdminUtilities {
	public static void saveAdminPassword(Context context, String password) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = preferences.edit();
		edit.putString(preferenceKeyForAdminPassword, password);
		edit.commit(); 
	}
	public static String getAdminPassword(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString(preferenceKeyForAdminPassword, null);
	}
	public static void deleteAdminPassword(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = preferences.edit();
		edit.remove(preferenceKeyForAdminPassword);
		edit.commit(); 
	}

	final static public String preferenceKeyForAdminPassword = "adminpassword";
}
