package de.greencity.bladenightapp.android.tracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.selection.SelectionActivity;
import de.greencity.bladenightapp.dev.android.R;

public class GpsTrackerService extends Service {

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");

		networkClient = new NetworkClient(this);

		gpsListener.requestLocationUpdates(5000);

		setNotification();

		periodicRunnable = new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "periodic task");
				sendLocationUpdateToNetworkService();
				handler.postDelayed(this, updatePeriod);
			}
		};
		handler.postDelayed(periodicRunnable, updatePeriod);
		
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		handler.removeCallbacks(periodicRunnable);
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

	private void setNotification() {
		Intent notificationIntent = new Intent(this, SelectionActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new  NotificationCompat.Builder(this)
		.setContentTitle(getString(R.string.msg_tracking_running))
		.setContentText(getString(R.string.app_name))
		.setSmallIcon(R.drawable.ic_launcher_bn)
		.setContentIntent(contentIntent)
		.build();
		
		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

		startForeground(NOTIFICATION_ID, notification);
	}

	private void sendLocationUpdateToNetworkService() {
		Log.i(TAG, "Sending:"+lastKnownLocation);
		networkClient.updateFromGpsTrackerService(lastKnownLocation);
	}


	private Location lastKnownLocation = new Location("INTERNAL");
	final private BladenightLocationListener locationListener = new BladenightLocationListener(this, lastKnownLocation);
	final private GpsListener gpsListener = new GpsListener(this, locationListener);
	private NetworkClient networkClient;
	private Runnable periodicRunnable;
	final Handler handler = new Handler();
	static private final int updatePeriod = 5000;
	static private final int NOTIFICATION_ID = 1;

	static final String TAG = "GpsTrackerService";
	static final String INTENT_PERIODIC = "de.greencity.bladenightapp.android.gps.periodic";
}
