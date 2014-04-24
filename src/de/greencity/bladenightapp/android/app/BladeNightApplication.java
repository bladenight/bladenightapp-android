package de.greencity.bladenightapp.android.app;

import de.greencity.bladenightapp.android.network.NetworkClient;
import android.app.Application;

public class BladeNightApplication extends Application {
	public static NetworkClient networkClient; 
	@Override
	public void onCreate() {
		super.onCreate();
		networkClient = new NetworkClient(this);
	}
}
