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
import android.util.Log;
import android.widget.Toast;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.selection.SelectionActivity;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.tavendo.autobahn.Wamp.CallHandler;

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
		handler.removeCallbacks(periodicRunnable);
		removeLocationUpdates();
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
		// NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = android.R.drawable.alert_dark_frame;
		CharSequence tickerText = "BladeNight";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Context context = getApplicationContext();
		CharSequence contentTitle = "BladeNight";
		CharSequence contentText = "Application started";

		Intent notificationIntent = new Intent(this, SelectionActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		startForeground(1, notification);
	}

	private void sendLocationUpdateToNetworkService() {
		CallHandler callHandler = new CallHandler() {
			@Override
			public void onResult(Object arg0) {
			}
			
			@Override
			public void onError(String arg0, String arg1) {
			}
		};
		Log.i(TAG, "Sending:"+lastKnownPosition);
		networkClient.updateFromGpsTrackerService(lastKnownPosition);
	}


	private LatLong lastKnownPosition = new LatLong(0, 0);
	final private BladenightLocationListener locationListener = new BladenightLocationListener(this, lastKnownPosition);
	private NetworkClient networkClient;
	private Runnable periodicRunnable;
	final Handler handler = new Handler();
	static final int updatePeriod = 5000;

	static final String TAG = "GpsTrackerService";
	static final String INTENT_PERIODIC = "de.greencity.bladenightapp.android.gps.periodic";
}
