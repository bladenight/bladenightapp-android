
package de.greencity.bladenightapp.android.selection;

import java.lang.ref.WeakReference;

import org.joda.time.DateTime;
import org.joda.time.Hours;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import de.greencity.bladenightapp.android.about.AboutActivity;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator.ActionItemType;
import de.greencity.bladenightapp.android.actionbar.ActionEventSelection;
import de.greencity.bladenightapp.android.admin.AdminActivity;
import de.greencity.bladenightapp.android.admin.AdminUtilities;
import de.greencity.bladenightapp.android.background.BackgroundHelper;
import de.greencity.bladenightapp.android.cache.EventsCache;
import de.greencity.bladenightapp.android.global.LocalBroadcast;
import de.greencity.bladenightapp.android.network.GlobalStateService;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.network.GlobalStateService.NetworkServiceBinder;
import de.greencity.bladenightapp.android.utils.DeviceId;
import de.greencity.bladenightapp.android.utils.LocalBroadcastReceiversRegister;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.events.EventList;
import de.greencity.bladenightapp.network.BladenightError;
import de.greencity.bladenightapp.network.messages.EventListMessage;
import de.greencity.bladenightapp.network.messages.HandshakeClientMessage;
import fr.ocroquette.wampoc.messages.CallErrorMessage;

