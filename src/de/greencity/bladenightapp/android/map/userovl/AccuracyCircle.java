package de.greencity.bladenightapp.android.map.userovl;

import org.mapsforge.android.maps.overlay.Circle;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.GeoPoint;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

class AccuracyCircle {
	public AccuracyCircle(int color) {
		circle = new Circle(new GeoPoint(0,0), 0, getPaintFill(color), getPaintStroke(color));
		this.color = color;
	}

	private Paint getPaintFill(int color) {
		Paint paintFill = new Paint();
		paintFill.setStyle(Paint.Style.FILL);

		paintFill.setColor(color);
		paintFill.setAlpha(alpha);
		paintFill.setAntiAlias(true);
		return paintFill;
	}

	private Paint getPaintStroke(int color) {
		Paint paintStroke = new Paint();
		paintStroke.setStyle(Paint.Style.STROKE);
		paintStroke.setColor(Color.WHITE);
		paintStroke.setAntiAlias(true);
		paintStroke.setStrokeWidth(3);

		return paintStroke;
	}

	public void setColor(int color) {
		if ( this.color == color )
			return;
		circle.setPaintFill(getPaintFill(color));
		circle.setPaintStroke(getPaintStroke(color));
	}
	public void setGeoPoint(GeoPoint geoPoint) {
		circle.setGeoPoint(geoPoint);
	}

	public void setRadius(float radius) {
		circle.setRadius(radius);
	}

	public OverlayItem getOverlayItem() {
		return circle;
	}


	private Circle circle;
	//	static private String TAG = "UserPositionOverlay.AccuracyCircle"; 
	final int alpha = 40;
	private int color;
}