package de.greencity.bladenightapp.android.map;


import java.io.File;
import java.lang.ref.WeakReference;

import org.apache.commons.io.FileUtils;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.reader.header.FileOpenResult;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.tracker.GpsListener;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTask;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class BladenightMapActivity extends MapActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		networkClient = new NetworkClient(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_action);
		createMapView();

		downloadProgressDialog = new ProgressDialog(this);
		processionProgressBar = (ProcessionProgressBar) findViewById(R.id.progress_procession);
	}

	@Override
	public void onStart() {
		super.onStart();

		verifyMapFile();

		configureActionBar();

		getActivityParametersFromIntent(getIntent());

		requestRouteFromNetworkService();

		if ( isRealTime ) {
			periodicTask = new Runnable() {
				@Override
				public void run() {
					Log.i(TAG, "periodic task");
					getRealTimeDataFromServer();
					periodicHandler.postDelayed(this, updatePeriod);
				}
			};
			periodicHandler.postDelayed(periodicTask, updatePeriod);
			gpsListener = new GpsListener(this, userPositionOverlay);
			gpsListener.requestLocationUpdates(updatePeriod);
		}
		else {
			processionProgressBar.setVisibility(View.GONE);
		}
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

	static class GetRealTimeDataFromServerHandler extends Handler {
		private WeakReference<BladenightMapActivity> reference;
		GetRealTimeDataFromServerHandler(BladenightMapActivity activity) {
			this.reference = new WeakReference<BladenightMapActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			RealTimeUpdateData realTimeUpdateData = (RealTimeUpdateData)msg.obj;
			reference.get().routeOverlay.update(realTimeUpdateData);
			reference.get().processionProgressBar.update(realTimeUpdateData);
		}
	}

	protected void getRealTimeDataFromServer() {
		networkClient.getRealTimeData(new GetRealTimeDataFromServerHandler(this), null);
	}

	protected void requestRouteFromNetworkService() {
		getRouteFromServer(routeName);
		if ( isRealTime ) {
			getRealTimeDataFromServer();
		}
	}

	static class GetRouteFromServerHandler extends Handler {
		private WeakReference<BladenightMapActivity> reference;
		GetRouteFromServerHandler(BladenightMapActivity activity) {
			this.reference = new WeakReference<BladenightMapActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			reference.get().routeOverlay.update((RouteMessage) msg.obj);
			reference.get().fitViewToRoute();
		}
	}

	private void getRouteFromServer(String routeName) {
		Log.i(TAG,"getRouteFromServer routeName="+routeName);
		networkClient.getRoute(routeName, new GetRouteFromServerHandler(this), null);
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
		periodicHandler.removeCallbacks(periodicTask);
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

		userPositionOverlay = new UserPositionOverlay(this, mapView);

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

		AsyncDownloadTask.StatusHandler handler = new AsyncDownloadTask.StatusHandler() {

			@Override
			public void onProgress(long current, long total) {
				int percent = (int)(current*100.0/total);
				downloadProgressDialog.setProgress(percent);
			}

			@Override
			public void onDownloadSuccess() {
				Log.i(TAG, "Download successful");
				downloadProgressDialog.dismiss();
				clearTileCache();
				setMapFile();
			}

			@Override
			public void onDownloadFailure() {
				Log.i(TAG, "Download failed");
				downloadProgressDialog.dismiss();
				clearTileCache();
				setMapFile();
			}
		};
		networkClient.downloadFile(mapLocalPath, mapRemotePath, handler);
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
	private final String mapLocalPath = Environment.getExternalStorageDirectory().getPath()+"/Bladenight/munich.map";
	private final String mapRemotePath = "maps/munich.map";
	private ProgressDialog downloadProgressDialog;
	private String routeName = "";
	private boolean isRealTime = false;
	private RouteOverlay routeOverlay;
	private BladenightMapView mapView;
	private ProcessionProgressBar processionProgressBar;
	private NetworkClient networkClient;
	private final int updatePeriod = 2000;
	private final Handler periodicHandler = new Handler();
	private Runnable periodicTask;
	private UserPositionOverlay userPositionOverlay;
	private GpsListener gpsListener;
} 