public class SelectionActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_selection);

		eventsCache = new EventsCache(this);
		// avoid NPE, will be replaced as soon as we get data from the network or the cache:
		eventList = new EventList(); 

		openDialog();
		
		new BackgroundHelper(this).scheduleNext();
		// Intent intent = new Intent(this, B)
		Intent intent = new Intent();
		intent.setAction("de.vogella.android.mybroadcast");
		sendBroadcast(intent); 
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Log.i(TAG, "onStart");

		shakeHands();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Log.i(TAG, "onResume");

		bindToGlobalStateService();
		
		broadcastReceiversRegister.registerReceiver(LocalBroadcast.GOT_EVENT_LIST, new EventListBroadcastReceiver());

		configureActionBar();

		getEventsFromCache();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindFromGlobalStateService();
		broadcastReceiversRegister.unregisterReceivers();
	}


	private ServiceConnection globalStateServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			globalStateService = ((NetworkServiceBinder)binder).getService();
			globalStateService.requestEventList();
			Log.i(TAG, "onServiceConnected name="+name);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "onServiceDisconnected name="+name);
		}
	};

	private void unbindFromGlobalStateService() {
		if (globalStateServiceConnection != null) {
            unbindService(globalStateServiceConnection);
            globalStateServiceConnection = null;
        }
	}

	private void bindToGlobalStateService() {
		Intent bindIntent = new Intent(this, GlobalStateService.class);
		bindService(bindIntent, globalStateServiceConnection, Context.BIND_AUTO_CREATE);
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

	class EventListBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "EventListBroadcastReceiver.onReceive");
			eventList = globalStateService.getGlobalState().getEventList();
			updateFragmentsFromEventList();
			saveEventsToCache(eventList);
		}
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
		else if( item.getItemId() == R.id.menu_item_help ){
			FragmentManager fm = getSupportFragmentManager();
			HelpDialog helpDialog = new HelpDialog();
			helpDialog.show(fm, "fragment_help");
			return true;
		}
		return false;
	}

	static class HandshakeErrorHandler extends Handler {
		private WeakReference<SelectionActivity> reference;
		HandshakeErrorHandler(SelectionActivity activity) {
			this.reference = new WeakReference<SelectionActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			final SelectionActivity selectionActivity = reference.get();
			if ( selectionActivity == null || selectionActivity.isFinishing() )
				return;
			CallErrorMessage errorMessage = (CallErrorMessage)msg.obj;
			if ( errorMessage == null ) {
				Log.w(TAG, "Failed to get the error message");
				return;
			}
			if ( BladenightError.OUTDATED_CLIENT.getText().equals(errorMessage.getErrorUri())) {
				Toast.makeText(selectionActivity, R.string.msg_outdated_client , Toast.LENGTH_LONG).show();
			}
			else {
				Log.e(TAG, "Unknown error occured in the handshake with the server: " + errorMessage);
			}
		}
	}

	private void shakeHands() {
		try {
			String deviceId = DeviceId.getDeviceId(this);
			int clientBuild = getDeviceVersionCode();

			String phoneManufacturer = android.os.Build.MANUFACTURER;
			String phoneModel = android.os.Build.MODEL;
			String androidRelease = Build.VERSION.RELEASE;

			HandshakeClientMessage msg = new HandshakeClientMessage(
					deviceId,
					clientBuild,
					phoneManufacturer,
					phoneModel,
					androidRelease);
			new NetworkClient(this).shakeHands(msg, null, new HandshakeErrorHandler(this));
		} catch (Exception e) {
			Log.e(TAG, "shakeHands failed to gather and send information: " + e.toString());
		}
	}

	private int getDeviceVersionCode() {
		PackageManager manager = this.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Failed to get device version code: " + e.toString());
			return 0;
		}
	}


	private void getEventsFromCache() {
		EventListMessage eventListFromCache = eventsCache.read();
		if ( eventListFromCache != null) {
			this.eventList = eventListFromCache.convertToEventsList();
			updateFragmentsFromEventList();
		}
	}

	private void saveEventsToCache(EventList eventList) {
		eventsCache.write(EventListMessage.newFromEventsList(eventList));
	}

	private void updateFragmentsFromEventList() {
		// Log.i(TAG, "updateFragmentsFromEventList eventList=" + eventList);

		eventList.sortByStartDate();

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPagerAdapter = new ViewPagerAdapter(viewPager, getSupportFragmentManager());
		viewPager.setAdapter(viewPagerAdapter);

		CirclePageIndicator circlePageIndicator = (CirclePageIndicator)findViewById(R.id.page_indicator);
		circlePageIndicator.setViewPager(viewPager);
		circlePageIndicator.setColorResolver(new CirclePageIndicator.ColorResolver() {

			@Override
			public int resolve(int index) {
				Event event = eventList.get(index);
				if ( event == null || ! showStatusForEvent(event) )
					return -1;
				switch(event.getStatus() ) {
				case CANCELLED:
					return R.color.light_red;
				case CONFIRMED:
					return R.color.light_green;
				case PENDING:
					return R.color.light_yellow;
				}
				return -1;
			}
		});

		circlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int page) {
				posEventShown = page;
				// Log.i(TAG, "onPageSelected: currentFragmentShown="+posEventShown);
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});


		viewPagerAdapter.setEventList(eventList);
		updatePositionEventCurrent();
		if ( ! tryToRestorePreviouslyShownEvent() ) {
			showUpcomingEvent();
		}
	}

	private boolean tryToRestorePreviouslyShownEvent() {
		int count = getFragmentCount();
		// Log.i(TAG, "restore: currentFragmentShown="+posEventShown);
		// Log.i(TAG, "restore: max="+count);
		if ( posEventShown >= 0 && posEventShown < count ) {
			viewPager.setCurrentItem(posEventShown, false);
			return true;
		}
		return false;
	}

	private void updatePositionEventCurrent() {
		posEventCurrent = -1;
		Event nextEvent = eventList.getNextEvent();
		if ( nextEvent != null ) {
			posEventCurrent = eventList.indexOf(nextEvent);
		}
	}

	private boolean isValidFragmentPosition(int pos) {
		return pos >=0 && pos < getFragmentCount();
	}

	private void showUpcomingEvent() {
		Event nextEvent = eventList.getNextEvent();
		if ( isValidFragmentPosition(posEventCurrent) ) {
			int startFragment = eventList.indexOf(nextEvent);
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
		if ( posEventShown < 0 || posEventShown >= eventList.size() )
			return null;
		return eventList.get(posEventShown);
	}

	private void openDialog(){
		SharedPreferences settings = getSharedPreferences("HelpPrefs", 0);
		// SharedPreferences.Editor editor = settings.edit();
		boolean firstCreate = settings.getBoolean("firstCreate", true);
		if(firstCreate){
			FragmentManager fm = getSupportFragmentManager();
			HelpDialog helpDialog = new HelpDialog();
			helpDialog.show(fm, "fragment_help");
			// for announcements
			// editor.putInt("announcementCounter", 0);
			// editor.putBoolean("firstCreate", false);
			// editor.commit();
		}
		else{		
			// openAnnouncement();
		}

	}

	//	private void openAnnouncement(){
	//		//TODO: check if internet-connection, if not -> skip
	//		//TODO: get the last anouncement from the server instead of the following
	//		String message_e = "On the statistics view you can now see data about all " +
	//				"past blade nights, e.g. the group velocity or the history of your " +
	//				"position in the group. Simply press the statistics button located " +
	//				"on the lower half of the screen on the right.";
	//		String message_d = "Dasselbe auf deutsch. blablablablabalba";
	//		String headline_e = "Statistics";
	//		String headline_d = "Statistiken";
	//		Announcement announcement = new Announcement(Announcement.Type.NEW_FEATURE, 1, message_d, headline_d,
	//				message_e, headline_e);
	//		
	//		
	//		SharedPreferences settings = getSharedPreferences("HelpPrefs", 0);
	//		SharedPreferences.Editor editor = settings.edit();
	//		int announcementCounter = settings.getInt("announcementCounter", -1);
	//		
	//		if (announcementCounter < announcement.getId()){
	//			FragmentManager fm = getSupportFragmentManager();
	//			AnnouncementDialog announcementDialog = new AnnouncementDialog();
	//			announcementDialog.setAnnouncement(announcement);
	//			announcementDialog.show(fm, "fragment_announcement");
	//			editor.putInt("announcementCounter", announcement.getId());
	//			editor.commit();
	//		}
	//	}

	public static class ViewPagerAdapter extends FragmentStatePagerAdapter {
		public ViewPagerAdapter(ViewPager viewPager, FragmentManager fm) {
			super(fm);
		}

		public void setEventList(EventList eventList) {
			this.eventList = eventList;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			//			Log.d(TAG, "getCount");
			return eventList.size();
		}

		@Override
		public int getItemPosition(Object object) {
			Log.d(TAG, "getItemPosition " + object);
			return POSITION_NONE;
		}

		@Override
		public Fragment getItem(int position) {
			Event event = eventList.get(position);
			boolean hasRight = position < getCount()-1;
			boolean hasLeft = position > 0;
			EventFragment fragment = new EventFragment();
			fragment.setArguments(EventFragment.prepareBundle(
					eventList.get(position),
					hasLeft,
					hasRight,
					showStatisticsForEvent(event),
					showStatusForEvent(event),
					eventList.isLive(event)
					));
			return fragment;      
		}

		final private String TAG = "SelectionActivity.MyAdapter"; 

		public EventList eventList = new EventList();
	}

	private static boolean showStatusForEvent(Event event) {
		DateTime now = new DateTime();
		Hours hoursToStart = Hours.hoursBetween(now, event.getStartDate());
		return hoursToStart.getHours() <= 24 || now.isAfter(event.getStartDate());
	}

	private static boolean showStatisticsForEvent(Event event) {
		// no statistics available for now
		// later, this boolean will be provided by the server
		return false;
	}


	private ViewPager viewPager;
	private ViewPagerAdapter viewPagerAdapter;
	private final static String TAG = "SelectionActivity"; 
	private static int posEventShown = -1;
	private static int posEventCurrent = -1;
	private EventList eventList;
	private EventsCache eventsCache;
	private LocalBroadcastReceiversRegister broadcastReceiversRegister = new LocalBroadcastReceiversRegister(this); 
	private GlobalStateService globalStateService;

} 