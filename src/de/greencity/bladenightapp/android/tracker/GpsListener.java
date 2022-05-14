package de.greencity.bladenightapp.android.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.intentfilter.androidpermissions.PermissionManager;

import static java.util.Collections.singleton;

public class GpsListener {
    private LocationHandler locationHandler; // Internal
    private LocationListener locationListener; // External
    private Context context;
    private LocationManager locationManager;
    static private final String TAG = "GpsListener";

    private class LocationHandler implements android.location.LocationListener {
        public LocationHandler() {
            Log.e(TAG, "LocationListener");
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged: " + location);
            locationListener.onLocationChanged(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged: " + provider + ' ' + status + ' ' + extras);
        }

    }

    public GpsListener(Context context, LocationListener locationListener) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.locationHandler = new LocationHandler();
        this.locationListener = locationListener;
    }

    public void requestLocationUpdates(final int period) {
        cancelLocationUpdates();

        PermissionManager permissionManager = PermissionManager.getInstance(context);
        permissionManager.checkPermissions(singleton(Manifest.permission.ACCESS_FINE_LOCATION), new PermissionManager.PermissionRequestListener() {
            @SuppressLint("MissingPermission") // PermissionManager takes care of it
            @Override
            public void onPermissionGranted() {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 0f, locationHandler);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, period, 0f, locationHandler);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                Log.i(TAG, "lastKnownLocation from network: " + lastKnownLocation);

                if (lastKnownLocation != null) {
                    locationHandler.onLocationChanged(lastKnownLocation);
                }
            }

            @Override
            public void onPermissionDenied() {
                String message = "Permission denied while retrieving location";
                Log.e(TAG, message);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void cancelLocationUpdates() {
        this.locationManager.removeUpdates(locationHandler);
    }
}
