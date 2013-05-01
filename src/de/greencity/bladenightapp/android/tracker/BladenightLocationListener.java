package de.greencity.bladenightapp.android.tracker;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;


public class BladenightLocationListener implements LocationListener {
	public BladenightLocationListener(Location location) {
		super();
		this.location = location;
	}

	@Override
	public void onLocationChanged(Location newLocation) {
		Log.d(this.toString(), "onLocationChanged" + newLocation);
		if ( ! isBetterLocation(newLocation, location) ) {
			Log.i(TAG, "New position is not better, ignoring it");
			Log.i(TAG, "new location ="+location);
			Log.i(TAG, "current location ="+newLocation);
		}
			
		location.setLatitude(newLocation.getLatitude());
		location.setLongitude(newLocation.getLongitude());
		location.setAltitude(newLocation.getAltitude());
		location.setAccuracy(newLocation.getAccuracy());
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(this.toString(), "onProviderDisabled: " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(this.toString(), "onProviderEnabled: " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(this.toString(), "onStatusChanged: " + provider + " status="+status);
	}

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > MAX_DELTA_TIME;
	    boolean isSignificantlyOlder = timeDelta < -MAX_DELTA_TIME;
	    boolean isNewer = timeDelta > 0;

	    // If it's been a long time since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	

	private Location location;
	private static final int MAX_DELTA_TIME = 1000 * 60;
	private static String TAG = "BladenightLocationListener";

}
