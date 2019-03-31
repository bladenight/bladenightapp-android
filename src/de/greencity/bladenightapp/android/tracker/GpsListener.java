package de.greencity.bladenightapp.android.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.intentfilter.androidpermissions.PermissionManager;

import de.greencity.bladenightapp.android.R;

import static java.util.Collections.singleton;

public class GpsListener {
    public GpsListener(Context context, LocationListener locationListener) {
        this.locationListener = locationListener;
        this.context = context;
    }

    public void requestLocationUpdates(final int period) {
        cancelLocationUpdates();

        PermissionManager permissionManager = PermissionManager.getInstance(context);
        permissionManager.checkPermissions(singleton(Manifest.permission.ACCESS_FINE_LOCATION), new PermissionManager.PermissionRequestListener() {
            @SuppressLint("MissingPermission") // PermissionManager takes care of it
            @Override
            public void onPermissionGranted() {
                // Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show();
                LocationManager locationManager;
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 1f, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.i(TAG, "lastKnownLocation=" + lastKnownLocation);
                if(lastKnownLocation != null)
                    locationListener.onLocationChanged(lastKnownLocation);
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(context, context.getString(R.string.msg_current_position_unknown), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void cancelLocationUpdates() {
        LocationManager locationManager;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationListener == null)
            Log.w(TAG, "locationManager==null in cancelLocationUpdates");
        else
            locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener;
    private Context context;
    static private final String TAG = "GpsListener";

}
