package de.greencity.bladenightapp.android.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class Permissions {
    /**
     * Verify permissions for application and request them from user if required.
     */
    static public void verifyPermissionsForApp(Activity activity) {
        // Storage Permissions required for MapsForge tile cache.
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            final int REQUEST_EXTERNAL_STORAGE_ID = 1;
            final String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE_ID
            );
        }
    }
}
