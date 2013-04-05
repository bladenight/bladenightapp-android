
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

import com.google.gson.Gson;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.actionbar.ActionEventSelection;
import de.greencity.bladenightapp.android.admin.AdminActivity;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.social.AddFriendDialog;
import de.greencity.bladenightapp.android.social.SocialActivity;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.android.utils.InternalStorageFile;
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

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
		viewPagerAdapter = new ViewPagerAdapter(viewPager, getSupportFragmentManager());
		viewPager.setAdapter(viewPagerAdapter);

		networkClient =  new NetworkClient(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");

		configureActionBar();

		tryToRestorePreviouslyShownEvent();

		getEventsFromCache();
		getEventsFromServer();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		Action actionGoToCurrentEvent = new ActionEventSelection() {
			@Override
			public void performAction(View view) {
				showNextEvent();
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if( item.getItemId() == R.id.menu_item_admin ){
			Intent intent = new Intent(this, AdminActivity.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

//	private void goStatistics(){
//		Intent intent = new Intent(SelectionActivity.this, StatisticsActivity.class);
//		startActivity(intent);
//	}
//
//	private void goAction(){
//		Intent intent = new Intent(SelectionActivity.this, BladenightMapActivity.class);
//		startActivity(intent);
//	}
//

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
		String data = eventListCacheFile.read();
		if (data == null)
			return;
		EventsListMessage eventsListMessage = null;
		try {
			eventsListMessage = gson.fromJson(data, EventsListMessage.class);
		}
		catch(Exception e) {
		}
		if ( eventsListMessage != null) {
			Log.i(TAG, "Updating event fragments from cached data");
			updateFragmentsFromEventList(eventsListMessage);
		}
		else {
			Log.e(TAG, "getEventsFromCache: failed to parse: " + data);
		}
	}

	private void saveEventsToCache(EventsListMessage eventsListMessage) {
		eventListCacheFile.write(gson.toJson(eventsListMessage));
	}

	private void getEventsFromServer() {
		networkClient.getAllEvents(new GetEventsFromServerHandler(this), null);
	}


	private void updateFragmentsFromEventList(EventsListMessage eventListMessage) {
		Log.i(TAG, "updateFragementsFromEventList " + eventListMessage);
		viewPager.setAdapter(null) ;
		viewPagerAdapter.setEventListMessage(eventListMessage);
		viewPager.setAdapter(viewPagerAdapter) ;
		eventsList = eventListMessage.convertToEventsList();
		updatePositionEventCurrent();
		if ( ! tryToRestorePreviouslyShownEvent() ) {
			showNextEvent();
		}
	}

	private boolean tryToRestorePreviouslyShownEvent() {
		int count = getFragmentCount();
		Log.i(TAG, "restore: currentFragmentShown="+posEventShown);
		Log.i(TAG, "restore: max="+count);
		if ( posEventShown >= 0 && posEventShown < count ) {
			viewPager.setCurrentItem(posEventShown);
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

	private void showNextEvent() {
		Event nextEvent = eventsList.getActiveEvent();
		if ( isValidFragmentPosition(posEventCurrent) ) {
			int startFragment = eventsList.indexOf(nextEvent);
			viewPager.setCurrentItem(startFragment);
		}
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
			this.viewPager = viewPager;
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
			//			Log.d(TAG, "getItemPosition " + object);
			return POSITION_NONE;
		}

		@Override
		public Fragment getItem(int position) {
			//			Log.d(TAG, "getItem("+position+")");
			//			Log.d(TAG, eventListMessage.get(position).toString());
			boolean hasRight = position < getCount()-1;
			boolean hasLeft = position > 0;
			EventFragment fragment = new EventFragment();
			fragment.setParameters(viewPager, eventListMessage.get(position), hasLeft, hasRight);
			fragment.setViewPager(viewPager);
			fragment.setEventMessage(eventListMessage.get(position));
			fragment.hasLeft(hasLeft);
			fragment.hasRight(hasRight);
			fragment.isCurrent(posEventCurrent == position);
			fragment.hasStatistics(position < posEventCurrent);
			return fragment;      
		}

		@SuppressWarnings("unused")
		final private String TAG = "SelectionActivity.MyAdapter"; 

		public EventsListMessage eventListMessage = new EventsListMessage();
		private ViewPager viewPager;

	}

	private ViewPagerAdapter viewPagerAdapter;
	private final static String TAG = "SelectionActivity"; 
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
	private static int posEventShown = -1;
	private static int posEventCurrent = -1;
	private EventList eventsList;
	private ViewPager viewPager;
	private NetworkClient networkClient;
	private InternalStorageFile eventListCacheFile = new InternalStorageFile(this, "event-list.json");
	private Gson gson = new Gson();

} 