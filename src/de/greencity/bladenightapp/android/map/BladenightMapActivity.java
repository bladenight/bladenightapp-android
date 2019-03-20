package de.greencity.bladenightapp.android.map;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.greencity.bladenightapp.android.app.BladeNightApplication;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.tracker.GpsListener;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.Paths;
import de.greencity.bladenightapp.android.utils.ResourceUtils;
import de.greencity.bladenightapp.dev.android.R;

public class BladenightMapActivity extends Activity {

    final static String TAG = "BladenightMapActivity";

    public static final String MAP_RESOURCE_PATH = "map/munich.map";
    public static final String MAP_LOCAL_PATH = "munich.map";

    private GlobalStateAccess globalStateAccess;
    private NetworkClient networkClient;
    private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
    private String routeName = "";
    private int routeLength;
    private boolean isLive = false;
    // private RouteOverlay routeOverlay;
    // private BladenightMapView mapView;
    private ProcessionProgressBar processionProgressBar;
    private TextView mapHeadline;
    private View mapHeadlineSeparator;
    private final int updatePeriod = 3000;
    private final Handler periodicHandler = new Handler();
    private Runnable periodicTask;
    // private UserPositionOverlay userPositionOverlay;
    private GpsListener gpsListener;
    private boolean isRouteInfoAvailable = false;
    public static final String PARAM_EVENT_MESSAGE = "eventMessage";
    private boolean isRunning = true;
    private boolean shallFitViewWhenPossible = true;
    private File mapLocalFile;

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        mapLocalFile = new File(Paths.getAppDataDirectory(this), MAP_LOCAL_PATH);
        verifyMapFile();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_action);


        // createMapView();
        // createOverlays();

        globalStateAccess = new GlobalStateAccess(this);
        networkClient = BladeNightApplication.networkClient;

        processionProgressBar = (ProcessionProgressBar) findViewById(R.id.progress_procession);
        mapHeadline = (TextView) findViewById(R.id.map_headline);
        mapHeadlineSeparator = (View) findViewById(R.id.map_headline_separator);

        /*
         * Before you make any calls on the mapsforge library, you need to initialize the
         * AndroidGraphicFactory. Behind the scenes, this initialization process gathers a bit of
         * information on your device, such as the screen resolution, that allows mapsforge to
         * automatically adapt the rendering for the device.
         * If you forget this step, your app will crash. You can place this code, like in the
         * Samples app, in the Android Application class. This ensures it is created before any
         * specific activity. But it can also be created in the onCreate() method in your activity.
         */
        AndroidGraphicFactory.createInstance(getApplication());

        /*
         * A MapView is an Android View (or ViewGroup) that displays a mapsforge map. You can have
         * multiple MapViews in your app or even a single Activity. Have a look at the mapviewer.xml
         * on how to create a MapView using the Android XML Layout definitions. Here we create a
         * MapView on the fly and make the content view of the activity the MapView. This means
         * that no other elements make up the content of this activity.
         */
        mapView = new MapView(this);
        setContentView(mapView);

        /*
         * We then make some simple adjustments, such as showing a scale bar and zoom controls.
         */
        mapView.setClickable(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.setBuiltInZoomControls(true);

        /*
         * To avoid redrawing all the tiles all the time, we need to set up a tile cache with an
         * utility method.
         */
        TileCache tileCache = AndroidUtil.createTileCache(this, "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                mapView.getModel().frameBufferModel.getOverdrawFactor());

        /*
         * Now we need to set up the process of displaying a map. A map can have several layers,
         * stacked on top of each other. A layer can be a map or some visual elements, such as
         * markers. Here we only show a map based on a mapsforge map file. For this we need a
         * TileRendererLayer. A TileRendererLayer needs a TileCache to hold the generated map
         * tiles, a map file from which the tiles are generated and Rendertheme that defines the
         * appearance of the map.
         */
        MapDataStore mapDataStore = new MapFile(mapLocalFile);
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(CustomRenderTheme.CUSTOM_RENDER);
        // tileRendererLayer.setXmlRenderTheme();
        // tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

        /*
         * On its own a tileRendererLayer does not know where to display the map, so we need to
         * associate it with our mapView.
         */
        mapView.getLayerManager().getLayers().add(tileRendererLayer);

        mapView.setCenter(new LatLong(48.1351, 11.5820));
        mapView.setZoomLevel((byte) 12);
    }

    @Override
    protected void onDestroy() {
        /*
         * Whenever your activity exits, some cleanup operations have to be performed lest your app
         * runs out of memory.
         */
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    private void verifyMapFile() {
        // TODO provide a way to delete the file in case it is corrupted
        if (! mapLocalFile.exists() || mapLocalFile.length() == 0) {
            if(! ResourceUtils.extractMapFile(MAP_RESOURCE_PATH, mapLocalFile)) {
                Toast.makeText(this, R.string.msg_failed_to_extract_map, Toast.LENGTH_LONG).show();
            }
        }
    }
}
