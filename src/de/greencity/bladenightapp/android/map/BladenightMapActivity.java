package de.greencity.bladenightapp.android.map;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.reader.header.FileOpenResult;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.actionbar.ActionLocateMe;
import de.greencity.bladenightapp.android.app.BladeNightApplication;
import de.greencity.bladenightapp.android.cache.EventsMessageCache;
import de.greencity.bladenightapp.android.cache.RoutesCache;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.map.userovl.UserPositionOverlay;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.tracker.GpsListener;
import de.greencity.bladenightapp.android.tracker.GpsTrackerService;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTaskHttpClient;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.Paths;
import de.greencity.bladenightapp.android.utils.ServiceUtils;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventListMessage;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class BladenightMapActivity extends MapActivity {
    final static String TAG = "BladenightMapActivity";
    private GlobalStateAccess globalStateAccess;
    private NetworkClient networkClient;
    private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
    private String mapLocalPath;
    private final String mapRemotePath = "maps/munich.map";
    private ProgressDialog downloadProgressDialog;
    private String routeName = "";
    private int routeLength;
    private boolean isLive = false;
    private RouteOverlay routeOverlay;
    private BladenightMapView mapView;
    private ProcessionProgressBar processionProgressBar;
    private TextView mapHeadline;
    private View mapHeadlineSeparator;
    private final int updatePeriod = 3000;
    private final Handler periodicHandler = new Handler();
    private Runnable periodicTask;
    private UserPositionOverlay userPositionOverlay;
    private GpsListener gpsListener;
    private boolean isRouteInfoAvailable = false;
    public static final String PARAM_EVENT_MESSAGE = "eventMessage";
    private boolean isRunning = true;
    private boolean shallFitViewWhenPossible = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        mapLocalPath = new File(Paths.getAppDataDirectory(this), "munich.map").getAbsolutePath();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_action);
        createMapView();
        createOverlays();

        globalStateAccess = new GlobalStateAccess(this);
        networkClient = BladeNightApplication.networkClient;

        downloadProgressDialog = new ProgressDialog(this);
        processionProgressBar = (ProcessionProgressBar) findViewById(R.id.progress_procession);
        mapHeadline = (TextView) findViewById(R.id.map_headline);
        mapHeadlineSeparator = (View) findViewById(R.id.map_headline_separator);
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");

        verifyMapFile();

        configureBasedOnIntent();

    }

    @Override
    public void onResume() {
        super.onResume();

        // Friend colors and stuff like that could have been changed in the meantime, so re-create the overlays
        userPositionOverlay.onResume();
        processionProgressBar.onResume();

        registerGpsListener();

        periodicTask = new Runnable() {
            @Override
            public void run() {
                if ( ! isRunning )
                    return;
                // Log.i(TAG, "periodic task");
                if ( ! isRouteInfoAvailable )
                    requestRouteFromServer();
                getRealTimeDataFromServer();
                periodicHandler.postDelayed(this, updatePeriod);
            }
        };
        periodicHandler.post(periodicTask);

        if ( ! isLive ) {
            processionProgressBar.setVisibility(View.GONE);
            mapHeadline.setVisibility(View.VISIBLE);
            mapHeadlineSeparator.setVisibility(View.VISIBLE);
            updateHeadline(null);
        }
        else {
            processionProgressBar.setVisibility(View.VISIBLE);
            mapHeadline.setVisibility(View.VISIBLE);
            mapHeadlineSeparator.setVisibility(View.GONE);
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

        broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_REALTIME_DATA, new RealTimeDataBroadcastReceiver());
        broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_GPS_UPDATE, new LocationBroadcastReceiver());

        isRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelAllAutomaticTasks();
        broadcastReceiversRegister.unregisterReceivers();
        isRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.i(TAG, "onNewIntent");

        setIntent(intent);
        configureBasedOnIntent();
    }

    private void configureBasedOnIntent() {

        Log.i(TAG, "configureBasedOnIntent");

        getActivityParametersFromIntentOrDefault(getIntent());

        configureActionBar();
    }


    @Override
    public void onPause() {
        super.onPause();
        cancelAllAutomaticTasks();
        isRunning = false;
    }

    public void cancelAllAutomaticTasks() {
        if ( periodicTask != null )
            periodicHandler.removeCallbacks(periodicTask);
        if ( gpsListener != null )
            gpsListener.cancelLocationUpdates();
    }

    public void registerGpsListener() {
        destroyGpsListener();
        gpsListener = new GpsListener(this, globalStateAccess);
        gpsListener.requestLocationUpdates(updatePeriod);
    }

    public void destroyGpsListener() {
        if ( gpsListener != null )
            gpsListener.cancelLocationUpdates();
        gpsListener = null;
    }


    private void triggerInitialRouteDataFetch() {
        Log.i(TAG, "triggerInitialRouteDataFetch");
        updateRouteFromCache();
        requestRouteFromServer();
    }


    private boolean getActivityParametersFromIntentOrDefault(Intent intent) {
        if ( getActivityParametersFromIntent(intent) )
            return true;
        Event nextEvent = getEventListFromCacheOrEmptyList().getNextEvent();
        if ( nextEvent == null )
            return false;
        getActivityParametersFromEvent(nextEvent);
        return true;
    }

    private boolean getActivityParametersFromIntent(Intent intent) {
        Log.i(TAG, "getActivityParametersFromIntent intent="+intent);

        if ( intent == null)
            return false;

        Bundle bundle = intent.getExtras();
        if ( bundle == null ) {
            Log.i(TAG, "getActivityParametersFromIntent bundle="+bundle);
            return false;
        }
        String json = bundle.getString(PARAM_EVENT_MESSAGE);
        if ( json == null ) {
            Log.i(TAG, "getActivityParametersFromIntent json="+json);
            return false;
        }

        Log.i(TAG, "json="+json);
        Event event = EventGsonHelper.getGson().fromJson(json, Event.class);

        if ( event == null ) {
            Log.i(TAG, "getActivityParametersFromIntent eventMessage="+event);
            return false;
        }

        getActivityParametersFromEvent(event);

        Log.i(TAG, "getActivityParametersFromIntent DONE routeName="+routeName);
        Log.i(TAG, "isLive="+isLive);
        return true;
    }

    private void getActivityParametersFromEvent(Event event) {
        setRoute(event.getRouteName());
        isLive = false;
        EventList eventList = getEventListFromCacheOrEmptyList();
        if ( eventList.isLive(event ) ) {
            isLive = true;
        }
    }

    private EventList getEventListFromCacheOrEmptyList() {
        EventListMessage eventListMessage = new EventsMessageCache(this).read();
        if ( eventListMessage == null )
            return new EventList();
        return eventListMessage.convertToEventsList();
    }

    private void setRoute(String routeName) {
        if ( ! routeName.equals(this.routeName)) {
            // Activity will now display a new new route, request automatic zooming
            shallFitViewWhenPossible = true;
            isRouteInfoAvailable = false;
        }
        this.routeName = routeName;
    }

    static class GetRealTimeDataFromServerHandler extends Handler {
        private WeakReference<BladenightMapActivity> reference;
        GetRealTimeDataFromServerHandler(BladenightMapActivity activity) {
            this.reference = new WeakReference<BladenightMapActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final BladenightMapActivity bladenightMapActivity = reference.get();
            if ( bladenightMapActivity == null || bladenightMapActivity.isFinishing() || ! bladenightMapActivity.isRunning )
                return;
            RealTimeUpdateData realTimeUpdateData = (RealTimeUpdateData)msg.obj;
            String liveRouteName = realTimeUpdateData.getRouteName();
            if ( bladenightMapActivity.isLive ) {
                if ( ! liveRouteName.equals(bladenightMapActivity.routeName) ) {
                    if ( bladenightMapActivity.routeName != null ) {
                        // the route has changed, typically Lang -> Kurz
                        Log.i(TAG, "GetRealTimeDataFromServerHandler: route has changed: " + bladenightMapActivity.routeName + " -> " + liveRouteName);
                        String text = bladenightMapActivity.getResources().getString(R.string.msg_route_has_changed);
                        Toast.makeText(bladenightMapActivity, text + " " + liveRouteName, Toast.LENGTH_LONG).show();
                    }
                    bladenightMapActivity.routeName = liveRouteName;
                    bladenightMapActivity.requestRouteFromServer();
                }
                bladenightMapActivity.routeOverlay.update(realTimeUpdateData);
                bladenightMapActivity.processionProgressBar.update(realTimeUpdateData);
                bladenightMapActivity.userPositionOverlay.update(realTimeUpdateData);
                bladenightMapActivity.update(realTimeUpdateData);
            }
            else {
                bladenightMapActivity.userPositionOverlay.update(realTimeUpdateData);
            }
        }
    }

    class RealTimeDataBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "RealTimeDataBroadcastReceiver.onReceive");
            final BladenightMapActivity bladenightMapActivity = BladenightMapActivity.this;
            if ( bladenightMapActivity == null || bladenightMapActivity.isFinishing() || ! bladenightMapActivity.isRunning )
                return;
            RealTimeUpdateData realTimeUpdateData = globalStateAccess.getRealTimeUpdateData();
            String liveRouteName = realTimeUpdateData.getRouteName();
            if ( bladenightMapActivity.isLive ) {
                if ( ! liveRouteName.equals(bladenightMapActivity.routeName) ) {
                    if ( bladenightMapActivity.routeName != null ) {
                        // the route has changed, typically Lang -> Kurz
                        Log.i(TAG, "GetRealTimeDataFromServerHandler: route has changed: " + bladenightMapActivity.routeName + " -> " + liveRouteName);
                        String text = bladenightMapActivity.getResources().getString(R.string.msg_route_has_changed);
                        Toast.makeText(bladenightMapActivity, text + " " + liveRouteName, Toast.LENGTH_LONG).show();
                    }
                    bladenightMapActivity.routeName = liveRouteName;
                    bladenightMapActivity.requestRouteFromServer();
                }
                bladenightMapActivity.routeOverlay.update(realTimeUpdateData);
                bladenightMapActivity.processionProgressBar.update(realTimeUpdateData);
                bladenightMapActivity.userPositionOverlay.update(realTimeUpdateData);
                bladenightMapActivity.update(realTimeUpdateData);
            }
            else {
                bladenightMapActivity.userPositionOverlay.update(realTimeUpdateData);
            }
        }
    }

    class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "LocationBroadcastReceiver.onReceive");
            final BladenightMapActivity bladenightMapActivity = BladenightMapActivity.this;
            Location location = globalStateAccess.getLocationFromGps();
            bladenightMapActivity.userPositionOverlay.onLocationChanged(location);
        }
    }

    protected void getRealTimeDataFromServer() {
        globalStateAccess.requestRealTimeUpdateData();
    }

    public void update(RealTimeUpdateData realTimeUpdateData) {
        updateHeadline(realTimeUpdateData);
    }

    protected void requestRouteFromServer() {
        if ( routeName.length() > 0 )
            getSpecificRouteFromServer(routeName);
        else
            getNextRouteFromServer();
    }

    static class GetRouteFromServerHandler extends Handler {
        private WeakReference<BladenightMapActivity> reference;
        GetRouteFromServerHandler(BladenightMapActivity activity) {
            this.reference = new WeakReference<BladenightMapActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final BladenightMapActivity bladenightMapActivity = reference.get();
            if ( bladenightMapActivity == null || bladenightMapActivity.isFinishing() || ! bladenightMapActivity.isRunning )
                return;
            RouteMessage routeMessage = (RouteMessage) msg.obj;
            bladenightMapActivity.updateRouteFromRouteMessage(routeMessage);
            new RoutesCache(bladenightMapActivity).write(routeMessage);
        }
    }

    private void getSpecificRouteFromServer(String routeName) {
        Log.i(TAG,"getSpecificRouteFromServer routeName="+routeName);
        networkClient.getRoute(routeName, new GetRouteFromServerHandler(this), null);
    }

    private void getNextRouteFromServer() {
        Log.i(TAG,"getNextRouteFromServer");
        networkClient.getActiveRoute(new GetRouteFromServerHandler(this), null);
    }


    private void updateRouteFromRouteMessage(RouteMessage routeMessage) {
        if ( ! routeMessage.getRouteName().equals(routeName) ) {
            Log.e(TAG, "Inconsistency: Got \"" + routeMessage.getRouteName() + "\" but expected: \"" + routeName + "\"");
            Log.i(TAG, "Trace: " + ExceptionUtils.getStackTrace( new Throwable()));
        }
        isRouteInfoAvailable = true;
        routeName = routeMessage.getRouteName();
        routeLength = routeMessage.getRouteLength();
        routeOverlay.update(routeMessage);
        if ( shallFitViewWhenPossible ) {
            shallFitViewWhenPossible = false;
            fitViewToRoute();
        }
        updateHeadline(null);
    }

    private void updateRouteFromCache() {
        RouteMessage message = new RoutesCache(this).read(routeName);
        if ( message != null ) {
            updateRouteFromRouteMessage(message);
        }
    }

    private void configureActionBar() {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        ActionBarConfigurator configurator = new ActionBarConfigurator(actionBar);

        configurator.show(ActionItemType.FRIENDS);

        if ( isLive ) {
            configurator
            .show(ActionItemType.TRACKER_CONTROL)
            .setTitle(R.string.title_map_live);
        }
        else {
            configurator.setTitle(R.string.title_map_default);
        }

        configurator.setAction(ActionItemType.LOCATE_ME, new ActionLocateMe() {
            @Override
            public void performAction(View view) {
                Toast.makeText(view.getContext(), view.getResources().getString(R.string.msg_locate), Toast.LENGTH_SHORT).show();
                BladenightMapActivity.this.centerViewOnLastKnownLocation();
            }
        });

        // If the tracker is currently running, show the control in the activity
        // to give a chance to the user to stop it from here without having to go
        // to the Selection activity.
        if ( ServiceUtils.isServiceRunning(this, GpsTrackerService.class))
            configurator.show(ActionItemType.TRACKER_CONTROL);

        configurator.configure();
    }

    private void updateHeadline(RealTimeUpdateData realTimeUpdateData) {
        if ( isLive ) {
            int trackedParticipants = ( realTimeUpdateData != null ? realTimeUpdateData.getUserTotal() : 0);
            String trackedParticipantsString = getResources().getString(R.string.word_active_trackers);
            String formattedText = String.format(Locale.getDefault(), "%s | %1.1fkm  |  %s: %d",
                    routeNameToText(routeName),
                    routeLength / 1000.0,
                    trackedParticipantsString,
                    trackedParticipants
                    );
            mapHeadline.setText(formattedText);
        }
        else {
            String formattedText = String.format(Locale.getDefault(), "%s | %1.1fkm",
                    routeNameToText(routeName),
                    routeLength / 1000.0
                    );
            mapHeadline.setText(formattedText);
        }
    }

    private String routeNameToText(String routeName){
        if (routeName.equals("Nord - kurz")){
            return getResources().getString(R.string.course_north_short);
        }
        if (routeName.equals("Nord - lang")){
            return getResources().getString(R.string.course_north_long);
        }
        if (routeName.equals("West - kurz")){
            return getResources().getString(R.string.course_west_short);
        }
        if (routeName.equals("West - lang")){
            return getResources().getString(R.string.course_west_long);
        }
        if (routeName.equals("Ost - kurz")){
            return getResources().getString(R.string.course_east_short);
        }
        if (routeName.equals("Ost - lang")){
            return getResources().getString(R.string.course_east_long);
        }
        if (routeName.equals("Familie")){
            return getResources().getString(R.string.course_family);
        }
        return routeName;
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

        TileCache fileSystemTileCache = mapView.getFileSystemTileCache();
        fileSystemTileCache.setPersistent(true);
        fileSystemTileCache.setCapacity(100);

        centerViewOnCoordinates(new GeoPoint(48.132491, 11.543474), (byte)13);
    }

    public void createOverlays() {
        if ( routeOverlay != null )
            mapView.getOverlays().remove(routeOverlay);
        routeOverlay = new RouteOverlay(mapView);
        if ( userPositionOverlay != null )
            mapView.getOverlays().remove(userPositionOverlay);
        userPositionOverlay = new UserPositionOverlay(this, mapView);
    }

    private void verifyMapFile() {
        // TODO provide a way to delete the file in case it is corrupted
        if ( ! new File(mapLocalPath).exists() ) {
            startMapFileDownload();
        }
    }

    private void startMapFileDownload() {
        downloadProgressDialog.setMessage(getResources().getString(R.string.msg_download_maps));
        downloadProgressDialog.setIndeterminate(false);
        downloadProgressDialog.setMax(100);
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        downloadProgressDialog.show();

        final WeakReference<BladenightMapActivity> weakReference = new WeakReference<BladenightMapActivity>(this);
        AsyncDownloadTaskHttpClient.StatusHandler handler = new AsyncDownloadTaskHttpClient.StatusHandler() {
            @Override
            public void onProgress(long current, long total) {
                int percent = (int)(current*100.0/total);
                downloadProgressDialog.setProgress(percent);
            }

            @Override
            public void onDownloadSuccess() {
                Log.i(TAG, "Download successful");
                BladenightMapActivity activity = getActivity("onDownloadSuccess");
                if (activity == null)
                    return;
                activity.downloadProgressDialog.dismiss();
                activity.clearTileCache();
                activity.setMapFile();
            }

            @Override
            public void onDownloadFailure() {
                Log.i(TAG, "Download failed");
                BladenightMapActivity activity = getActivity("onDownloadFailure");
                if (activity == null)
                    return;
                activity.downloadProgressDialog.dismiss();
                activity.clearTileCache();
                activity.setMapFile();
            }

            public BladenightMapActivity getActivity(String tag) {
                BladenightMapActivity activity = weakReference.get();
                if (activity == null) {
                    Log.i(TAG, tag+": activity has been dismissed in the meantime");
                    return null;
                }
                if (! activity.isRunning) {
                    Log.i(TAG, tag+": activity is currently not running");
                    return null;
                }
                return activity;
            }

        };
        networkClient.downloadFile(mapLocalPath, mapRemotePath, handler);
    }

    private void setMapFile() {
        if ( mapView.setMapFile(new File(mapLocalPath)) == FileOpenResult.SUCCESS ) {
            mapView.redraw();
            // mapView.getMapViewPosition().setZoomLevel((byte) 15);
            fitViewToRoute();
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
        if ( routeOverlay != null ) {
            shallFitViewWhenPossible = false;
            fitViewToBoundingBox(routeOverlay.getRouteBoundingBox());
        }
    }

    protected void fitViewToProcession() {
        if ( routeOverlay != null ) {
            fitViewToBoundingBox(routeOverlay.getProcessionBoundingBox());
        }
    }


    protected void centerViewOnCoordinates(GeoPoint center, byte zoomLevel) {
        mapView.getMapViewPosition().setMapPosition(new MapPosition(center, zoomLevel));
    }

    protected void centerViewOnLastKnownLocation() {
        Location location = userPositionOverlay.getLastOwnLocation();
        if ( location != null  ) {
            GeoPoint pos = new GeoPoint(location.getLatitude(), location.getLongitude());
            this.mapView.getMapViewPosition().setCenter(pos);
        }
        else {
            String text = getResources().getString(R.string.msg_current_position_unknown);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
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
}
