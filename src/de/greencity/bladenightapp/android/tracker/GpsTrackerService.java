package de.greencity.bladenightapp.android.tracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.selection.SelectionActivity;
import de.greencity.bladenightapp.network.messages.LatLong;

public class GpsTrackerService extends Service {

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");

		networkClient = new NetworkClient(this);

		requestLocationUpdates();

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
		removeLocationUpdates();
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

	private void requestLocationUpdates() {
		removeLocationUpdates();
		LocationManager locationManager;
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		int period = 1000;
		try {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 10f, locationListener);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, period, 10f, locationListener);
		}
		catch(Exception e) {
			Log.e(this.toString(), "Failed to subscribe some of the location listeners",e);
			Toast.makeText(this, "Fehler in dem Ortsbestimmungssystem. Die Position kann ungenau oder unverfübar sein", Toast.LENGTH_LONG).show();
		}

	}

	private void removeLocationUpdates() {
		LocationManager locationManager;
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(locationListener);
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
		Log.i(TAG, "Sending:"+lastKnownPosition);
		networkClient.updateFromGpsTrackerService(lastKnownPosition);
	}


	private LatLong lastKnownPosition = new LatLong(0, 0);
	final private BladenightLocationListener locationListener = new BladenightLocationListener(this, lastKnownPosition);
	private NetworkClient networkClient;
	private Runnable periodicRunnable;
	final Handler handler = new Handler();
	static private final int updatePeriod = 5000;
	static private final int NOTIFICATION_ID = 1;

	static final String TAG = "GpsTrackerService";
	static final String INTENT_PERIODIC = "de.greencity.bladenightapp.android.gps.periodic";
}
