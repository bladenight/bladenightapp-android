package de.greencity.bladenightapp.android.map.userovl;

import android.graphics.Color;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Circle;
import de.greencity.bladenightapp.android.map.userovl.Utils;

class AccuracyCircle {
    private final MapView mapView;
    private Circle circle;
    //  static private String TAG = "UserPositionsOverlay.AccuracyCircle";
    static final int ALPHA = 40;
    static final float MAX_RADIUS = 300;
    private int color;

    public AccuracyCircle(MapView mapView, int color) {
        this.color = color;
        this.mapView = mapView;
        circle = new Circle(new LatLong(0, 0), 0, getPaintFill(color), getPaintStroke(color));
    }

    private Paint getPaintFill(int color) {
        Paint paintFill = getPaint(color, ALPHA);
        paintFill.setStyle(Style.FILL);
        return paintFill;
    }

    private Paint getPaintStroke(int color) {
        Paint paintStroke = getPaint(color, ALPHA);
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(Color.WHITE);
        paintStroke.setStrokeWidth(3);

        return paintStroke;
    }

    private Paint getPaint(int color, int alpha) {
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        Paint p = AndroidGraphicFactory.INSTANCE.createPaint();
        p.setStyle(Style.FILL);
        p.setColor(AndroidGraphicFactory.INSTANCE.createColor(alpha, red, green, blue));

        return p;
    }

    public void setColor(int color) {
        if (this.color == color)
            return;
        circle.setPaintFill(getPaintFill(color));
        circle.setPaintStroke(getPaintStroke(color));
    }

    public void setLatLong(LatLong latLong) {
        circle.setLatLong(latLong);
    }

    public void show() {
        Utils.addLayerIfAbsent(mapView.getLayerManager().getLayers(), circle);
    }

    public void hide() {
        Utils.removeLayerIfPresent(mapView.getLayerManager().getLayers(), circle);
    }

    public void setRadius(float radius) {
        radius = Math.min(radius, MAX_RADIUS);
        circle.setRadius(radius);
    }
}