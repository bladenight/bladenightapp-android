package de.greencity.bladenightapp.android.admin;


import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.actionbar.ActionBarConfigurator;
import de.greencity.bladenightapp.android.network.NetworkClient;
import de.greencity.bladenightapp.android.utils.BroadcastReceiversRegister;
import de.greencity.bladenightapp.network.messages.EventMessage;
import de.greencity.bladenightapp.network.messages.EventMessage.EventStatus;
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
		
		routeNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String routeName = (String) routeNameSpinner.getSelectedItem();
				setActiveRouteOnServer(routeName);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		getRouteListFromServer();
	}

	protected void getAllInformationFromServer() {
		getNextEventFromServer();
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
			reference.get().updateGuiRouteListFromServerResponse(routeNamesMessage);
		}
	}

	public void updateGuiRouteListFromServerResponse(RouteNamesMessage routeNamesMessage) {
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
				reference.get().updateGuiRouteCurrent(eventMessage.rou);
		}
	}


	protected void getNextEventFromServer() {
		networkClient.getActiveEvent(new GetActiveEventFromServerHandler(this), null);
	}


	public void updateGuiRouteCurrent(String currentRouteName) {
		for(int i = 0 ; i < spinnerRouteNameAdapter.getCount() ; i ++) {
			if ( currentRouteName.equals(spinnerRouteNameAdapter.getItem(i)) )
				routeNameSpinner.setSelection(i);
		}
	}


	static class SetActiveRouteOnServerHandler extends Handler {
		private WeakReference<AdminActivity> reference;
		SetActiveRouteOnServerHandler(AdminActivity activity) {
			this.reference = new WeakReference<AdminActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(this.reference.get(), "Route has been changed", Toast.LENGTH_SHORT).show();
			reference.get().getAllInformationFromServer();
		}
	}


	protected void setActiveRouteOnServer(String routeName) {
		networkClient.setActiveRoute(routeName, new SetActiveStatusOnServerHandler(this), null);
	}

	static class SetActiveStatusOnServerHandler extends Handler {
		private WeakReference<AdminActivity> reference;
		SetActiveStatusOnServerHandler(AdminActivity activity) {
			this.reference = new WeakReference<AdminActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			reference.get().getAllInformationFromServer();
		}
	}


	protected void setActiveStatusOnServer() {
		networkClient.setActiveStatus(EventStatus.CAN, new SetActiveStatusOnServerHandler(this), null);
	}


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



	final static String TAG = "BladenightAdminActivity";
	private BroadcastReceiversRegister broadcastReceiversRegister = new BroadcastReceiversRegister(this); 
	private NetworkClient networkClient;
	private ArrayAdapter<CharSequence> spinnerRouteNameAdapter;
	private Spinner routeNameSpinner;
} 
