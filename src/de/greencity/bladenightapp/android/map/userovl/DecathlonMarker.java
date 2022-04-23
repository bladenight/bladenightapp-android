package de.greencity.bladenightapp.android.map.userovl;

import android.annotation.SuppressLint;
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

public class DecathlonMarker {

    public DecathlonMarker(MapView mapView) {
        this.mapView = mapView;
        marker = new Marker(new LatLong(0, 0), getBitmap(R.color.black), 0, 0);
        // setColor(color);
    }

    @SuppressLint("ResourceAsColor")
    private Bitmap getBitmap(int color) {
        int resourceIdentifier = R.drawable.decathlon_logo_25p;
        Drawable drawable = mapView.getResources().getDrawable(resourceIdentifier);

        return AndroidGraphicFactory.convertToBitmap(drawable);
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
