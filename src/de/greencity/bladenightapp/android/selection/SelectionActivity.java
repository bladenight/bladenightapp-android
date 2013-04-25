
package de.greencity.bladenightapp.android.selection;

import java.lang.ref.WeakReference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.android.about.AboutActivity;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.actionbar.ActionEventSelection;
import de.greencity.bladenightapp.android.admin.AdminActivity;
import de.greencity.bladenightapp.android.admin.AdminUtilities;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.JsonCacheAccess;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventsListMessage;

public class SelectionActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_selection);

		eventsCache = new JsonCacheAccess<EventsListMessage>(this, EventsListMessage.class, JsonCacheAccess.FILE_EVENTS);

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPagerAdapter = new ViewPagerAdapter(viewPager, getSupportFragmentManager());
		viewPager.setAdapter(viewPagerAdapter);

		CirclePageIndicator titleIndicator = (CirclePageIndicator)findViewById(R.id.page_indicator);
		titleIndicator.setViewPager(viewPager);

		titleIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int page) {
				posEventShown = page;
				Log.i(TAG, "onPageSelected: currentFragmentShown="+posEventShown);
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		networkClient =  new NetworkClient(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");

	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		Action actionGoToCurrentEvent = new ActionEventSelection() {
			@Override
			public void performAction(View view) {
				showUpcomingEvent();
			}
		};
		new ActionBarConfigurator(actionBar)
		.setAction(ActionItemType.HOME, actionGoToCurrentEvent)
		.show(ActionItemType.FRIENDS)
		.show(ActionItemType.TRACKER_CONTROL)
		.setTitle(R.string.title_selection)
		.configure();

	}

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();

		broadcastReceiversRegister.unregisterReceivers();
		// unbindService(networkServiceConnection);
	}

	@Override
	protected void onResume() {
		super.onResume();

		configureActionBar();

		getEventsFromCache();
		getEventsFromServer();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	// Will be called via the onClick attribute
	// of the buttons in main.xml
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.arrow_left:
			viewPager.setCurrentItem(viewPager.getCurrentItem()-1, true);
			break;
		case R.id.arrow_right:
			viewPager.setCurrentItem(viewPager.getCurrentItem()+1, true);
			break;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_selection, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if ( AdminUtilities.getAdminPassword(this) == null )
			menu.findItem(R.id.menu_item_admin).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if( item.getItemId() == R.id.menu_item_admin ){
			Intent intent = new Intent(this, AdminActivity.class);
			startActivity(intent);
			return true;
		}
		else if( item.getItemId() == R.id.menu_item_about ){
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	static class GetEventsFromServerHandler extends Handler {
		private WeakReference<SelectionActivity> reference;
		GetEventsFromServerHandler(SelectionActivity activity) {
			this.reference = new WeakReference<SelectionActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			EventsListMessage eventsListMessage = (EventsListMessage)msg.obj;
			Log.i(TAG, "Updating event fragments from server data");
			reference.get().updateFragmentsFromEventList((EventsListMessage)eventsListMessage);
			reference.get().saveEventsToCache(eventsListMessage);
		}
	}

	private void getEventsFromCache() {
		EventsListMessage eventsListMessage = eventsCache.get();
		if ( eventsListMessage != null) {
			Log.i(TAG, "Updating event fragments from cached data");
			updateFragmentsFromEventList(eventsListMessage);
		}
	}

	private void saveEventsToCache(EventsListMessage eventsListMessage) {
		eventsCache.set(eventsListMessage);
	}

	private void getEventsFromServer() {
		networkClient.getAllEvents(new GetEventsFromServerHandler(this), null);
	}


	private void updateFragmentsFromEventList(EventsListMessage eventListMessage) {
		Log.i(TAG, "updateFragmentsFromEventList " + eventListMessage);

		eventsList = eventListMessage.convertToEventsList();
		eventsList.sortByStartDate();

		viewPager.setAdapter(null) ;
		viewPagerAdapter.setEventListMessage(eventListMessage);
		viewPager.setAdapter(viewPagerAdapter) ;
		updatePositionEventCurrent();
		if ( ! tryToRestorePreviouslyShownEvent() ) {
			showUpcomingEvent();
		}
	}

	private boolean tryToRestorePreviouslyShownEvent() {
		int count = getFragmentCount();
		Log.i(TAG, "restore: currentFragmentShown="+posEventShown);
		Log.i(TAG, "restore: max="+count);
		if ( posEventShown >= 0 && posEventShown < count ) {
			viewPager.setCurrentItem(posEventShown, false);
			return true;
		}
		return false;
	}

	private void updatePositionEventCurrent() {
		posEventCurrent = -1;
		Event nextEvent = eventsList.getActiveEvent();
		if ( nextEvent != null ) {
			posEventCurrent = eventsList.indexOf(nextEvent);
		}
	}

	private boolean isValidFragmentPosition(int pos) {
		return pos >=0 && pos < getFragmentCount();
	}

	private void showUpcomingEvent() {
		Event nextEvent = eventsList.getActiveEvent();
		if ( isValidFragmentPosition(posEventCurrent) ) {
			int startFragment = eventsList.indexOf(nextEvent);
			viewPager.setCurrentItem(startFragment);
		}
	}

	public void showNextEvent() {
		viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
	}

	public void showPreviousEvent() {
		viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
	}


	private int getFragmentCount() {
		if ( viewPager == null || viewPager.getAdapter() == null )
			return 0;
		return viewPager.getAdapter().getCount();
	}

	protected Event getEventShown() {
		if ( posEventShown < 0 || posEventShown >= eventsList.size() )
			return null;
		return eventsList.get(posEventShown);
	}

	public static class ViewPagerAdapter extends FragmentStatePagerAdapter {
		public ViewPagerAdapter(ViewPager viewPager, FragmentManager fm) {
			super(fm);
		}

		public void setEventListMessage(EventsListMessage eventListMessage) {
			this.eventListMessage = eventListMessage;
		}

		@Override
		public int getCount() {
			//			Log.d(TAG, "getCount");
			return eventListMessage.size();
		}

		@Override
		public int getItemPosition(Object object) {
			//						Log.d(TAG, "getItemPosition " + object);
			return POSITION_NONE;
		}

		@Override
		public Fragment getItem(int position) {
//			Log.d(TAG, "getItem("+position+")");
//			Log.d(TAG, eventListMessage.get(position).toString());
			boolean hasRight = position < getCount()-1;
			boolean hasLeft = position > 0;
			EventFragment fragment = new EventFragment();
			fragment.setArguments(EventFragment.prepareBundle(eventListMessage.get(position), hasLeft, hasRight, posEventCurrent == position));
			return fragment;      
		}

		@SuppressWarnings("unused")
		final private String TAG = "SelectionActivity.MyAdapter"; 

		public EventsListMessage eventListMessage = new EventsListMessage();
	}

	private ViewPagerAdapter viewPagerAdapter;
	private final static String TAG = "SelectionActivity"; 
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
	private static int posEventShown = -1;
	private static int posEventCurrent = -1;
	private EventList eventsList;
	private ViewPager viewPager;
	private NetworkClient networkClient;
	private JsonCacheAccess<EventsListMessage> eventsCache;
} 