package de.greencity.bladenightapp.android.network;

import java.io.IOException;
import java.net.URI;

import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;
import fr.ocroquette.wampoc.client.WelcomeListener;

public class BladenightWampClient {
	BladenightWampClient() {
		adapter = new NetworkClient.WebSocketClientChannelAdapter();
		final WampClient wampClient = new WampClient(adapter);
		this.wampClient = wampClient;

		listener = new WebSocketClient.Listener() {
			@Override
			public void onConnect() {
				Log.d(TAG, "Connected!");
				isConnected = true;
			}

			@Override
			public void onMessage(String message) {
				Log.d(TAG, String.format("Got string message! %s", message));
				wampClient.handleIncomingMessage(message);
			}

			@Override
			public void onMessage(byte[] data) {
				Log.d(TAG, String.format("Got binary message!"));
			}

			@Override
			public void onDisconnect(int code, String reason) {
				Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
				isConnected = false;
				wampClient.reset();
			}

			@Override
			public void onError(Exception error) {
				Log.e(TAG, "Error!", error);
			}
		};

	}
	
	public void connect(URI serverUri) {
		webSocketClient = new WebSocketClient(serverUri, listener, null);
		adapter.setClient(webSocketClient);
		wampClient.reset();
		webSocketClient.connect();
	}

	public boolean isConnectionUsable() {
		return isConnected && wampClient != null && wampClient.hasBeenWelcomed();
	}
	
	public void setWelcomeListener(WelcomeListener welcomeListener) {
		wampClient.setWelcomeListener(welcomeListener);
	}

	public void call(String procedureId, RpcResultReceiver rpcResultHandler) throws IOException {
		wampClient.call(procedureId, rpcResultHandler);
	}

	public void call(String procedureId, RpcResultReceiver rpcResultHandler, Object payload) throws IOException {
		wampClient.call(procedureId, rpcResultHandler, payload);
	}
	
	private WampClient wampClient;
	private WebSocketClient webSocketClient;
	private boolean isConnected;
	private NetworkClient.WebSocketClientChannelAdapter adapter;

	WebSocketClient.Listener listener;

	final static private String TAG = "BladenightWampClient"; 
}
