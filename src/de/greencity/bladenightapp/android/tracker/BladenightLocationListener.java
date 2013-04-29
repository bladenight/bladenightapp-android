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
	public void onLocationChanged(Location location) {
		Log.d(this.toString(), "onLocationChanged" + location);
		this.location.setLatitude(location.getLatitude());
		this.location.setLongitude(location.getLongitude());
		this.location.setAccuracy(location.getAccuracy());
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

	private Location location;
}
