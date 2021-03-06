package de.greencity.bladenightapp.android.tableactivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.cache.EventsMessageCache;
import de.greencity.bladenightapp.android.global.GlobalStateAccess;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.DateFormatter;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventGsonHelper;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventListMessage;

public class TableActivity extends Activity {

    private EventList eventList;
    private EventsMessageCache eventsCache;
    private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
    private GlobalStateAccess globalStateAccess;
    private final static String TAG = "TableActivity";
    private TableLayout tableLayout;
    private DateFormatter dateFormatter = new DateFormatter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        tableLayout = (TableLayout) findViewById(R.id.table_layout);
        globalStateAccess = new GlobalStateAccess(this);
        eventsCache = new EventsMessageCache(this);
        // avoid NPE, will be replaced as soon as we get data from the network or the cache:
        eventList = new EventList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        globalStateAccess.requestEventList();

        broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_EVENT_LIST, new EventListBroadcastReceiver());

        configureActionBar();

        getEventsFromCache();
    }

    private void configureActionBar() {
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        new ActionBarConfigurator(actionBar)
                .show(ActionBarConfigurator.ActionItemType.FRIENDS)
                .setTitle(R.string.title_main)
                .configure();
    }

    private void getEventsFromCache() {
        EventListMessage eventListFromCache = eventsCache.read();
        if (eventListFromCache != null) {
            this.eventList = eventListFromCache.convertToEventsList();
            updateUiFromEventList();
        }
    }

    private void updateUiFromEventList() {
        eventList.sortByStartDate();
        while (tableLayout.getChildCount() > 1) {
            tableLayout.removeViewAt(1);
        }

        int cellHorizontalMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                15,
                getResources().getDisplayMetrics()
        );
        int cellVerticalMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5,
                getResources().getDisplayMetrics()
        );
        TableRow.LayoutParams tvParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tvParams.setMargins(cellHorizontalMargin, cellVerticalMargin, cellHorizontalMargin, cellVerticalMargin);

        int imageViewSizeDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20,
                getResources().getDisplayMetrics()
        );
        TableRow.LayoutParams ivParams = new TableRow.LayoutParams(imageViewSizeDp, imageViewSizeDp);
        ivParams.setMargins(cellHorizontalMargin, cellVerticalMargin, cellHorizontalMargin, cellVerticalMargin);

        tableLayout.getChildAt(0).setLayoutParams(tvParams);

        for (final Event event : eventList) {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TableActivity.this, BladenightMapActivity.class);
                    intent.putExtra(BladenightMapActivity.PARAM_EVENT_MESSAGE, EventGsonHelper.toJson(event));
                    startActivity(intent);
                }
            });
            tableLayout.addView(tr);

            TextView tv1 = new TextView(this);
            tv1.setText(dateFormatter.format(event.getStartDate()));
            tv1.setLayoutParams(tvParams);
            tr.addView(tv1);

            TextView tv2 = new TextView(this);
            tv2.setText(event.getRouteName());
            tv2.setLayoutParams(tvParams);
            tr.addView(tv2);

            if (event == eventList.getNextEvent()) {
                tr.setBackgroundResource(R.drawable.cell_shape);
                tv1.setTypeface(null, Typeface.BOLD);
                tv2.setTypeface(null, Typeface.BOLD);
            }

            ImageView iv = new ImageView(this);
            // This badly calls for an Optional, but we don't have them in the current API level
            boolean hasIcon = false;
            int icon = 0;
            switch (event.getStatus()) {
                case CONFIRMED:
                    hasIcon = true;
                    icon = R.drawable.icon_ok;
                    break;
                case CANCELLED:
                    hasIcon = true;
                    icon = R.drawable.icon_no;
                    break;
            }
            if (hasIcon) {
                iv.setImageResource(icon);
            }
            iv.setLayoutParams(ivParams);
            tr.addView(iv);

            TextView tv3 = new TextView(this);
            if (event.getParticipants() > 0)
                tv3.setText(Integer.toString(event.getParticipants()));
            tv3.setLayoutParams(tvParams);
            tr.addView(tv3);
        }
    }

    private void saveEventsToCache(EventList eventList) {
        eventsCache.write(EventListMessage.newFromEventsList(eventList));
    }

    class EventListBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "EventListBroadcastReceiver.onReceive");
            eventList = globalStateAccess.getEventList();
            updateUiFromEventList();
            saveEventsToCache(eventList);
        }
    }

}
