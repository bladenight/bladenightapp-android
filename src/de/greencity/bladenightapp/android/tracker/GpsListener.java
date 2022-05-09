package de.greencity.bladenightapp.android.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.intentfilter.androidpermissions.PermissionManager;

import de.greencity.bladenightapp.android.R;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

import static java.util.Collections.singleton;

public class GpsListener {
    public GpsListener(Context context, LocationListener locationListener) {
        this.locationListener = locationListener;
        this.context = context;
    }

    public void requestLocationUpdates(final int period) {
        SmartLocation.with(context).location()
                .start((location) -> locationListener.onLocationChanged(location));
    }

    public void cancelLocationUpdates() {
        LocationManager locationManager;
        SmartLocation.with(context).location()
                .stop();
    }

    private LocationListener locationListener;
    private Context context;
    static private final String TAG = "GpsListener";

}
