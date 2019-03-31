package de.greencity.bladenightapp.android.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import de.greencity.bladenightapp.network.scanner.PortScanner;

public class ServerFinderAsyncTask extends AsyncTask<Integer, Integer, String> {
    ServerFinderAsyncTask(Context context) {
        this.context = context;
    }

    protected String doInBackground(Integer... ports) {
        PortScanner scanner = new PortScanner(ports[0]);

        scanner.setTimeout(1000);

        Log.i(TAG, "BUILD_PRODUCT=" + Build.PRODUCT);
        if ( Build.PRODUCT.contains("sdk") ) {
            // Assume we are running in the emulator, and the server runs on the development host
            scanner.addHost("10.0.2.2");
        }

        String wifiSubnet = getWifiSubnetAsString();
        Log.i(TAG, "wifiSubnet=" + wifiSubnet);
        if ( wifiSubnet != null )
            scanner.addIpRange(wifiSubnet, 1, 254);

        try {
            scanner.scan();
        } catch (InterruptedException e) {
            Log.e("TAG", "While scanning: ",e);
        }

        Log.i(TAG, "doInBackground result=" + scanner.getFoundHost());

        return scanner.getFoundHost();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        // setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        // showDialog("Downloaded " + result + " bytes");
    }

    protected int getWifiIp() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    @SuppressLint("DefaultLocale")
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

    private Context context;
    final static String TAG = "ServerFinderAsyncTask";
}
