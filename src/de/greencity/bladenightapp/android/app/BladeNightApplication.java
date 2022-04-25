package de.greencity.bladenightapp.android.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.messages.HandshakeClientMessage;
import fr.ocroquette.wampoc.messages.CallErrorMessage;

public class BladeNightApplication extends MultiDexApplication {
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
            BladeNightApplication.networkClient.shakeHands(msg, null, new HandshakeErrorHandler(this));
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
        private final Context context;
        private LocalBroadcast broadcastReceiversRegister;

        HandshakeErrorHandler(Context context) {
            this.context = context;
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
                Toast.makeText(context, R.string.msg_outdated_client , Toast.LENGTH_LONG).show();
                LocalBroadcast.ERROR.sendWithExtra(context, "message", context.getResources().getString(R.string.msg_outdated_client));
            }
            else {
                Log.e(TAG, "Unknown error occured in the handshake with the server: " + errorMessage);
            }
        }
    }
}
