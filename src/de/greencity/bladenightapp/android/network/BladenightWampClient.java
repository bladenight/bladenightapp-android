package de.greencity.bladenightapp.android.network;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.exception.ExceptionUtils;

import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;
import fr.ocroquette.wampoc.client.WelcomeListener;

public class BladenightWampClient {
	enum State {
		DISCONNECTED,
		CONNECTING,
		SHAKING_HANDS,
		USUABLE
	};
	BladenightWampClient() {
		adapter = new NetworkClient.WebSocketClientChannelAdapter();
		final WampClient wampClient = new WampClient(adapter);
		this.wampClient = wampClient;

		listener = new WebSocketClient.Listener() {
			@Override
			public void onConnect() {
				Log.d(TAG, "Connected!");
				state = State.SHAKING_HANDS;
			}

			@Override
			public void onMessage(String message) {
				Log.d(TAG, String.format("Got string message! %s", message));
				wampClient.handleIncomingMessage(message);
				if ( wampClient.hasBeenWelcomed())
					state = State.USUABLE;
			}

			@Override
			public void onMessage(byte[] data) {
				Log.d(TAG, String.format("Got binary message!"));
			}

			@Override
			public void onDisconnect(int code, String reason) {
				Log.e(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
				wampClient.reset();
				state = State.DISCONNECTED;
			}

			@Override
			public void onError(Exception error) {
				Log.e(TAG, "Error:" + error);
				Log.i(TAG, "Trace: " + ExceptionUtils.getStackTrace( new Throwable()));
				webSocketClient.disconnect();
				wampClient.reset();
				state = State.DISCONNECTED;
			}
		};

	}
	
	public void connect(URI serverUri) {
		state = State.CONNECTING;
		webSocketClient = new WebSocketClient(serverUri, listener, null);
		adapter.setClient(webSocketClient);
		wampClient.reset();
		webSocketClient.connect();
	}

	public State getState() {
		return state;
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
	private NetworkClient.WebSocketClientChannelAdapter adapter;
	private State state = State.DISCONNECTED;

	WebSocketClient.Listener listener;

	final static private String TAG = "BladenightWampClient"; 
}
