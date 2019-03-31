package de.greencity.bladenightapp.android.network;

import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.URI;

import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;
import fr.ocroquette.wampoc.client.WelcomeListener;
import fr.ocroquette.wampoc.common.Channel;

public class BladenightWampClient {
    enum State {
        DISCONNECTED,
        CONNECTING,
        SHAKING_HANDS,
        USEABLE
    };
    BladenightWampClient() {
        adapter = new WebSocketClientChannelAdapter();
        final WampClient wampClient = new WampClient(adapter);
        this.wampClient = wampClient;

        listener = new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
                Log.i(TAG, "Connected!");
                state = State.SHAKING_HANDS;
            }

            @Override
            public void onMessage(String message) {
                // Log.d(TAG, String.format("Got string message! %s", message));
                wampClient.handleIncomingMessage(message);
                if ( wampClient.hasBeenWelcomed())
                    state = State.USEABLE;
                updateLastServerLifeSign();
            }

            @Override
            public void onMessage(byte[] data) {
                Log.e(TAG, String.format("Got binary message!"));
                updateLastServerLifeSign();
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
                disconnect();
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

    public void disconnect() {
        if ( webSocketClient != null )
            webSocketClient.disconnect();
        if ( wampClient != null )
            wampClient.reset();
        state = State.DISCONNECTED;
    }

    public void destroy() {
        disconnect();
        webSocketClient.destroy();
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

    private void updateLastServerLifeSign() {
        lastServerLifeSign = System.currentTimeMillis();
    }

    private void updateLastClientLifeSign() {
        lastClientLifeSign = System.currentTimeMillis();
    }

    /* The WebSocketClient (or Android) does handle hand over correctly. For instance, when
     * handing over from WLAN to Mobile, the websocket connections is not interrupted and
     * the data to the server just gets queued and never sent.
     */
    boolean verifyTimeOut() {
        if ( lastServerLifeSign >= lastClientLifeSign )
            return false;
        if ( lastClientLifeSign - lastServerLifeSign < TIMEOUT )
            return false;
        disconnect();
        return true;
    }

    private WampClient wampClient;
    private WebSocketClient webSocketClient;
    private WebSocketClientChannelAdapter adapter;
    private State state = State.DISCONNECTED;
    private long lastServerLifeSign = 0;
    private long lastClientLifeSign = 0;
    private final static long TIMEOUT = 6000;

    WebSocketClient.Listener listener;

    final static private String TAG = "BladenightWampClient";

    class WebSocketClientChannelAdapter implements Channel {
        private WebSocketClient client;
        public void setClient(WebSocketClient client) {
            this.client = client;
        }
        @Override
        public void handle(String message) throws IOException {
            updateLastClientLifeSign();
            client.send(message);
        }
    }

}
