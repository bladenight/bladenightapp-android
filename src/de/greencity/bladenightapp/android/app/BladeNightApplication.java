package de.greencity.bladenightapp.android.app;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.selection.SelectionActivity;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.messages.HandshakeClientMessage;
import fr.ocroquette.wampoc.messages.CallErrorMessage;

public class BladeNightApplication extends Application {
    public static NetworkClient networkClient;
    private final static String TAG = "BladeNightApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        networkClient = new NetworkClient(this);
        shakeHands();
    }

    private void shakeHands() {
        try {
            String deviceId = DeviceId.getDeviceId(this);
            int clientBuild = getDeviceVersionCode();

            String phoneManufacturer = android.os.Build.MANUFACTURER;
            String phoneModel = android.os.Build.MODEL;
            String androidRelease = Build.VERSION.RELEASE;

            HandshakeClientMessage msg = new HandshakeClientMessage(
                    deviceId,
                    clientBuild,
                    phoneManufacturer,
                    phoneModel,
                    androidRelease);
            BladeNightApplication.networkClient.shakeHands(msg, null, new HandshakeErrorHandler());
        } catch (Exception e) {
            Log.e(TAG, "shakeHands failed to gather and send information", e);
        }
    }

    private int getDeviceVersionCode() {
        PackageManager manager = this.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to get device version code: " + e.toString());
            return 0;
        }
    }


    static class HandshakeErrorHandler extends Handler {
        HandshakeErrorHandler() {
        }
        @Override
        public void handleMessage(Message msg) {
            CallErrorMessage errorMessage = (CallErrorMessage)msg.obj;
            if ( errorMessage == null ) {
                Log.w(TAG, "Failed to get the error message");
                return;
            }
            if ( BladenightError.OUTDATED_CLIENT.getText().equals(errorMessage.getErrorUri())) {
                Log.e(TAG, "Outdated client: " + errorMessage);
                // TODO show error to user
            }
            else {
                Log.e(TAG, "Unknown error occured in the handshake with the server: " + errorMessage);
            }
        }
    }
}
