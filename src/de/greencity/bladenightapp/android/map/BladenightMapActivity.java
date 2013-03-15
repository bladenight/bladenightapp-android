package de.greencity.bladenightapp.android.map;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.android.maps.overlay.PolygonalChain;
import org.mapsforge.android.maps.overlay.Polyline;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.map.reader.header.FileOpenResult;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.network.Actions;
import de.greencity.bladenightapp.android.network.NetworkService;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.JsonBroadcastReceiver;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class BladenightMapActivity extends MapActivity {

	private RouteOverlay routeOverlay;
	private MapView mapView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_action);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		ImageView titlebar = (ImageView)(findViewById(R.id.icon));
		titlebar.setImageResource(R.drawable.ic_map);
		TextView titletext = (TextView)findViewById(R.id.title);
		titletext.setText(R.string.title_map);

		createMapView();

		broadcastReceiversRegister.registerReceiver(Actions.GOT_ACTIVE_ROUTE, gotActiveRouteReceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		broadcastReceiversRegister.unregisterReceivers();
	}

	@Override
	public void onStart() {
		super.onStart();
		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG+".ServiceConnection", "onServiceConnected");
				sendBroadcast(new Intent(Actions.GET_ACTIVE_ROUTE));
			}
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG+".ServiceConnection", "onServiceDisconnected");
			}

		};
		bindService(new Intent(this, NetworkService.class), serviceConnection,  BIND_AUTO_CREATE);
	}

	public void createMapView() {
		mapView = new MapView(this);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

		String externalStoragePath = Environment.getExternalStorageDirectory().getPath();

		String mapPath = externalStoragePath+"/Bladenight/munich-new.map";
		if ( mapView.setMapFile(new File(mapPath)) != FileOpenResult.SUCCESS ) {
			Log.e(TAG, "Failed to set map file: " + mapPath);
			Toast.makeText(this, R.string.msg_failed_to_load_map , Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(this, "File opened", Toast.LENGTH_LONG).show();
		}
		RelativeLayout parent = (RelativeLayout) findViewById(R.id.map_parent);
		parent.removeAllViews();

		mapView.getMapViewPosition().setZoomLevel((byte) 15);
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

	private final BroadcastReceiver gotActiveRouteReceiver = new JsonBroadcastReceiver<RouteMessage>("gotActiveRouteReceiver", RouteMessage.class) {
		@Override
		public void onReceive(RouteMessage routeMessage) {
			routeOverlay.update(routeMessage);
		}
	};

	final String TAG = "BladenightMapActivity";
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this); 
	private ServiceConnection serviceConnection;
} 
