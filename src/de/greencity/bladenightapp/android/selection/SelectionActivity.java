
package de.greencity.bladenightapp.android.selection;

import java.lang.ref.WeakReference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.actionbar.ActionMap;
import de.greencity.bladenightapp.android.admin.AdminActivity;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.statistics.StatisticsActivity;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.messages.EventsListMessage;

public class SelectionActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//				WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

		networkClient =  new NetworkClient(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");

		configureActionBar();

		tryToRestorePreviouslyShownEvent();

		getEventsFromServer();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		Action mapActionWithParameters = new ActionMap() {
			@Override
			public void performAction(View view) {
				Intent intent = new Intent(view.getContext(), BladenightMapActivity.class);
				Event event = getEventShown();
				if ( event == null ) {
					Log.e(TAG, "No event currently shown");
					return;
				}
				intent.putExtra("routeName", event.getRouteName());
				intent.putExtra("isRealTime", posEventCurrent == posEventShown);
				view.getContext().startActivity(intent);
			}
		};
		new ActionBarConfigurator(actionBar)
		.hide(ActionItemType.EVENT_SELECTION)
		.replaceAction(ActionItemType.MAP, mapActionWithParameters)
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
		case R.id.group_top: 
			System.out.println(view.getTag());
			if(view.getTag().equals("old")){
				goStatistics();
			}
			else if(view.getTag().equals("upcoming")){
				goAction();
			}
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

	private void goStatistics(){
		Intent intent = new Intent(SelectionActivity.this, StatisticsActivity.class);
		startActivity(intent);
	}

	private void goAction(){
		Intent intent = new Intent(SelectionActivity.this, BladenightMapActivity.class);
		startActivity(intent);
	}


	static class GetEventsFromServerHandler extends Handler {
		private WeakReference<SelectionActivity> reference;
		GetEventsFromServerHandler(SelectionActivity activity) {
			this.reference = new WeakReference<SelectionActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			EventsListMessage eventsListMessage = (EventsListMessage)msg.obj;
			reference.get().updateFragementsFromEventList((EventsListMessage)eventsListMessage);
		}
	}
	
	private void getEventsFromServer() {
		networkClient.getAllEvents(new GetEventsFromServerHandler(this), null);
	}


	private void updateFragementsFromEventList(EventsListMessage eventsListMessage) {
		mAdapter = new MyAdapter(getSupportFragmentManager(), eventsListMessage);
		viewPager.setAdapter(mAdapter);
		eventsList = eventsListMessage.convertToEventsList();
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

	public static class MyAdapter extends FragmentPagerAdapter {
		@SuppressWarnings("unused")
		final private String TAG = "SelectionActivity.MyAdapter"; 

		public EventsListMessage eventsListMessage;

		public MyAdapter(FragmentManager fm, EventsListMessage eventsListMessage) {
			super(fm);
			this.eventsListMessage = eventsListMessage;
		}

		@Override
		public int getCount() {
			// Log.d(TAG, "getCount");
			return eventsListMessage.size();
		}

		@Override
		public int getItemPosition(Object object) {
			// Log.d(TAG, "getItemPosition");
			return POSITION_NONE;
		}

		@Override
		public Fragment getItem(int position) {
			// Log.d(TAG, "getItem("+position+")");
			boolean hasRight = position < getCount()-1;
			boolean hasLeft = position > 0;
			Fragment fragment = new EventFragment(eventsListMessage.get(position), hasLeft, hasRight);
			return fragment;      
		}
	}

	private MyAdapter mAdapter;
	private final String TAG = "SelectionActivity"; 
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this);
	private static int posEventShown = -1;
	private static int posEventCurrent = -1;
	private EventList eventsList;
	private ViewPager viewPager;
	private NetworkClient networkClient;

} 