package de.greencity.bladenightapp.android.tracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import de.greencity.bladenightapp.android.network.NetworkIntents;
import de.greencity.bladenightapp.android.network.NetworkService;
import de.greencity.bladenightapp.android.selection.SelectionActivity;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.PeriodicBroadcastIntentManager;
import de.greencity.bladenightapp.network.messages.LatLong;

public class GpsTrackerService extends Service {

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		networkServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG+".ServiceConnection", "onServiceConnected");
				sendBroadcast(new Intent(NetworkIntents.GET_ALL_EVENTS));
			}
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG+".ServiceConnection", "onServiceDisconnected");
			}

		};
		bindService(new Intent(this, NetworkService.class), networkServiceConnection,  BIND_AUTO_CREATE);
		requestLocationUpdates();

		broadcastReceiversRegister.registerReceiver(INTENT_PERIODIC, sendPositionUpdateToNetworkService);

		setNotification();

		schedulePeriodicBroadcastIntents();

	}

	@Override
	public void onDestroy() {
		unschedulePeriodicBroadcastIntents();
		broadcastReceiversRegister.unregisterReceivers();
		removeLocationUpdates();
		unbindService(networkServiceConnection);
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

	private void schedulePeriodicBroadcastIntents() {
		periodicBroadcastIntentManager.cancelPeriodicBroadcastIntents();
		int period = 1000;
		periodicBroadcastIntentManager.schedulePeriodicBroadcastIntent(new Intent(INTENT_PERIODIC), period);
	}

	private void unschedulePeriodicBroadcastIntents() {
		periodicBroadcastIntentManager.cancelPeriodicBroadcastIntents();
	}

	private final BroadcastReceiver sendPositionUpdateToNetworkService = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			sendLocationUpdateToNetworkService();
		}
	};
	
	private void sendLocationUpdateToNetworkService() {
		Intent outgoingIntent = new Intent(NetworkIntents.LOCATION_UPDATE);
		String json = new Gson().toJson(lastKnownPosition);
		outgoingIntent.putExtra("json", json);
		Log.d(TAG, "sendPositionUpdateToNetworkService: "+json);
		sendBroadcast(outgoingIntent);
	}


	private ServiceConnection networkServiceConnection;
	private LatLong lastKnownPosition = new LatLong(0, 0);
	final private BladenightLocationListener locationListener = new BladenightLocationListener(this, lastKnownPosition);
	private PeriodicBroadcastIntentManager periodicBroadcastIntentManager = new PeriodicBroadcastIntentManager(this);
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);

	static final String TAG = "GpsTrackerService";
	static final String INTENT_PERIODIC = "de.greencity.bladenightapp.android.gps.periodic";
}
