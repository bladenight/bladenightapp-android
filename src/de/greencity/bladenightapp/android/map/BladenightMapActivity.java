package de.greencity.bladenightapp.android.map;


import java.io.File;

import org.apache.commons.io.FileUtils;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.reader.header.FileOpenResult;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.network.NetworkIntents;
import de.greencity.bladenightapp.android.network.NetworkService;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.JsonBroadcastReceiver;
import de.greencity.bladenightapp.android.utils.PeriodicBroadcastIntentManager;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class BladenightMapActivity extends MapActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_action);
		configureActionBar();
		createMapView();

		downloadProgressDialog = new ProgressDialog(this);
		processionProgressBar = (ProcessionProgressBar) findViewById(R.id.progress_procession);

		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG+".ServiceConnection", "onServiceConnected");
				requestRouteFromNetworkService();
			}
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG+".ServiceConnection", "onServiceDisconnected");
			}

		};

		broadcastReceiversRegister.registerReceiver(NetworkIntents.GOT_ACTIVE_ROUTE, gotActiveRouteReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.GOT_ROUTE, gotRouteReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.GOT_REAL_TIME_DATA, gotRealTimeDataReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.DOWNLOAD_FAILURE, gotDownloadFailureReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.DOWNLOAD_SUCCESS, gotDownloadSuccessReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.DOWNLOAD_PROGRESS, gotDownloadProgressReceiver);
		broadcastReceiversRegister.registerReceiver(NetworkIntents.CONNECTED, connectedReceiver);
	}

	@Override
	public void onStart() {
		super.onStart();

		bindService(new Intent(this, NetworkService.class), serviceConnection,  BIND_AUTO_CREATE);

		verifyMapFile();

		configureActionBar();

		getActivityParametersFromIntent(getIntent());
		
		if ( isRealTime )
			periodicBroadcastIntentManager.schedulePeriodicBroadcastIntent(new Intent(NetworkIntents.GET_REAL_TIME_DATA), 5000);
	}


	private void getActivityParametersFromIntent(Intent intent) {
		if ( intent != null) {
			Bundle bundle = intent.getExtras();
			if ( bundle != null ) {
				String s = bundle.getString("routeName");
				if ( s != null)
					routeName = s;
				Log.i(TAG, "routeName="+routeName);
				Boolean b = bundle.getBoolean("isRealTime");
				if ( b != null)
					isRealTime = b;
				Log.i(TAG, "isRealTime="+isRealTime);
			}
			else {
				Log.w(TAG, "bundle="+bundle);
			}
		}
		else {
			Log.w(TAG, "intent="+intent);
		}
	}

	protected void requestRouteFromNetworkService() {
		if ( isRealTime ) {
			sendBroadcast(new Intent(NetworkIntents.GET_ACTIVE_ROUTE));
			sendBroadcast(new Intent(NetworkIntents.GET_REAL_TIME_DATA));
		}
		else {
			Intent intent = new Intent(NetworkIntents.GET_ROUTE);
			intent.putExtra("json", new Gson().toJson(routeName));
			sendBroadcast(intent);
		}
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		broadcastReceiversRegister.unregisterReceivers();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		new ActionBarConfigurator(actionBar)
		.hide(ActionItemType.MAP)
		.setTitle(R.string.title_map)
		.configure();
	}


	@Override
	public void onStop() {
		super.onStop();

		unbindService(serviceConnection);

		periodicBroadcastIntentManager.cancelPeriodicBroadcastIntents();
	}

	public void createMapView() {
		// TODO to remove !
		// clearTileCache();

		mapView = new BladenightMapView(this);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setRenderTheme(CustomRenderTheme.CUSTOM_RENDER);


		setMapFile();

		LinearLayout parent = (LinearLayout) findViewById(R.id.map_parent);
		parent.removeAllViews();

		parent.addView(mapView);

		routeOverlay = new RouteOverlay(mapView);

		TileCache fileSystemTileCache = mapView.getFileSystemTileCache();
		fileSystemTileCache.setPersistent(true);
		fileSystemTileCache.setCapacity(20000);
	}

	// Will be called via the onClick attribute
	// of the buttons in main.xml
	public void onClick(View view) {	  
		switch (view.getId()) {
		//	    case R.id.next: goUp();
		//	      break;

		}
	}

	private void verifyMapFile() {
		if ( ! new File(mapLocalPath).exists() ) {
			startMapFileDownload();
		}
	}

	private void startMapFileDownload() {
		downloadProgressDialog.setMessage("Kartenmaterial wird heruntergeladen...");
		downloadProgressDialog.setIndeterminate(false);
		downloadProgressDialog.setMax(100);
		downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		downloadProgressDialog.show();

		Intent intent = new Intent(NetworkIntents.DOWNLOAD_REQUEST);
		intent.putExtra("localPath", mapLocalPath);
		intent.putExtra("remotePath", mapRemotePath);
		sendBroadcast(intent);
	}

	private void setMapFile() {
		if ( mapView.setMapFile(new File(mapLocalPath)) == FileOpenResult.SUCCESS ) {
			mapView.redraw();
			mapView.getMapViewPosition().setZoomLevel((byte) 15);
		}
		else {
			Log.e(TAG, "Failed to set map file: " + mapLocalPath);
		}
	}

	protected void fitViewToBoundingBox(BoundingBox boundingBox) {
		if ( boundingBox != null && boundingBox.getLatitudeSpan() > 0 && boundingBox.getLongitudeSpan() > 0 )
			mapView.fitViewToBoundingBox(boundingBox);
	}

	protected void fitViewToRoute() {
		fitViewToBoundingBox(routeOverlay.getRouteBoundingBox());
	}

	protected void fitViewToProcession() {
		fitViewToBoundingBox(routeOverlay.getProcessionBoundingBox());
	}


	protected void centerViewOnCoordinates(GeoPoint center, byte zoomLevel) {
		mapView.getMapViewPosition().setMapPosition(new MapPosition(center, zoomLevel));
	}


	private final BroadcastReceiver gotActiveRouteReceiver = new JsonBroadcastReceiver<RouteMessage>("gotRouteReceiver", RouteMessage.class) {
		@Override
		public void onReceive(RouteMessage routeMessage) {
			routeOverlay.update(routeMessage);
			fitViewToRoute();
		}
	};

	private final BroadcastReceiver gotRouteReceiver = new JsonBroadcastReceiver<RouteMessage>("gotRouteReceiver", RouteMessage.class) {
		@Override
		public void onReceive(RouteMessage routeMessage) {
			routeOverlay.update(routeMessage);
			fitViewToRoute();
		}
	};

	private final BroadcastReceiver gotRealTimeDataReceiver = new JsonBroadcastReceiver<RealTimeUpdateData>("gotRealTimeDataReceiver", RealTimeUpdateData.class) {
		@Override
		public void onReceive(RealTimeUpdateData data) {
			routeOverlay.update(data);
			processionProgressBar.update(data);
			// fitViewToProcession();
		}
	};

	private final BroadcastReceiver gotDownloadSuccessReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ( mapRemotePath.equals(intent.getExtras().getString("id")) ) {
				onMapFileDownloadSuccess();
			}
		}
	};

	private final BroadcastReceiver gotDownloadFailureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ( mapRemotePath.equals(intent.getExtras().getString("id")) ) {
				onMapFileDownloadFailure();
			}
		}
	};

	private final BroadcastReceiver gotDownloadProgressReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ( mapRemotePath.equals(intent.getExtras().getString("id")) ) {
				long total = intent.getExtras().getLong("total");
				long current = intent.getExtras().getLong("current");
				if ( total > 0) {
					double percent = current * ( 100.0 / total  );
					downloadProgressDialog.setProgress((int) percent );
				}
			}
		}
	};

	private final BroadcastReceiver connectedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"connectedReceiver.onReceive");
			requestRouteFromNetworkService();
		}
	};


	private void onMapFileDownloadSuccess() {
		downloadProgressDialog.dismiss();
		Toast.makeText(this, "Download success", Toast.LENGTH_LONG).show();
		clearTileCache();
		setMapFile();
	}

	private void onMapFileDownloadFailure() {
		downloadProgressDialog.dismiss();
		Toast.makeText(this, "Download failure", Toast.LENGTH_LONG).show();
	}

	private void clearTileCache() {
		try {
			Log.i(TAG, "Clearing Mapsforge cache...");
			String externalStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
			String CACHE_DIRECTORY = "/Android/data/org.mapsforge.android.maps/cache/";
			String cacheDirectoryPath = externalStorageDirectory + CACHE_DIRECTORY;
			Log.i(TAG, "cacheDirectoryPath="+cacheDirectoryPath);
			FileUtils.deleteDirectory(new File(cacheDirectoryPath));
		} catch (Exception e) {
			Log.w(TAG, "Failed to clear the MapsForge cache",e);
		}
	}


	final String TAG = "BladenightMapActivity";
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this); 
	private ServiceConnection serviceConnection;
	private final String mapLocalPath = Environment.getExternalStorageDirectory().getPath()+"/Bladenight/munich.map";
	private final String mapRemotePath = "maps/munich.map";
	private ProgressDialog downloadProgressDialog;
	private String routeName = "";
	private boolean isRealTime = false;
	private RouteOverlay routeOverlay;
	private BladenightMapView mapView;
	private PeriodicBroadcastIntentManager periodicBroadcastIntentManager = new PeriodicBroadcastIntentManager(this);
	private ProcessionProgressBar processionProgressBar;
} 
