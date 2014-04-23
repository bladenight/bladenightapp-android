package de.greencity.bladenightapp.android.network;

import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class NetworkClientSharedState {
	
	NetworkClientSharedState() {
	}
	
	public boolean isServerConfigured() {
		return server != null;
	}
	
	public boolean setServerInfoFromUrl(String urlString) {
		if (urlString == null || urlString.length() == 0 )
			return false;
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Failed to parse URL: ", e);
			Log.i(TAG, urlString);
			return false;
		}
		final String protocol = url.getProtocol();
		if ( ! isAcceptedProcol(protocol) ) {
			Log.e(TAG, "Invalid protocol: " + protocol);
			return false;
		}
		useSsl(isSslProtocol(protocol));
		server = url.getHost();
		port = url.getPort();
		Log.i(TAG, "Url set to: " + urlString);
		return true;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
	
	private boolean isAcceptedProcol(String protocol) {
		return protocol.matches("^(ws|http)([s])?$");
	}
	
	private boolean isSslProtocol(String protocol) {
		return protocol.equals("https") || protocol.equals("wss");
	}
	
	public String getHttpUrl() {
		if ( ! isServerConfigured() )
			return null;
		String protocol = useSsl() ? "https" : "http"; 
		return protocol + "://" + server + ":" + port;
	}

	public String getWebSocketUrl() {
		if ( ! isServerConfigured() )
			return null;
		String protocol = useSsl() ? "wss" : "ws"; 
		return protocol + "://" + server + ":" + port;
	}

	
	public boolean useSsl() {
		return useSsl;
	}

	public void useSsl(boolean useSsl) {
		this.useSsl = useSsl;
	}

	public long 				lookingForServerTimestamp = 0;
	public long 				connectingSinceTimestamp;
	
	private String 				server;
	private int 				port;
	private boolean				useSsl;
	
	static final String TAG = "NetworkClientSharedState";

}
