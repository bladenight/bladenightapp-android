package de.greencity.bladenightapp.android.mainactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionHome;
import de.greencity.bladenightapp.android.app.BladeNightApplication;
import de.greencity.bladenightapp.android.cache.EventsMessageCache;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.utils.AsyncDownloadTaskHttpClient;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.DateFormatter;
import de.greencity.bladenightapp.android.utils.Paths;
import de.greencity.bladenightapp.android.utils.Permissions;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventListMessage;

public class MainActivity extends Activity {

    private static final String LANDING_PAGE_REMOTE_PATH = "landing.html";

    private WebView webView;
    private TextView textViewNext;
    private TextView textViewRouteName;
    private TextView textViewEventDate;
    private TextView textViewEventStatus;
    private ImageView imageViewMap;

    private GlobalStateAccess globalStateAccess;
    private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
    private EventList eventList;
    private EventsMessageCache eventsCache;
    private static final String TAG = "MainActivity";

    private DateFormatter dateFormatter = new DateFormatter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        configureActionBar();

        Permissions.verifyPermissionsForApp(this);

        broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_EVENT_LIST, new EventListBroadcastReceiver());

        globalStateAccess = new GlobalStateAccess(this);

        eventsCache = new EventsMessageCache(this);
        // avoid NPE, will be replaced as soon as we get data from the network or the cache:
        eventList = new EventList();

        webView = (WebView) findViewById(R.id.main_webview);
        textViewNext = (TextView) findViewById(R.id.textview_next_event_label);
        textViewRouteName = (TextView) findViewById(R.id.textview_route_name);
        textViewEventDate = (TextView) findViewById(R.id.textview_event_date);
        textViewEventStatus = (TextView) findViewById(R.id.textview_event_status);
        imageViewMap = (ImageView) findViewById(R.id.button_map);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setLoadsImagesAutomatically(true);
        // webView.getSettings().setDomStorageEnabled(true);

        webView.loadUrl("file://" + getLandingPageLocalPath());

        imageViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastReceiversRegister.unregisterReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        triggerLandingPageDownload();

        getEventsFromCache();
        globalStateAccess.requestEventList();

        configureActionBar();
    }

    private void configureActionBar() {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        new ActionBarConfigurator(actionBar)
                .setAction(ActionBarConfigurator.ActionItemType.HOME, new ActionHome() {
                    @Override
                    public void performAction(View view) {
                        triggerLandingPageDownload();
                        globalStateAccess.requestEventList();
                    }
                })
                .show(ActionBarConfigurator.ActionItemType.FRIENDS)
                // .show(ActionBarConfigurator.ActionItemType.TRACKER_CONTROL)
                .setTitle(R.string.title_main)
                .configure();
    }


    private String getLandingPageRemotePath() {
        return LANDING_PAGE_REMOTE_PATH;
    }

    private String getLandingPageLocalPath() {
        return Paths.getAppDataDirectory(this) + "/" + LANDING_PAGE_REMOTE_PATH;
    }

    private void triggerLandingPageDownload() {
        BladeNightApplication.networkClient.downloadFile(getLandingPageLocalPath(), getLandingPageRemotePath(),
                new AsyncDownloadTaskHttpClient.StatusHandler() {
                    @Override
                    public void onProgress(long current, long total) {
                        // We don't care about progress
                    }

                    @Override
                    public void onDownloadFailure() {
                        // Hopefully a temporary issue, ignore
                    }

                    @Override
                    public void onDownloadSuccess() {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                webView.reload();
                            }
                        });
                    }
                });
    }

    class EventListBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            eventList = globalStateAccess.getEventList();
            updateUiFromEventList();
            saveEventsToCache(eventList);
        }
    }

    private void updateUiFromEventList() {
        eventList.sortByStartDate();
        Event nextEvent = eventList.getNextEvent();
        if ( nextEvent != null ) {
            // Log.i(TAG, "nextEvent=" + nextEvent);
            textViewNext.setText(R.string.text_next_event);
            textViewRouteName.setText(nextEvent.getRouteName());
            textViewEventDate.setText(dateFormatter.format(nextEvent.getStartDate()));
            textViewEventStatus.setText("Status: " + getEventStatusAsText(nextEvent.getStatus()));

            showNextEvent(true);
        }
        else {
            showNextEvent(false);
        }
    }

    private void showNextEvent(boolean status) {
        int visible = ( status ? View.VISIBLE :  View.GONE);
        textViewNext.setVisibility(visible);
        textViewRouteName.setVisibility(visible);
        textViewEventDate.setVisibility(visible);
        textViewEventStatus.setVisibility(visible);
    }

    private void getEventsFromCache() {
        EventListMessage eventListFromCache = eventsCache.read();
        if ( eventListFromCache != null) {
            this.eventList = eventListFromCache.convertToEventsList();
            updateUiFromEventList();
        }
    }

    private void saveEventsToCache(EventList eventList) {
        eventsCache.write(EventListMessage.newFromEventsList(eventList));
    }

    private String getEventStatusAsText(Event.EventStatus status) {
        int id = -1;
        switch (status) {
            case PENDING:
                id = R.string.status_pending;
                break;
            case CONFIRMED:
                id = R.string.status_confirmed;
                break;
            case CANCELLED:
                id = R.string.status_cancelled;
                break;
        }
        return getString(id);
    }

    private void startMapActivity() {
        Intent intent = new Intent(this, BladenightMapActivity.class);
        intent.putExtra(BladenightMapActivity.PARAM_EVENT_MESSAGE, EventGsonHelper.toJson(eventList.getNextEvent()));
        startActivity(intent);
    }
}
