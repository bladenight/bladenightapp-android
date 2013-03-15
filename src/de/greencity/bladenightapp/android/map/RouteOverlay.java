package de.greencity.bladenightapp.android.map;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.android.maps.overlay.PolygonalChain;
import org.mapsforge.android.maps.overlay.Polyline;
import org.mapsforge.core.model.GeoPoint;

import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.Log;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RealTimeUpdateData;
import de.greencity.bladenightapp.network.messages.RouteMessage;

public class RouteOverlay extends ListOverlay {

	public RouteOverlay(MapView mapView) {
		this.mapView = mapView;
		reinit();
	}

	private void reinit() {
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
		paintStroke.setColor(Color.MAGENTA);
		paintStroke.setAlpha(128);
		paintStroke.setStrokeWidth(7);
		// paintStroke.setPathEffect(new DashPathEffect(new float[] { 25, 15 }, 0));

		return new Polyline(polygonalChain, paintStroke);
	}

	private Polyline createProcessionPolyline() {
		PolygonalChain polygonalChain = new PolygonalChain(new ArrayList<GeoPoint>());

		Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintStroke.setStyle(Paint.Style.STROKE);
		paintStroke.setColor(Color.BLUE);
		// paintStroke.setAlpha(128);
		paintStroke.setStrokeWidth(4);
		// paintStroke.setPathEffect(new DashPathEffect(new float[] { 25, 15 }, 0));

		return new Polyline(polygonalChain, paintStroke);
	}

	public void update(RouteMessage routeMessage) {
		routeNodes = new ArrayList<GeoPoint>();
		for (LatLong node : routeMessage.nod) {
			routeNodes.add(new GeoPoint(node.getLatitude(), node.getLongitude()));
		}
		routePolyline.setPolygonalChain(new PolygonalChain(routeNodes));

		List<GeoPoint> geoPoints = generateProcessionsGeopoints(1000,2000);
		processionPolyline.setPolygonalChain(new PolygonalChain(geoPoints));

		mapView.redraw();
	}

	public void update(RealTimeUpdateData realTimeUpdateData) {
		List<GeoPoint> geoPoints = generateProcessionsGeopoints(realTimeUpdateData);
		processionPolyline.setPolygonalChain(new PolygonalChain(geoPoints));
		mapView.redraw();
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

		Log.i(TAG, method + ": " + tailPosition + " - " + headPosition);

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

	final private MapView mapView;
	private Polyline routePolyline;
	private Polyline processionPolyline;
	private List<GeoPoint> routeNodes;

	final String TAG = "RouteOverlay";
}
