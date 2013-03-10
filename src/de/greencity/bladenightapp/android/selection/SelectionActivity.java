
package de.greencity.bladenightapp.android.selection;

import java.util.LinkedList;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.TextView;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.action.ActionActivity;
import de.greencity.bladenightapp.android.network.Actions;
import de.greencity.bladenightapp.android.network.NewNetworkService;
import de.greencity.bladenightapp.android.options.OptionsActivity;
import de.greencity.bladenightapp.android.social.SocialActivity;
import de.greencity.bladenightapp.android.statistics.StatisticsActivity;

public class SelectionActivity extends FragmentActivity {
	private EventsDataSource datasource;
	private MyAdapter mAdapter;
	private ViewPager mPager;
	final private String TAG = "SelectionActivity"; 
	private ServiceConnection serviceConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_selection);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		ImageView titlebar = (ImageView)findViewById(R.id.icon);
		titlebar.setImageResource(R.drawable.ic_calendar);
		TextView titletext = (TextView)findViewById(R.id.title);
		titletext.setText(R.string.title_selection);

		datasource = new EventsDataSource(this);
		datasource.open();

		LinkedList<Event> allEvents = datasource.getAllEvents();

		//workaround, should be refreshed with data on server
		if(allEvents.size()==0){
			datasource.createEvent("Nord - lang", "15.06.2012", "confirmed", "17.6 km");
			datasource.createEvent("Ost - kurz", "22.06.2012", "cancelled", "11.3 km");
			datasource.createEvent("West - kurz", "11.06.2013", "confirmed", "12.4 km");
			datasource.createEvent("West - lang", "18.06.2013", "pending", "17.4 km");
			datasource.createEvent("West - kurz", "27.06.2013", "pending", "12.4 km");
			allEvents = datasource.getAllEvents();
		}

		allEvents = new LinkedList<Event>();

		mAdapter = new MyAdapter(getSupportFragmentManager(),allEvents);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

	}

	@Override
	protected void onStart() {
		Log.i(TAG, "onStart");
		super.onStart();

		registerReceivers();

		serviceConnection = new ServiceConnection() {
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
		bindService(new Intent(this, NewNetworkService.class), serviceConnection,  BIND_AUTO_CREATE);
	}	

	private void registerReceivers() {
		{
			Log.i(TAG, "Registering " + Actions.GOT_ALL_EVENTS);
			IntentFilter filter = new IntentFilter();
			filter.addAction(Actions.GOT_ALL_EVENTS);
			registerReceiver(gotAllEventsReceiver, filter);
		}
		{
			Log.i(TAG, "Registering " + Actions.CONNECTED);
			IntentFilter filter = new IntentFilter();
			filter.addAction(Actions.CONNECTED);
			registerReceiver(connectedReceiver, filter);
		}
	}

	
	@Override
	protected void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();

		unregisterReceivers();
		unbindService(serviceConnection);
	}

	private void unregisterReceivers() {
		unregisterReceiver(gotAllEventsReceiver);
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
		case R.id.options: goOptions();
		break;
		case R.id.social: goSocial();
		break;
		}
	}

	private void goStatistics(){
		Intent intent = new Intent(SelectionActivity.this, StatisticsActivity.class);
		startActivity(intent);
	}

	private void goAction(){
		Intent intent = new Intent(SelectionActivity.this, ActionActivity.class);
		startActivity(intent);
	}

	private void goSocial(){
		Intent intent = new Intent(SelectionActivity.this, SocialActivity.class);
		startActivity(intent);
	}

	private void goOptions(){
		Intent intent = new Intent(SelectionActivity.this, OptionsActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		datasource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		datasource.close();
		super.onPause();
	}

	private final BroadcastReceiver gotAllEventsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"getAllEventsReceiver.onReceive");
			String json = (String) intent.getExtras().get("json");
			if ( json == null ) {
				Log.e(TAG,"Failed to get json");
				return;
			}
			Log.d(TAG, json);
		}
	};

	private final BroadcastReceiver connectedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"getAllEventsReceiver.onReceive");
			sendBroadcast(new Intent(Actions.GET_ALL_EVENTS));
		}
	};

	public static class MyAdapter extends FragmentPagerAdapter {
		final private String TAG = "SelectionActivity.MyAdapter"; 

		public LinkedList<Event> allEvents;

		public MyAdapter(FragmentManager fm, LinkedList<Event> allEvents) {
			super(fm);
			this.allEvents = allEvents;
		}

		@Override
		public int getCount() {
			// Log.d(TAG, "getCount");
			return allEvents.size();
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
			Fragment fragment = new EventFragment(allEvents.get(position), hasLeft, hasRight);
			return fragment;      
		}

	}
} 