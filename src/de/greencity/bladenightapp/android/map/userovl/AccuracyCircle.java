package de.greencity.bladenightapp.android.map.userovl;

import android.graphics.Color;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Circle;

class AccuracyCircle {
    private final MapView mapView;
    private Circle circle;
    //  static private String TAG = "UserPositionsOverlay.AccuracyCircle";
    static final int ALPHA = 40;
    static final float MAX_RADIUS = 300;
    private int color;

    public AccuracyCircle(MapView mapView, int color) {
        circle = new Circle(new LatLong(0,0), 0, getPaintFill(color), getPaintStroke(color));
        this.color = color;
        this.mapView = mapView;
    }

    private Paint getPaintFill(int color) {
        Paint paintFill = AndroidGraphicFactory.INSTANCE.createPaint();
        paintFill.setStyle(Style.FILL);
        paintFill.setColor(color);
        // TODO
        // paintFill.setAlpha(ALPHA);
        // paintFill.setAntiAlias(true);
        return paintFill;
    }

    private Paint getPaintStroke(int color) {
        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(Color.WHITE);
        // TODO
        // paintStroke.setAlpha(ALPHA);
        // paintStroke.setAntiAlias(true);
        paintStroke.setStrokeWidth(3);

        return paintStroke;
    }

    public void setColor(int color) {
        if ( this.color == color )
            return;
        circle.setPaintFill(getPaintFill(color));
        circle.setPaintStroke(getPaintStroke(color));
    }
    public void setLatLong(LatLong latLong) {
        circle.setLatLong(latLong);
    }

    public void show() {
        mapView.getLayerManager().getLayers().add(circle);
    }

    public void hide() {
        mapView.getLayerManager().getLayers().remove(circle);
    }

    public void setRadius(float radius) {
        radius = Math.min(radius, MAX_RADIUS);
        circle.setRadius(radius);
    }
}