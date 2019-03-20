package de.greencity.bladenightapp.android.map.userovl;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.view.MapView;

public class FriendMarker {
    private AccuracyCircle accuracyCircle;
    private PositionMarker positionMarker;
    private MapView mapView;
    //  private static final String TAG = "FriendMarker";

    FriendMarker(MapView mapView, int color) {
        this.mapView = mapView;
        accuracyCircle = new AccuracyCircle(mapView, color);
        positionMarker = new PositionMarker(mapView, color);
        // Log.i(TAG, "FriendMarker() " + ExceptionUtils.getStackTrace( new Throwable()));
    }


    public void setColor(int color) {
        accuracyCircle.setColor(color);
        positionMarker.setColor(color);
    }


    public void setLatLong(LatLong geoPoint) {
        accuracyCircle.setLatLong(geoPoint);
        positionMarker.setLatLong(geoPoint);
    }


    public void show() {
        accuracyCircle.show();
        positionMarker.show();
    }

    public void hide() {
        accuracyCircle.hide();
        positionMarker.hide();
    }


    public void setRadius(float radius) {
        accuracyCircle.setRadius(radius);
    }
}
