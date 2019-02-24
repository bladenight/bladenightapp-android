package de.greencity.bladenightapp.android.app;

import android.app.Application;

import de.greencity.bladenightapp.android.network.NetworkClient;

public class BladeNightApplication extends Application {
    public static NetworkClient networkClient;
    @Override
    public void onCreate() {
        super.onCreate();
        networkClient = new NetworkClient(this);
    }
}
