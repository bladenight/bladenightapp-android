package de.greencity.bladenightapp.android.map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import android.content.Context;
import android.util.Log;

public class BladenightMapView extends MapView {

    public BladenightMapView(Context context) {
        super(context);
    }

    public synchronized void fitViewToBoundingBox(final BoundingBox boundingBox) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "Invalid dimension " + width + "/" + height);
            Log.i(TAG, "Invalid dimension " + boundingBox.toString());
            Log.i(TAG, "Trace: " + ExceptionUtils.getStackTrace( new Throwable()));
            return;
        }
        Projection projection1 = getProjection();
        GeoPoint pointSouthWest = new GeoPoint(boundingBox.minLatitude, boundingBox.minLongitude);
        GeoPoint pointNorthEast = new GeoPoint(boundingBox.maxLatitude, boundingBox.maxLongitude);
        android.graphics.Point pointSW = new android.graphics.Point();
        android.graphics.Point pointNE = new android.graphics.Point();
        byte maximumZoom = getMapZoomControls().getZoomLevelMax();
        byte zoomLevel = 0;
        while (zoomLevel < maximumZoom) {
            byte tmpZoomLevel = (byte) (zoomLevel + 1);
            projection1.toPoint(pointSouthWest, pointSW, tmpZoomLevel);
            projection1.toPoint(pointNorthEast, pointNE, tmpZoomLevel);
            if (pointNE.x - pointSW.x > width) {
                break;
            }
            if (pointSW.y - pointNE.y > height) {
                break;
            }
            zoomLevel = tmpZoomLevel;
        }
        getMapViewPosition().setMapPosition(new MapPosition(boundingBox.getCenterPoint(), zoomLevel));
    }

    final String TAG = "BladenightMapView";
}
