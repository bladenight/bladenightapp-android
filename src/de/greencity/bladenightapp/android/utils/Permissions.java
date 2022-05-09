package de.greencity.bladenightapp.android.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permissions {
    /**
     * Verify permissions for application and request them from user if required.
     */
    static public void verifyPermissionsForApp(Activity activity) {
        boolean accessFinePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean accessCoarsePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if( ! accessFinePermission || ! accessCoarsePermission) {
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(activity, permissions, 1);
        }
    }
}
