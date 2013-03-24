package de.greencity.bladenightapp.android.network;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import de.greencity.bladenightapp.network.scanner.PortScanner;

public class ServerFinder {
	public ServerFinder(Context context, int port) {
		this.context = context;
		this.port = port;
	}

	public String findServer() throws InterruptedException {
		PortScanner scanner = new PortScanner(port);

		scanner.setTimeout(1000);

		Log.i(TAG, "BUILD_PRODUCT=" + Build.PRODUCT);
		if ( "google_sdk".equals( Build.PRODUCT ) || "sdk".equals( Build.PRODUCT ) ) {
			// Assume we are running in the emulator, and the server runs on the development host
			scanner.addHost("10.0.2.2");
		}

		String wifiSubnet = getWifiSubnetAsString();
		Log.i(TAG, "wifiSubnet=" + wifiSubnet);
		if ( wifiSubnet != null )
			scanner.addIpRange(wifiSubnet, 1, 254);

		scanner.scan();

		return scanner.getFoundHost();
	}


	protected int getWifiIp() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getIpAddress();
	}

	protected String getWifiSubnetAsString() {
		int ip = getWifiIp();

		if ( ip == 0)
			return null;

		String ipString = String.format(
				"%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff) );

		return ipString;
	}

	private final Context context;
	private final int port;
	final static String TAG = "ServerFinder"; 

}
