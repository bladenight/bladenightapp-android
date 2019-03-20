package de.greencity.bladenightapp.android.map.userovl;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.overlay.Marker;

import de.greencity.bladenightapp.dev.android.R;

public class PositionMarker {

    public PositionMarker(MapView mapView, int color) {
        this.mapView = mapView;

        int resourceIdentifier = R.drawable.user_symbol;
        Drawable drawable = mapView.getResources().getDrawable(resourceIdentifier);

        drawable.mutate().setColorFilter(color, Mode.MULTIPLY);

        marker = new Marker(new LatLong(0, 0), AndroidGraphicFactory.convertToBitmap(drawable), 0, 0);
    }

    public void setColor(int color) {
        // TODO
        // marker.getDrawable().setColorFilter(color, Mode.MULTIPLY);
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
