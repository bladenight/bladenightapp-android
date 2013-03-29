package de.greencity.bladenightapp.android.map;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.android.maps.overlay.Marker;
import org.mapsforge.core.model.GeoPoint;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import de.greencity.bladenightapp.android.R;

public class UserPositionOverlay extends ListOverlay implements LocationListener {

	public UserPositionOverlay(Context context, MapView mapView) {
		this.mapView = mapView;
		this.context = context;
		reinit();
	}

	private void reinit() {
		int resourceIdentifier = R.drawable.ic_map_userposition;
		Drawable drawable = context.getResources().getDrawable(resourceIdentifier);
		userSymbol = new Marker(new GeoPoint(0, 0), Marker.boundCenter(drawable));
		getOverlayItems().add(userSymbol);
	}
	
	public void show() {
		mapView.getOverlays().add(this);
		mapView.redraw();
	}
	
	public void hide() {
		mapView.getOverlays().remove(this);
		mapView.redraw();
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.i(TAG, "onLocationChanged: " + location);
		userSymbol.setGeoPoint(new GeoPoint(location.getLatitude(), location.getLongitude()));
		show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i(TAG, "onProviderDisabled: " + provider);
		hide();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i(TAG, "onProviderEnabled: " + provider);
		show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(TAG, "onStatusChanged: " + provider + " status="+status);
	}

	
	private final MapView mapView;
	private Context context;
	private Marker userSymbol;

	private final String TAG = "UserPositionOverlay";

}
