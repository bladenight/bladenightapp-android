
package de.greencity.bladenightapp.android.selection;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.map.BladenightMapActivity;
import de.greencity.bladenightapp.android.network.Actions;
import de.greencity.bladenightapp.android.network.NetworkService;
import de.greencity.bladenightapp.android.options.OptionsActivity;
import de.greencity.bladenightapp.android.social.SocialActivity;
import de.greencity.bladenightapp.android.statistics.StatisticsActivity;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.network.messages.EventsListMessage;

public class SelectionActivity extends FragmentActivity {
	private MyAdapter mAdapter;
	private ViewPager mPager;
	private final String TAG = "SelectionActivity"; 
	private ServiceConnection serviceConnection;
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this); 


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

		// mAdapter = new MyAdapter(getSupportFragmentManager(), );
		mPager = (ViewPager) findViewById(R.id.pager);
		// mPager.setAdapter(mAdapter);
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "onStart");
		super.onStart();

		broadcastReceiversRegister.registerReceiver(Actions.GOT_ALL_EVENTS, gotAllEventsReceiver);
		broadcastReceiversRegister.registerReceiver(Actions.CONNECTED, connectedReceiver);

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
		bindService(new Intent(this, NetworkService.class), serviceConnection,  BIND_AUTO_CREATE);
	}	

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();

		broadcastReceiversRegister.unregisterReceivers();
		unbindService(serviceConnection);
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
		Intent intent = new Intent(SelectionActivity.this, BladenightMapActivity.class);
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


	private final BroadcastReceiver gotAllEventsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG,"getAllEventsReceiver.onReceive");
			Log.d(TAG,"getAllEventsReceiver.onReceive " + intent);
			Log.d(TAG,"getAllEventsReceiver.onReceive " + intent.getExtras());
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
			mPager.setAdapter(mAdapter);
			// mAdapter.notifyDataSetChanged();
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