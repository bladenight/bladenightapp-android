package de.greencity.bladenightapp.android.tracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
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
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(this.toString(), "Failed to subscribe some of the location listeners");
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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
