package de.greencity.bladenightapp.android.tracker;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import de.greencity.bladenightapp.network.messages.LatLong;


public class BladenightLocationListener implements LocationListener {
	public BladenightLocationListener(Context context, LatLong latLong) {
		super();
		this.latLong = latLong;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(this.toString(), "onLocationChanged" + location);
		latLong.setLatitude(location.getLatitude());
		latLong.setLongitude(location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(this.toString(), "onProviderDisabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(this.toString(), "onProviderEnabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(this.toString(), "onStatusChanged");
	}

	private LatLong latLong;
}
