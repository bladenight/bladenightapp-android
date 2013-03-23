
package de.greencity.bladenightapp.android.selection;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.google.gson.Gson;
import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.network.Actions;
import de.greencity.bladenightapp.android.network.NetworkService;
import de.greencity.bladenightapp.android.statistics.StatisticsActivity;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventsList;
import de.greencity.bladenightapp.network.messages.EventsListMessage;

public class SelectionActivity extends FragmentActivity {
	private MyAdapter mAdapter;
	private final String TAG = "SelectionActivity"; 
	private ServiceConnection networkServiceConnection;
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this); 


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_selection);

	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");

		broadcastReceiversRegister.registerReceiver(Actions.GOT_ALL_EVENTS, gotAllEventsReceiver);
		broadcastReceiversRegister.registerReceiver(Actions.CONNECTED, connectedReceiver);

		networkServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG+".ServiceConnection", "onServiceConnected");
				sendBroadcast(new Intent(Actions.GET_ALL_EVENTS));
			}
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG+".ServiceConnection", "onServiceDisconnected");
			}

		};

		bindService(new Intent(this, NetworkService.class), networkServiceConnection,  BIND_AUTO_CREATE);
		
		configureActionBar();
	}	

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		new ActionBarConfigurator(actionBar)
		.hide(ActionItemType.EVENT_SELECTION)
		.setTitle(R.string.title_selection)
		.configure();
	}


	@Override
	protected void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();

		broadcastReceiversRegister.unregisterReceivers();
		unbindService(networkServiceConnection);
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

	private void goStatistics(){
		Intent intent = new Intent(SelectionActivity.this, StatisticsActivity.class);
		startActivity(intent);
	}

	private void goAction(){
		Intent intent = new Intent(SelectionActivity.this, BladenightMapActivity.class);
		startActivity(intent);
	}

	private final BroadcastReceiver gotAllEventsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"getAllEventsReceiver.onReceive");
			ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
			if ( viewPager == null ) {
				Log.e(TAG, "viewPager is null");
				return;
			}
			String json = (String) intent.getExtras().get("json");
			if ( json == null ) {
				Log.e(TAG,"Failed to get json");
				return;
			}
			Log.d(TAG, json);
			EventsListMessage eventsListMessage = new Gson().fromJson(json, EventsListMessage.class);
			if ( eventsListMessage == null ) {
				Log.e(TAG,"Failed to parse json");
				return;
			}
			mAdapter = new MyAdapter(getSupportFragmentManager(), eventsListMessage);
			viewPager.setAdapter(mAdapter);

			EventsList eventsList = eventsListMessage.convertToEventsList();
			Event nextEvent = eventsList.getNextEvent();
			if ( nextEvent != null ) {
				int startFragment = eventsList.indexOf(nextEvent);
				viewPager.setCurrentItem(startFragment);
			}
		}
	};

	private final BroadcastReceiver connectedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"connectedReceiver.onReceive");
			sendBroadcast(new Intent(Actions.GET_ALL_EVENTS));
		}
	};

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

} 