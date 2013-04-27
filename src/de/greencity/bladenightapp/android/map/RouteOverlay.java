package de.greencity.bladenightapp.android.map;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.android.maps.overlay.PolygonalChain;
import org.mapsforge.android.maps.overlay.Polyline;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;

import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.location.Location;
import android.util.Log;
import de.greencity.bladenightapp.dev.android.R;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class RouteOverlay extends ListOverlay {

	public RouteOverlay(MapView mapView) {
		this.mapView = mapView;
		Log.i(TAG, "RouteOverlay:RouteOverlay");
		reinit();
	}

	private void reinit() {

		Log.i(TAG, "reinit");
		
		routePolyline = createRoutePolyline();
		getOverlayItems().add(routePolyline);

		processionPolyline = createProcessionPolyline();
		getOverlayItems().add(processionPolyline);

		mapView.getOverlays().add(this);
	}

	private Polyline createRoutePolyline() {

		PolygonalChain polygonalChain = new PolygonalChain(new ArrayList<GeoPoint>());

		Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintStroke.setStyle(Paint.Style.STROKE);
		paintStroke.setColor(mapView.getResources().getColor(R.color.new_route));
		//>paintStroke.setAlpha(100);
		paintStroke.setStrokeWidth(12);
		paintStroke.setStrokeCap(Cap.ROUND);

		return new Polyline(polygonalChain, paintStroke);
	}

	private Polyline createProcessionPolyline() {
		PolygonalChain polygonalChain = new PolygonalChain(new ArrayList<GeoPoint>());

		Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintStroke.setStyle(Paint.Style.STROKE);
		paintStroke.setColor(mapView.getResources().getColor(R.color.new_procession));
		paintStroke.setStrokeWidth(12);
		paintStroke.setStrokeCap(Cap.ROUND);

		return new Polyline(polygonalChain, paintStroke);
	}

	public void update(RouteMessage routeMessage) {

		Log.i(TAG, "update: routeName=" + routeMessage.getRouteName());
		Log.i(TAG, "update: routeMessage=" + routeMessage);

		routeNodes = new ArrayList<GeoPoint>();
		for (LatLong node : routeMessage.nod) {
			routeNodes.add(new GeoPoint(node.getLatitude(), node.getLongitude()));
		}
		updateRouteBoundingBox();
		
		routePolyline.setPolygonalChain(new PolygonalChain(routeNodes));

		redrawMapView();
	}

	public void update(RealTimeUpdateData realTimeUpdateData) {

		Log.i(TAG, "update: realTimeUpdateData=" + realTimeUpdateData);

		List<GeoPoint> geoPoints = generateProcessionsGeopoints(realTimeUpdateData);
		processionBoundingBox = computeBoundingBox(geoPoints);
		processionPolyline.setPolygonalChain(new PolygonalChain(geoPoints));
		redrawMapView();
	}
	
	public void redrawMapView() {
		// Catch this kind of unexplainable exceptions:
		// java.lang.IllegalStateException: copyPixelsFromBuffer called on recycled bitmap
		// at android.graphics.Bitmap.checkRecycled(Bitmap.java:180)
		// at android.graphics.Bitmap.copyPixelsFromBuffer(Bitmap.java:277)
		// at org.mapsforge.android.maps.mapgenerator.FileSystemTileCache.get(FileSystemTileCache.java:302)
		try {
			mapView.redraw();
		}
		catch(IllegalStateException e) {
			Log.e(TAG, "Exception in redrawMapView:" + e.toString());
		}
	}

	private List<GeoPoint>  generateProcessionsGeopoints(RealTimeUpdateData realTimeUpdateData) {
		double tailPosition = realTimeUpdateData.getTail().getPosition();
		double headPosition = realTimeUpdateData.getHead().getPosition();
		return generateProcessionsGeopoints(tailPosition, headPosition); 
	}

	private List<GeoPoint>  generateProcessionsGeopoints(double tailPosition, double headPosition) {
		String method = "generateProcessionsGeopoints";
		List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
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
			GeoPoint n1 = routeNodes.get(i);
			GeoPoint n2 = routeNodes.get(i+1);
			float[] results = new float[1];
			Location.distanceBetween (n1.latitude, n1.longitude, n2.latitude, n2.longitude, results);
			double segmentLength = results[0]; 

			//			 Log.i(TAG, ""+i);
			//			 Log.i(TAG, "segmentLength="+segmentLength);
			//			 Log.i(TAG, "currentLinearPosition="+currentLinearPosition);

			double deltaLatitude = n2.latitude - n1.latitude;
			double deltaLongitude = n2.longitude - n1.longitude;
			if ( currentLinearPosition <= tailPosition && tailPosition <= currentLinearPosition + segmentLength ) {
				// We found the segment where the the procession starts
				inIntersection = true; 
				// TODO interpolation of the lat/long is theoritically incorrect (but good enough for these scales) 
				double relativePositionOnSegment = (tailPosition - currentLinearPosition) / segmentLength; 
				geoPoints.add(new GeoPoint(n1.latitude+relativePositionOnSegment*deltaLatitude, n1.longitude+relativePositionOnSegment*deltaLongitude));
			}
			else if ( inIntersection )
				geoPoints.add(new GeoPoint(n1.latitude, n1.longitude));

			if ( currentLinearPosition <= headPosition && headPosition <= currentLinearPosition + segmentLength ) {
				// We found the segment where the the procession ends
				// TODO interpolation of the lat/long is theoretically incorrect (but good enough for these scales) 
				double relativePositionOnSegment = (headPosition - currentLinearPosition) / segmentLength; 
				geoPoints.add(new GeoPoint(n1.latitude+relativePositionOnSegment*deltaLatitude, n1.longitude+relativePositionOnSegment*deltaLongitude));
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

	protected BoundingBox computeBoundingBox(List<GeoPoint> geoPoints) {
		if ( geoPoints.size() == 0 ) {
			Log.e(TAG, "computeBoundingBox: no nodes available");
			return new BoundingBox(0,0,0,0);
		}
		GeoPoint firstPoint = geoPoints.get(0);
		double minLatitude = firstPoint.latitude;
		double maxLatitude = minLatitude;
		double minLongitude = firstPoint.longitude;
		double maxLongitude = minLongitude;
		
		for (GeoPoint geoPoint : geoPoints) {
			minLatitude = Math.min(minLatitude, geoPoint.latitude);
			maxLatitude = Math.max(maxLatitude, geoPoint.latitude);

			minLongitude = Math.min(minLongitude, geoPoint.longitude);
			maxLongitude = Math.max(maxLongitude, geoPoint.longitude);
			
		}
		return new BoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude);
	}

	public GeoPoint getRouteCenter() {
		double latitudeSum = 0;
		double longitudeSum = 0;
		double weightSum = 0;
		for (int i=0; i <= routeNodes.size() - 1; i++) {
			GeoPoint n1;
			if ( i > 0 )
				n1 = routeNodes.get(i-1);
			else
				n1 = routeNodes.get(routeNodes.size() - 1); 
			GeoPoint n2 = routeNodes.get(i);
			float[] results = new float[1];
			Location.distanceBetween (n1.latitude, n1.longitude, n2.latitude, n2.longitude, results);
			float distance = results[0];

			latitudeSum += distance * n2.latitude; 
			longitudeSum += distance * n2.longitude; 
			weightSum += distance; 
		}
		return new GeoPoint(latitudeSum/weightSum, longitudeSum/weightSum);
	}

	
	private final MapView mapView;

	private Polyline routePolyline;
	private List<GeoPoint> routeNodes;
	private BoundingBox routeBoundingBox = new BoundingBox(0, 0, 0, 0);
	private BoundingBox processionBoundingBox  = new BoundingBox(0, 0, 0, 0);

	private Polyline processionPolyline;

	private final String TAG = "RouteOverlay";

}
