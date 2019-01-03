package de.greencity.bladenightapp.android.app;

import de.greencity.bladenightapp.android.network.NetworkClient;
import android.app.Application;
import android.support.multidex.MultiDexApplication;

public class BladeNightApplication extends MultiDexApplication {
    public static NetworkClient networkClient;
    @Override
    public void onCreate() {
        super.onCreate();
        networkClient = new NetworkClient(this);
    }
}
