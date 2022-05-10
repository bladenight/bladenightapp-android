package de.greencity.bladenightapp.android.tracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.io.File;
import java.io.IOException;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.network.RealTimeDataConsumer;
import de.greencity.bladenightapp.android.progressbar.ProgressBarRenderer;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.Paths;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;

public class GpsTrackerService extends Service {

    private GlobalStateAccess globalStateAccess;
    private Location lastKnownLocation;
    private BladenightLocationListener locationListener;
    private GpsListener gpsListener;
    private NetworkClient networkClient;
    private Runnable periodicNetworkSenderRunnable;
    private RealTimeDataConsumer realTimeDataConsumer;
    private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
    private ProgressBarRenderer progressBarRenderer;
    private NotificationCompat.Builder builder;
    private Notification notification;

    final Handler handler = new Handler();
    static private final int SEND_PERIOD = 10000;
    static private final int NOTIFICATION_ID = 1;

    static private final int notificationIconId = R.drawable.application_prod;

    static final String TAG = "GpsTrackerService";

    class RealTimeDataBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            RealTimeUpdateData realTimeUpdateData = globalStateAccess.getRealTimeUpdateData();
            Log.i(TAG, "Consuming: " + realTimeUpdateData);
            progressBarRenderer.updateRealTimeUpdateData(realTimeUpdateData);
            updateNotification();
        }
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        globalStateAccess = new GlobalStateAccess(this);

        lastKnownLocation = new Location("INTERNAL");
        locationListener = new BladenightLocationListener(lastKnownLocation);
        gpsListener = new GpsListener(this, locationListener);

        gpsListener.requestLocationUpdates(5000);

        broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_REALTIME_DATA, new RealTimeDataBroadcastReceiver());

        progressBarRenderer = new ProgressBarRenderer(this);

        createNotification();
        updateNotification();

        periodicNetworkSenderRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "periodic task");
                globalStateAccess.setLocationFromGps(lastKnownLocation);
                globalStateAccess.requestRealTimeUpdateData();
                // sendLocationUpdateToServer();
                handler.postDelayed(this, SEND_PERIOD);
            }
        };
        handler.post(periodicNetworkSenderRunnable);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        broadcastReceiversRegister.unregisterReceivers();

        handler.removeCallbacks(periodicNetworkSenderRunnable);
        gpsListener.cancelLocationUpdates();
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        // If we get killed, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");

        return super.onUnbind(intent);
    }

    private void createNotification() {
        Intent notificationIntent = new Intent(this, BladenightMapActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap icon = getNotificationIcon();
        Log.i(TAG, icon.toString());

        String NOTIFICATION_CHANNEL_ID = "notification";


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // NotificationChannel has been introduced in API level 26
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
        }

        builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(getString(R.string.msg_tracking_running))
                .setContentText(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_notification_tracking)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent);

        notification = builder.build();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }
        else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void updateNotification() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int viewWidth = (int) (0.9 * displayMetrics.heightPixels);
        int viewHeight = 128;

        progressBarRenderer.setFontSize(30);

        Bitmap bitmap = progressBarRenderer.renderToBitmap(viewWidth, viewHeight, displayMetrics);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_progressbar);
        remoteViews.setImageViewBitmap(R.id.imageview_progressbar, bitmap);

        notification.bigContentView = remoteViews;
        notification.contentView = remoteViews;

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification);
    }

    @SuppressLint("InlinedApi")
    private Bitmap getNotificationIcon() {
        Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), notificationIconId);

        if (android.os.Build.VERSION.SDK_INT < 11) {
            return rawBitmap;
        } else {
            Resources res = getResources();
            int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
            int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

            return Bitmap.createScaledBitmap(rawBitmap, width, height, false);
        }
    }
}
