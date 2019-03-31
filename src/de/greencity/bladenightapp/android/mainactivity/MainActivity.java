package de.greencity.bladenightapp.android.mainactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.about.AboutActivity;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.actions.ActionHome;
import de.greencity.bladenightapp.android.actionbar.actions.ActionMore;
import de.greencity.bladenightapp.android.admin.AdminActivity;
import de.greencity.bladenightapp.android.admin.AdminUtilities;
import de.greencity.bladenightapp.android.cache.EventsMessageCache;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.DateFormatter;
import de.greencity.bladenightapp.android.utils.Paths;
import de.greencity.bladenightapp.android.utils.Permissions;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventListMessage;

public class MainActivity extends FragmentActivity {

    private static final String LANDING_PAGE_REMOTE_PATH = "landing.html";

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

        textViewNext = (TextView) findViewById(R.id.textview_next_event_label);
        textViewRouteName = (TextView) findViewById(R.id.textview_route_name);
        textViewEventDate = (TextView) findViewById(R.id.textview_event_date);
        textViewEventStatus = (TextView) findViewById(R.id.textview_event_status);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastReceiversRegister.unregisterReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                        globalStateAccess.requestEventList();
                    }
                })
                .show(ActionBarConfigurator.ActionItemType.FRIENDS)
                .show(ActionBarConfigurator.ActionItemType.TRACKER_CONTROL)
                .show(ActionBarConfigurator.ActionItemType.TABLE)
                .show(ActionBarConfigurator.ActionItemType.MORE)
                .show(ActionBarConfigurator.ActionItemType.MAP)
                .hide(ActionBarConfigurator.ActionItemType.HOME)
                .setAction(ActionBarConfigurator.ActionItemType.MORE, new ActionMore() {
                    @Override
                    public void performAction(View view) {
                        PopupMenu popup = new PopupMenu(MainActivity.this, actionBar);
                        popup.getMenuInflater().inflate(R.menu.menu_popup_more, popup.getMenu());
                        if (AdminUtilities.getAdminPassword(MainActivity.this) == null) {
                            // Show the admin menu entry only if an admin password was entered
                            popup.getMenu().findItem(R.id.menu_item_admin).setVisible(false);
                        }
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.menu_item_admin:
                                        startActivity(new Intent(MainActivity.this, AdminActivity.class));
                                        return true;
                                    case R.id.menu_item_about:
                                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                                        return true;
                                    case R.id.menu_item_help:
                                        DialogFragment dialog = new HelpDialog();
                                        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
                                        return false;
                                    default:
                                        return false;
                                }
                            }
                        });
                        popup.show();
                    }
                })
                .setTitle(R.string.title_main)
                .configure();

    }


    private String getLandingPageRemotePath() {
        return LANDING_PAGE_REMOTE_PATH;
    }

    private String getLandingPageLocalPath() {
        return Paths.getAppDataDirectory(this) + "/" + LANDING_PAGE_REMOTE_PATH;
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
        if (nextEvent != null) {
            // Log.i(TAG, "nextEvent=" + nextEvent);
            textViewNext.setText(R.string.text_next_event);
            textViewRouteName.setText(nextEvent.getRouteName());
            textViewEventDate.setText(dateFormatter.format(nextEvent.getStartDate()));
            textViewEventStatus.setText("Status: " + getEventStatusAsText(nextEvent.getStatus()));

            showNextEvent(true);
        } else {
            showNextEvent(false);
        }
    }

    private void showNextEvent(boolean status) {
        int visible = (status ? View.VISIBLE : View.GONE);
        textViewNext.setVisibility(visible);
        textViewRouteName.setVisibility(visible);
        textViewEventDate.setVisibility(visible);
        textViewEventStatus.setVisibility(visible);
    }

    private void getEventsFromCache() {
        EventListMessage eventListFromCache = eventsCache.read();
        if (eventListFromCache != null) {
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

    static public class HelpDialog extends DialogFragment {
        public HelpDialog() {
            // Empty constructor required for DialogFragment
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.help_dialog, container);
            getDialog().setTitle(getResources().getString(R.string.title_help));
            setButtonListeners(rootView);
            TextView text = (TextView) rootView.findViewById(R.id.help_text);
            text.setMovementMethod(new ScrollingMovementMethod());

            return rootView;
        }

        private void setButtonListeners(View view) {
            Button confirmButton = (Button) view.findViewById(R.id.help_confirm);
            confirmButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View view) {
                    dismiss();
                }
            });
        }

        private View rootView;
    }
}
