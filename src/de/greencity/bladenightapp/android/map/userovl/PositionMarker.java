package de.greencity.bladenightapp.android.map.userovl;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

import de.greencity.bladenightapp.android.R;

public class PositionMarker {

    public PositionMarker(MapView mapView, int color) {
        this.mapView = mapView;
        marker = new Marker(new LatLong(0, 0), getBitmap(R.color.black), 0, 0);
        setColor(color);
    }

    public void setColor(int color) {
        marker.setBitmap(getBitmap(color));
    }

    private Bitmap getBitmap(int color) {
        int resourceIdentifier = R.drawable.user_symbol;
        Drawable drawable = mapView.getResources().getDrawable(resourceIdentifier);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));

        return AndroidGraphicFactory.convertToBitmap(drawable, paint);
    }

    public void show() {
        Utils.addLayerIfAbsent(mapView.getLayerManager().getLayers(), marker);
    }

    public void hide() {
        Utils.removeLayerIfPresent(mapView.getLayerManager().getLayers(), marker);
    }

    public void setLatLong(LatLong latLong) {
        marker.setLatLong(latLong);
    }

    private Marker marker;
    private MapView mapView;
}
