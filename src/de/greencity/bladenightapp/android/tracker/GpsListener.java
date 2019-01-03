package de.greencity.bladenightapp.android.tracker;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class GpsListener {
    public GpsListener(Context context, LocationListener locationListener) {
        this.locationListener = locationListener;
        this.context = context;
    }

    public void requestLocationUpdates(int period) {
        cancelLocationUpdates();
        LocationManager locationManager;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 1f, locationListener);
        }
        catch(Exception e) {
            Log.e(this.toString(), "Failed to subscribe some of the location listeners",e);
            Toast.makeText(context, "Fehler in dem Ortsbestimmungssystem. Die Position kann ungenau oder unverfügbar sein", Toast.LENGTH_LONG).show();
        }

    }

    public void cancelLocationUpdates() {
        LocationManager locationManager;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if ( locationListener == null )
            Log.w(TAG, "locationManager==null in cancelLocationUpdates");
        else
            locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener;
    private Context context;
    static private final String TAG = "GpsListener";

}
