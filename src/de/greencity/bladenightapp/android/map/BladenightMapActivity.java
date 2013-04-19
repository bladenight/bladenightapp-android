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
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
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
import de.greencity.bladenightapp.android.utils.JsonCacheAccess;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class BladenightMapActivity extends MapActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "onCreate");

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

		Log.i(TAG, "onStart");

		verifyMapFile();

		configureActionBar();

		getActivityParametersFromIntent(getIntent());

		routeCache = new JsonCacheAccess<RouteMessage>(this, RouteMessage.class, JsonCacheAccess.getNameForRoute(routeName));

		if ( isShowingActiveEvent ) {
			periodicTask = new Runnable() {
				@Override
				public void run() {
					Log.i(TAG, "periodic task");
					if ( ! isRouteInfoAvailable )
						requestRouteFromNetworkService();
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

		// The auto-zooming of the fetched route requires to have the layout 
		if (mapView.getWidth() == 0 || mapView.getHeight() == 0 ) {
			Log.i(TAG, "scheduling triggerInitialRouteDataFetch");
			ViewTreeObserver vto = mapView.getViewTreeObserver(); 
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
				@Override 
				public void onGlobalLayout() { 
					triggerInitialRouteDataFetch();
					mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
				} 
			}); 
		}
		else {
			triggerInitialRouteDataFetch();
		}
	}

	private void triggerInitialRouteDataFetch() {
		Log.i(TAG, "triggerInitialRouteDataFetch");
		updateRouteFromCache();
		requestRouteFromNetworkService();
	}

	@Override
	public void onStop() {
		super.onStop();
		if ( periodicTask != null )
			periodicHandler.removeCallbacks(periodicTask);
		if ( gpsListener != null)
			gpsListener.cancelLocationUpdates();
	}


	private void getActivityParametersFromIntent(Intent intent) {
		isShowingActiveEvent = true;
		if ( intent != null) {
			Bundle bundle = intent.getExtras();
			if ( bundle != null ) {
				String routeNameFromBundle = bundle.getString(PARAM_ROUTENAME);
				if ( routeNameFromBundle != null) {
					routeName = routeNameFromBundle;
					isShowingActiveEvent = false;
					if ( ! routeNameFromBundle.equals(routeName))
						isRouteInfoAvailable = false;
				}
				Log.i(TAG, "isShowingActiveEvent="+isShowingActiveEvent);
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
			reference.get().userPositionOverlay.update(realTimeUpdateData);
		}
	}

	protected void getRealTimeDataFromServer() {
		networkClient.getRealTimeData(new GetRealTimeDataFromServerHandler(this), null);
	}

	protected void requestRouteFromNetworkService() {
		if ( routeName.length() > 0 )
			getSpecificRouteFromServer(routeName);
		else
			getActiveRouteFromServer();
	}

	static class GetRouteFromServerHandler extends Handler {
		private WeakReference<BladenightMapActivity> reference;
		GetRouteFromServerHandler(BladenightMapActivity activity) {
			this.reference = new WeakReference<BladenightMapActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			RouteMessage routeMessage = (RouteMessage) msg.obj;
			reference.get().updateRouteFromRouteMessage(routeMessage);
			reference.get().routeCache.set(routeMessage);
		}
	}

	private void getSpecificRouteFromServer(String routeName) {
		Log.i(TAG,"getSpecificRouteFromServer routeName="+routeName);
		networkClient.getRoute(routeName, new GetRouteFromServerHandler(this), null);
	}

	private void getActiveRouteFromServer() {
		Log.i(TAG,"getActiveRouteFromServer");
		networkClient.getActiveRoute(new GetRouteFromServerHandler(this), null);
	}

	private void updateRouteFromRouteMessage(RouteMessage routeMessage) {
		isRouteInfoAvailable = true;;
		routeName = routeMessage.getRouteName();
		routeOverlay.update(routeMessage);
		fitViewToRoute();
	}

	private void updateRouteFromCache() {
		RouteMessage message = routeCache.get();
		if ( message != null ) {
			updateRouteFromRouteMessage(message);
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
		.show(ActionItemType.FRIENDS)
		.show(ActionItemType.TRACKER_CONTROL)
		.setTitle(R.string.title_map)
		.configure();
	}


	public void createMapView() {

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

		centerViewOnCoordinates(new GeoPoint(48.132491, 11.543474), (byte)13);
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
	private boolean isShowingActiveEvent = false;
	private RouteOverlay routeOverlay;
	private BladenightMapView mapView;
	private ProcessionProgressBar processionProgressBar;
	private NetworkClient networkClient;
	private final int updatePeriod = 2000;
	private final Handler periodicHandler = new Handler();
	private Runnable periodicTask;
	private UserPositionOverlay userPositionOverlay;
	private GpsListener gpsListener;
	private boolean isRouteInfoAvailable = false;
	private JsonCacheAccess<RouteMessage> routeCache;
	static public final String PARAM_ROUTENAME = "routeName";

} 
