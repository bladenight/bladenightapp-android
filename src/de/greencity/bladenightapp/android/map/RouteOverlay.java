package de.greencity.bladenightapp.android.map;

import android.location.Location;
import android.util.Log;

import org.mapsforge.core.graphics.Cap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import de.greencity.bladenightapp.android.R;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class RouteOverlay {

    public RouteOverlay(MapView mapView) {
        this.mapView = mapView;
        Log.i(TAG, "RouteOverlay:RouteOverlay");
        reinit();
    }

    private void reinit() {

        Log.i(TAG, "reinit");


        routePolylineFrame = createRoutePolylineFrame();
        routePolyline = createRoutePolyline();
        processionPolyline = createProcessionPolyline();

        mapView.getLayerManager().getLayers().add(routePolylineFrame);
        mapView.getLayerManager().getLayers().add(routePolyline);
        mapView.getLayerManager().getLayers().add(processionPolyline);
    }

    private Polyline createRoutePolyline() {

        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(mapView.getResources().getColor(R.color.map_route_fill));
        paintStroke.setStrokeWidth(12);
        paintStroke.setStrokeCap(Cap.ROUND);

        Polyline polyline = new Polyline(paintStroke, AndroidGraphicFactory.INSTANCE);
        List<org.mapsforge.core.model.LatLong> latLongs = new ArrayList<>();
        polyline.setPoints(latLongs);

        polyline.setPaintStroke(paintStroke);

        return polyline;
    }

    private Polyline createRoutePolylineFrame() {

        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(mapView.getResources().getColor(R.color.map_route_outline));
        //>paintStroke.setAlpha(100);
        paintStroke.setStrokeWidth(16);
        paintStroke.setStrokeCap(Cap.ROUND);

        Polyline polyline = new Polyline(paintStroke, AndroidGraphicFactory.INSTANCE);
        List<org.mapsforge.core.model.LatLong> latLongs = new ArrayList<>();
        polyline.setPoints(latLongs);

        polyline.setPaintStroke(paintStroke);

        return polyline;
    }

    private Polyline createProcessionPolyline() {
        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(mapView.getResources().getColor(R.color.map_procession_fill));
        paintStroke.setStrokeWidth(12);
        paintStroke.setStrokeCap(Cap.ROUND);

        Polyline polyline = new Polyline(paintStroke, AndroidGraphicFactory.INSTANCE);
        List<org.mapsforge.core.model.LatLong> latLongs = new ArrayList<>();
        polyline.setPoints(latLongs);

        polyline.setPaintStroke(paintStroke);

        return polyline;
    }

    public void update(RouteMessage routeMessage) {

        // Log.i(TAG, "update: routeName=" + routeMessage.getRouteName());
        // Log.i(TAG, "update: routeMessage=" + routeMessage);

        routeNodes = new ArrayList<org.mapsforge.core.model.LatLong>();
        for (LatLong node : routeMessage.nod) {
            routeNodes.add(new org.mapsforge.core.model.LatLong(node.getLatitude(), node.getLongitude()));
        }
        updateRouteBoundingBox();

        routePolyline.setPoints(routeNodes);
        routePolylineFrame.setPoints(routeNodes);

        redrawMapView();
    }

    public void update(RealTimeUpdateData realTimeUpdateData) {

        // Log.i(TAG, "update: realTimeUpdateData=" + realTimeUpdateData);

        List<org.mapsforge.core.model.LatLong> geoPoints = generateProcessionsGeopoints(realTimeUpdateData);
        processionBoundingBox = computeBoundingBox(geoPoints);
        processionPolyline.setPoints(geoPoints);
        redrawMapView();
    }

    public void redrawMapView() {
        // Catch this kind of unexplainable exceptions:
        // java.lang.IllegalStateException: copyPixelsFromBuffer called on recycled bitmap
        // at android.graphics.Bitmap.checkRecycled(Bitmap.java:180)
        // at android.graphics.Bitmap.copyPixelsFromBuffer(Bitmap.java:277)
        // at org.mapsforge.android.maps.mapgenerator.FileSystemTileCache.get(FileSystemTileCache.java:302)
        try {
            mapView.repaint();
        }
        catch(IllegalStateException e) {
            Log.e(TAG, "Exception in redrawMapView:" + e.toString());
        }
    }

    private List<org.mapsforge.core.model.LatLong>  generateProcessionsGeopoints(RealTimeUpdateData realTimeUpdateData) {
        double tailPosition = realTimeUpdateData.getTail().getPosition();
        double headPosition = realTimeUpdateData.getHead().getPosition();
        return generateProcessionsGeopoints(tailPosition, headPosition);
    }

    private List<org.mapsforge.core.model.LatLong>  generateProcessionsGeopoints(double tailPosition, double headPosition) {
        String method = "generateProcessionsGeopoints";
        List<org.mapsforge.core.model.LatLong> geoPoints = new ArrayList<>();
        double currentLinearPosition = 0.0;

        // Log.i(TAG, method + ": " + tailPosition + " - " + headPosition);

        if ( ( tailPosition == 0.0 && headPosition == 0.0 ) || headPosition < tailPosition )
            return geoPoints;

        if ( routeNodes == null ) {
            Log.w(TAG, method + ": " + "route nodes are not available");
            return geoPoints;
        }

        boolean inIntersection = false;
        for (int i=0; i <= routeNodes.size() - 2; i++) {
            org.mapsforge.core.model.LatLong n1 = routeNodes.get(i);
            org.mapsforge.core.model.LatLong n2 = routeNodes.get(i+1);
            float[] results = new float[1];
            Location.distanceBetween (n1.latitude, n1.longitude, n2.latitude, n2.longitude, results);
            double segmentLength = results[0];

            //           Log.i(TAG, ""+i);
            //           Log.i(TAG, "segmentLength="+segmentLength);
            //           Log.i(TAG, "currentLinearPosition="+currentLinearPosition);

            double deltaLatitude = n2.latitude - n1.latitude;
            double deltaLongitude = n2.longitude - n1.longitude;
            if ( currentLinearPosition <= tailPosition && tailPosition <= currentLinearPosition + segmentLength ) {
                // We found the segment where the the procession starts
                inIntersection = true;
                // TODO interpolation of the lat/long is theoritically incorrect (but good enough for these scales)
                double relativePositionOnSegment = (tailPosition - currentLinearPosition) / segmentLength;
                geoPoints.add(new org.mapsforge.core.model.LatLong(n1.latitude+relativePositionOnSegment*deltaLatitude, n1.longitude+relativePositionOnSegment*deltaLongitude));
            }
            else if ( inIntersection )
                geoPoints.add(new org.mapsforge.core.model.LatLong(n1.latitude, n1.longitude));

            if ( currentLinearPosition <= headPosition && headPosition <= currentLinearPosition + segmentLength ) {
                // We found the segment where the the procession ends
                // TODO interpolation of the lat/long is theoretically incorrect (but good enough for these scales)
                double relativePositionOnSegment = (headPosition - currentLinearPosition) / segmentLength;
                geoPoints.add(new org.mapsforge.core.model.LatLong(n1.latitude+relativePositionOnSegment*deltaLatitude, n1.longitude+relativePositionOnSegment*deltaLongitude));
                inIntersection = false;
                break;
            }

            currentLinearPosition += segmentLength;
        }
        return geoPoints;
    }

    public BoundingBox getProcessionBoundingBox() {
        return processionBoundingBox;
    }

    public BoundingBox getRouteBoundingBox() {
        return routeBoundingBox;
    }

    public void updateRouteBoundingBox() {
        routeBoundingBox = computeRouteBoundingBox();
    }

    protected BoundingBox computeRouteBoundingBox() {
        if ( routeNodes == null ) {
            Log.e(TAG, "getRouteBoundingBox: no nodes available " + routeNodes);
            return new BoundingBox(0,0,0,0);
        }
        return computeBoundingBox(routeNodes);
    }

    protected BoundingBox computeBoundingBox(List<org.mapsforge.core.model.LatLong> geoPoints) {
        if ( geoPoints.size() == 0 ) {
            // Log.w(TAG, "computeBoundingBox: no nodes available");
            return new BoundingBox(0,0,0,0);
        }
        org.mapsforge.core.model.LatLong firstPoint = geoPoints.get(0);
        double minLatitude = firstPoint.latitude;
        double maxLatitude = minLatitude;
        double minLongitude = firstPoint.longitude;
        double maxLongitude = minLongitude;

        for (org.mapsforge.core.model.LatLong geoPoint : geoPoints) {
            minLatitude = Math.min(minLatitude, geoPoint.latitude);
            maxLatitude = Math.max(maxLatitude, geoPoint.latitude);

            minLongitude = Math.min(minLongitude, geoPoint.longitude);
            maxLongitude = Math.max(maxLongitude, geoPoint.longitude);

        }
        return new BoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude);
    }

    public org.mapsforge.core.model.LatLong getRouteCenter() {
        double latitudeSum = 0;
        double longitudeSum = 0;
        double weightSum = 0;
        for (int i=0; i <= routeNodes.size() - 1; i++) {
            org.mapsforge.core.model.LatLong n1;
            if ( i > 0 )
                n1 = routeNodes.get(i-1);
            else
                n1 = routeNodes.get(routeNodes.size() - 1);
            org.mapsforge.core.model.LatLong n2 = routeNodes.get(i);
            float[] results = new float[1];
            Location.distanceBetween (n1.latitude, n1.longitude, n2.latitude, n2.longitude, results);
            float distance = results[0];

            latitudeSum += distance * n2.latitude;
            longitudeSum += distance * n2.longitude;
            weightSum += distance;
        }
        return new org.mapsforge.core.model.LatLong(latitudeSum/weightSum, longitudeSum/weightSum);
    }


    private final MapView mapView;

    private Polyline routePolyline;
    private Polyline routePolylineFrame;
    private List<org.mapsforge.core.model.LatLong> routeNodes;
    private BoundingBox routeBoundingBox = new BoundingBox(0, 0, 0, 0);
    private BoundingBox processionBoundingBox  = new BoundingBox(0, 0, 0, 0);

    private Polyline processionPolyline;

    private final String TAG = "RouteOverlay";

}
