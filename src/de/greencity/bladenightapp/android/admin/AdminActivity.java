package de.greencity.bladenightapp.android.admin;


import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.RouteNamesMessage;

public class AdminActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		networkClient = new NetworkClient(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_admin);
	}

	@Override
	public void onStart() {
		super.onStart();

		configureActionBar();

		routeNameSpinner = (Spinner) findViewById(R.id.spinner_active_route);	    
		// Spinner eventStatusSpinner = (Spinner) findViewById(R.id.spinnerStatus);	    
		spinnerRouteNameAdapter = new ArrayAdapter<CharSequence>(AdminActivity.this, android.R.layout.simple_spinner_item);
		spinnerRouteNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);        
		routeNameSpinner.setAdapter(spinnerRouteNameAdapter);
		
		getRouteListFromServer();
	}


	static class GetAllRouteNamesFromServerHandler extends Handler {
		private WeakReference<AdminActivity> reference;
		GetAllRouteNamesFromServerHandler(AdminActivity activity) {
			this.reference = new WeakReference<AdminActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			RouteNamesMessage routeNamesMessage = (RouteNamesMessage)msg.obj;
			reference.get().updateRouteList(routeNamesMessage);
		}
	}

	public void updateRouteList(RouteNamesMessage routeNamesMessage) {
		spinnerRouteNameAdapter.clear();
		for(String name: routeNamesMessage.rna) {
			spinnerRouteNameAdapter.add(name);
		}
		spinnerRouteNameAdapter.notifyDataSetChanged();
		getNextEventFromServer();
	}

	protected void getRouteListFromServer() {
		networkClient.getAllRouteNames(new GetAllRouteNamesFromServerHandler(this), null);
	}

	static class GetActiveEventFromServerHandler extends Handler {
		private WeakReference<AdminActivity> reference;
		GetActiveEventFromServerHandler(AdminActivity activity) {
			this.reference = new WeakReference<AdminActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			EventMessage eventMessage = (EventMessage)msg.obj;
			if ( eventMessage.rou == null)
				Log.e(TAG, "Server sent invalid route name:" + eventMessage.rou);
			else
				reference.get().updateRouteCurrent(eventMessage.rou);
		}
	}


	protected void getNextEventFromServer() {
		networkClient.getActiveEvent(new GetActiveEventFromServerHandler(this), null);
	}


	public void updateRouteCurrent(String currentRouteName) {
		for(int i = 0 ; i < spinnerRouteNameAdapter.getCount() ; i ++) {
			if ( currentRouteName.equals(spinnerRouteNameAdapter.getItem(i)) )
				routeNameSpinner.setSelection(i);
		}
	}


	//	static class GetRealTimeDataFromServerHandler extends Handler {
	//		private WeakReference<AdminActivity> reference;
	//		GetRealTimeDataFromServerHandler(AdminActivity activity) {
	//			this.reference = new WeakReference<AdminActivity>(activity);
	//		}
	//		@Override
	//		public void handleMessage(Message msg) {
	//			RealTimeUpdateData realTimeUpdateData = (RealTimeUpdateData)msg.obj;
	//			reference.get().routeOverlay.update(realTimeUpdateData);
	//			reference.get().processionProgressBar.update(realTimeUpdateData);
	//		}
	//	}
	//
	//	protected void getRealTimeDataFromServer() {
	//		networkClient.getRealTimeData(new GetRealTimeDataFromServerHandler(this), null);
	//	}

	//	static class GetRouteFromServerHandler extends Handler {
	//		private WeakReference<AdminActivity> reference;
	//		GetRouteFromServerHandler(AdminActivity activity) {
	//			this.reference = new WeakReference<AdminActivity>(activity);
	//		}
	//		@Override
	//		public void handleMessage(Message msg) {
	//			reference.get().routeOverlay.update((RouteMessage) msg.obj);
	//			reference.get().fitViewToRoute();
	//		}
	//	}
	//
	//	private void getRouteFromServer(String routeName) {
	//		Log.i(TAG,"getRouteFromServer routeName="+routeName);
	//		networkClient.getRoute(routeName, new GetRouteFromServerHandler(this), null);
	//	}
	//

	@Override
	public void onDestroy() {
		super.onDestroy();
		broadcastReceiversRegister.unregisterReceivers();
	}

	private void configureActionBar() {
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		new ActionBarConfigurator(actionBar)
		.setTitle(R.string.title_admin)
		.configure();
	}


	@Override
	public void onStop() {
		super.onStop();
	}



	final static String TAG = "BladenightMapActivity";
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this); 
	private NetworkClient networkClient;
	private ArrayAdapter<CharSequence> spinnerRouteNameAdapter;
	private Spinner routeNameSpinner;
} 
