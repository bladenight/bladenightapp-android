package de.greencity.bladenightapp.android.tracker;

import java.io.File;
import java.io.IOException;

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
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.network.RealTimeDataConsumer;
import de.greencity.bladenightapp.android.utils.Paths;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.tracking.TraceLogger;

public class GpsTrackerService extends Service {

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");

		networkClient = new NetworkClient(this);

		lastKnownLocation = new Location("INTERNAL");
		locationListener = new BladenightLocationListener(lastKnownLocation);
		gpsListener = new GpsListener(this, locationListener);

		gpsListener.requestLocationUpdates(5000);
		
		traceLogger = new TraceLogger(new File(Paths.getAppDataDirectory(), "gps-trace.txt"));
		
		realTimeDataConsumer = new RealTimeDataConsumer() {
			@Override
			public void consume(RealTimeUpdateData realTimeUpdateData) {
				Log.i(TAG, "Consuming: " + realTimeUpdateData);
				if ( realTimeUpdateData.isUserOnRoute() )
					traceLogger.setLinearPosition(realTimeUpdateData.getUserPosition());
				else
					traceLogger.setLinearPosition(-1);
				writeTraceEntry();
			}
		};
		
		networkClient.addRealTimeDataConsumer(realTimeDataConsumer);

		setNotification();

		periodicNetworkSenderRunnable = new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "periodic task");
				sendLocationUpdateToNetworkService();
				handler.postDelayed(this, SEND_PERIOD);
				writeTraceEntry();
			}
		};
		handler.post(periodicNetworkSenderRunnable);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		handler.removeCallbacks(periodicNetworkSenderRunnable);
		gpsListener.cancelLocationUpdates();
		stopForeground(true);
		networkClient.removeRealTimeDataConsumer(realTimeDataConsumer);
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
		Intent notificationIntent = new Intent(this, BladenightMapActivity.class);
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
		Log.i(TAG, "Sending: "+lastKnownLocation);
		networkClient.updateFromGpsTrackerService(lastKnownLocation);
	}

	private void writeTraceEntry() {
		traceLogger.setAccuracy(lastKnownLocation.getAccuracy());
		traceLogger.setLongitude(lastKnownLocation.getLongitude());
		traceLogger.setLatitude(lastKnownLocation.getLatitude());
		try {
			traceLogger.writeWithTimeLimit(MIN_LOG_PERIOD);
		} catch (IOException e) {
			Log.e(TAG, "Failed to write trace entry: " + e);
		}
	}


	private Location lastKnownLocation;
	private BladenightLocationListener locationListener;
	private GpsListener gpsListener;
	private NetworkClient networkClient;
	private Runnable periodicNetworkSenderRunnable;
	private RealTimeDataConsumer realTimeDataConsumer;
	private TraceLogger traceLogger;
	
	final Handler handler = new Handler();
	static private final int SEND_PERIOD = 10000;
	static private final int MIN_LOG_PERIOD = 2000;
	static private final int NOTIFICATION_ID = 1;
	
	static final String TAG = "GpsTrackerService";
	static final String INTENT_PERIODIC = "de.greencity.bladenightapp.android.gps.periodic";
}
