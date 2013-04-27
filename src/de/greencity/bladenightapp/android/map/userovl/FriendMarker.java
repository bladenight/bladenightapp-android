package de.greencity.bladenightapp.android.map.userovl;

import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.core.model.GeoPoint;

import android.content.Context;

public class FriendMarker {

	FriendMarker(Context context, ListOverlay listOverlay, int color) {
		this.listOverlay = listOverlay;
		accuracyCircle = new AccuracyCircle(color);
		positionMarker = new PositionMarker(context, color);
		// Log.i(TAG, "FriendMarker() " + ExceptionUtils.getStackTrace( new Throwable()));
	}


	public void show() {
		listOverlay.getOverlayItems().add(accuracyCircle.getOverlayItem());
		listOverlay.getOverlayItems().add(positionMarker.getOverlayItem());
	}

	public void remove() {
		listOverlay.getOverlayItems().remove(positionMarker.getOverlayItem());
		listOverlay.getOverlayItems().remove(accuracyCircle.getOverlayItem());
	}

	public void setColor(int color) {
		accuracyCircle.setColor(color);
		positionMarker.setColor(color);
	}


	public void setGeoPoint(GeoPoint geoPoint) {
		accuracyCircle.setGeoPoint(geoPoint);
		positionMarker.setGeoPoint(geoPoint);
	}


	public void setRadius(float radius) {
		accuracyCircle.setRadius(radius);
	}

	private AccuracyCircle accuracyCircle;
	private PositionMarker positionMarker;
	private ListOverlay listOverlay;
	private static final String TAG = "FriendMarker";
}
