package de.greencity.bladenightapp.android.map.userovl;

import org.mapsforge.android.maps.overlay.Marker;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.GeoPoint;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import de.greencity.bladenightapp.dev.android.R;

public class PositionMarker {

    public PositionMarker(Context context, int color) {
        int resourceIdentifier = R.drawable.user_symbol;
        Drawable drawable = context.getResources().getDrawable(resourceIdentifier);

        drawable.mutate().setColorFilter(color, Mode.MULTIPLY);

        marker = new Marker(new GeoPoint(0, 0), Marker.boundCenter(drawable));
    }

    public OverlayItem getOverlayItem() {
        return marker;
    }

    public void setColor(int color) {
        marker.getDrawable().setColorFilter(color, Mode.MULTIPLY);
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        marker.setGeoPoint(geoPoint);
    }

    private Marker marker;

}
