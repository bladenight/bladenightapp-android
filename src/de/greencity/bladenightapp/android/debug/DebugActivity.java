package de.greencity.bladenightapp.android.debug;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.android.network.Actions;
import de.greencity.bladenightapp.android.network.NetworkService;
import de.greencity.bladenightapp.android.network.NetworkServiceClient;

public class DebugActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);

		networkServiceClient.bindToService();

		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnFindServer = (Button) findViewById(R.id.btnFindServer);
		btnGetActiveEvent = (Button) findViewById(R.id.btnGetNextEvent);
		tvServer = (TextView) findViewById(R.id.tvServer);
		tvResult = (TextView) findViewById(R.id.tvResult);

		btnFindServer.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				networkServiceClient.findServer();
			}
		});

		btnGetActiveEvent.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				networkServiceClient.getActiveEvent();
			}
		});

		registerBroadcastReceivers();

		updateStateDisconnected();
	}


	@Override
	protected void onStart() {
		super.onStart();
		networkServiceClient.bindToService();
	}	

	@Override
	protected void onStop() {
		super.onStop();
		networkServiceClient.unbindFromService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_debug, menu);
		return true;
	}


	void updateStateDisconnected() {
		btnConnect.setText("Connect");
		btnConnect.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				networkServiceClient.connectToServer();
			}
		});
	}

	void updateStateConnected() {
		btnConnect.setText("Disconnect");
		btnConnect.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				networkServiceClient.disconnectFromServer();
			}
		});
	}

	private void registerBroadcastReceivers() {
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				tvServer.setText(intent.getStringExtra("uri"));
			} }, new IntentFilter(Actions.CONNECTED));

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				tvServer.setText("disconnected !");
			} }, new IntentFilter(Actions.DISCONNECTED));

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				tvResult.setText(intent.getStringExtra("json"));
			} }, new IntentFilter(Actions.GOT_ACTIVE_EVENT));
	}

	private final String TAG = "DebugActivity";
	Button btnConnect;
	Button btnFindServer;
	Button btnGetActiveEvent;
	TextView tvServer;
	TextView tvResult;
	boolean isBound;
	final NetworkServiceClient networkServiceClient = new NetworkServiceClient(this);

}