package de.greencity.bladenightapp.android.map;

import java.util.ArrayList;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.android.maps.overlay.PolygonalChain;
import org.mapsforge.android.maps.overlay.Polyline;
import org.mapsforge.core.model.GeoPoint;

import de.greencity.bladenightapp.network.messages.RouteMessage;

import android.graphics.Color;
import android.graphics.Paint;

public class ProcessionOverlay extends ListOverlay {

	public ProcessionOverlay(MapView mapView) {
		this.mapView = mapView;
		reinit();
	}

	private void reinit() {
		Polyline processionPolyline = createProcessionPolyline();
		if ( processionPolyline != null ) // you never know
			getOverlayItems().add(processionPolyline);
		mapView.getOverlays().add(this);
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
	}
	
	final MapView mapView;
}
