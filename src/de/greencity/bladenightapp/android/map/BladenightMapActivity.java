package de.greencity.bladenightapp.android.map;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.util.MapViewProjection;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.actions.ActionLocateMe;
import de.greencity.bladenightapp.android.app.BladeNightApplication;
import de.greencity.bladenightapp.android.cache.EventsMessageCache;
import de.greencity.bladenightapp.android.cache.RoutesCache;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.map.userovl.UserPositionsOverlay;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.progressbar.ProgressBarRenderer;
import de.greencity.bladenightapp.android.tracker.GpsListener;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.Paths;
import de.greencity.bladenightapp.android.utils.ResourceUtils;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventListMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;

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
    private RouteOverlay routeOverlay;
    private ImageView progressBarImageView;
    private TextView mapHeadline;
    private View mapHeadlineSeparator;
    private final int updatePeriod = 3000;
    private final Handler periodicHandler = new Handler();
    private Runnable periodicTask;
    private UserPositionsOverlay userPositionOverlay;
    private GpsListener gpsListener;
    private boolean isRouteInfoAvailable = false;
    public static final String PARAM_EVENT_MESSAGE = "eventMessage";
    private boolean isRunning = true;
    private boolean shallFitViewWhenPossible = true;
    private File mapLocalFile;
    private ProgressBarRenderer progressBarRenderer;

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        mapLocalFile = new File(Paths.getAppDataDirectory(this), MAP_LOCAL_PATH);
        verifyMapFile();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_action);

        globalStateAccess = new GlobalStateAccess(this);
        networkClient = BladeNightApplication.networkClient;

        progressBarImageView = (ImageView) findViewById(R.id.progress_procession);
        mapHeadline = (TextView) findViewById(R.id.map_headline);
        mapHeadlineSeparator = (View) findViewById(R.id.map_headline_separator);

        progressBarRenderer = new ProgressBarRenderer(this);
        progressBarRenderer.setFontSize(18);

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

        createMapView();
        createOverlays();

        configureActionBar();
    }

    private void createOverlays() {
        /*
        if (routeOverlay != null)
            mapView.getLayerManager().getLayers().remove();
            */
        routeOverlay = new RouteOverlay(mapView);
        /*
        TODO
        if (userPositionOverlay != null)
            mapView.getOverlays().remove(userPositionOverlay);
            */
        userPositionOverlay = new UserPositionsOverlay(this, mapView);
    }

    @Override
    protected void onDestroy() {
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");

        configureBasedOnIntent();

    }

    @Override
    public void onResume() {
        super.onResume();

        // Friend colors and stuff like that could have been changed in the meantime, so re-create the overlays
        userPositionOverlay.onResume();

        broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_REALTIME_DATA, new RealTimeDataBroadcastReceiver());
        broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_GPS_UPDATE, new LocationBroadcastReceiver());

        registerGpsListener();

        periodicTask = new Runnable() {
            @Override
            public void run() {
                if (!isRunning)
                    return;
                // Log.i(TAG, "periodic task");
                if (!isRouteInfoAvailable)
                    requestRouteFromServer();
                getRealTimeDataFromServer();
                periodicHandler.postDelayed(this, updatePeriod);
            }
        };
        periodicHandler.post(periodicTask);

        if (!isLive) {
            progressBarImageView.setVisibility(View.GONE);
            mapHeadline.setVisibility(View.VISIBLE);
            mapHeadlineSeparator.setVisibility(View.VISIBLE);
            updateHeadline(null);
        } else {
            progressBarImageView.setVisibility(View.VISIBLE);
            mapHeadline.setVisibility(View.VISIBLE);
            mapHeadlineSeparator.setVisibility(View.GONE);
        }

        // The auto-zooming of the fetched route requires to have the layout
        if (mapView.getWidth() == 0 || mapView.getHeight() == 0) {
            Log.i(TAG, "scheduling triggerInitialRouteDataFetch");
            ViewTreeObserver vto = mapView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    triggerInitialRouteDataFetch();
                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        } else {
            triggerInitialRouteDataFetch();
        }

        isRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelAllAutomaticTasks();
        broadcastReceiversRegister.unregisterReceivers();
        isRunning = false;
    }


    class RealTimeDataBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "RealTimeDataBroadcastReceiver.onReceive");
            final BladenightMapActivity bladenightMapActivity = BladenightMapActivity.this;
            if (bladenightMapActivity == null || bladenightMapActivity.isFinishing() || !bladenightMapActivity.isRunning)
                return;
            RealTimeUpdateData realTimeUpdateData = globalStateAccess.getRealTimeUpdateData();
            String liveRouteName = realTimeUpdateData.getRouteName();
            if (bladenightMapActivity.isLive) {
                if (!liveRouteName.equals(bladenightMapActivity.routeName)) {
                    if (bladenightMapActivity.routeName != null) {
                        // the route has changed, typically Lang -> Kurz
                        Log.i(TAG, "GetRealTimeDataFromServerHandler: route has changed: " + bladenightMapActivity.routeName + " -> " + liveRouteName);
                        String text = bladenightMapActivity.getResources().getString(R.string.msg_route_has_changed);
                        Toast.makeText(bladenightMapActivity, text + " " + liveRouteName, Toast.LENGTH_LONG).show();
                    }
                    bladenightMapActivity.routeName = liveRouteName;
                    bladenightMapActivity.requestRouteFromServer();
                }
                bladenightMapActivity.routeOverlay.update(realTimeUpdateData);
                bladenightMapActivity.userPositionOverlay.update(realTimeUpdateData);
                bladenightMapActivity.update(realTimeUpdateData);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);

                progressBarRenderer.updateRealTimeUpdateData(realTimeUpdateData);
                if(progressBarImageView.getWidth() > 0 && progressBarImageView.getHeight() > 0)
                    progressBarImageView.setImageBitmap(progressBarRenderer.renderToBitmap(progressBarImageView.getWidth(), progressBarImageView.getHeight(), displayMetrics));
                else
                    progressBarImageView.setBackgroundColor(getResources().getColor(R.color.new_background));
            } else {
                bladenightMapActivity.userPositionOverlay.update(realTimeUpdateData);
            }
        }
    }

    class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "LocationBroadcastReceiver.onReceive " + intent);
            final BladenightMapActivity bladenightMapActivity = BladenightMapActivity.this;
            Location location = globalStateAccess.getLocationFromGps();
            bladenightMapActivity.userPositionOverlay.onLocationChanged(location);
        }
    }


    private void triggerInitialRouteDataFetch() {
        Log.i(TAG, "triggerInitialRouteDataFetch");
        updateRouteFromCache();
        requestRouteFromServer();
    }


    @Override
    public void onPause() {
        super.onPause();
        cancelAllAutomaticTasks();
        isRunning = false;
    }

    public void cancelAllAutomaticTasks() {
        if (periodicTask != null)
            periodicHandler.removeCallbacks(periodicTask);
        if (gpsListener != null)
            gpsListener.cancelLocationUpdates();
    }


    public void registerGpsListener() {
        destroyGpsListener();
        gpsListener = new GpsListener(this, globalStateAccess);
        gpsListener.requestLocationUpdates(updatePeriod);
    }

    public void destroyGpsListener() {
        if (gpsListener != null)
            gpsListener.cancelLocationUpdates();
        gpsListener = null;
    }

    private void configureBasedOnIntent() {

        Log.i(TAG, "configureBasedOnIntent");

        getActivityParametersFromIntentOrDefault(getIntent());

        configureActionBar();
    }

    private boolean getActivityParametersFromIntentOrDefault(Intent intent) {
        if (getActivityParametersFromIntent(intent))
            return true;
        Event nextEvent = getEventListFromCacheOrEmptyList().getNextEvent();
        if (nextEvent == null)
            return false;
        getActivityParametersFromEvent(nextEvent);
        return true;
    }

    private boolean getActivityParametersFromIntent(Intent intent) {
        Log.i(TAG, "getActivityParametersFromIntent intent=" + intent);

        if (intent == null)
            return false;

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Log.i(TAG, "getActivityParametersFromIntent bundle=" + bundle);
            return false;
        }
        String json = bundle.getString(PARAM_EVENT_MESSAGE);
        if (json == null) {
            Log.i(TAG, "getActivityParametersFromIntent json=" + json);
            return false;
        }

        Log.i(TAG, "json=" + json);
        Event event = EventGsonHelper.getGson().fromJson(json, Event.class);

        if (event == null) {
            Log.i(TAG, "getActivityParametersFromIntent eventMessage=" + event);
            return false;
        }

        getActivityParametersFromEvent(event);

        Log.i(TAG, "getActivityParametersFromIntent DONE routeName=" + routeName);
        Log.i(TAG, "isLive=" + isLive);
        return true;
    }

    private void getActivityParametersFromEvent(Event event) {
        setRoute(event.getRouteName());
        isLive = false;
        EventList eventList = getEventListFromCacheOrEmptyList();
        if (eventList.isLive(event)) {
            isLive = true;
        }
    }

    private EventList getEventListFromCacheOrEmptyList() {
        EventListMessage eventListMessage = new EventsMessageCache(this).read();
        if (eventListMessage == null)
            return new EventList();
        return eventListMessage.convertToEventsList();
    }

    private void setRoute(String routeName) {
        if (!routeName.equals(this.routeName)) {
            // Activity will now display a new new route, request automatic zooming
            shallFitViewWhenPossible = true;
            isRouteInfoAvailable = false;
        }
        this.routeName = routeName;
    }

    public void createMapView() {

        // mapView = new BladenightMapView(this);
        mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getMapScaleBar().setVisible(true);

        LinearLayout parent = (LinearLayout) findViewById(R.id.map_parent);
        parent.removeAllViews();
        parent.addView(mapView);

        TileCache tileCache = AndroidUtil.createTileCache(this, "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                mapView.getModel().frameBufferModel.getOverdrawFactor(), true);

        MapDataStore mapDataStore = new MapFile(mapLocalFile);
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(CustomRenderTheme.CUSTOM_RENDER);

        mapView.getLayerManager().getLayers().add(tileRendererLayer);

        mapView.setCenter(new LatLong(48.132491, 11.543474));
        mapView.setZoomLevel((byte) 13);
    }


    private void verifyMapFile() {
        if (!mapLocalFile.exists() || mapLocalFile.length() == 0) {
            if (!ResourceUtils.extractMapFile(MAP_RESOURCE_PATH, mapLocalFile)) {
                Toast.makeText(this, R.string.msg_failed_to_extract_map, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void configureActionBar() {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        ActionBarConfigurator configurator = new ActionBarConfigurator(actionBar);

        configurator.show(ActionBarConfigurator.ActionItemType.FRIENDS);

        if (isLive) {
            configurator
                    .show(ActionBarConfigurator.ActionItemType.TRACKER_CONTROL)
                    .setTitle(R.string.title_map_live);
        } else {
            configurator.setTitle(R.string.title_map_default);
        }

        configurator.setAction(ActionBarConfigurator.ActionItemType.LOCATE_ME, new ActionLocateMe() {
            @Override
            public void performAction(View view) {
                Toast.makeText(view.getContext(), view.getResources().getString(R.string.msg_locate), Toast.LENGTH_SHORT).show();
                BladenightMapActivity.this.centerViewOnLastKnownLocation();
            }
        });

        // If the tracker is currently running, show the control in the activity
        // to give a chance to the user to stop it from here
        if (ServiceUtils.isServiceRunning(this, GpsTrackerService.class))
            configurator.show(ActionBarConfigurator.ActionItemType.TRACKER_CONTROL);

        configurator.configure();
    }

    protected void getRealTimeDataFromServer() {
        globalStateAccess.requestRealTimeUpdateData();
    }

    public void update(RealTimeUpdateData realTimeUpdateData) {
        updateHeadline(realTimeUpdateData);
    }

    protected void requestRouteFromServer() {
        if (routeName.length() > 0)
            getSpecificRouteFromServer(routeName);
        else
            getNextRouteFromServer();
    }

    private void updateHeadline(RealTimeUpdateData realTimeUpdateData) {
        if (isLive) {
            int trackedParticipants = (realTimeUpdateData != null ? realTimeUpdateData.getUserTotal() : 0);
            String trackedParticipantsString = getResources().getString(R.string.word_active_trackers);
            String formattedText = String.format(Locale.getDefault(), "%s | %1.1fkm  |  %s: %d",
                    routeNameToText(routeName),
                    routeLength / 1000.0,
                    trackedParticipantsString,
                    trackedParticipants
            );
            mapHeadline.setText(formattedText);
        } else {
            String formattedText = String.format(Locale.getDefault(), "%s | %1.1fkm",
                    routeNameToText(routeName),
                    routeLength / 1000.0
            );
            mapHeadline.setText(formattedText);
        }
    }

    private String routeNameToText(String routeName) {
        if (routeName.equals("Nord - kurz")) {
            return getResources().getString(R.string.course_north_short);
        }
        if (routeName.equals("Nord - lang")) {
            return getResources().getString(R.string.course_north_long);
        }
        if (routeName.equals("West - kurz")) {
            return getResources().getString(R.string.course_west_short);
        }
        if (routeName.equals("West - lang")) {
            return getResources().getString(R.string.course_west_long);
        }
        if (routeName.equals("Ost - kurz")) {
            return getResources().getString(R.string.course_east_short);
        }
        if (routeName.equals("Ost - lang")) {
            return getResources().getString(R.string.course_east_long);
        }
        if (routeName.equals("Familie")) {
            return getResources().getString(R.string.course_family);
        }
        return routeName;
    }


    static class GetRouteFromServerHandler extends Handler {
        private WeakReference<BladenightMapActivity> reference;

        GetRouteFromServerHandler(BladenightMapActivity activity) {
            this.reference = new WeakReference<BladenightMapActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final BladenightMapActivity bladenightMapActivity = reference.get();
            if (bladenightMapActivity == null || bladenightMapActivity.isFinishing() || !bladenightMapActivity.isRunning)
                return;
            RouteMessage routeMessage = (RouteMessage) msg.obj;
            bladenightMapActivity.updateRouteFromRouteMessage(routeMessage);
            new RoutesCache(bladenightMapActivity).write(routeMessage);
        }
    }

    private void getSpecificRouteFromServer(String routeName) {
        Log.i(TAG, "getSpecificRouteFromServer routeName=" + routeName);
        networkClient.getRoute(routeName, new GetRouteFromServerHandler(this), null);
    }

    private void getNextRouteFromServer() {
        Log.i(TAG, "getNextRouteFromServer");
        networkClient.getActiveRoute(new GetRouteFromServerHandler(this), null);
    }


    private void updateRouteFromRouteMessage(RouteMessage routeMessage) {
        if (!routeMessage.getRouteName().equals(routeName)) {
            Log.e(TAG, "Inconsistency: Got \"" + routeMessage.getRouteName() + "\" but expected: \"" + routeName + "\"");
            Log.i(TAG, "Trace: " + ExceptionUtils.getStackTrace(new Throwable()));
        }
        isRouteInfoAvailable = true;
        routeName = routeMessage.getRouteName();
        routeLength = routeMessage.getRouteLength();
        // TODO
        routeOverlay.update(routeMessage);
        if (shallFitViewWhenPossible) {
            shallFitViewWhenPossible = false;
            fitViewToRoute();
        }
        updateHeadline(null);
    }

    private void updateRouteFromCache() {
        RouteMessage message = new RoutesCache(this).read(routeName);
        if (message != null) {
            updateRouteFromRouteMessage(message);
        }
    }

    protected void fitViewToRoute() {
        if (routeOverlay != null) {
            shallFitViewWhenPossible = false;
            fitViewToBoundingBox(routeOverlay.getRouteBoundingBox());
        }
    }

    protected void fitViewToProcession() {
        if (routeOverlay != null) {
            fitViewToBoundingBox(routeOverlay.getProcessionBoundingBox());
        }
    }


    protected void centerViewOnCoordinates(LatLong center, byte zoomLevel) {
        mapView.setCenter(center);
        mapView.setZoomLevel(zoomLevel);
    }

    protected void centerViewOnLastKnownLocation() {
        Location location = userPositionOverlay.getLastOwnLocation();
        if (location != null) {
            this.mapView.setCenter(new LatLong(location.getLatitude(), location.getLongitude()));
        } else {
            String text = getResources().getString(R.string.msg_current_position_unknown);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
    }

    public synchronized void fitViewToBoundingBox(final BoundingBox boundingBox) {
        if (boundingBox != null && boundingBox.getLatitudeSpan() > 0 && boundingBox.getLongitudeSpan() > 0) {
            int width = mapView.getWidth();
            int height = mapView.getHeight();
            if (width <= 0 || height <= 0) {
                Log.w(TAG, "Invalid dimension " + width + "/" + height);
                Log.i(TAG, "Invalid dimension " + boundingBox.toString());
                Log.i(TAG, "Trace: " + ExceptionUtils.getStackTrace(new Throwable()));
                return;
            }
            MapViewProjection projection = mapView.getMapViewProjection();
            LatLong pointSouthWest = new LatLong(boundingBox.minLatitude, boundingBox.minLongitude);
            LatLong pointNorthEast = new LatLong(boundingBox.maxLatitude, boundingBox.maxLongitude);
            byte maximumZoom = mapView.getMapZoomControls().getZoomLevelMax();
            byte zoomLevel = 0;
            while (zoomLevel < maximumZoom) {
                mapView.setZoomLevel(zoomLevel);
                Point pointSW = projection.toPixels(pointSouthWest);
                Point pointNE = projection.toPixels(pointNorthEast);
                if (pointNE.x - pointSW.x > width || pointSW.y - pointNE.y > height) {
                    zoomLevel--;
                    break;
                }
                zoomLevel++;
            }
            mapView.setZoomLevel(zoomLevel);
            mapView.setCenter(boundingBox.getCenterPoint());
        }
    }
}
