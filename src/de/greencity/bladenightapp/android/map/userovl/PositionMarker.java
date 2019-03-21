package de.greencity.bladenightapp.android.map.userovl;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import org.mapsforge.core.graphics.Bitmap;
import android.graphics.Paint;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

import de.greencity.bladenightapp.dev.android.R;

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
        mapView.getLayerManager().getLayers().add(marker);
    }

    public void hide() {
        mapView.getLayerManager().getLayers().remove(marker);
    }

    public void setLatLong(LatLong latLong) {
        marker.setLatLong(latLong);
    }

    private Marker marker;
    private MapView mapView;
}
